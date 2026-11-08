package com.houjiang.base.controller;

import com.houjiang.base.dto.BaseSmsConfigDTO;
import com.houjiang.base.entity.SmsInfoEntity;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.base.security.BaseOrganizationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName SmsInfoController
 * @Description SMS配置信息
 * @Author Amos
 * @Date 2023/5/19 18:04
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("base/sms")
public class SmsInfoController extends BaseController {
    
    private static final Logger LOG = LoggerFactory.getLogger(SmsInfoController.class);
    
    @org.springframework.beans.factory.annotation.Autowired
    private com.houjiang.commons.config.RedisCache redisCache;

    @org.springframework.beans.factory.annotation.Autowired
    private BaseOrganizationResolver organizationResolver;

    /** 短信配置列表缓存key */
    private static final String SMS_INFO_LIST_CACHE_KEY = "base:sms:selectSMSInfoList";
    /** 短信设置缓存key */
    private static final String SMS_SETTING_CACHE_KEY = "base:sms:getSmsSetting";
    /** 启用中的短信通道类型缓存key */
    private static final String SMS_ACTIVE_TYPE_CACHE_KEY = "base:sms:querySmsStates";
    /** Redis缓存TTL（秒） */
    private static final int SMS_CACHE_TTL_SECONDS = 180;
    /** 本地缓存TTL（毫秒） */
    private static final long SMS_LOCAL_CACHE_TTL_MS = 30_000L;
    /** 本地缓存容器 */
    private final ConcurrentHashMap<String, LocalCacheEntry<Object>> localCache = new ConcurrentHashMap<>();


    /**
     * 初始化SMS配置信息 加密返回
     *
     * @param
     * @return
     */
    @PostMapping(value = "/goods/oss/selectSMSInfoEncryption")
    public ResponseResult selectSMSInfoEncryption()  {
        ResponseResult result = null;
        try {
            result = selectSMSInfo.selectSMSInfoEncryption();

        } catch (Exception e) {
            return ResponseResult.error(e.getMessage());
        }

        return result;
    }
    /**
     * update sms配置 存在就更新不存在就添加
     *
     * @param baseSmsConfigReq
     * @return
     */
    @PostMapping("updateSmsConfig")
    public ResponseResult updateSmsConfig(@RequestBody BaseSmsConfigDTO baseSmsConfigReq) {
        ResponseResult result = selectSMSInfo.updateSmsConfig(baseSmsConfigReq);
        if (result != null && result.getCode() == 1) {
            clearSmsCache();
        }
        return result;
    }

    /**
     * 更新短信配置 - 支持Map参数（用于BFF调用）
     * @param configMap
     * @return
     */
    @PostMapping("/updateSmsConfigMap")
    public ResponseResult updateSmsConfigFromMap(@RequestBody Map<String, Object> configMap) {
        try {
            LOG.info("=== 接收到短信配置Map参数: {}", configMap);
            
            // 将Map转换为BaseSmsConfigDTO
            BaseSmsConfigDTO dto = new BaseSmsConfigDTO();
            
            // 映射基本字段
            if (configMap.get("id") != null) {
                dto.setId(Long.valueOf(configMap.get("id").toString()));
            }
            if (configMap.get("smsSecretId") != null) {
                dto.setSmsSecretId(configMap.get("smsSecretId").toString());
            }
            if (configMap.get("smsSecretKey") != null) {
                dto.setSmsSecretKey(configMap.get("smsSecretKey").toString());
            }
            if (configMap.get("smsSdkAppId") != null) {
                dto.setSmsSdkAppId(configMap.get("smsSdkAppId").toString());
            }
            if (configMap.get("smsSign") != null) {
                dto.setSmsSign(configMap.get("smsSign").toString());
            }
            if (configMap.get("smsStatus") != null) {
                dto.setSmsStatus(configMap.get("smsStatus").toString());
            }
            if (configMap.get("smsType") != null) {
                dto.setSmsType(configMap.get("smsType").toString());
            }
            if (configMap.get("smsRegion") != null) {
                dto.setSmsRegion(configMap.get("smsRegion").toString());
            }
            if (configMap.get("smsIsactive") != null) {
                dto.setSmsIsactive(configMap.get("smsIsactive").toString());
            }
            if (configMap.get("smsRemark") != null) {
                dto.setSmsRemark(configMap.get("smsRemark").toString());
            }
            if (configMap.get("smsGeneral") != null) {
                dto.setSmsGeneral(configMap.get("smsGeneral").toString());
            }
            
            LOG.info("=== 转换后的DTO: {}", dto);
            ResponseResult result = selectSMSInfo.updateSmsConfig(dto);
            if (result != null && result.getCode() == 1) {
                clearSmsCache();
            }
            return result;
            
        } catch (Exception e) {
            LOG.error("=== 更新短信配置异常: {}", e.getMessage(), e);
            return ResponseResult.error("更新短信配置失败: " + e.getMessage());
        }
    }

