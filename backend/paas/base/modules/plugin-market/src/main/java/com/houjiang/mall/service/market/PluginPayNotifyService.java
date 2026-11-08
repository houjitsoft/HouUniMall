package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.market.MarketPluginOrder;
import com.houjiang.mall.entity.market.MarketPluginPayNotify;
import com.houjiang.mall.mapper.market.MarketPluginOrderMapper;
import com.houjiang.mall.mapper.market.MarketPluginPayNotifyMapper;
import com.houjiang.mall.pay.plugin.PayNotifyException;
import com.houjiang.mall.pay.plugin.PayNotifyPayload;
import com.houjiang.mall.pay.plugin.PluginPayChannel;
import com.houjiang.mall.pay.plugin.PluginPayChannelRouter;
import com.houjiang.mall.pay.plugin.PluginPaySigner;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付回调编排：验签 → 幂等去重 → 状态机 → 授权开通。
 *
 * <p><b>异常覆盖矩阵</b>（本类是这套闭环「异常分支」的落点）：
 * <table>
 *   <tr><td>验签失败</td><td>落 REJECTED 流水 + 抛 fatal，渠道会持续重发，等人工修密钥</td></tr>
 *   <tr><td>金额不符</td><td>落 REJECTED 流水 + 抛 fatal，防「改小金额」攻击</td></tr>
 *   <tr><td>订单不存在</td><td>落 REJECTED 流水 + 抛 fatal（订单号被伪造或数据被清）</td></tr>
 *   <tr><td>重复回调</td><td>命中 (order_no, notify_id) 唯一键 → DUPLICATED，直接返回成功让渠道停止重发</td></tr>
 *   <tr><td>已支付后再收到成功</td><td>DUPLICATED，不重复发证</td></tr>
 *   <tr><td>支付失败</td><td>订单置 PAY_FAILED，可重新发起支付，不关闭订单</td></tr>
 *   <tr><td>超时关闭后才到账</td><td>LATE_PAID：补发授权 + warn 告警，由运营决定是否退款</td></tr>
 *   <tr><td>退款</td><td>吊销该订单签发的授权，订单置 REFUNDED</td></tr>
 * </table>
 *
 * <p><b>事务边界</b>：本方法<b>不开启事务</b>——回调流水必须先落库（即便后续业务失败也要留痕，
 * 否则「渠道说通知过、我们查不到」无从查证），业务状态变更由各 Service 自带事务完成。
 *
 * @author Amos
 */
@Slf4j
@Service
public class PluginPayNotifyService {

    private final PluginPayChannelRouter channelRouter;
    private final MarketPluginOrderMapper orderMapper;
    private final MarketPluginPayNotifyMapper notifyMapper;
    private final PluginOrderService orderService;

    @Autowired
    public PluginPayNotifyService(PluginPayChannelRouter channelRouter,
                                  MarketPluginOrderMapper orderMapper,
                                  MarketPluginPayNotifyMapper notifyMapper,
                                  PluginOrderService orderService) {
        this.channelRouter = channelRouter;
        this.orderMapper = orderMapper;
        this.notifyMapper = notifyMapper;
        this.orderService = orderService;
    }

