package com.houjiang.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.aiagent.entity.AiConfig;
import com.houjiang.aiagent.mapper.AiConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 配置项。
 *
 * <p>前端「AI设置」按扁平 map 读写（{@code cfg.default_model}），
 * 因此 {@code /ai/config/all} 必须返回 {@code {key: value}}，不能包一层 list。
 */
@Service
@RequiredArgsConstructor
public class AiConfigService {

    private final AiConfigMapper configMapper;

    /** 全量配置，扁平 map */
    public Map<String, String> all() {
        Map<String, String> result = new LinkedHashMap<>();
        for (AiConfig config : configMapper.selectList(new LambdaQueryWrapper<>())) {
            result.put(config.getConfigKey(), config.getConfigValue() == null ? "" : config.getConfigValue());
        }
        return result;
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiConfig config : configMapper.selectList(new LambdaQueryWrapper<>())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", config.getId());
            item.put("configKey", config.getConfigKey());
            item.put("configValue", config.getConfigValue());
            item.put("configGroup", config.getConfigGroup());
            item.put("remark", config.getRemark());
            result.add(item);
        }
        return result;
    }

    /**
     * 批量保存。入参形态：{@code {configs: {k: v, …}}}，也兼容直接传扁平 map。
     */
    public int save(Map<String, Object> data) {
        Map<String, Object> configs = extractConfigs(data);
        if (configs == null || configs.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            upsert(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()), null);
            count++;
        }
        return count;
    }

    public int batch(Map<String, Object> data) {
        return save(data);
    }

    private void upsert(String key, String value, String group) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        AiConfig existing = configMapper.selectOne(
                new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getConfigKey, key).last("limit 1"));
        if (existing != null) {
            existing.setConfigValue(value);
            if (StringUtils.hasText(group)) {
                existing.setConfigGroup(group);
            }
            existing.setUpdateTime(LocalDateTime.now());
            configMapper.updateById(existing);
            return;
        }
        AiConfig config = new AiConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigGroup(StringUtils.hasText(group) ? group : "global");
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        configMapper.insert(config);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractConfigs(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return Map.of();
        }
        Object nested = data.get("configs");
        if (nested instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        Object list = data.get("list");
        if (list instanceof List<?> items) {
            Map<String, Object> flat = new LinkedHashMap<>();
            for (Object item : items) {
                if (item instanceof Map<?, ?> m) {
                    Object key = m.get("configKey");
                    if (key == null) {
                        key = m.get("key");
                    }
                    Object value = m.get("configValue");
                    if (value == null) {
                        value = m.get("value");
                    }
                    if (key != null) {
                        flat.put(String.valueOf(key), value);
                    }
                }
            }
            return flat;
        }
        return data;
    }
}
