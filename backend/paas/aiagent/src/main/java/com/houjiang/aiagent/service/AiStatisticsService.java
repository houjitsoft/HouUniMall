package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.aiagent.entity.AiGenerationRecord;
import com.houjiang.aiagent.mapper.AiChatSessionMapper;
import com.houjiang.aiagent.mapper.AiGenerationRecordMapper;
import com.houjiang.aiagent.mapper.AiModelMapper;
import com.houjiang.aiagent.mapper.AiPromptMapper;
import com.houjiang.aiagent.support.AiPageSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 概览统计与生成记录查询。对应后台「AI智能系统」仪表盘。
 */
@Service
@RequiredArgsConstructor
public class AiStatisticsService {

    private final AiModelMapper modelMapper;
    private final AiPromptMapper promptMapper;
    private final AiChatSessionMapper sessionMapper;
    private final AiGenerationRecordMapper recordMapper;

    /**
     * 字段名必须与前端 AiDashboard.vue 的 stats 对齐：
     * totalChats / totalGenerations / activeModels / totalModels / totalPrompts
     */
    public Map<String, Object> statistics() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalChats", sessionMapper.selectCount(new LambdaQueryWrapper<>()));
        data.put("totalGenerations", recordMapper.selectCount(new LambdaQueryWrapper<>()));
        data.put("activeModels", modelMapper.selectCount(
                new LambdaQueryWrapper<com.houjiang.aiagent.entity.AiModel>()
                        .eq(com.houjiang.aiagent.entity.AiModel::getStatus, 1)));
        data.put("totalModels", modelMapper.selectCount(new LambdaQueryWrapper<>()));
        data.put("totalPrompts", promptMapper.selectCount(new LambdaQueryWrapper<>()));
        return data;
    }

    public Map<String, Object> generations(Map<String, Object> params) {
        LambdaQueryWrapper<AiGenerationRecord> wrapper = new LambdaQueryWrapper<>();
        String scene = AiPageSupport.str(params, "scene");
        if (StringUtils.hasText(scene)) {
            wrapper.eq(AiGenerationRecord::getScene, scene);
        }
        Integer status = AiPageSupport.toInt(params, "status");
        if (status != null) {
            wrapper.eq(AiGenerationRecord::getStatus, status);
        }
        wrapper.orderByDesc(AiGenerationRecord::getId);
        Page<AiGenerationRecord> page = recordMapper.selectPage(AiPageSupport.page(params), wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (AiGenerationRecord record : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getId());
            // 前端按 type 区分图标与标题文案（image / chat / 其他）
            item.put("type", toType(record.getScene()));
            item.put("scene", record.getScene());
            item.put("prompt", record.getPrompt());
            item.put("result", record.getResult());
            item.put("status", record.getStatus());
            item.put("tokens", record.getTokens());
            item.put("model", record.getModelName());
            item.put("createTime", record.getCreateTime());
            records.add(item);
        }
        return AiPageSupport.pageResult(page, records);
    }

    private String toType(String scene) {
        if (scene == null) {
            return "text";
        }
        if (scene.contains("image")) {
            return "image";
        }
        if (scene.contains("chat")) {
            return "chat";
        }
        return "text";
    }
}
