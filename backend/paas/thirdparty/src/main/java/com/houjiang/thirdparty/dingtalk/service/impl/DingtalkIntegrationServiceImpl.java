package com.houjiang.thirdparty.dingtalk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.thirdparty.dingtalk.client.DingtalkApiClient;
import com.houjiang.thirdparty.dingtalk.dto.AttendancePlatformConfigDTO;
import com.houjiang.thirdparty.dingtalk.service.DingtalkIntegrationService;
import com.houjiang.thirdparty.service.ThirdPartyConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Amos
 */
@Slf4j
@Service
public class DingtalkIntegrationServiceImpl implements DingtalkIntegrationService {

    private static final String PLATFORM_DINGTALK = "dingtalk";
    private static final String PLATFORM_WECHAT_WORK = "wechat-work";
    private static final String PLATFORM_ATTENDANCE = "attendance-platform";

    /** 考勤平台配置缓存，60s TTL，避免每次请求触发4次串行DB查询 */
    private static final long CACHE_TTL_MS = 60_000L;
    private final ConcurrentHashMap<String, CacheEntry<AttendancePlatformConfigDTO>> attendancePlatformCache = new ConcurrentHashMap<>();

    @Autowired
    private ThirdPartyConfigService thirdPartyConfigService;

    @Autowired
    private DingtalkApiClient dingtalkApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ThirdPartyConfigDTO getConfig() {
        return thirdPartyConfigService.getConfigByPlatform(PLATFORM_DINGTALK);
    }

    @Override
    public boolean saveConfig(ThirdPartyConfigDTO config) {
        config.setPlatform(PLATFORM_DINGTALK);
        return thirdPartyConfigService.saveConfig(PLATFORM_DINGTALK, config);
    }

    @Override
    public boolean testConnection(ThirdPartyConfigDTO config) {
        ThirdPartyConfigDTO.DingtalkConfig dingtalk = resolveDingtalkConfig(config);
        if (dingtalk == null) {
            return false;
        }
        return dingtalkApiClient.testConnection(dingtalk.getAppKey(), dingtalk.getAppSecret());
    }

    @Override
    public Map<String, Object> healthCheck() {
        ThirdPartyConfigDTO config = getConfig();
        ThirdPartyConfigDTO.DingtalkConfig dingtalk = config != null ? config.getDingtalk() : null;
        if (dingtalk == null) {
            Map<String, Object> health = new HashMap<>();
            health.put("connected", false);
            health.put("enabled", false);
            health.put("message", "未配置钉钉");
            return health;
        }
        Map<String, Object> health = dingtalkApiClient.healthCheck(dingtalk.getAppKey(), dingtalk.getAppSecret());
        health.put("enabled", Boolean.TRUE.equals(config.getEnabled()));
        return health;
    }

    @Override
    public AttendancePlatformConfigDTO getAttendancePlatformConfig() {
        return getAttendancePlatformConfig(null);
    }

    @Override
    public AttendancePlatformConfigDTO getAttendancePlatformConfig(String orgId) {
        String cacheKey = StringUtils.hasText(orgId) ? orgId.trim() : "__default__";
        CacheEntry<AttendancePlatformConfigDTO> cached = attendancePlatformCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("考勤平台配置命中缓存: orgId={}", cacheKey);
            return cached.getValue();
        }

        AttendancePlatformConfigDTO dto = readAttendancePlatform(orgId);
        ThirdPartyConfigDTO dingtalkConfig = thirdPartyConfigService.getConfigByPlatform(PLATFORM_DINGTALK, orgId);
        ThirdPartyConfigDTO.DingtalkConfig dingtalk = dingtalkConfig != null ? dingtalkConfig.getDingtalk() : null;
        dto.setDingtalkConfigured(dingtalk != null
                && StringUtils.hasText(dingtalk.getAppKey())
                && StringUtils.hasText(dingtalk.getAppSecret())
                && Boolean.TRUE.equals(dingtalkConfig.getEnabled()));