    /**
     * 查询开启的sms配置
     * @return 开启的sms配置
     */
    @PostMapping("/querySmsStates")
    public ResponseResult<String> querySmsStates() {
        try {
            String orgId = organizationResolver.resolveCurrentOrgId();
            if (orgId == null) return ResponseResult.error("当前组织未确定或无权访问短信配置");
            String cacheKey = SMS_ACTIVE_TYPE_CACHE_KEY + ":" + orgId;
            Object localCached = getFromLocalCache(cacheKey);
            if (localCached instanceof String && !((String) localCached).isBlank()) {
                return ResponseResult.success((String) localCached);
            }
            Object redisCached = getFromRedisCache(cacheKey);
            if (redisCached instanceof String && !((String) redisCached).isBlank()) {
                putLocalCache(cacheKey, redisCached);
                return ResponseResult.success((String) redisCached);
            }

            String result = selectSMSInfo.querySmsStates();
            if (result == null) {
                return ResponseResult.error("未找到启用的短信配置");
            }
            putLocalCache(cacheKey, result);
            putRedisCache(cacheKey, result);
            return ResponseResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("查询短信配置失败: " + e.getMessage());
        }
    }

    /**
     * 根据短信类型查询sms配置
     * @return 开启的sms配置
     */
    @GetMapping("/selectSMSInfoByType")
    public ResponseResult<Map<String, Object>> selectSMSInfoByType(@RequestParam("type") String type){
        return selectSMSInfo.selectSMSInfoByType(type);
    }

