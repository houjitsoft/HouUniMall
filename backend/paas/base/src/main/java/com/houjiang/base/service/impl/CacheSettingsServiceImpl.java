package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.houjiang.base.entity.BaseSysConfigsEntity;
import com.houjiang.base.mapper.BaseSysConfigsMapper;
import com.houjiang.base.service.CacheSettingsService;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.dto.cache.CacheSettingsDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.support.CacheSettingsSupport;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Properties;

/**
 * @author Amos
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CacheSettingsServiceImpl implements CacheSettingsService {

    private final BaseSysConfigsMapper baseSysConfigsMapper;
    private final Environment environment;
    private final RedisCache redisCache;
    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public ResponseResult getSettings() {
        CacheSettingsDTO dto = loadPersistedSettings();
        CacheSettingsSupport.applyRuntime(environment, dto);
        fillRedisMemory(dto);
        dto.setRedisConnected(testRedisPing());
        return ResponseResult.success(dto);
    }

    @Override
    public ResponseResult saveSettings(CacheSettingsDTO settings) {
        if (settings == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "参数不能为空");
        }
        normalize(settings);
        BaseSysConfigsEntity entity = baseSysConfigsMapper.selectById(CacheSettingsSupport.SYS_CONFIG_KEY);
        boolean isNew = entity == null;
        if (isNew) {
            entity = new BaseSysConfigsEntity();
            entity.setConfigKey(CacheSettingsSupport.SYS_CONFIG_KEY);
            entity.setConfigType(CacheSettingsSupport.SYS_CONFIG_TYPE);
            entity.setConfigComment("系统缓存设置");
            entity.setConfigSort(1);
            entity.setConfigEnable(1);
            entity.setTerminalType(1);
            entity.setOpType("");
            entity.setOpValue("");
            entity.setCreateDate(new Date());
        }
        entity.setConfigValue(JSON.toJSONString(settings));
        entity.setUpdateDate(new Date());
        if (isNew) {
            baseSysConfigsMapper.insert(entity);
        } else {
            baseSysConfigsMapper.updateById(entity);
        }
        CacheSettingsDTO merged = loadPersistedSettings();
        CacheSettingsSupport.applyRuntime(environment, merged);
        fillRedisMemory(merged);
        merged.setRedisConnected(testRedisPing());
        return ResponseResult.success(merged);
    }

    @Override
    public ResponseResult testConnection() {
        boolean ok = testRedisPing();
        if (ok) {
            return ResponseResult.success("Redis 连接正常");
        }
        return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED.getCode(), "Redis 连接失败，请检查 Nacos redis.yaml 配置");
    }

    private CacheSettingsDTO loadPersistedSettings() {
        BaseSysConfigsEntity entity = baseSysConfigsMapper.selectById(CacheSettingsSupport.SYS_CONFIG_KEY);
        if (entity != null && StringUtils.hasText(entity.getConfigValue())) {
            try {
                CacheSettingsDTO parsed = JSON.parseObject(entity.getConfigValue(), CacheSettingsDTO.class);
                if (parsed != null) {
                    normalize(parsed);
                    return parsed;
                }
            } catch (Exception e) {
                log.warn("解析 cache_settings 失败，使用默认值: {}", e.getMessage());
            }
        }
        return CacheSettingsSupport.defaultSettings();
    }

    private void normalize(CacheSettingsDTO dto) {
        if (dto.getCleanupStrategy() == null || dto.getCleanupStrategy().isBlank()) {
            dto.setCleanupStrategy("lru");
        }
        if (dto.getStaticCompression() == null) {
            dto.setStaticCompression(true);
        }
        if (dto.getPreloadEnabled() == null) {
            dto.setPreloadEnabled(true);
        }
        if (dto.getCompressionEnabled() == null) {
            dto.setCompressionEnabled(true);
        }
    }

    private void fillRedisMemory(CacheSettingsDTO dto) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            Properties info = connection.serverCommands().info("memory");
            if (info == null) {
                return;
            }
            long used = parseLong(info.getProperty("used_memory"));
            long max = parseLong(info.getProperty("maxmemory"));
            if (used > 0) {
                dto.setRedisMemoryUsedMb((int) Math.max(1, used / 1024 / 1024));
            }
            if (max > 0) {
                dto.setRedisMaxMemory((int) Math.max(1, max / 1024 / 1024));
            }
        } catch (Exception e) {
            log.warn("读取 Redis 内存信息失败: {}", e.getMessage());
        }
    }

    private long parseLong(String raw) {
        if (!StringUtils.hasText(raw)) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private boolean testRedisPing() {
        try {
            redisCache.getCacheObject("__cache_settings_ping__");
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                String pong = connection.ping();
                return pong != null && pong.equalsIgnoreCase("PONG");
            }
        } catch (Exception e) {
            log.warn("Redis 连接测试失败: {}", e.getMessage());
            return false;
        }
    }
}