        ThirdPartyConfigDTO wechatConfig = thirdPartyConfigService.getConfigByPlatform(PLATFORM_WECHAT_WORK, orgId);
        ThirdPartyConfigDTO.WechatWorkConfig wechat = wechatConfig != null ? wechatConfig.getWechatWork() : null;
        dto.setWechatWorkConfigured(wechat != null
                && StringUtils.hasText(wechat.getCorpId())
                && StringUtils.hasText(wechat.getSecret())
                && Boolean.TRUE.equals(wechatConfig.getEnabled()));

        ThirdPartyConfigDTO feishuConfig = thirdPartyConfigService.getConfigByPlatform("feishu", orgId);
        ThirdPartyConfigDTO.FeishuConfig feishu = feishuConfig != null ? feishuConfig.getFeishu() : null;
        dto.setFeishuConfigured(feishu != null
                && StringUtils.hasText(feishu.getAppId())
                && StringUtils.hasText(feishu.getAppSecret())
                && Boolean.TRUE.equals(feishuConfig.getEnabled()));

        attendancePlatformCache.put(cacheKey, new CacheEntry<>(dto, System.currentTimeMillis() + CACHE_TTL_MS));
        return dto;
    }

    @Override
    public boolean saveAttendancePlatformConfig(AttendancePlatformConfigDTO config) {
        if (config == null) {
            return false;
        }
        try {
            Map<String, Object> json = new HashMap<>();
            json.put("source", StringUtils.hasText(config.getSource()) ? config.getSource() : "platform");
            json.put("syncMode", StringUtils.hasText(config.getSyncMode()) ? config.getSyncMode() : "auto");
            json.put("enabled", Boolean.TRUE.equals(config.getEnabled()));
            String configJson = objectMapper.writeValueAsString(json);
            boolean result = thirdPartyConfigService.saveJsonConfig(
                    PLATFORM_ATTENDANCE,
                    "考勤数据来源",
                    config.getEnabled(),
                    "考勤数据来源: " + json.get("source"),
                    configJson
            );
            if (result) {
                attendancePlatformCache.clear();
            }
            return result;
        } catch (Exception e) {
            log.error("保存考勤平台配置失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private AttendancePlatformConfigDTO readAttendancePlatform(String orgId) {
        AttendancePlatformConfigDTO defaults = new AttendancePlatformConfigDTO();
        try {
            String configJson = thirdPartyConfigService.getConfigJsonByPlatform(PLATFORM_ATTENDANCE, orgId);
            if (!StringUtils.hasText(configJson)) {
                return defaults;
            }
            Map<String, Object> map = objectMapper.readValue(configJson, new TypeReference<>() {});
            if (map.get("source") != null) {
                defaults.setSource(map.get("source").toString());
            }
            if (map.get("syncMode") != null) {
                defaults.setSyncMode(map.get("syncMode").toString());
            }
            if (map.get("enabled") != null) {
                defaults.setEnabled(parseJsonBoolean(map.get("enabled")));
            }
        } catch (Exception e) {
            log.warn("读取考勤平台配置失败，使用默认值: {}", e.getMessage());
        }
        return defaults;
    }

    private static boolean parseJsonBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        String s = value.toString().trim();
        return "true".equalsIgnoreCase(s) || "1".equals(s);
    }

    private ThirdPartyConfigDTO.DingtalkConfig resolveDingtalkConfig(ThirdPartyConfigDTO config) {
        if (config != null && config.getDingtalk() != null) {
            return config.getDingtalk();
        }
        ThirdPartyConfigDTO saved = getConfig();
        return saved != null ? saved.getDingtalk() : null;
    }

    /**
     * 带过期时间的缓存条目
     *
     * @param <T> 缓存值类型
     */
    private static class CacheEntry<T> {
        private final T value;
        private final long expireAt;

        CacheEntry(T value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        T getValue() {
            return value;
        }

        boolean isExpired() {
            return System.currentTimeMillis() >= expireAt;
        }
    }
}
