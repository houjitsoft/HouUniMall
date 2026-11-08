package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.aiagent.entity.AiTokenUsage;
import com.houjiang.aiagent.mapper.AiTokenUsageMapper;
import com.houjiang.aiagent.support.AiPageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 中台 - 调用计量明细（{@code hou_ai_token_usage}）。
 *
 * <p>该表由 GEO 建并持续写入（1500+ 行历史），前缀本就符合中台规范，
 * 因此中台接管不搬数据，只补「按模块归集 + 汇总」的读能力；
 * 将来 GEO 改为远程记账时，数据仍在同一张表里，无需迁移。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiUsageRecordService {

    private final AiTokenUsageMapper usageMapper;

    public Map<String, Object> list(Map<String, Object> params) {
        LambdaQueryWrapper<AiTokenUsage> wrapper = new LambdaQueryWrapper<>();
        String module = AiPageSupport.str(params, "module");
        if (StringUtils.hasText(module)) {
            wrapper.eq(AiTokenUsage::getModuleName, module);
        }
        String model = AiPageSupport.str(params, "model");
        if (StringUtils.hasText(model)) {
            wrapper.eq(AiTokenUsage::getModelName, model);
        }
        String bizType = AiPageSupport.str(params, "bizType");
        if (StringUtils.hasText(bizType)) {
            wrapper.eq(AiTokenUsage::getBizType, bizType);
        }
        Long tenantId = AiPageSupport.toLong(params, "tenantId");
        if (tenantId != null) {
            wrapper.eq(AiTokenUsage::getTenantId, tenantId);
        }
        wrapper.orderByDesc(AiTokenUsage::getId);
        IPage<AiTokenUsage> page = usageMapper.selectPage(AiPageSupport.page(params), wrapper);
        return AiPageSupport.pageResult(page, page.getRecords());
    }

    /** 记一笔调用明细；失败只记日志，绝不影响主流程 */
    public void record(AiTokenUsage usage) {
        if (usage == null) {
            return;
        }
        if (!StringUtils.hasText(usage.getModuleName())) {
            usage.setModuleName("aiagent");
        }
        try {
            usage.setId(null);
            usageMapper.insert(usage);
        } catch (Exception e) {
            log.warn("[aiagent] 计量明细写入失败 module={} msg={}", usage.getModuleName(), e.getMessage());
        }
    }

    /** 总览：调用次数、token 总量、金额总量、平均耗时 */
    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Long count = usageMapper.selectCount(null);
            result.put("totalCalls", count == null ? 0L : count);
            result.put("modules", moduleBreakdown());
        } catch (Exception e) {
            log.warn("[aiagent] 计量汇总失败 msg={}", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> moduleBreakdown() {
        Map<String, Object> breakdown = new LinkedHashMap<>();
        for (String module : new String[]{"geo", "aiagent", "mall"}) {
            Long c = usageMapper.selectCount(new LambdaQueryWrapper<AiTokenUsage>()
                    .eq(AiTokenUsage::getModuleName, module));
            breakdown.put(module, c == null ? 0L : c);
        }
        return breakdown;
    }

    public static BigDecimal priceOf(Long promptTokens, Long completionTokens,
                                     BigDecimal per1kInput, BigDecimal per1kOutput) {
        BigDecimal in = per1kInput == null ? BigDecimal.ZERO : per1kInput;
        BigDecimal out = per1kOutput == null ? BigDecimal.ZERO : per1kOutput;
        long p = promptTokens == null ? 0L : promptTokens;
        long c = completionTokens == null ? 0L : completionTokens;
        return in.multiply(BigDecimal.valueOf(p)).divide(BigDecimal.valueOf(1000), 8, java.math.RoundingMode.HALF_UP)
                .add(out.multiply(BigDecimal.valueOf(c)).divide(BigDecimal.valueOf(1000), 8, java.math.RoundingMode.HALF_UP));
    }
}