    /**
     * 处理一次渠道回调。
     *
     * @param channelCode 渠道编码（来自回调路径末段）
     * @param rawBody     原始报文体
     * @param params      表单/查询参数
     * @param headers     请求头
     * @return 处理结果；{@link NotifyOutcome#isAccepted()} 决定给渠道回成功还是失败
     * @throws PayNotifyException fatal=true 表示需人工介入
     */
    public NotifyOutcome handle(String channelCode, String rawBody,
                                Map<String, String> params, Map<String, String> headers) {
        String code = channelCode == null ? "" : channelCode.trim().toUpperCase();

        // 1. 取渠道（未接入 / 商户参数缺失 → 记录并报 fatal）
        PluginPayChannel channel;
        try {
            channel = channelRouter.require(code);
        } catch (IllegalStateException e) {
            reject(null, code, null, rawBody, "渠道不可用: " + e.getMessage());
            throw new PayNotifyException("渠道不可用: " + e.getMessage());
        }

        // 2. 验签 + 解析
        PayNotifyPayload payload;
        try {
            payload = channel.parseAndVerify(rawBody, params, headers);
        } catch (PayNotifyException e) {
            reject(null, code, null, rawBody, e.getMessage());
            throw e;
        }

        // 3. 定位订单
        MarketPluginOrder order = findOrder(payload.getOrderNo());
        if (order == null) {
            String message = "回调订单不存在: " + payload.getOrderNo();
            reject(payload.getOrderNo(), code, payload.getTradeNo(), payload.getRaw(), message);
            throw new PayNotifyException(message);
        }

        // 4. 幂等第一道：回调流水唯一键（渠道重发 / 网络重试都会撞在这里）
        String notifyId = resolveNotifyId(payload);
        NotifyReceipt receipt = tryInsert(order.getOrderNo(), code, notifyId,
                payload.getTradeNo(), payload.getRawStatus(), payload.getAmount(), payload.getRaw());
        if (receipt.isDuplicate()) {
            log.info("重复回调已忽略: order={} notifyId={}", order.getOrderNo(), notifyId);
            return NotifyOutcome.duplicated();
        }
        MarketPluginPayNotify record = receipt.record();
        if (record == null) {
            // 流水没落库（表不可用）：不能回「重复」让渠道停止重发，继续往下走业务，
            // 幂等第二道（订单状态 + settle 抢锁）仍然保证不会重复发证
            log.warn("回调流水未落库，继续按订单状态幂等处理: order={} notifyId={}",
                    order.getOrderNo(), notifyId);
        }

        // 5. 幂等第二道 + 金额校验 + 状态机
        try {
            PluginPaySigner.assertAmountMatches(order.getAmount(), payload.getAmount());
        } catch (PayNotifyException e) {
            finish(record, MarketPluginPayNotify.RESULT_REJECTED, e.getMessage());
            throw e;
        }

        // 6. 状态机：成功发证 / 退款吊销 / 失败可重付
        //    业务失败必须撤掉占位流水，否则渠道重发会被判成「重复」而永久不再处理，
        //    表现为「用户付了钱、订单永远待支付」且无流水可查。
        NotifyDecision decision;
        try {
            decision = decide(order, payload);
        } catch (RuntimeException e) {
            dropPlaceholder(record);
            log.error("支付回调业务处理失败，已撤销占位流水等待渠道重发: order={} channel={} err={}",
                    order.getOrderNo(), code, e.getMessage());
            throw e;
        }

        // 7. 回写订单的回调计数（排障用：一眼看出渠道重发了几次）
        bumpNotifyCounter(order.getOrderNo());
        finish(record, decision.getResult(), decision.getMessage());
        log.info("支付回调处理完成: order={} channel={} result={} msg={}",
                order.getOrderNo(), code, decision.getResult(), decision.getMessage());
        return new NotifyOutcome(true, decision.getResult(), decision.getMessage());
    }

    /**
     * 按回调结果推进订单状态机。
     *
     * <p><b>为什么单独成方法</b>：只有业务处理这一段需要「失败即撤销占位流水」的补偿，
     * 抽出来才能在 catch 里干净地回滚，而不是把 try 套在整个 switch 上影响可读性。
     */
    private NotifyDecision decide(MarketPluginOrder order, PayNotifyPayload payload) {
        switch (payload.getResult() == null ? PayNotifyPayload.Result.FAILED : payload.getResult()) {
            case SUCCESS -> {
                if (Integer.valueOf(MarketPluginOrder.STATUS_PAID).equals(order.getStatus())) {
                    log.info("重复成功回调已忽略: order={}", order.getOrderNo());
                    return new NotifyDecision(MarketPluginPayNotify.RESULT_DUPLICATED,
                            "订单已支付，本次回调未重复发证");
                }
                boolean late = Integer.valueOf(MarketPluginOrder.STATUS_CLOSED).equals(order.getStatus())
                        || (order.getExpireTime() != null
                        && order.getExpireTime().isBefore(LocalDateTime.now()));
                orderService.settle(order);
                return new NotifyDecision(
                        late ? MarketPluginPayNotify.RESULT_LATE_PAID : MarketPluginPayNotify.RESULT_SUCCESS,
                        late ? "超时后到账，已补发授权（需人工确认是否退款）" : "支付成功，已签发授权");
            }
            case REFUNDED -> {
                MarketPluginOrder after =
                        orderService.refund(order.getOrderNo(), "渠道通知退款: " + payload.getRawStatus());
                boolean applied = Integer.valueOf(MarketPluginOrder.STATUS_REFUNDED)
                        .equals(after.getStatus());
                // 未支付订单收到退款通知是异常情形（渠道侧重复退款 / 订单号对不上），
                // 流水里必须写清「没生效」，不能一律报「退款成功」——那会让对账少一笔核查
                return new NotifyDecision(MarketPluginPayNotify.RESULT_REFUNDED,
                        applied ? "退款成功，已吊销授权"
                                : "退款通知未生效（订单当前状态 " + after.getStatus() + "，非已支付），需人工核对");
            }
            default -> {
                orderService.markPayFailed(order.getOrderNo(),
                        "渠道通知支付失败: " + payload.getRawStatus());
                return new NotifyDecision(MarketPluginPayNotify.RESULT_FAILED,
                        "支付失败: " + payload.getRawStatus());
            }
        }
    }

