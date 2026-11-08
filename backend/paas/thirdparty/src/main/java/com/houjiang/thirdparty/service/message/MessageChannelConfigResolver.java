package com.houjiang.thirdparty.service.message;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.dto.goods.NavSortLogoDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.feign.BaseServiceClient;
import com.houjiang.thirdparty.service.ThirdPartyConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息渠道配置解析器，在 hou_message_channel 未配置时回退到系统已有配置。
 * @author Amos
 */
@Slf4j
@Service
public class MessageChannelConfigResolver {

    private static final String WECOM_CHANNEL = "WECOM";
    private static final String EMAIL_CHANNEL = "EMAIL";

    @Autowired
    private MessageChannelService channelService;

    @Autowired
    private ThirdPartyConfigService thirdPartyConfigService;

    @Autowired(required = false)
    private BaseServiceClient baseServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析渠道有效配置（含回退逻辑）。
     *
     * @param channelCode 渠道编码
     * @param orgId       组织 ID，企微配置回退时使用
     * @return 渠道配置 Map，无法解析时返回空 Map
     */
    public Map<String, Object> resolve(String channelCode, String orgId) {
        Map<String, Object> dbConfig = parseConfig(getDbConfigJson(channelCode));
        if (EMAIL_CHANNEL.equalsIgnoreCase(channelCode)) {
            return resolveEmailConfig(dbConfig);
        }
        if (!dbConfig.isEmpty()) {
            return dbConfig;
        }
        if (WECOM_CHANNEL.equalsIgnoreCase(channelCode)) {
            return buildWecomFallbackConfig(orgId);
        }
        return new HashMap<>();
    }

    /**
     * 邮件渠道：库里若只有空壳配置（无账号/密码），回退到系统设置 setting_email。
     */
    private Map<String, Object> resolveEmailConfig(Map<String, Object> dbConfig) {
        if (hasEmailCredentials(dbConfig)) {
            return dbConfig;
        }
        Map<String, Object> fallback = buildEmailFallbackConfig();
        if (!fallback.isEmpty()) {
            return fallback;
        }
        return dbConfig == null ? new HashMap<>() : dbConfig;
    }

    private static boolean hasEmailCredentials(Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return false;
        }
        Object username = config.get("username");
        Object password = config.get("password");
        return StringUtils.hasText(username == null ? null : username.toString())
                && StringUtils.hasText(password == null ? null : password.toString());
    }

    /**
     * 判断渠道是否可发送（数据库启用或存在回退配置）。
     *
     * @param channelCode 渠道编码
     * @param orgId       组织 ID
     * @return 是否可发送
     */
    public boolean isChannelAvailable(String channelCode, String orgId) {
        if (EMAIL_CHANNEL.equalsIgnoreCase(channelCode)) {
            return hasEmailCredentials(resolve(channelCode, orgId));
        }
        var channel = channelService.getChannelByCode(channelCode);
        if (channel != null && channel.getStatus() != null && channel.getStatus() == 1
                && StringUtils.hasText(channel.getConfig())) {
            return true;
        }
        return !resolve(channelCode, orgId).isEmpty();
    }

    private String getDbConfigJson(String channelCode) {
        var channel = channelService.getChannelByCode(channelCode);
        return channel != null ? channel.getConfig() : null;
    }

    private Map<String, Object> buildWecomFallbackConfig(String orgId) {
        ThirdPartyConfigDTO.WechatWorkConfig ww = thirdPartyConfigService.resolveWechatWorkConfig(orgId);
        if (ww == null || !StringUtils.hasText(ww.getCorpId()) || !StringUtils.hasText(ww.getSecret())) {
            return new HashMap<>();
        }
        Map<String, Object> config = new HashMap<>();
        config.put("type", "app");
        config.put("corpId", ww.getCorpId());
        config.put("corpSecret", ww.getSecret());
        if (StringUtils.hasText(ww.getAgentId())) {
            config.put("agentId", ww.getAgentId());
        }
        return config;
    }

    private Map<String, Object> buildEmailFallbackConfig() {
        if (baseServiceClient == null) {
            return new HashMap<>();
        }
        try {
            NavSortLogoDTO query = new NavSortLogoDTO();
            query.setConfigType(CommonType.configKey.SETTING.getDesc());
            ResponseResult resp = baseServiceClient.quertNavLogo(query);
            if (resp == null || resp.getData() == null) {
                return new HashMap<>();
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) resp.getData();
            String username = null;
            String password = null;
            for (Map<String, Object> item : list) {
                Object key = item.get("configKey");
                Object value = item.get("configValue");
                if (key == null || value == null) {
                    continue;
                }
                if (CommonType.configKey.SETTING_EMAIL.getDesc().equals(String.valueOf(key))) {
                    username = value.toString();
                }
                if (CommonType.configKey.SETTING_EMAIL_CODE.getDesc().equals(String.valueOf(key))) {
                    password = value.toString();
                }
            }
            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                return new HashMap<>();
            }
            Map<String, Object> config = new HashMap<>();
            config.put("host", "smtp.exmail.qq.com");
            config.put("port", "465");
            config.put("username", username);
            config.put("password", password);
            config.put("from", username);
            config.put("ssl", "true");
            return config;
        } catch (Exception e) {
            log.warn("读取邮件回退配置失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<String, Object> parseConfig(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析渠道配置失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
