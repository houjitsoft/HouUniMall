package com.houjiang.commons.support;

import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.cache.CacheSettingsDTO;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 从 commons / Nacos redis.yaml 读取缓存运行时参数。
 * @author Amos
 */
public final class CacheSettingsSupport {

    public static final String SYS_CONFIG_KEY = "cache_settings";
    public static final String SYS_CONFIG_TYPE = "cache";

    private CacheSettingsSupport() {
    }

    public static CacheSettingsDTO defaultSettings() {
        CacheSettingsDTO dto = new CacheSettingsDTO();
        dto.setRedisExpire(HouConfig.TOKEN_OUTTIME);
        dto.setRedisMaxMemory(0);
        dto.setRedisPoolSize(10);
        dto.setAppExpire(1800);
        dto.setAppMaxSize(256);
        dto.setStaticExpire(86400);
        dto.setStaticCompression(true);
        dto.setPreloadEnabled(true);
        dto.setCompressionEnabled(true);
        dto.setHitRateThreshold(80);
        dto.setCleanupStrategy("lru");
        return dto;
    }

    public static void applyRuntime(Environment environment, CacheSettingsDTO dto) {
        if (dto == null) {
            return;
        }
        dto.setRuntimeTokenExpire(HouConfig.TOKEN_OUTTIME);
        if (dto.getRedisExpire() == null || dto.getRedisExpire() <= 0) {
            dto.setRedisExpire(HouConfig.TOKEN_OUTTIME);
        }
        if (environment == null) {
            return;
        }
        dto.setRedisHost(environment.getProperty("spring.redis.host"));
        Integer port = environment.getProperty("spring.redis.port", Integer.class);
        dto.setRedisPort(port);
        dto.setRedisDatabase(environment.getProperty("spring.redis.database", Integer.class));

        Duration timeout = environment.getProperty("spring.redis.timeout", Duration.class, Duration.ofMillis(3000));
        dto.setRedisCommandTimeoutMs((int) timeout.toMillis());

        Integer poolSize = environment.getProperty("spring.redis.lettuce.pool.max-active", Integer.class);
        if (poolSize == null) {
            poolSize = environment.getProperty("spring.redis.jedis.pool.max-active", Integer.class);
        }
        if (poolSize != null && poolSize > 0) {
            dto.setRedisPoolSize(poolSize);
        }
    }

    public static Map<String, Object> runtimeSummary(Environment environment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tokenExpireSeconds", HouConfig.TOKEN_OUTTIME);
        map.put("imTokenExpireSeconds", HouConfig.IM_TOKEN_OUTTIME);
        if (environment != null) {
            map.put("redisHost", environment.getProperty("spring.redis.host"));
            map.put("redisPort", environment.getProperty("spring.redis.port", Integer.class));
            map.put("redisDatabase", environment.getProperty("spring.redis.database", Integer.class));
            Duration timeout = environment.getProperty("spring.redis.timeout", Duration.class);
            if (timeout != null) {
                map.put("redisCommandTimeoutMs", timeout.toMillis());
            }
            Integer pool = environment.getProperty("spring.redis.lettuce.pool.max-active", Integer.class);
            if (pool == null) {
                pool = environment.getProperty("spring.redis.jedis.pool.max-active", Integer.class);
            }
            map.put("redisPoolMaxActive", pool);
        }
        return map;
    }

    public static boolean isBlankJson(String json) {
        return !StringUtils.hasText(json) || "{}".equals(json.trim());
    }
}
