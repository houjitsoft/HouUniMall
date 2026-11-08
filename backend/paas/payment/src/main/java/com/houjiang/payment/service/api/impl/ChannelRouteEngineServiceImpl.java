package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.payment.dto.channel.ChannelLogListRequest;
import com.houjiang.payment.dto.channel.ChannelResultReport;
import com.houjiang.payment.dto.channel.RouteSelectRequest;
import com.houjiang.payment.dto.channel.RouteSelectResponse;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.entity.PayChannelLogEntity;
import com.houjiang.payment.entity.PayChannelRouteEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayChannelLogMapper;
import com.houjiang.payment.mapper.PayChannelMapper;
import com.houjiang.payment.mapper.PayChannelRouteMapper;
import com.houjiang.payment.service.api.ChannelRouteEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 渠道路由核心引擎
 *
 * select 流程：
 *   1. 根据 bizType/payMethod/terminal/merchantNo/amount 匹配有效路由规则（priority asc 取第一条）
 *   2. 解析 channelCodes，加载候选渠道
 *   3. 过滤不可用渠道（stopped / 熔断 / 金额越界）
 *   4. 按 strategy 选中一个（PRIORITY/WEIGHT/RANDOM）
 *   5. 落 hou_pay_channel_log（result=0 未知）并返回 logNo
 *
 * report 流程：
 *   1. 回写 log 的 result/costMs/errorCode
 *   2. 更新渠道 totalCount/successCount/healthScore/lastFailedAt/circuitStatus
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelRouteEngineServiceImpl implements ChannelRouteEngineService {

    private final PayChannelRouteMapper routeMapper;
    private final PayChannelMapper channelMapper;
    private final PayChannelLogMapper logMapper;

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RouteSelectResponse select(RouteSelectRequest req) {
        log.info("[route/select] bizType={}, payMethod={}, terminal={}, merchantNo={}, amount={}, bizNo={}",
                req.getBizType(), req.getPayMethod(), req.getTerminal(), req.getMerchantNo(), req.getAmount(), req.getBizNo());

        String logNo = buildLogNo();
        Date now = new Date();

        // 1. 匹配路由规则
        PayChannelRouteEntity route = matchRoute(req, now);
        if (route == null) {
            persistLog(logNo, req, null, null, null, null, "NO_ROUTE_MATCHED");
            return RouteSelectResponse.fail(logNo, "未匹配到可用路由规则");
        }

        // 2. 候选渠道
        List<String> codes = splitCodes(route.getChannelCodes());
        if (codes.isEmpty()) {
            persistLog(logNo, req, route.getRouteNo(), null, route.getStrategy(), null, "EMPTY_CANDIDATES");
            return RouteSelectResponse.fail(logNo, "路由规则未配置候选渠道");
        }

        List<PayChannelEntity> candidates = channelMapper.selectList(new LambdaQueryWrapper<PayChannelEntity>()
                .in(PayChannelEntity::getChannelCode, codes)
                .eq(PayChannelEntity::getStatus, 1));
        // 过滤熔断 & 金额越界
        List<PayChannelEntity> usable = candidates.stream()
                .filter(c -> c.getCircuitStatus() == null || c.getCircuitStatus() != 2)
                .filter(c -> c.getSingleLimit() == null || req.getAmount().compareTo(c.getSingleLimit()) <= 0)
                .filter(c -> c.getSingleMin() == null || req.getAmount().compareTo(c.getSingleMin()) >= 0)
                .collect(Collectors.toList());

        if (usable.isEmpty()) {
            persistLog(logNo, req, route.getRouteNo(), null, route.getStrategy(), candidates.stream().map(PayChannelEntity::getChannelCode).collect(Collectors.joining(",")), "NO_USABLE_CHANNEL");
            return RouteSelectResponse.fail(logNo, "所有候选渠道均不可用（停用/熔断/超限额）");
        }

        // 3. 按策略选中一个
        String strategy = StrUtil.blankToDefault(route.getStrategy(), "WEIGHT");
        PayChannelEntity selected = pickByStrategy(usable, strategy);

        // 4. 组装降级链（主渠道 + 其他可用候选作为备选）
        String fallbackChain = usable.stream()
                .map(PayChannelEntity::getChannelCode)
                .collect(Collectors.joining("->"));

        // 5. 落日志
        persistLog(logNo, req, route.getRouteNo(), selected.getChannelCode(), strategy, fallbackChain, null);

        // 6. 返回
        RouteSelectResponse resp = new RouteSelectResponse();
        resp.setMatched(true);
        resp.setLogNo(logNo);
        resp.setRouteNo(route.getRouteNo());
        resp.setChannelCode(selected.getChannelCode());
        resp.setChannelName(selected.getChannelName());
        resp.setProvider(selected.getProvider());
        resp.setSelectStrategy(strategy);
        resp.setCandidates(usable.stream().map(PayChannelEntity::getChannelCode).collect(Collectors.toList()));
        return resp;
    }

    /**
     * 退款渠道路由（W5+ P2-1）
     * <p>
     * 核心差异于 {@link #select}：
     * <ol>
     *   <li>强制覆盖 bizType=2（REFUND）</li>
     *   <li>要求传 originChannelCode（原支付走的渠道）</li>
     *   <li>匹配到路由后，需检查原渠道是否在 channelCodes（白名单）中</li>
     *   <li>原渠道不在白名单 → 返回错误（路由配置缺失）</li>
     *   <li>原渠道在白名单但被熔断/超限 → 返回 MANUAL_REFUND_REQUIRED（提示人工冲正）</li>
     *   <li>原渠道可用 → 选中原渠道（原路退回硬约束，忽略 strategy）</li>
     * </ol>
     * <p>业务价值：
     * <ul>
     *   <li>金额段灰度：在 PayChannelRoute 中配 amountMin/amountMax，仅对区间内金额选中该路由</li>
     *   <li>商户白名单：PayChannelRoute.merchantNo 不为空时仅对该商户生效</li>
     *   <li>退款冲正阈值：PayChannel.singleLimit 超限 → 人工冲正</li>
     * </ul>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RouteSelectResponse selectRefund(RouteSelectRequest req) {
        // 1. 参数校验：退款路由必须传 originChannelCode
        if (StrUtil.isBlank(req.getOriginChannelCode())) {
            throw PaymentException.bizError("退款路由必须提供 originChannelCode");
        }
        // 2. 强制覆盖 bizType=2（退款）
        req.setBizType(2);
        log.info("[route/selectRefund] originChannel={}, payMethod={}, terminal={}, merchantNo={}, amount={}, bizNo={}",
                req.getOriginChannelCode(), req.getPayMethod(), req.getTerminal(),
                req.getMerchantNo(), req.getAmount(), req.getBizNo());

        String logNo = buildLogNo();
        Date now = new Date();

        // 3. 匹配退款路由规则
        PayChannelRouteEntity route = matchRoute(req, now);
        if (route == null) {
            persistLog(logNo, req, null, null, null, null, "NO_REFUND_ROUTE_MATCHED");
            return RouteSelectResponse.fail(logNo, "未匹配到退款路由规则");
        }

        // 4. 原渠道是否在路由白名单中
        List<String> codes = splitCodes(route.getChannelCodes());
        if (!codes.contains(req.getOriginChannelCode())) {
            persistLog(logNo, req, route.getRouteNo(), null, route.getStrategy(), route.getChannelCodes(), "ORIGIN_CHANNEL_NOT_IN_ROUTE");
            return RouteSelectResponse.fail(logNo,
                    "原渠道 " + req.getOriginChannelCode() + " 不在退款路由白名单中：" + route.getChannelCodes());
        }

        // 5. 加载原渠道实体检查可用性
        PayChannelEntity origin = channelMapper.selectOne(new LambdaQueryWrapper<PayChannelEntity>()
                .eq(PayChannelEntity::getChannelCode, req.getOriginChannelCode())
                .eq(PayChannelEntity::getStatus, 1)
                .last("LIMIT 1"));
        if (origin == null) {
            persistLog(logNo, req, route.getRouteNo(), req.getOriginChannelCode(), route.getStrategy(), route.getChannelCodes(), "ORIGIN_CHANNEL_DISABLED");
            return RouteSelectResponse.fail(logNo, "原渠道已停用，请人工冲正");
        }

        // 6. 原渠道状态检查：熔断/超限 → MANUAL_REFUND_REQUIRED
        boolean circuitBreak = origin.getCircuitStatus() != null && origin.getCircuitStatus() == 2;
        boolean overSingleLimit = origin.getSingleLimit() != null && req.getAmount().compareTo(origin.getSingleLimit()) > 0;
        boolean belowSingleMin = origin.getSingleMin() != null && req.getAmount().compareTo(origin.getSingleMin()) < 0;
        if (circuitBreak || overSingleLimit || belowSingleMin) {
            String reason;
            if (circuitBreak) reason = "原渠道已熔断";
            else if (overSingleLimit) reason = "退款金额超原渠道单笔上限";
            else reason = "退款金额低于原渠道单笔下限";
            persistLog(logNo, req, route.getRouteNo(), origin.getChannelCode(), route.getStrategy(), route.getChannelCodes(), "MANUAL_REFUND_REQUIRED");
            RouteSelectResponse manual = new RouteSelectResponse();
            manual.setMatched(false);
            manual.setLogNo(logNo);
            manual.setRouteNo(route.getRouteNo());
            manual.setChannelCode(origin.getChannelCode());
            manual.setChannelName(origin.getChannelName());
            manual.setSelectStrategy("MANUAL");
            manual.setCandidates(java.util.Collections.singletonList(origin.getChannelCode()));
            manual.setErrorMsg("MANUAL_REFUND_REQUIRED: " + reason + "，请走人工冲正流程");
            return manual;
        }

        // 7. 原渠道可用：原路退回，忽略 strategy，选中原渠道
        persistLog(logNo, req, route.getRouteNo(), origin.getChannelCode(), "ORIGIN", origin.getChannelCode(), null);
        RouteSelectResponse resp = new RouteSelectResponse();
        resp.setMatched(true);
        resp.setLogNo(logNo);
        resp.setRouteNo(route.getRouteNo());
        resp.setChannelCode(origin.getChannelCode());
        resp.setChannelName(origin.getChannelName());
        resp.setProvider(origin.getProvider());
        resp.setSelectStrategy("ORIGIN");
        resp.setCandidates(java.util.Collections.singletonList(origin.getChannelCode()));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean report(ChannelResultReport report) {
        if (StrUtil.isBlank(report.getLogNo()) || report.getResult() == null) {
            throw PaymentException.bizError("logNo 与 result 必填");
        }
        log.info("[route/report] logNo={}, channelCode={}, result={}, costMs={}", report.getLogNo(), report.getChannelCode(), report.getResult(), report.getCostMs());

        // 1. 回写 log
        int rows = logMapper.update(null, new LambdaUpdateWrapper<PayChannelLogEntity>()
                .eq(PayChannelLogEntity::getLogNo, report.getLogNo())
                .set(PayChannelLogEntity::getResult, report.getResult())
                .set(PayChannelLogEntity::getCostMs, report.getCostMs())
                .set(PayChannelLogEntity::getErrorCode, report.getErrorCode())
                .set(PayChannelLogEntity::getErrorMsg, report.getErrorMsg())
                .set(PayChannelLogEntity::getUpdateTime, new Date()));
        if (rows == 0) throw PaymentException.bizError("调用日志不存在: " + report.getLogNo());

        // 2. 更新渠道指标
        String channelCode = report.getChannelCode();
        if (StrUtil.isBlank(channelCode)) {
            PayChannelLogEntity lg = logMapper.selectOne(new LambdaQueryWrapper<PayChannelLogEntity>()
                    .eq(PayChannelLogEntity::getLogNo, report.getLogNo()));
            if (lg != null) channelCode = lg.getChannelCode();
        }
        if (StrUtil.isBlank(channelCode)) return true;

        PayChannelEntity ch = channelMapper.selectOne(new LambdaQueryWrapper<PayChannelEntity>()
                .eq(PayChannelEntity::getChannelCode, channelCode));
        if (ch == null) return true;

        long total = (ch.getTotalCount() == null ? 0 : ch.getTotalCount()) + 1;
        long success = (ch.getSuccessCount() == null ? 0 : ch.getSuccessCount())
                + (report.getResult() == 1 ? 1 : 0);
        int health = (int) Math.max(0, Math.min(100, total == 0 ? 100 : success * 100 / total));
        int newCircuit = computeCircuit(health, report.getResult());

        LambdaUpdateWrapper<PayChannelEntity> uw = new LambdaUpdateWrapper<PayChannelEntity>()
                .eq(PayChannelEntity::getChannelCode, channelCode)
                .set(PayChannelEntity::getTotalCount, total)
                .set(PayChannelEntity::getSuccessCount, success)
                .set(PayChannelEntity::getHealthScore, health)
                .set(PayChannelEntity::getCircuitStatus, newCircuit)
                .set(PayChannelEntity::getUpdateTime, new Date());
        if (report.getResult() != 1) {
            uw.set(PayChannelEntity::getLastFailedAt, new Date());
        }
        channelMapper.update(null, uw);
        return true;
    }

    @Override
    public IPage<PayChannelLogEntity> logPage(ChannelLogListRequest req) {
        Page<PayChannelLogEntity> page = new Page<>(
                req.getPageNo() == null ? 1 : req.getPageNo(),
                req.getPageSize() == null ? 10 : req.getPageSize());
        LambdaQueryWrapper<PayChannelLogEntity> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getLogNo()))       qw.eq(PayChannelLogEntity::getLogNo, req.getLogNo());
        if (StrUtil.isNotBlank(req.getBizNo()))       qw.eq(PayChannelLogEntity::getBizNo, req.getBizNo());
        if (StrUtil.isNotBlank(req.getChannelCode())) qw.eq(PayChannelLogEntity::getChannelCode, req.getChannelCode());
        if (req.getResult() != null)                  qw.eq(PayChannelLogEntity::getResult, req.getResult());
        qw.orderByDesc(PayChannelLogEntity::getId);
        return logMapper.selectPage(page, qw);
    }

    @Override
    public PayChannelLogEntity logDetail(String logNo) {
        return logMapper.selectOne(new LambdaQueryWrapper<PayChannelLogEntity>()
                .eq(PayChannelLogEntity::getLogNo, logNo));
    }

    /** =============== private helpers =============== */

    private PayChannelRouteEntity matchRoute(RouteSelectRequest req, Date now) {
        List<PayChannelRouteEntity> list = routeMapper.selectList(new LambdaQueryWrapper<PayChannelRouteEntity>()
                .eq(PayChannelRouteEntity::getStatus, 1)
                .and(w -> w.eq(PayChannelRouteEntity::getBizType, 0).or().eq(PayChannelRouteEntity::getBizType, req.getBizType()))
                .orderByAsc(PayChannelRouteEntity::getPriority));
        for (PayChannelRouteEntity r : list) {
            // 支付方式
            if (StrUtil.isNotBlank(r.getPayMethod()) && !r.getPayMethod().equals(req.getPayMethod())) continue;
            // 终端
            if (StrUtil.isNotBlank(r.getTerminal()) && !r.getTerminal().equals(req.getTerminal())) continue;
            // 商户号
            if (StrUtil.isNotBlank(r.getMerchantNo()) && !r.getMerchantNo().equals(req.getMerchantNo())) continue;
            // 金额
            if (r.getAmountMin() != null && req.getAmount().compareTo(r.getAmountMin()) < 0) continue;
            if (r.getAmountMax() != null && r.getAmountMax().compareTo(ZERO) > 0 && req.getAmount().compareTo(r.getAmountMax()) > 0) continue;
            // 时间
            if (r.getEffectiveTime() != null && now.before(r.getEffectiveTime())) continue;
            if (r.getExpireTime() != null && now.after(r.getExpireTime())) continue;
            return r;
        }
        return null;
    }

    private PayChannelEntity pickByStrategy(List<PayChannelEntity> usable, String strategy) {
        if ("PRIORITY".equalsIgnoreCase(strategy)) {
            return usable.stream()
                    .sorted((a, b) -> Integer.compare(
                            a.getPriority() == null ? 100 : a.getPriority(),
                            b.getPriority() == null ? 100 : b.getPriority()))
                    .findFirst().orElse(usable.get(0));
        }
        if ("RANDOM".equalsIgnoreCase(strategy)) {
            return usable.get(ThreadLocalRandom.current().nextInt(usable.size()));
        }
        // WEIGHT 加权随机
        int total = usable.stream().mapToInt(c -> c.getWeight() == null ? 1 : Math.max(1, c.getWeight())).sum();
        int r = ThreadLocalRandom.current().nextInt(total);
        int acc = 0;
        for (PayChannelEntity c : usable) {
            acc += c.getWeight() == null ? 1 : Math.max(1, c.getWeight());
            if (r < acc) return c;
        }
        return usable.get(usable.size() - 1);
    }

    /**
     * 熔断决策：
     *   - 失败且健康度 < 30 → 熔断（2）
     *   - 失败且健康度 30~60 → 半开（1）
     *   - 成功或健康度 > 60 → 正常（0）
     */
    private int computeCircuit(int health, Integer result) {
        if (result != null && result == 1) {
            return health > 60 ? 0 : 1;
        }
        if (health < 30) return 2;
        if (health < 60) return 1;
        return 0;
    }

    private List<String> splitCodes(String codes) {
        if (StrUtil.isBlank(codes)) return new ArrayList<>();
        return Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    private void persistLog(String logNo, RouteSelectRequest req, String routeNo, String channelCode,
                            String strategy, String fallbackChain, String errorCode) {
        PayChannelLogEntity lg = new PayChannelLogEntity();
        lg.setLogNo(logNo);
        lg.setBizNo(req.getBizNo());
        lg.setRouteNo(routeNo);
        lg.setChannelCode(channelCode);
        lg.setPayMethod(req.getPayMethod());
        lg.setTerminal(req.getTerminal());
        lg.setMerchantNo(req.getMerchantNo());
        lg.setAmount(req.getAmount());
        lg.setSelectStrategy(strategy);
        lg.setFallbackChain(fallbackChain);
        lg.setResult(0);
        lg.setErrorCode(errorCode);
        lg.setCreateTime(new Date());
        lg.setUpdateTime(new Date());
        logMapper.insert(lg);
    }

    private String buildLogNo() {
        return "CL" + System.currentTimeMillis() + IdUtil.fastSimpleUUID().substring(0, 6);
    }
}
