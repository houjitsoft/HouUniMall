package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.aiagent.entity.AiUsageDaily;
import com.houjiang.aiagent.mapper.AiUsageDailyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用量日结。token 以 AI 网关返回的 usage 为准，这里只做业务维度归因
 * （场景 / 模型 / 商家），用于成本分摊与配额。日终与网关对账，误差大于 1% 需排查。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiUsageService {

    private final AiUsageDailyMapper usageMapper;

    public void record(String scene, String model, Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        if (scene == null) {
            return;
        }
        try {
            LocalDate today = LocalDate.now();
            String modelName = model == null ? "" : model;
            AiUsageDaily daily = usageMapper.selectOne(new LambdaQueryWrapper<AiUsageDaily>()
                    .eq(AiUsageDaily::getStatDate, today)
                    .eq(AiUsageDaily::getScene, scene)
                    .eq(AiUsageDaily::getModelName, modelName)
                    .last("limit 1"));
            long p = promptTokens == null ? 0 : promptTokens;
            long c = completionTokens == null ? 0 : completionTokens;
            long t = totalTokens == null ? (p + c) : totalTokens;
            if (daily != null) {
                daily.setRequestCount(daily.getRequestCount() + 1);
                daily.setPromptTokens(daily.getPromptTokens() + p);
                daily.setCompletionTokens(daily.getCompletionTokens() + c);
                daily.setTotalTokens(daily.getTotalTokens() + t);
                daily.setUpdateTime(LocalDateTime.now());
                usageMapper.updateById(daily);
                return;
            }
            AiUsageDaily created = new AiUsageDaily();
            created.setStatDate(today);
            created.setScene(scene);
            created.setModelName(modelName);
            created.setRequestCount(1L);
            created.setPromptTokens(p);
            created.setCompletionTokens(c);
            created.setTotalTokens(t);
            created.setCreateTime(LocalDateTime.now());
            created.setUpdateTime(LocalDateTime.now());
            usageMapper.insert(created);
        } catch (Exception e) {
            // 用量统计失败绝不能影响主流程
            log.warn("[aiagent] 用量记录失败 scene={} msg={}", scene, e.getMessage());
        }
    }
}