    /**
     * 撤销占位流水：业务处理失败时删掉，让渠道下一次重发能重新走完整流程。
     *
     * <p>留着这条 RECEIVED 流水的唯一作用是「看起来有记录」，代价是订单被永久卡死，
     * 不划算。真正需要留痕时，失败原因会由调用方记 error 日志。
     */
    private void dropPlaceholder(MarketPluginPayNotify record) {
        if (record == null || record.getId() == null) {
            return;
        }
        try {
            notifyMapper.deleteById(record.getId());
        } catch (RuntimeException e) {
            log.error("撤销回调占位流水失败（该订单后续重发会被判重复，需人工清理）: id={} err={}",
                    record.getId(), e.getMessage());
        }
    }

    private MarketPluginOrder findOrder(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return null;
        }
        return orderMapper.selectOne(new LambdaQueryWrapper<MarketPluginOrder>()
                .eq(MarketPluginOrder::getOrderNo, orderNo.trim())
                .last("limit 1"));
    }

    private String resolveNotifyId(PayNotifyPayload payload) {
        if (StringUtils.hasText(payload.getNotifyId())) {
            return payload.getNotifyId().trim();
        }
        if (StringUtils.hasText(payload.getTradeNo())) {
            return payload.getTradeNo().trim();
        }
        return PluginPaySigner.digest(String.valueOf(payload.getRaw()));
    }

    /**
     * 抢先占位插入流水，返回本次插入的结论。
     *
     * <p><b>必须区分三种结果，不能合并成「返回 null 就是重复」</b>：
     * 早期实现把「唯一键冲突」和「写入失败（表不存在 / 库抖动）」都返回 null，
     * 上层一律当成重复回调给渠道回成功 —— 于是<b>钱收了、授权没发、渠道还停止重发</b>，
     * 且没有任何留痕可查。写入失败必须继续走业务（幂等交给订单状态兜底），
     * 只有真正撞唯一键才能判定为重复。
     */
    private NotifyReceipt tryInsert(String orderNo, String channel, String notifyId,
                                    String tradeNo, String rawStatus,
                                    java.math.BigDecimal amount, String raw) {
        MarketPluginPayNotify record = new MarketPluginPayNotify();
        record.setOrderNo(orderNo);
        record.setChannel(channel);
        record.setNotifyId(notifyId);
        record.setTradeNo(tradeNo);
        record.setRawStatus(rawStatus);
        record.setAmount(amount);
        record.setRaw(raw);
        record.setResult("RECEIVED");
        record.setCreateTime(LocalDateTime.now());
        try {
            notifyMapper.insert(record);
            return NotifyReceipt.fresh(record);
        } catch (DuplicateKeyException e) {
            return NotifyReceipt.duplicate();
        } catch (RuntimeException e) {
            log.error("回调流水写入失败，降级为按订单状态幂等（继续处理业务，不回「重复」让渠道停止重发）: "
                    + "order={} err={}", orderNo, e.getMessage());
            return NotifyReceipt.unpersisted();
        }
    }

    /** 回调流水占位的结果：新增 / 重复 / 未落库。 */
    private static final class NotifyReceipt {

        private final MarketPluginPayNotify record;
        private final boolean duplicate;

        private NotifyReceipt(MarketPluginPayNotify record, boolean duplicate) {
            this.record = record;
            this.duplicate = duplicate;
        }

        static NotifyReceipt fresh(MarketPluginPayNotify record) {
            return new NotifyReceipt(record, false);
        }

        static NotifyReceipt duplicate() {
            return new NotifyReceipt(null, true);
        }

        /** 流水没写进去（表不可用），业务仍要继续，但后续无法回写流水。 */
        static NotifyReceipt unpersisted() {
            return new NotifyReceipt(null, false);
        }

        boolean isDuplicate() {
            return duplicate;
        }

        MarketPluginPayNotify record() {
            return record;
        }
    }

    private void finish(MarketPluginPayNotify record, String result, String message) {
        if (record == null || record.getId() == null) {
            // 流水没落库（表不可用）：没有可回写的行，只留日志，业务结果已由订单状态承载
            log.warn("回调流水缺失，跳过回写: result={} msg={}", result, message);
            return;
        }
        record.setResult(result);
        record.setMessage(message == null ? null : message.length() > 500
                ? message.substring(0, 500) : message);
        try {
            notifyMapper.updateById(record);
        } catch (RuntimeException e) {
            log.error("回调流水回写失败: id={} err={}", record.getId(), e.getMessage());
        }
    }

    private void reject(String orderNo, String channel, String tradeNo, String raw, String message) {
        MarketPluginPayNotify record = new MarketPluginPayNotify();
        record.setOrderNo(orderNo);
        record.setChannel(channel);
        record.setTradeNo(tradeNo);
        record.setNotifyId(orderNo == null
                ? PluginPaySigner.digest(String.valueOf(raw))
                : "REJECT-" + orderNo + "-" + System.currentTimeMillis());
        record.setRaw(raw);
        record.setResult(MarketPluginPayNotify.RESULT_REJECTED);
        record.setMessage(message == null ? null : message.length() > 500
                ? message.substring(0, 500) : message);
        record.setCreateTime(LocalDateTime.now());
        try {
            notifyMapper.insert(record);
        } catch (RuntimeException e) {
            log.error("回调拒收流水写入失败: order={} err={}", orderNo, e.getMessage());
        }
        log.warn("支付回调被拒收: order={} channel={} reason={}", orderNo, channel, message);
    }

    private void bumpNotifyCounter(String orderNo) {
        MarketPluginOrder fresh = findOrder(orderNo);
        if (fresh == null) {
            return;
        }
        fresh.setNotifyCount((fresh.getNotifyCount() == null ? 0 : fresh.getNotifyCount()) + 1);
        if (fresh.getNotifyTime() == null) {
            fresh.setNotifyTime(LocalDateTime.now());
        }
        fresh.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(fresh);
    }

    /** 状态机判决：本次回调落在哪个结果上。 */
    @Data
    public static class NotifyDecision {

        private final String result;

        private final String message;

        public NotifyDecision(String result, String message) {
            this.result = result;
            this.message = message;
        }
    }

    /**
     * 回调处理结果。
     *
     * <p>{@code accepted} 恒为 true：走到这里的回调都已被<b>完整处理</b>（含「支付失败」「重复」），
     * 渠道没必要重发；真正需要渠道重发的是验签失败、金额不符这类 fatal 异常，
     * 那些走异常路径由 Controller 回失败应答。字段保留是为了让调用方的判断显式而非隐式猜测。
     */
    @Data
    public static class NotifyOutcome {

        private final boolean accepted;
        private final String result;
        private final String message;

        public NotifyOutcome(boolean accepted, String result, String message) {
            this.accepted = accepted;
            this.result = result;
            this.message = message;
        }

        public static NotifyOutcome duplicated() {
            return new NotifyOutcome(true, MarketPluginPayNotify.RESULT_DUPLICATED, "重复回调，未重复处理");
        }
    }
}
