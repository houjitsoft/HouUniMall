package com.houjiang.thirdparty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.thirdparty.config.FeishuClientManager;
import com.houjiang.thirdparty.dingtalk.client.DingtalkApiClient;
import com.houjiang.thirdparty.wecom.client.WecomApiClient;
import com.houjiang.thirdparty.entity.ThirdPartyConfigEntity;
import com.houjiang.thirdparty.mapper.ThirdPartyConfigMapper;
import com.houjiang.thirdparty.service.ThirdPartyConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 第三方集成配置Service实现类
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
@Service
public class ThirdPartyConfigServiceImpl extends ServiceImpl<ThirdPartyConfigMapper, ThirdPartyConfigEntity> 
    implements ThirdPartyConfigService {

    private static final String PLATFORM_WECHAT_WORK = "wechat-work";
    private static final long CONFIG_CACHE_TTL_MS = 60_000L;
    private static final List<String> BATCH_PLATFORMS = List.of(
            "dingtalk", "wechat-work", "feishu", "yuque", "wechat-login", "esign"
    );

    private final ConcurrentHashMap<String, ConfigCacheEntry> configCache = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired(required = false)
    private FeishuClientManager feishuClientManager;

    @Autowired
    private DingtalkApiClient dingtalkApiClient;

    @Autowired
    private WecomApiClient wecomApiClient;

    @Override
    public ThirdPartyConfigDTO getConfigByPlatform(String platform) {
        return getConfigByPlatform(platform, null);
    }

    @Override
    public ThirdPartyConfigDTO getConfigByPlatform(String platform, String orgId) {
        try {
            String resolvedOrgId = StringUtils.hasText(orgId) ? orgId.trim() : getCurrentOrgId();
            if (!StringUtils.hasText(resolvedOrgId)) {
                return createDefaultConfig(platform);
            }
            String cacheKey = platform + ":" + resolvedOrgId;
            ConfigCacheEntry cached = configCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                return (ThirdPartyConfigDTO) cached.value;
            }

            LambdaQueryWrapper<ThirdPartyConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ThirdPartyConfigEntity::getPlatform, platform)
                       .eq(ThirdPartyConfigEntity::getOrgId, resolvedOrgId)
                       .eq(ThirdPartyConfigEntity::getDeleted, 0)
                       .orderByDesc(ThirdPartyConfigEntity::getUpdateTime)
                       .last("LIMIT 1");

            ThirdPartyConfigEntity entity = this.getOne(queryWrapper, false);
            ThirdPartyConfigDTO dto = entity == null ? createDefaultConfig(platform) : entityToDto(platform, entity);
            configCache.put(cacheKey, new ConfigCacheEntry(dto));
            return dto;
        } catch (Exception e) {
            log.error("获取第三方配置失败 platform={} orgId={}", platform, orgId, e);
            return createDefaultConfig(platform);
        }
    }

    @Override
    public Map<String, ThirdPartyConfigDTO> getAllConfigs(String orgId) {
        String resolvedOrgId = StringUtils.hasText(orgId) ? orgId.trim() : getCurrentOrgId();
        if (!StringUtils.hasText(resolvedOrgId)) {
            Map<String, ThirdPartyConfigDTO> empty = new LinkedHashMap<>();
            for (String platform : BATCH_PLATFORMS) {
                empty.put(platform, createDefaultConfig(platform));
            }
            return empty;
        }
        String batchCacheKey = "__batch__:" + resolvedOrgId;
        ConfigCacheEntry batchCached = configCache.get(batchCacheKey);
        if (batchCached != null && !batchCached.isExpired()) {
            @SuppressWarnings("unchecked")
            Map<String, ThirdPartyConfigDTO> cachedMap = (Map<String, ThirdPartyConfigDTO>) batchCached.value;
            return cachedMap;
        }

        LambdaQueryWrapper<ThirdPartyConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ThirdPartyConfigEntity::getOrgId, resolvedOrgId)
                .eq(ThirdPartyConfigEntity::getDeleted, 0)
                .in(ThirdPartyConfigEntity::getPlatform, BATCH_PLATFORMS)
                .orderByDesc(ThirdPartyConfigEntity::getUpdateTime);

        Map<String, ThirdPartyConfigEntity> latestByPlatform = new HashMap<>();
        for (ThirdPartyConfigEntity entity : this.list(queryWrapper)) {
            latestByPlatform.putIfAbsent(entity.getPlatform(), entity);
        }

        Map<String, ThirdPartyConfigDTO> result = new LinkedHashMap<>();
        for (String platform : BATCH_PLATFORMS) {
            ThirdPartyConfigEntity entity = latestByPlatform.get(platform);
            ThirdPartyConfigDTO dto = entity == null ? createDefaultConfig(platform) : entityToDto(platform, entity);
            result.put(platform, dto);
            configCache.put(platform + ":" + resolvedOrgId, new ConfigCacheEntry(dto));
        }
        configCache.put(batchCacheKey, new ConfigCacheEntry(result));
        return result;
    }

    @Override
    public ThirdPartyConfigDTO.WechatWorkConfig resolveWechatWorkConfig(String orgId) {
        if (!StringUtils.hasText(orgId)) {
            return null;
        }
        return pickConfiguredWechatWork(orgId.trim());
    }

    /**
     * 读取登录渠道凭据：未登录态没有组织上下文，取该平台下「凭据填写完整」的最近一条记录。
     *
     * <p>为什么不能复用 {@link #getConfigByPlatform(String)}：后者依赖当前登录上下文解析 orgId，
     * 登录页请求没有登录态，会一律落到默认（未开通）配置，出现「设置页填了却提示未开通」。</p>
     */
    @Override
    public ThirdPartyConfigDTO getLoginChannelConfig(String platform) {
        if (!StringUtils.hasText(platform)) {
            return null;
        }
        String normalized = platform.trim().toLowerCase();
        if (!LOGIN_CHANNEL_PLATFORMS.contains(normalized)) {
            log.warn("非登录类平台不允许匿名读取凭据: {}", platform);
            return null;
        }
        try {
            LambdaQueryWrapper<ThirdPartyConfigEntity> qw = new LambdaQueryWrapper<>();
            qw.eq(ThirdPartyConfigEntity::getPlatform, normalized)
                    .eq(ThirdPartyConfigEntity::getDeleted, 0)
                    .orderByDesc(ThirdPartyConfigEntity::getUpdateTime);
            for (ThirdPartyConfigEntity entity : this.list(qw)) {
                ThirdPartyConfigDTO dto = entityToDto(normalized, entity);
                if (hasLoginCredential(dto)) {
                    return dto;
                }
            }
        } catch (Exception e) {
            log.error("读取登录渠道配置失败 platform={}", platform, e);
        }
        return null;
    }

    /**
     * 判断该平台配置是否真的可以用于登录。
     * 显式关闭（enabled=false）即视为未开通；凭据字段没填满的占位记录同样跳过。
     */
    private boolean hasLoginCredential(ThirdPartyConfigDTO dto) {
        if (dto == null || Boolean.FALSE.equals(dto.getEnabled())) {
            return false;
        }
        switch (dto.getPlatform() == null ? "" : dto.getPlatform()) {
            case "wechat-login":
                ThirdPartyConfigDTO.WechatLoginConfig wechat = dto.getWechatLogin();
                return wechat != null && isFilled(wechat.getAppId()) && isFilled(wechat.getAppSecret());
            case "qq-login":
                ThirdPartyConfigDTO.QqLoginConfig qq = dto.getQqLogin();
                return qq != null && isFilled(qq.getAppId()) && isFilled(qq.getAppSecret());
            case "wechat-work":
                ThirdPartyConfigDTO.WechatWorkConfig work = dto.getWechatWork();
                return work != null && isFilled(work.getCorpId()) && isFilled(work.getSecret());
            case "dingtalk":
                ThirdPartyConfigDTO.DingtalkConfig dingtalk = dto.getDingtalk();
                return dingtalk != null && isFilled(dingtalk.getAppKey()) && isFilled(dingtalk.getAppSecret());
            case "feishu":
                ThirdPartyConfigDTO.FeishuConfig feishu = dto.getFeishu();
                return feishu != null && isFilled(feishu.getAppId()) && isFilled(feishu.getAppSecret());
            default:
                return false;
        }
    }

    private boolean isFilled(String value) {
        return StringUtils.hasText(value);
    }

    private ThirdPartyConfigDTO.WechatWorkConfig pickConfiguredWechatWork(String org) {
        ThirdPartyConfigDTO dto = getConfigByPlatform(PLATFORM_WECHAT_WORK, org);
        ThirdPartyConfigDTO.WechatWorkConfig ww = dto != null ? dto.getWechatWork() : null;
        return isWechatWorkConfigured(ww) ? ww : null;
    }

    private ThirdPartyConfigDTO.WechatWorkConfig findAnyConfiguredWechatWork() {
        try {
            LambdaQueryWrapper<ThirdPartyConfigEntity> qw = new LambdaQueryWrapper<>();
            qw.eq(ThirdPartyConfigEntity::getPlatform, PLATFORM_WECHAT_WORK)
                    .eq(ThirdPartyConfigEntity::getDeleted, 0)
                    .orderByDesc(ThirdPartyConfigEntity::getUpdateTime);
            for (ThirdPartyConfigEntity entity : this.list(qw)) {
                ThirdPartyConfigDTO dto = entityToDto(PLATFORM_WECHAT_WORK, entity);
                ThirdPartyConfigDTO.WechatWorkConfig ww = dto.getWechatWork();
                if (isWechatWorkConfigured(ww)) {
                    return ww;
                }
            }
        } catch (Exception e) {
            log.error("扫描企业微信配置失败", e);
        }
        return null;
    }

    private boolean isWechatWorkConfigured(ThirdPartyConfigDTO.WechatWorkConfig ww) {
        if (ww == null || !StringUtils.hasText(ww.getCorpId())) {
            return false;
        }
        String secret = StringUtils.hasText(ww.getAddressBookSecret())
                ? ww.getAddressBookSecret()
                : ww.getSecret();
        return StringUtils.hasText(secret);
    }

    private void evictConfigCache(String orgId, String platform) {
        if (!StringUtils.hasText(orgId)) {
            return;
        }
        String resolvedOrgId = orgId.trim();
        if (StringUtils.hasText(platform)) {
            configCache.remove(platform + ":" + resolvedOrgId);
        } else {
            for (String p : BATCH_PLATFORMS) {
                configCache.remove(p + ":" + resolvedOrgId);
            }
        }
        configCache.remove("__batch__:" + resolvedOrgId);
    }

    private ThirdPartyConfigDTO entityToDto(String platform, ThirdPartyConfigEntity entity) {
            ThirdPartyConfigDTO dto = new ThirdPartyConfigDTO();
            dto.setPlatform(platform);
            dto.setEnabled(entity.getEnabled());
            dto.setDescription(entity.getDescription());

            if (StringUtils.hasText(entity.getConfigJson())) {
                try {
                    String configJson = entity.getConfigJson();
                    switch (platform) {
                        case "dingtalk":
                            ThirdPartyConfigDTO.DingtalkConfig dingtalkConfig = 
                                objectMapper.readValue(configJson, ThirdPartyConfigDTO.DingtalkConfig.class);
                            dto.setDingtalk(dingtalkConfig);
                            break;
                        case "wechat-work":
                            ThirdPartyConfigDTO.WechatWorkConfig wechatWorkConfig = 
                                objectMapper.readValue(configJson, ThirdPartyConfigDTO.WechatWorkConfig.class);
                            dto.setWechatWork(wechatWorkConfig);
                            break;
                        case "feishu":
                            ThirdPartyConfigDTO.FeishuConfig feishuConfig = 
                                objectMapper.readValue(configJson, ThirdPartyConfigDTO.FeishuConfig.class);
                            dto.setFeishu(feishuConfig);
                            break;
                        case "yuque":
                            ThirdPartyConfigDTO.YuqueConfig yuqueConfig = 
                                objectMapper.readValue(configJson, ThirdPartyConfigDTO.YuqueConfig.class);
                            dto.setYuque(yuqueConfig);
                            break;
                        case "esign":
                            ThirdPartyConfigDTO.EsignConfig esignConfig = 
                                objectMapper.readValue(configJson, ThirdPartyConfigDTO.EsignConfig.class);
                            dto.setEsign(esignConfig);
                            break;
                        case "wechat-login":
                            ThirdPartyConfigDTO.WechatLoginConfig wechatLoginConfig = 
                                objectMapper.readValue(configJson, ThirdPartyConfigDTO.WechatLoginConfig.class);
                            dto.setWechatLogin(wechatLoginConfig);
                            break;
                        case "qq-login":
                            ThirdPartyConfigDTO.QqLoginConfig qqLoginConfig =
                                objectMapper.readValue(configJson, ThirdPartyConfigDTO.QqLoginConfig.class);
                            dto.setQqLogin(qqLoginConfig);
                            break;
                    }
                } catch (JsonProcessingException e) {
                    log.error("解析第三方配置JSON失败: {}", e.getMessage());
                }
            }

            return dto;
    }

    @Override
    public boolean saveConfig(String platform, ThirdPartyConfigDTO config) {
        try {
            String orgId = getCurrentOrgId();
            
            // 查找现有配置
            LambdaQueryWrapper<ThirdPartyConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ThirdPartyConfigEntity::getPlatform, platform)
                       .eq(ThirdPartyConfigEntity::getOrgId, orgId)
                       .eq(ThirdPartyConfigEntity::getDeleted, 0);
            
            ThirdPartyConfigEntity existingEntity = this.getOne(queryWrapper);
            
            // 准备配置JSON
            String configJson = "";
            try {
                switch (platform) {
                    case "dingtalk":
                        configJson = objectMapper.writeValueAsString(config.getDingtalk());
                        break;
                    case "wechat-work":
                        if (existingEntity != null && config.getWechatWork() != null) {
                            mergeWechatWorkSecrets(config.getWechatWork(), existingEntity.getConfigJson());
                        }
                        configJson = objectMapper.writeValueAsString(config.getWechatWork());
                        break;
                    case "feishu":
                        configJson = objectMapper.writeValueAsString(config.getFeishu());
                        break;
                    case "yuque":
                        configJson = objectMapper.writeValueAsString(config.getYuque());
                        break;
                    case "esign":
                        configJson = objectMapper.writeValueAsString(config.getEsign());
                        break;
                    case "wechat-login":
                        configJson = objectMapper.writeValueAsString(config.getWechatLogin());
                        break;
                    case "qq-login":
                        configJson = objectMapper.writeValueAsString(config.getQqLogin());
                        break;
                }
            } catch (JsonProcessingException e) {
                log.error("序列化第三方配置JSON失败: {}", e.getMessage());
                return false;
            }

            boolean saveResult;
            if (existingEntity != null) {
                // 更新现有配置
                existingEntity.setEnabled(config.getEnabled());
                existingEntity.setConfigJson(configJson);
                existingEntity.setDescription(config.getDescription());
                existingEntity.setUpdateTime(LocalDateTime.now());
                existingEntity.setUpdateBy("system");
                saveResult = this.updateById(existingEntity);
            } else {
                // 创建新配置
                ThirdPartyConfigEntity newEntity = new ThirdPartyConfigEntity();
                newEntity.setOrgId(orgId);
                newEntity.setPlatform(platform);
                newEntity.setConfigName(getPlatformDisplayName(platform));
                newEntity.setEnabled(config.getEnabled());
                newEntity.setConfigJson(configJson);
                newEntity.setDescription(config.getDescription());
                newEntity.setCreateTime(LocalDateTime.now());
                newEntity.setUpdateTime(LocalDateTime.now());
                newEntity.setCreateBy("system");
                newEntity.setUpdateBy("system");
                newEntity.setDeleted(0);
                saveResult = this.save(newEntity);
            }
            
            // 如果是飞书配置且已启用，启动长连接客户端
            if (saveResult && "feishu".equals(platform) && config.getEnabled() && feishuClientManager != null) {
                try {
                    ThirdPartyConfigDTO.FeishuConfig feishuConfig = config.getFeishu();
                    if (feishuConfig != null && 
                        StringUtils.hasText(feishuConfig.getAppId()) && 
                        StringUtils.hasText(feishuConfig.getAppSecret())) {
                        boolean clientStarted = feishuClientManager.startClient(
                                feishuConfig.getAppId(), 
                                feishuConfig.getAppSecret());
                        if (clientStarted) {
                            log.info("飞书长连接客户端启动成功: appId={}", feishuConfig.getAppId());
                        } else {
                            log.warn("飞书长连接客户端启动失败: appId={}", feishuConfig.getAppId());
                        }
                    }
                } catch (Exception e) {
                    log.error("启动飞书长连接客户端异常: error={}", e.getMessage(), e);
                    // 不影响配置保存，只记录日志
                }
            } else if (saveResult && "feishu".equals(platform) && !config.getEnabled() && feishuClientManager != null) {
                // 如果飞书配置被禁用，停止长连接客户端
                try {
                    ThirdPartyConfigDTO.FeishuConfig feishuConfig = config.getFeishu();
                    if (feishuConfig != null && StringUtils.hasText(feishuConfig.getAppId())) {
                        feishuClientManager.stopClient(feishuConfig.getAppId());
                        log.info("飞书长连接客户端已停止: appId={}", feishuConfig.getAppId());
                    }
                } catch (Exception e) {
                    log.error("停止飞书长连接客户端异常: error={}", e.getMessage(), e);
                }
            }
            
            if (saveResult) {
                evictConfigCache(orgId, platform);
            }
            
            return saveResult;
        } catch (Exception e) {
            log.error("保存第三方配置失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean testConnection(String platform, ThirdPartyConfigDTO config) {
        try {
            // 这里应该调用具体的第三方平台API进行连接测试
            // 目前先返回模拟结果
            log.info("测试{}平台连接: {}", platform, config);
            
            // 模拟连接测试逻辑
            switch (platform) {
                case "dingtalk":
                    return testDingtalkConnection(config.getDingtalk());
                case "wechat-work":
                    return testWechatWorkConnection(config.getWechatWork());
                case "feishu":
                    return testFeishuConnection(config.getFeishu());
                case "yuque":
                    return testYuqueConnection(config.getYuque());
                case "wechat-login":
                    return testWechatLoginConnection(config.getWechatLogin());
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("测试第三方连接失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteConfig(String platform) {
        try {
            String orgId = getCurrentOrgId();
            
            LambdaQueryWrapper<ThirdPartyConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ThirdPartyConfigEntity::getPlatform, platform)
                       .eq(ThirdPartyConfigEntity::getOrgId, orgId)
                       .eq(ThirdPartyConfigEntity::getDeleted, 0);
            
            ThirdPartyConfigEntity entity = this.getOne(queryWrapper);
            if (entity != null) {
                entity.setDeleted(1);
                entity.setUpdateTime(LocalDateTime.now());
                entity.setUpdateBy("system");
                boolean ok = this.updateById(entity);
                if (ok) {
                    evictConfigCache(orgId, platform);
                }
                return ok;
            }
            return true;
        } catch (Exception e) {
            log.error("删除第三方配置失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getConfigJsonByPlatform(String platform) {
        return getConfigJsonByPlatform(platform, null);
    }

    @Override
    public String getConfigJsonByPlatform(String platform, String orgId) {
        String resolvedOrgId = StringUtils.hasText(orgId) ? orgId.trim() : getCurrentOrgId();
        LambdaQueryWrapper<ThirdPartyConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ThirdPartyConfigEntity::getPlatform, platform)
                .eq(ThirdPartyConfigEntity::getOrgId, resolvedOrgId)
                .eq(ThirdPartyConfigEntity::getDeleted, 0)
                .orderByDesc(ThirdPartyConfigEntity::getUpdateTime)
                .last("LIMIT 1");
        ThirdPartyConfigEntity entity = this.getOne(queryWrapper);
        return entity != null ? entity.getConfigJson() : null;
    }

    @Override
    public boolean saveJsonConfig(String platform, String configName, Boolean enabled, String description, String configJson) {
        try {
            String orgId = getCurrentOrgId();
            LambdaQueryWrapper<ThirdPartyConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ThirdPartyConfigEntity::getPlatform, platform)
                    .eq(ThirdPartyConfigEntity::getOrgId, orgId)
                    .eq(ThirdPartyConfigEntity::getDeleted, 0);
            ThirdPartyConfigEntity existing = this.getOne(queryWrapper);
            if (existing != null) {
                existing.setEnabled(enabled);
                existing.setConfigJson(configJson);
                existing.setDescription(description);
                existing.setUpdateTime(LocalDateTime.now());
                existing.setUpdateBy("system");
                return this.updateById(existing);
            }
            ThirdPartyConfigEntity entity = new ThirdPartyConfigEntity();
            entity.setOrgId(orgId);
            entity.setPlatform(platform);
            entity.setConfigName(configName != null ? configName : platform);
            entity.setEnabled(enabled);
            entity.setConfigJson(configJson);
            entity.setDescription(description);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            entity.setCreateBy("system");
            entity.setUpdateBy("system");
            entity.setDeleted(0);
            return this.save(entity);
        } catch (Exception e) {
            log.error("保存 JSON 配置失败: platform={}, error={}", platform, e.getMessage());
            return false;
        }
    }

    /**
     * 创建默认配置
     */
    private ThirdPartyConfigDTO createDefaultConfig(String platform) {
        ThirdPartyConfigDTO dto = new ThirdPartyConfigDTO();
        dto.setOrgId(getCurrentOrgId());
        dto.setPlatform(platform);
        dto.setEnabled(false);
        dto.setDescription("");

        switch (platform) {
            case "dingtalk":
                dto.setDingtalk(new ThirdPartyConfigDTO.DingtalkConfig());
                break;
            case "wechat-work":
                dto.setWechatWork(new ThirdPartyConfigDTO.WechatWorkConfig());
                break;
            case "feishu":
                dto.setFeishu(new ThirdPartyConfigDTO.FeishuConfig());
                break;
            case "yuque":
                dto.setYuque(new ThirdPartyConfigDTO.YuqueConfig());
                break;
            case "esign":
                dto.setEsign(new ThirdPartyConfigDTO.EsignConfig());
                break;
            case "wechat-login":
                dto.setWechatLogin(new ThirdPartyConfigDTO.WechatLoginConfig());
                break;
            case "qq-login":
                dto.setQqLogin(new ThirdPartyConfigDTO.QqLoginConfig());
                break;
        }

        return dto;
    }

    /**
     * 获取平台显示名称
     */
    private String getPlatformDisplayName(String platform) {
        switch (platform) {
            case "dingtalk":
                return "钉钉集成";
            case "wechat-work":
                return "企业微信集成";
            case "feishu":
                return "飞书集成";
            case "yuque":
                return "语雀集成";
            case "esign":
                return "电子签名集成";
            case "wechat-login":
                return "微信配置";
            case "qq-login":
                return "QQ 登录配置";
            default:
                return "第三方集成";
        }
    }

    /**
     * 保存时保留未重新填写的 Secret 字段（避免前端空密码框覆盖已有配置）
     */
    private void mergeWechatWorkSecrets(ThirdPartyConfigDTO.WechatWorkConfig incoming, String existingJson) {
        if (incoming == null || !StringUtils.hasText(existingJson)) {
            return;
        }
        try {
            ThirdPartyConfigDTO.WechatWorkConfig existing =
                    objectMapper.readValue(existingJson, ThirdPartyConfigDTO.WechatWorkConfig.class);
            if (existing == null) {
                return;
            }
            if (shouldKeepExistingSecret(incoming.getSecret())) {
                incoming.setSecret(existing.getSecret());
            }
            if (shouldKeepExistingSecret(incoming.getAddressBookSecret())) {
                incoming.setAddressBookSecret(existing.getAddressBookSecret());
            }
            if (shouldKeepExistingSecret(incoming.getContactSecret())) {
                incoming.setContactSecret(existing.getContactSecret());
            }
            if (shouldKeepExistingSecret(incoming.getCheckinSecret())) {
                incoming.setCheckinSecret(existing.getCheckinSecret());
            }
        } catch (JsonProcessingException e) {
            log.warn("合并企业微信 Secret 失败，将按提交内容保存: {}", e.getMessage());
        }
    }

    private boolean shouldKeepExistingSecret(String submitted) {
        if (!StringUtils.hasText(submitted)) {
            return true;
        }
        String trimmed = submitted.trim();
        return trimmed.contains("****") || "******".equals(trimmed);
    }

    /**
     * 测试钉钉连接
     */
    private boolean testDingtalkConnection(ThirdPartyConfigDTO.DingtalkConfig config) {
        if (config == null || !StringUtils.hasText(config.getAppKey()) || !StringUtils.hasText(config.getAppSecret())) {
            return false;
        }
        return dingtalkApiClient.testConnection(config.getAppKey(), config.getAppSecret());
    }

    /**
     * 测试企业微信连接
     */
    private boolean testWechatWorkConnection(ThirdPartyConfigDTO.WechatWorkConfig config) {
        if (config == null || !StringUtils.hasText(config.getCorpId()) || !StringUtils.hasText(config.getSecret())) {
            return false;
        }
        if (!wecomApiClient.testConnection(config.getCorpId(), config.getSecret())) {
            return false;
        }
        if (StringUtils.hasText(config.getContactSecret())) {
            return wecomApiClient.testConnection(config.getCorpId(), config.getContactSecret().trim());
        }
        return true;
    }

    /**
     * 测试飞书连接
     */
    private boolean testFeishuConnection(ThirdPartyConfigDTO.FeishuConfig config) {
        // 这里应该调用飞书API进行实际连接测试
        // 目前返回模拟结果
        return config != null && 
               StringUtils.hasText(config.getAppId()) && 
               StringUtils.hasText(config.getAppSecret());
    }

    /**
     * 测试语雀连接
     */
    private boolean testYuqueConnection(ThirdPartyConfigDTO.YuqueConfig config) {
        // 这里应该调用语雀API进行实际连接测试
        // 目前返回模拟结果
        return config != null && 
               StringUtils.hasText(config.getAppId()) && 
               StringUtils.hasText(config.getAppSecret()) &&
               StringUtils.hasText(config.getAccessToken());
    }

    /**
     * 测试微信登录连接
     */
    private boolean testWechatLoginConnection(ThirdPartyConfigDTO.WechatLoginConfig config) {
        // 这里应该调用微信开放平台API进行实际连接测试
        // 目前返回模拟结果
        return config != null && 
               StringUtils.hasText(config.getAppId()) && 
               StringUtils.hasText(config.getAppSecret());
    }

    /**
     * 获取请求头中的当前组织ID；缺少组织上下文时返回 null，由调用方 fail-closed。
     */
    private String getCurrentOrgId() {
        String fromHeader = readOrgIdHeader();
        if (StringUtils.hasText(fromHeader)) {
            return fromHeader.trim();
        }
        return null;
    }

    private String readOrgIdHeader() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String header = request.getHeader("X-Org-Id");
            return StringUtils.hasText(header) ? header.trim() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static final class ConfigCacheEntry {
        private final Object value;
        private final long expireAtMs;

        ConfigCacheEntry(Object value) {
            this.value = value;
            this.expireAtMs = System.currentTimeMillis() + CONFIG_CACHE_TTL_MS;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAtMs;
        }
    }
}
