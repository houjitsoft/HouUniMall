package com.houjiang.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.houjiang.payment.constant.CashierOrderStatusEnum;
import com.houjiang.payment.channel.biz.PayChannelStrategyFactory;
import com.houjiang.payment.constant.PayMethodEnum;
import com.houjiang.payment.constant.TerminalTypeEnum;
import com.houjiang.payment.dto.cashier.CashierCreateRequest;
import com.houjiang.payment.dto.cashier.CashierCreateResponse;
import com.houjiang.payment.dto.cashier.CashierStatusResponse;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import com.houjiang.payment.service.api.CashierService;
import com.houjiang.payment.service.cashier.WxJsapiPayHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一收银台服务实现（Task 0 骨架）
 *
 * 本实现聚焦"收银台订单生命周期 + BALANCE 真实扣款"，
 * 其他 payMethod（WECHAT/ALIPAY/UNIONPAY/...）下单时返回占位 payData，
 * 后续 Task 1/2/... 逐个对接真实渠道 SDK。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CashierServiceImpl implements CashierService {

    private final PayCashierOrderMapper cashierOrderMapper;
    private final PayUserResourceMapper payUserResourceMapper;
    private final WxJsapiPayHandler wxJsapiPayHandler;
    private final PayChannelStrategyFactory channelStrategyFactory;

    private static final long EXPIRE_MILLIS = 30 * 60 * 1000L;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CashierCreateResponse create(CashierCreateRequest request, String userId) {
        validateRequest(request);

        if (StringUtils.hasText(request.getRequestNo())) {
            PayCashierOrderEntity existing = cashierOrderMapper.selectByRequestNo(request.getRequestNo());
            if (existing != null) {
                if (!userId.equals(existing.getUserId())
                        || !request.getMallOrderNo().equals(existing.getMallOrderNo())
                        || request.getAmount().compareTo(existing.getAmount()) != 0) {
                    throw new IllegalStateException("幂等请求号已被其他支付请求使用");
                }
                return buildCreateResponse(existing, parsePayData(existing.getPayData()));
            }
        }

        PayCashierOrderEntity order = new PayCashierOrderEntity();
        order.setCashierNo(generateCashierNo());
        order.setMchOrderNo(StringUtils.hasText(request.getMchOrderNo())
                ? request.getMchOrderNo() : order.getCashierNo());
        order.setRequestNo(request.getRequestNo());
        order.setMallOrderNo(StringUtils.hasText(request.getMallOrderNo())
                ? request.getMallOrderNo() : order.getMchOrderNo());
        order.setUserId(userId);
        order.setTerminal(request.getTerminal());
        order.setPayMethod(request.getPayMethod());
        order.setAmount(request.getAmount());
        order.setCurrency(StringUtils.hasText(request.getCurrency()) ? request.getCurrency() : "CNY");
        order.setSubject(request.getSubject());
        order.setBody(request.getBody());
        order.setClientIp(request.getClientIp());
        order.setNotifyUrl(request.getNotifyUrl());
        order.setReturnUrl(request.getReturnUrl());
        order.setExtParam(request.getExtParam());
        order.setStatus(CashierOrderStatusEnum.WAIT_PAY.getCode());
        order.setExpireTime(new Date(System.currentTimeMillis() + EXPIRE_MILLIS));
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        cashierOrderMapper.insert(order);
        Map<String, Object> payData = buildPayData(order, request);
        order.setPayData(JSON.toJSONString(payData));
        cashierOrderMapper.updateById(order);

        log.info("[cashier] create ok, cashierNo={}, userId={}, payMethod={}, terminal={}, amount={}",
                order.getCashierNo(), userId, order.getPayMethod(), order.getTerminal(), order.getAmount());

        return buildCreateResponse(order, payData);
    }

    private CashierCreateResponse buildCreateResponse(PayCashierOrderEntity order, Map<String, Object> payData) {
        CashierCreateResponse resp = new CashierCreateResponse();
        resp.setCashierNo(order.getCashierNo());
        resp.setMchOrderNo(order.getMchOrderNo());
        resp.setPayOrderId(order.getPayOrderId());
        resp.setPayMethod(order.getPayMethod());
        resp.setTerminal(order.getTerminal());
        resp.setAmount(order.getAmount());
        resp.setStatus(order.getStatus());
        resp.setPayData(payData);
        resp.setExpireTime(order.getExpireTime().getTime());
        return resp;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayData(String payData) {
        if (!StringUtils.hasText(payData)) return new HashMap<>();
        return JSON.parseObject(payData, Map.class);
    }

    @Override
    public CashierStatusResponse query(String cashierNo) {
        if (!StringUtils.hasText(cashierNo)) {
            throw new IllegalArgumentException("cashierNo 不能为空");
        }
        PayCashierOrderEntity order = cashierOrderMapper.selectByCashierNo(cashierNo);
        if (order == null) {
            throw new IllegalStateException("收银台订单不存在: " + cashierNo);
        }
        return buildStatusResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CashierStatusResponse close(String cashierNo) {
        PayCashierOrderEntity order = cashierOrderMapper.selectByCashierNo(cashierNo);
        if (order == null) {
            throw new IllegalStateException("收银台订单不存在: " + cashierNo);
        }
        if (CashierOrderStatusEnum.SUCCESS.getCode() == order.getStatus()) {
            throw new IllegalStateException("已支付成功，无法关闭");
        }
        order.setStatus(CashierOrderStatusEnum.CLOSED.getCode());
        order.setUpdateTime(new Date());
        cashierOrderMapper.update(order, Wrappers.<PayCashierOrderEntity>lambdaUpdate()
                .eq(PayCashierOrderEntity::getCashierNo, cashierNo));
        log.info("[cashier] close ok, cashierNo={}", cashierNo);
        return buildStatusResponse(order);
    }

    /**
     * 余额扣款：复用 handleBalancePay，增加身份与状态校验
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CashierStatusResponse payByBalance(String cashierNo, String userId) {
        if (!StringUtils.hasText(cashierNo)) {
            throw new IllegalArgumentException("cashierNo 不能为空");
        }
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        PayCashierOrderEntity order = cashierOrderMapper.selectByCashierNo(cashierNo);
        if (order == null) {
            throw new IllegalStateException("收银台订单不存在: " + cashierNo);
        }
        if (!userId.equals(order.getUserId())) {
            throw new IllegalStateException("订单不属于当前用户");
        }
        if (!PayMethodEnum.BALANCE.getCode().equals(order.getPayMethod())) {
            throw new IllegalStateException("该订单非余额支付，不可调用余额扣款");
        }
        if (CashierOrderStatusEnum.SUCCESS.getCode() == order.getStatus()) {
            return buildStatusResponse(order);
        }
        if (CashierOrderStatusEnum.PAYING.getCode() == order.getStatus()) {
            return query(cashierNo);
        }
        if (CashierOrderStatusEnum.WAIT_PAY.getCode() != order.getStatus()) {
            throw new IllegalStateException("订单状态不允许扣款: " + order.getStatus());
        }
        if (order.getExpireTime() != null && order.getExpireTime().before(new Date())) {
            order.setStatus(CashierOrderStatusEnum.CLOSED.getCode());
            cashierOrderMapper.updateById(order);
            throw new IllegalStateException("订单已过期");
        }
        int claimed = cashierOrderMapper.claimForPay(cashierNo,
                CashierOrderStatusEnum.WAIT_PAY.getCode(), CashierOrderStatusEnum.PAYING.getCode());
        if (claimed == 0) {
            return query(cashierNo);
        }
        int debited = payUserResourceMapper.deductBalance(userId, order.getAmount());
        if (debited != 1) {
            throw new IllegalStateException("Insufficient account balance");
        }
        Date successTime = new Date();
        String payOrderId = "BAL" + order.getCashierNo();
        int completed = cashierOrderMapper.markBalanceSuccess(cashierNo,
                CashierOrderStatusEnum.PAYING.getCode(), CashierOrderStatusEnum.SUCCESS.getCode(),
                payOrderId, successTime);
        if (completed != 1) {
            throw new IllegalStateException("Balance payment status update failed");
        }
        order.setStatus(CashierOrderStatusEnum.SUCCESS.getCode());
        order.setSuccessTime(successTime);
        order.setPayOrderId(payOrderId);
        log.info("[cashier/walletPay] ok, cashierNo={}, userId={}, amount={}",
                order.getCashierNo(), userId, order.getAmount());
        return buildStatusResponse(order);
    }

    /** ================== 私有方法 ================== */

    private void validateRequest(CashierCreateRequest req) {
        if (TerminalTypeEnum.ofCode(req.getTerminal()) == null) {
            throw new IllegalArgumentException("非法的 terminal: " + req.getTerminal());
        }
        if (PayMethodEnum.ofCode(req.getPayMethod()) == null) {
            throw new IllegalArgumentException("非法的 payMethod: " + req.getPayMethod());
        }
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount 必须 > 0");
        }
    }

    private String generateCashierNo() {
        String seq = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return "CS" + seq + IdUtil.getSnowflakeNextIdStr().substring(12);
    }

    /**
     * 构造下发给前端的 payData
     * Task 0 阶段：对 BALANCE 立即扣款并标记成功，对其他渠道返回占位结构；
     * 后续 Task 1~7 会按照 terminal + payMethod 对接真实 SDK 替换本方法。
     */
    private Map<String, Object> buildPayData(PayCashierOrderEntity order, CashierCreateRequest req) {
        Map<String, Object> payData = new HashMap<>();
        PayMethodEnum method = PayMethodEnum.ofCode(order.getPayMethod());
        if (method == null) {
            return payData;
        }
        switch (method) {
            case BALANCE:
                payData.put("requiresPassword", true);
                payData.put("status", order.getStatus());
                break;
            case WECHAT:
                payData.putAll(handleWechatV3Pay(order, req));
                break;
            case ALIPAY:
                // Task 1 将对接 ALiPayService / AliPayAPPServiceImpl 等
                payData.putAll(handleAlipayPay(order, req));
                break;
            case STRIPE:
                payData.putAll(handleStripePay(order, req));
                break;
            case COD:
                // 货到付款不应被标记为线上支付成功，保持 WAIT_PAY，交由履约/收款流程确认。
                payData.put("channel", "COD");
                payData.put("status", CashierOrderStatusEnum.WAIT_PAY.getCode());
                payData.put("requiresDeliveryConfirmation", true);
                payData.put("message", "订单将在送达时收款");
                break;
            default:
                payData.put("__placeholder__", true);
                payData.put("tips", method.getName() + " 将在后续 Task 对接");
        }
        return payData;
    }

    private Map<String, Object> handleAlipayPay(PayCashierOrderEntity order, CashierCreateRequest request) {
        Map<String, Object> channelRequest = new HashMap<>();
        channelRequest.put("mchNo", order.getMchNo());
        channelRequest.put("outTradeNo", order.getCashierNo());
        channelRequest.put("amount", order.getAmount());
        channelRequest.put("subject", order.getSubject());
        channelRequest.put("body", order.getBody());
        channelRequest.put("terminal", order.getTerminal());
        channelRequest.put("notifyUrl", order.getNotifyUrl());
        channelRequest.put("returnUrl", order.getReturnUrl());
        channelRequest.put("channelCode", request.getChannelCode());
        Map<String, Object> result = channelStrategyFactory
                .require(PayMethodEnum.ALIPAY.getCode())
                .createPay(channelRequest);
        order.setStatus(CashierOrderStatusEnum.PAYING.getCode());
        Object channelOrderNo = result.get("channelOrderNo");
        if (channelOrderNo != null) order.setPayOrderId(String.valueOf(channelOrderNo));
        return result;
    }

    private Map<String, Object> handleWechatV3Pay(PayCashierOrderEntity order, CashierCreateRequest request) {
        Map<String, Object> channelRequest = new HashMap<>();
        channelRequest.put("mchNo", order.getMchNo());
        channelRequest.put("outTradeNo", order.getCashierNo());
        channelRequest.put("amount", order.getAmount());
        channelRequest.put("subject", order.getSubject());
        channelRequest.put("body", order.getBody());
        channelRequest.put("terminal", order.getTerminal());
        channelRequest.put("openId", request.getOpenId());
        channelRequest.put("notifyUrl", order.getNotifyUrl());
        channelRequest.put("channelCode", request.getChannelCode());
        Map<String, Object> result = channelStrategyFactory
                .require(PayMethodEnum.WECHAT_V3.getCode())
                .createPay(channelRequest);
        order.setStatus(CashierOrderStatusEnum.PAYING.getCode());
        Object channelOrderNo = result.get("channelOrderNo");
        if (channelOrderNo != null) order.setPayOrderId(String.valueOf(channelOrderNo));
        return result;
    }

    private Map<String, Object> handleStripePay(PayCashierOrderEntity order, CashierCreateRequest request) {
        Map<String, Object> channelRequest = new HashMap<>();
        channelRequest.put("mchNo", order.getMchNo());
        channelRequest.put("outTradeNo", order.getCashierNo());
        channelRequest.put("amount", order.getAmount());
        channelRequest.put("currency", order.getCurrency());
        channelRequest.put("subject", StringUtils.hasText(order.getSubject()) ? order.getSubject() : "订单支付");
        channelRequest.put("body", order.getBody());
        channelRequest.put("terminal", order.getTerminal());
        channelRequest.put("notifyUrl", order.getNotifyUrl());
        channelRequest.put("returnUrl", order.getReturnUrl());
        channelRequest.put("channelCode", request.getChannelCode());
        Map<String, Object> result = channelStrategyFactory
                .require(PayMethodEnum.STRIPE.getCode())
                .createPay(channelRequest);
        order.setStatus(CashierOrderStatusEnum.PAYING.getCode());
        Object channelOrderNo = result.get("channelOrderNo");
        if (channelOrderNo != null) {
            order.setPayOrderId(String.valueOf(channelOrderNo));
        }
        return result;
    }

    /**
     * 微信支付真实下单（按 terminal 路由：目前支持 WX_MINI/WAP/PC 走 JSAPI；
     * 其余 terminal（APP/Native）待后续 Task 扩展）
     */
    private Map<String, Object> handleWechatPay(PayCashierOrderEntity order, CashierCreateRequest req) {
        Map<String, Object> data = new HashMap<>();
        String terminal = order.getTerminal();
        TerminalTypeEnum terminalEnum = TerminalTypeEnum.ofCode(terminal);
        if (terminalEnum == null) {
            throw new IllegalArgumentException("非法的 terminal: " + terminal);
        }
        switch (terminalEnum) {
            case WX_MINI:
            case WAP:
            case PC: {
                // JSAPI 支付：需要 openId
                if (!StringUtils.hasText(req.getOpenId())) {
                    throw new IllegalArgumentException("微信 JSAPI 支付需要 openId，请前端登录后传入");
                }
                Map<String, String> packageParams = wxJsapiPayHandler.unifiedOrder(order, req.getOpenId(), order.getClientIp(), req.getChannelCode());
                // 订单状态推进为支付中
                order.setStatus(CashierOrderStatusEnum.PAYING.getCode());
                data.put("channel", "WX_JSAPI");
                data.put("packageParams", packageParams);
                return data;
            }
            case APP:
            case HARMONY:
                // 预留：APP 支付（trade_type=APP），后续 Task 1 扩展
                data.put("__placeholder__", true);
                data.put("tips", "微信 APP 支付后续扩展，当前 terminal=" + terminal);
                return data;
            default:
                // 其余终端（如其他小程序/快应用）目前不支持微信支付
                throw new IllegalArgumentException("terminal=" + terminal + " 暂不支持微信支付");
        }
    }

    /**
     * 余额支付立即扣款（事务内）
     */
    private CashierStatusResponse buildStatusResponse(PayCashierOrderEntity order) {
        CashierStatusResponse resp = new CashierStatusResponse();
        resp.setCashierNo(order.getCashierNo());
        resp.setPayOrderId(order.getPayOrderId());
        resp.setMchOrderNo(order.getMchOrderNo());
        resp.setPayMethod(order.getPayMethod());
        resp.setTerminal(order.getTerminal());
        resp.setAmount(order.getAmount());
        resp.setStatus(order.getStatus());
        CashierOrderStatusEnum st = CashierOrderStatusEnum.ofCode(order.getStatus());
        resp.setStatusDesc(st == null ? "未知" : st.getName());
        resp.setSuccessTime(order.getSuccessTime());
        resp.setErrCode(order.getErrCode());
        resp.setErrMsg(order.getErrMsg());
        return resp;
    }
}
