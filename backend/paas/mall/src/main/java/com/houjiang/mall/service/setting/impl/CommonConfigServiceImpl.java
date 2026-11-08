package com.houjiang.mall.service.setting.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.mall.entity.setting.SettingConfig;
import com.houjiang.mall.mapper.setting.SettingConfigMapper;
import com.houjiang.mall.service.setting.CommonConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 通用键值配置 Service 实现。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonConfigServiceImpl implements CommonConfigService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /** hou_merchant_config.value 历史字段为 varchar(255)，预留少量字符避免不同字符集下截断。 */
    static final int VALUE_CHUNK_SIZE = 240;
    static final String CHUNK_MARKER_PREFIX = "__HC_CHUNKS_V1__:";
    static final String CHUNK_NAME_SEPARATOR = "__hc_chunk_";

    private final SettingConfigMapper configMapper;

    @Override
    public String get(String type, String name) {
        SettingConfig cfg = selectLatest(type, name);
        if (cfg == null) {
            return null;
        }
        Integer chunkCount = parseChunkCount(cfg.getValue());
        if (chunkCount == null) {
            return cfg.getValue();
        }
        String prefix = name + CHUNK_NAME_SEPARATOR;
        Map<String, String> chunkValues = new LinkedHashMap<>();
        configMapper.selectList(new QueryWrapper<SettingConfig>()
                        .eq("type", type)
                        .likeRight("name", prefix)
                        .orderByAsc("name")
                        .orderByDesc("id"))
                .stream()
                .filter(item -> item.getName() != null && item.getName().startsWith(prefix))
                .forEach(item -> chunkValues.putIfAbsent(item.getName(), item.getValue()));
        StringBuilder value = new StringBuilder(chunkCount * VALUE_CHUNK_SIZE);
        for (int index = 0; index < chunkCount; index++) {
            String chunk = chunkValues.get(chunkName(name, index));
            if (chunk == null) {
                log.error("[CommonConfig] missing value chunk type={} name={} index={}/{}",
                        type, name, index + 1, chunkCount);
                return null;
            }
            value.append(chunk);
        }
        return value.toString();
    }

    @Override
    public String getOrDefault(String type, String name, String defaults) {
        String v = get(type, name);
        return (v == null || v.isEmpty()) ? defaults : v;
    }

    @Override
    public Map<String, String> getAll(String type) {
        List<SettingConfig> list = configMapper.selectList(new QueryWrapper<SettingConfig>()
                .eq("type", type)
                .orderByAsc("id"));
        Map<String, String> map = new LinkedHashMap<>();
        for (SettingConfig cfg : list) {
            if (cfg.getName() != null && cfg.getName().contains(CHUNK_NAME_SEPARATOR)) {
                continue;
            }
            map.put(cfg.getName(), get(type, cfg.getName()));
        }
        return map;
    }

    @Override
    public Map<String, String> getJsonMap(String type, String name) {
        String raw = get(type, name);
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        // 兼容 like-admin 历史脏数据 "[]" → "{}"
        String normalized = "[]".equals(raw.trim()) ? "{}" : raw;
        try {
            return OBJECT_MAPPER.readValue(normalized,
                    new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("[CommonConfig] parse json fail type={} name={} raw={}", type, name, raw);
            return Collections.emptyMap();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void set(String type, String name, String value) {
        String normalizedValue = value == null ? "" : value;
        List<String> chunks = splitValue(normalizedValue);
        String storedValue = chunks.size() > 1
                ? CHUNK_MARKER_PREFIX + chunks.size()
                : normalizedValue;

        deleteChunks(type, name);

        long now = System.currentTimeMillis() / 1000L;
        if (chunks.size() > 1) {
            for (int index = 0; index < chunks.size(); index++) {
                SettingConfig chunk = new SettingConfig();
                chunk.setType(type);
                chunk.setName(chunkName(name, index));
                chunk.setValue(chunks.get(index));
                chunk.setCreateTime(now);
                chunk.setUpdateTime(now);
                configMapper.insert(chunk);
            }
        }

        SettingConfig exist = selectLatest(type, name);
        if (exist != null) {
            exist.setValue(storedValue);
            exist.setUpdateTime(now);
            configMapper.updateById(exist);
        } else {
            SettingConfig cfg = new SettingConfig();
            cfg.setType(type);
            cfg.setName(name);
            cfg.setValue(storedValue);
            cfg.setCreateTime(now);
            cfg.setUpdateTime(now);
            configMapper.insert(cfg);
        }
    }

    private SettingConfig selectLatest(String type, String name) {
        return configMapper.selectOne(new QueryWrapper<SettingConfig>()
                .eq("type", type)
                .eq("name", name)
                .orderByDesc("id")
                .last("limit 1"));
    }

    private void deleteChunks(String type, String name) {
        String prefix = name + CHUNK_NAME_SEPARATOR;
        List<Integer> chunkIds = configMapper.selectList(new QueryWrapper<SettingConfig>()
                        .eq("type", type)
                        .likeRight("name", prefix))
                .stream()
                .filter(item -> item.getName() != null && item.getName().startsWith(prefix))
                .map(SettingConfig::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (!chunkIds.isEmpty()) {
            configMapper.deleteByIds(chunkIds);
        }
    }

    static List<String> splitValue(String value) {
        String normalized = value == null ? "" : value;
        if (normalized.length() <= VALUE_CHUNK_SIZE) {
            return List.of(normalized);
        }
        List<String> chunks = new ArrayList<>((normalized.length() + VALUE_CHUNK_SIZE - 1) / VALUE_CHUNK_SIZE);
        for (int start = 0; start < normalized.length(); start += VALUE_CHUNK_SIZE) {
            chunks.add(normalized.substring(start, Math.min(start + VALUE_CHUNK_SIZE, normalized.length())));
        }
        return chunks;
    }

    static Integer parseChunkCount(String value) {
        if (value == null || !value.startsWith(CHUNK_MARKER_PREFIX)) {
            return null;
        }
        try {
            int count = Integer.parseInt(value.substring(CHUNK_MARKER_PREFIX.length()));
            return count > 1 ? count : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static String chunkName(String name, int index) {
        return name + CHUNK_NAME_SEPARATOR + String.format("%04d", index);
    }
}