    /**
     * 查询短信配置列表
     * @return
     */
    @PostMapping("/selectSMSInfoList")
    public ResponseResult selectSMSInfoList() {
        try {
            LOG.info("=== 查询短信配置列表");
            String orgId = organizationResolver.resolveCurrentOrgId();
            if (orgId == null) return ResponseResult.error("当前组织未确定或无权访问短信配置");
            String cacheKey = SMS_INFO_LIST_CACHE_KEY + ":" + orgId;
            // 1) 本地缓存命中
            Object localCached = getFromLocalCache(cacheKey);
            if (localCached instanceof List<?>) {
                LOG.debug("=== 命中本地缓存: {}", SMS_INFO_LIST_CACHE_KEY);
                return ResponseResult.success(localCached);
            }

            // 2) Redis缓存命中
            Object redisCached = getFromRedisCache(cacheKey);
            if (redisCached instanceof List<?>) {
                putLocalCache(cacheKey, redisCached);
                LOG.debug("=== 命中Redis缓存: {}", SMS_INFO_LIST_CACHE_KEY);
                return ResponseResult.success(redisCached);
            }

            // 3) 回源数据库（只查询未删除记录）
            List<SmsInfoEntity> list = selectSMSInfo.list(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SmsInfoEntity>()
                            .and(w -> w.eq(SmsInfoEntity::getOrgId, orgId)
                                    .or().apply("FIND_IN_SET({0}, org_id)", orgId))
                            .eq(SmsInfoEntity::getSmsIsactive, "01")
                            .orderByDesc(SmsInfoEntity::getSmsCreateddate)
            );
            if (list == null) {
                list = Collections.emptyList();
            }
            putLocalCache(cacheKey, list);
            putRedisCache(cacheKey, list);
            LOG.info("=== 查询到短信配置数量: {}", list.size());
            return ResponseResult.success(list);
        } catch (Exception e) {
            LOG.error("=== 查询短信配置列表异常: {}", e.getMessage(), e);
            return ResponseResult.error("查询短信配置列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存短信设置（验证码有效期、记录有效期）
     * @param params 包含 codeExpireMinutes 和 recordExpireDays 的Map
     * @return
     */
    @PostMapping("/saveSmsSetting")
    public ResponseResult saveSmsSetting(@RequestBody Map<String, Object> params) {
        try {
            LOG.info("=== 保存短信设置: {}", params);
            
            Integer codeExpireMinutes = params.get("codeExpireMinutes") != null ? 
                Integer.valueOf(params.get("codeExpireMinutes").toString()) : 30;
            Integer recordExpireDays = params.get("recordExpireDays") != null ? 
                Integer.valueOf(params.get("recordExpireDays").toString()) : 90;
            
            // 验证参数范围
            if (codeExpireMinutes < 1 || codeExpireMinutes > 1440) {
                return ResponseResult.error("验证码有效期必须在1-1440分钟之间");
            }
            if (recordExpireDays < 1 || recordExpireDays > 365) {
                return ResponseResult.error("记录有效期必须在1-365天之间");
            }
            
            // 保存验证码有效期配置
            com.houjiang.base.entity.BaseSysConfigsEntity codeExpireConfig = 
                baseSysConfigsService.getById("sms_code_expire_minutes");
            if (codeExpireConfig == null) {
                codeExpireConfig = new com.houjiang.base.entity.BaseSysConfigsEntity();
                codeExpireConfig.setConfigKey("sms_code_expire_minutes");
                codeExpireConfig.setConfigValue(String.valueOf(codeExpireMinutes));
                codeExpireConfig.setConfigType("sms_setting");
                codeExpireConfig.setConfigComment("短信验证码有效期（分钟）");
                codeExpireConfig.setConfigEnable(1);
                codeExpireConfig.setConfigSort(0);
                baseSysConfigsService.save(codeExpireConfig);
            } else {
                codeExpireConfig.setConfigValue(String.valueOf(codeExpireMinutes));
                baseSysConfigsService.updateById(codeExpireConfig);
            }
            
            // 保存记录有效期配置
            com.houjiang.base.entity.BaseSysConfigsEntity recordExpireConfig = 
                baseSysConfigsService.getById("sms_record_expire_days");
            if (recordExpireConfig == null) {
                recordExpireConfig = new com.houjiang.base.entity.BaseSysConfigsEntity();
                recordExpireConfig.setConfigKey("sms_record_expire_days");
                recordExpireConfig.setConfigValue(String.valueOf(recordExpireDays));
                recordExpireConfig.setConfigType("sms_setting");
                recordExpireConfig.setConfigComment("短信发送记录有效期（天）");
                recordExpireConfig.setConfigEnable(1);
                recordExpireConfig.setConfigSort(0);
                baseSysConfigsService.save(recordExpireConfig);
            } else {
                recordExpireConfig.setConfigValue(String.valueOf(recordExpireDays));
                baseSysConfigsService.updateById(recordExpireConfig);
            }
            
            // 清理缓存（配置项缓存 + 接口缓存）
            if (redisCache != null) {
                redisCache.deleteObject("sms_code_expire_minutes");
                redisCache.deleteObject("sms_record_expire_days");
            }
            clearSmsCache();
            
            LOG.info("=== 短信设置保存成功: 验证码有效期={}分钟, 记录有效期={}天", 
                codeExpireMinutes, recordExpireDays);
            return ResponseResult.success("短信设置保存成功");
        } catch (Exception e) {
            LOG.error("=== 保存短信设置异常: {}", e.getMessage(), e);
            return ResponseResult.error("保存短信设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询短信设置
     * @return
     */
    @GetMapping("/getSmsSetting")
    public ResponseResult getSmsSetting() {
        // 默认值兜底，避免异常导致前端失败
        Map<String, Object> result = new HashMap<>();
        result.put("codeExpireMinutes", 30);
        result.put("recordExpireDays", 90);
        
        try {
            LOG.info("=== 查询短信设置");

            // 1) 本地缓存命中
            Object localCached = getFromLocalCache(SMS_SETTING_CACHE_KEY);
            if (localCached instanceof Map<?, ?>) {
                LOG.debug("=== 命中本地缓存: {}", SMS_SETTING_CACHE_KEY);
                return ResponseResult.success(localCached);
            }

            // 2) Redis缓存命中
            Object redisCached = getFromRedisCache(SMS_SETTING_CACHE_KEY);
            if (redisCached instanceof Map<?, ?>) {
                putLocalCache(SMS_SETTING_CACHE_KEY, redisCached);
                LOG.debug("=== 命中Redis缓存: {}", SMS_SETTING_CACHE_KEY);
                return ResponseResult.success(redisCached);
            }
            
            // 检查服务是否注入
            if (baseSysConfigsService == null) {
                LOG.warn("=== baseSysConfigsService未注入，返回默认值");
                return ResponseResult.success(result);
            }
            
            // 查询验证码有效期配置
            try {
                String codeExpireValue = baseSysConfigsService.selectConfigValueByKey("sms_code_expire_minutes");
                if (codeExpireValue != null && !codeExpireValue.trim().isEmpty()) {
                    try {
                        int codeExpireMinutes = Integer.parseInt(codeExpireValue.trim());
                        // 验证范围
                        if (codeExpireMinutes >= 1 && codeExpireMinutes <= 1440) {
                            result.put("codeExpireMinutes", codeExpireMinutes);
                            LOG.info("=== 查询到验证码有效期配置: {} 分钟", codeExpireMinutes);
                        } else {
                            LOG.warn("=== 验证码有效期配置值超出范围: {}, 使用默认值30分钟", codeExpireMinutes);
                        }
                    } catch (NumberFormatException e) {
                        LOG.warn("=== 验证码有效期配置值格式错误: {}, 使用默认值30分钟", codeExpireValue);
                    }
                } else {
                    LOG.info("=== 未找到验证码有效期配置，使用默认值30分钟");
                }
            } catch (Exception e) {
                LOG.warn("=== 查询验证码有效期配置失败，使用默认值30分钟: {}", e.getMessage());
            }
            
            // 查询记录有效期配置
            try {
                String recordExpireValue = baseSysConfigsService.selectConfigValueByKey("sms_record_expire_days");
                if (recordExpireValue != null && !recordExpireValue.trim().isEmpty()) {
                    try {
                        int recordExpireDays = Integer.parseInt(recordExpireValue.trim());
                        // 验证范围
                        if (recordExpireDays >= 1 && recordExpireDays <= 365) {
                            result.put("recordExpireDays", recordExpireDays);
                            LOG.info("=== 查询到记录有效期配置: {} 天", recordExpireDays);
                        } else {
                            LOG.warn("=== 记录有效期配置值超出范围: {}, 使用默认值90天", recordExpireDays);
                        }
                    } catch (NumberFormatException e) {
                        LOG.warn("=== 记录有效期配置值格式错误: {}, 使用默认值90天", recordExpireValue);
                    }
                } else {
                    LOG.info("=== 未找到记录有效期配置，使用默认值90天");
                }
            } catch (Exception e) {
                LOG.warn("=== 查询记录有效期配置失败，使用默认值90天: {}", e.getMessage());
            }
            
            LOG.info("=== 查询到短信设置: {}", result);
            putLocalCache(SMS_SETTING_CACHE_KEY, result);
            putRedisCache(SMS_SETTING_CACHE_KEY, result);
            return ResponseResult.success(result);
        } catch (Throwable e) {
            // 捕获所有异常，包括Error
            LOG.error("=== 查询短信设置发生严重异常: {}", e.getMessage(), e);
            e.printStackTrace();
            // 即使发生异常，也返回默认值，避免前端报错
            return ResponseResult.success(result);
        }
    }

    /**
     * 清理短信相关缓存（本地 + Redis）
     */
    private void clearSmsCache() {
        String orgId = organizationResolver == null ? null : organizationResolver.resolveCurrentOrgId();
        localCache.keySet().removeIf(key -> key.equals(SMS_INFO_LIST_CACHE_KEY)
                || key.equals(SMS_ACTIVE_TYPE_CACHE_KEY)
                || (orgId != null && (key.equals(SMS_INFO_LIST_CACHE_KEY + ":" + orgId)
                || key.equals(SMS_ACTIVE_TYPE_CACHE_KEY + ":" + orgId))));
        localCache.remove(SMS_SETTING_CACHE_KEY);
        if (redisCache == null) {
            return;
        }
        try {
            redisCache.deleteObject(SMS_INFO_LIST_CACHE_KEY);
            redisCache.deleteObject(SMS_SETTING_CACHE_KEY);
            redisCache.deleteObject(SMS_ACTIVE_TYPE_CACHE_KEY);
            if (orgId != null) {
                redisCache.deleteObject(SMS_INFO_LIST_CACHE_KEY + ":" + orgId);
                redisCache.deleteObject(SMS_ACTIVE_TYPE_CACHE_KEY + ":" + orgId);
            }
            // thirdparty 侧通道类型缓存，避免切换短信通道后仍用旧值
            redisCache.deleteObject("SMS_ACTIVE_TYPE");
            redisCache.deleteObject("SMS_CODE_EXPIRE_MINUTES");
        } catch (Exception e) {
            LOG.warn("=== 清理短信缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 写入本地缓存
     */
    private void putLocalCache(String key, Object data) {
        localCache.put(key, new LocalCacheEntry<>(data, System.currentTimeMillis() + SMS_LOCAL_CACHE_TTL_MS));
    }

    /**
     * 获取本地缓存
     */
    private Object getFromLocalCache(String key) {
        LocalCacheEntry<Object> entry = localCache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expireAt < System.currentTimeMillis()) {
            localCache.remove(key);
            return null;
        }
        return entry.data;
    }

    /**
     * 写入Redis缓存
     */
    private void putRedisCache(String key, Object data) {
        if (redisCache == null) {
            return;
        }
        try {
            redisCache.setCacheObject(key, data, SMS_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.warn("=== 写入Redis缓存失败 key={}, msg={}", key, e.getMessage());
        }
    }

    /**
     * 获取Redis缓存
     */
    private Object getFromRedisCache(String key) {
        if (redisCache == null) {
            return null;
        }
        try {
            return redisCache.getCacheObject(key);
        } catch (Exception e) {
            LOG.warn("=== 读取Redis缓存失败 key={}, msg={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 本地缓存条目
     */
    private static class LocalCacheEntry<T> {
        private final T data;
        private final long expireAt;

        private LocalCacheEntry(T data, long expireAt) {
            this.data = data;
            this.expireAt = expireAt;
        }
    }

}
