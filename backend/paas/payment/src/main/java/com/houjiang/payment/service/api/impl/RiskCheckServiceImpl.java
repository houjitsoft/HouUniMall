package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.payment.dto.risk.RiskCheckRequest;
import com.houjiang.payment.dto.risk.RiskCheckResponse;
import com.houjiang.payment.entity.PayRiskBlacklistEntity;
import com.houjiang.payment.entity.PayRiskLogEntity;
import com.houjiang.payment.entity.PayRiskRuleEntity;
import com.houjiang.payment.mapper.PayRiskLogMapper;
import com.houjiang.payment.mapper.PayRiskRuleMapper;
import com.houjiang.payment.service.api.RiskBlacklistService;
import com.houjiang.payment.service.api.RiskCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 风控引擎核心实现
 * 覆盖：黑白名单前置 → 金额限额 → 频次控制 → 累计金额 → 记录日志
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskCheckServiceImpl implements RiskCheckService {

    private final PayRiskRuleMapper ruleMapper;
    private final PayRiskLogMapper logMapper;
    private final RiskBlacklistService blacklistService;

    /** 业务类型与黑名单 bizScope 语义一致 */
    @Override
    public RiskCheckResponse check(RiskCheckRequest req) {
        String logNo = generateLogNo();
        log.info("[risk/check] logNo={}, bizType={}, amount={}, userId={}, mchNo={}",
                logNo, req.getBizType(), req.getAmount(), req.getUserId(), req.getMchNo());

        RiskCheckResponse resp = new RiskCheckResponse();
        resp.setLogNo(logNo);
        resp.setAction(1);
        resp.setRiskLevel(0);
        resp.setPass(true);

        // ============= 1. 白名单 =============
        if (matchWhitelist(req)) {
            saveLog(logNo, req, resp, "命中白名单直接放行");
            resp.setPass(true);
            resp.setAction(1);
            return resp;
        }

        // ============= 2. 黑名单 =============
        PayRiskBlacklistEntity black = matchBlacklist(req);
        if (black != null) {
            RiskCheckResponse.HitRuleItem h = new RiskCheckResponse.HitRuleItem();
            h.setRuleNo("BLACKLIST_" + black.getTargetType());
            h.setRuleName("黑名单命中（targetType=" + black.getTargetType() + "）");
            h.setRuleType(3);
            h.setAction(2);
            h.setReason(black.getReason());
            resp.getHitRules().add(h);
            resp.setPass(false);
            resp.setAction(2);
            resp.setRiskLevel(3);
            resp.setRejectReason("命中黑名单: " + StrUtil.blankToDefault(black.getReason(), "未提供原因"));
            saveLog(logNo, req, resp, resp.getRejectReason());
            return resp;
        }

        // ============= 3. 遍历规则 =============
        List<PayRiskRuleEntity> rules = loadApplicableRules(req);
        for (PayRiskRuleEntity rule : rules) {
            RiskCheckResponse.HitRuleItem hit = evaluate(rule, req);
            if (hit == null) continue;
            resp.getHitRules().add(hit);
            // action：2-拦截 优先级最高，3-告警 4-审核 1-放行
            if (rule.getAction() == 2) {
                resp.setPass(false);
                resp.setAction(2);
                resp.setRiskLevel(3);
                resp.setRejectReason("命中规则[" + rule.getRuleName() + "]");
                break;
            } else if (rule.getAction() == 4 && resp.getAction() < 4) {
                resp.setAction(4);
                resp.setRiskLevel(Math.max(resp.getRiskLevel(), 2));
                resp.setPass(false);
                resp.setRejectReason("需人工审核: " + rule.getRuleName());
            } else if (rule.getAction() == 3 && resp.getAction() < 3) {
                resp.setAction(3);
                resp.setRiskLevel(Math.max(resp.getRiskLevel(), 1));
                resp.setRejectReason("规则告警: " + rule.getRuleName());
            }
        }

        saveLog(logNo, req, resp, resp.getRejectReason());
        return resp;
    }

    // ============================ 工具方法 ============================

    /** 白名单匹配：用户或商户命中 bizScope=0 或对应 bizType */
    private boolean matchWhitelist(RiskCheckRequest req) {
        int bizScope = req.getBizType() == null ? 0 : req.getBizType();
        if (StrUtil.isNotBlank(req.getUserId())
                && blacklistService.matchTarget(2, 1, req.getUserId(), bizScope) != null) return true;
        if (StrUtil.isNotBlank(req.getMchNo())
                && blacklistService.matchTarget(2, 2, req.getMchNo(), bizScope) != null) return true;
        return false;
    }

    /** 黑名单匹配：用户/商户/IP/卡/手机/身份证 任一命中 */
    private PayRiskBlacklistEntity matchBlacklist(RiskCheckRequest req) {
        int bizScope = req.getBizType() == null ? 0 : req.getBizType();
        PayRiskBlacklistEntity r;
        if (StrUtil.isNotBlank(req.getUserId())    && (r = blacklistService.matchTarget(1, 1, req.getUserId(), bizScope)) != null) return r;
        if (StrUtil.isNotBlank(req.getMchNo())     && (r = blacklistService.matchTarget(1, 2, req.getMchNo(), bizScope)) != null) return r;
        if (StrUtil.isNotBlank(req.getUserIp())    && (r = blacklistService.matchTarget(1, 3, req.getUserIp(), bizScope)) != null) return r;
        if (StrUtil.isNotBlank(req.getBankCardNo()) && (r = blacklistService.matchTarget(1, 4, req.getBankCardNo(), bizScope)) != null) return r;
        if (StrUtil.isNotBlank(req.getPhone())     && (r = blacklistService.matchTarget(1, 5, req.getPhone(), bizScope)) != null) return r;
        if (StrUtil.isNotBlank(req.getIdCardNo())  && (r = blacklistService.matchTarget(1, 6, req.getIdCardNo(), bizScope)) != null) return r;
        return null;
    }

    /** 加载当前生效且适用的规则（按 priority 升序） */
    private List<PayRiskRuleEntity> loadApplicableRules(RiskCheckRequest req) {
        Date now = new Date();
        LambdaQueryWrapper<PayRiskRuleEntity> qw = new LambdaQueryWrapper<PayRiskRuleEntity>()
                .eq(PayRiskRuleEntity::getStatus, 1)
                // 黑白名单由专用表处理，这里仅金额/频次/自定义
                .ne(PayRiskRuleEntity::getRuleType, 3)
                .and(w -> w.isNull(PayRiskRuleEntity::getEffectiveTime)
                           .or().le(PayRiskRuleEntity::getEffectiveTime, now))
                .and(w -> w.isNull(PayRiskRuleEntity::getExpireTime)
                           .or().ge(PayRiskRuleEntity::getExpireTime, now))
                .and(w -> w.eq(PayRiskRuleEntity::getBizType, 0)
                           .or().eq(PayRiskRuleEntity::getBizType, req.getBizType()))
                .orderByAsc(PayRiskRuleEntity::getPriority);
        List<PayRiskRuleEntity> all = ruleMapper.selectList(qw);
        // 再按 scope 过滤
        return all.stream().filter(r -> scopeApplicable(r, req)).collect(Collectors.toList());
    }

    /** 校验规则 scope 是否适用 */
    private boolean scopeApplicable(PayRiskRuleEntity rule, RiskCheckRequest req) {
        // 全局
        if (rule.getScope() == null || rule.getScope() == 1) return true;
        if (rule.getScope() == 2) {
            return StrUtil.isBlank(rule.getScopeValue())
                    || rule.getScopeValue().equals(req.getMchNo());
        }
        if (rule.getScope() == 3) {
            return StrUtil.isBlank(rule.getScopeValue())
                    || rule.getScopeValue().equals(req.getUserId());
        }
        if (rule.getScope() == 4) {
            return StrUtil.isBlank(rule.getScopeValue())
                    || rule.getScopeValue().equals(req.getPayMethod());
        }
        return true;
    }

    /** 评估单条规则；返回 HitRuleItem 代表命中，null 代表未命中 */
    private RiskCheckResponse.HitRuleItem evaluate(PayRiskRuleEntity rule, RiskCheckRequest req) {
        String field = StrUtil.blankToDefault(rule.getTriggerField(), "amount");
        BigDecimal actual;
        try {
            actual = computeActualValue(field, rule, req);
        } catch (Exception ex) {
            log.warn("[risk] evaluate fail rule={} err={}", rule.getRuleNo(), ex.getMessage());
            return null;
        }
        if (actual == null) return null;
        BigDecimal threshold = rule.getThresholdValue() == null ? BigDecimal.ZERO : rule.getThresholdValue();
        boolean violate = compareViolate(rule.getOperator(), actual, threshold);
        if (!violate) return null;

        RiskCheckResponse.HitRuleItem hit = new RiskCheckResponse.HitRuleItem();
        hit.setRuleNo(rule.getRuleNo());
        hit.setRuleName(rule.getRuleName());
        hit.setRuleType(rule.getRuleType());
        hit.setAction(rule.getAction());
        hit.setTriggerField(field);
        hit.setOperator(rule.getOperator());
        hit.setThresholdValue(threshold.toPlainString());
        hit.setActualValue(actual.toPlainString());
        hit.setReason("规则命中：" + field + " " + rule.getOperator() + " " + threshold + " 但实际=" + actual);
        return hit;
    }

    /** 计算规则实际值 */
    private BigDecimal computeActualValue(String field, PayRiskRuleEntity rule, RiskCheckRequest req) {
        if ("amount".equalsIgnoreCase(field)) {
            return req.getAmount() == null ? BigDecimal.ZERO : req.getAmount();
        }
        // 时间窗口内的聚合查询
        Integer tw = rule.getTimeWindow() == null ? 0 : rule.getTimeWindow();
        Date since = tw > 0 ? new Date(System.currentTimeMillis() - tw * 1000L) : new Date(0);
        LambdaQueryWrapper<PayRiskLogEntity> qw = new LambdaQueryWrapper<PayRiskLogEntity>()
                .ge(PayRiskLogEntity::getCreateTime, since)
                .in(PayRiskLogEntity::getAction, java.util.Arrays.asList(1, 3));
        if (req.getBizType() != null) qw.eq(PayRiskLogEntity::getBizType, req.getBizType());
        if (StrUtil.isNotBlank(req.getUserId())) qw.eq(PayRiskLogEntity::getUserId, req.getUserId());
        if (StrUtil.isNotBlank(req.getMchNo())) qw.eq(PayRiskLogEntity::getMchNo, req.getMchNo());
        List<PayRiskLogEntity> logs = logMapper.selectList(qw);
        if ("count".equalsIgnoreCase(field)) {
            // 本次 +1
            return new BigDecimal(logs.size() + 1);
        }
        if ("sumAmount".equalsIgnoreCase(field)) {
            BigDecimal sum = logs.stream()
                    .map(PayRiskLogEntity::getAmount)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (req.getAmount() != null) sum = sum.add(req.getAmount());
            return sum;
        }
        return null;
    }

    /** 返回 true 代表违反规则（即需要触发 action） */
    private boolean compareViolate(String operator, BigDecimal actual, BigDecimal threshold) {
        String op = StrUtil.blankToDefault(operator, ">=");
        int cmp = actual.compareTo(threshold);
        switch (op) {
            case ">":  return cmp > 0;
            case ">=": return cmp >= 0;
            case "<":  return cmp < 0;
            // <= 语义：阈值上限，若 actual > threshold 则违反
            case "<=": return !(cmp <= 0);
            case "=":
            case "==": return cmp == 0;
            case "!=": return cmp != 0;
            default:   return false;
        }
    }

    /** 日志落库（放行/拦截/告警/待审核 都记录，保留审计轨迹） */
    private void saveLog(String logNo, RiskCheckRequest req, RiskCheckResponse resp, String reason) {
        try {
            PayRiskLogEntity e = new PayRiskLogEntity();
            e.setLogNo(logNo);
            e.setBizType(req.getBizType());
            e.setBizNo(req.getBizNo());
            e.setMchNo(req.getMchNo());
            e.setUserId(req.getUserId());
            e.setUserIp(req.getUserIp());
            e.setAmount(req.getAmount());
            e.setPayMethod(req.getPayMethod());
            List<String> hitNos = new ArrayList<>();
            for (RiskCheckResponse.HitRuleItem h : resp.getHitRules()) hitNos.add(h.getRuleNo());
            e.setHitRuleNos(String.join(",", hitNos));
            e.setHitRulesJson(JSONUtil.toJsonStr(resp.getHitRules()));
            e.setRiskLevel(resp.getRiskLevel());
            e.setAction(resp.getAction());
            e.setRejectReason(reason);
            e.setExtraContext(req.getExtraContext());
            e.setCreateTime(new Date());
            e.setUpdateTime(new Date());
            logMapper.insert(e);
        } catch (Exception ex) {
            log.error("[risk/log] save fail logNo={} err={}", logNo, ex.getMessage());
        }
    }

    private String generateLogNo() {
        String ts = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        int rand = new Random().nextInt(900) + 100;
        return "RISK" + ts + rand;
    }
}
