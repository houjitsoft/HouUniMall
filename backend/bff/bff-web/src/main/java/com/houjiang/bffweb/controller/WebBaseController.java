package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.util.CommonStatusEnum;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Web端基础管理BFF控制器
/**
 * 
 * @description 处理Web端的基础管理相关请求,包括敏感词管理和系统配置管理,转发到对应的base服务
 * @author Amos
 * @date 2024-12-20
 */
@RestController
@RequestMapping("/web/base")
public class WebBaseController {

    @Autowired
    private BaseServiceFeign baseServiceFeign;

    /** 网站开关本地缓存(5分钟过期) */
    private static final long WEBSITE_SWITCHES_CACHE_TTL_MS = 5 * 60 * 1000L;
    private volatile CachedWebsiteSwitches cachedWebsiteSwitches = null;
    /** CAS锁：保证同一时刻只有一个线程在刷新缓存，其他线程直接返回旧缓存或默认值 */
    private final AtomicBoolean refreshingWebsiteSwitches = new AtomicBoolean(false);
    /** 网站开关刷新不能阻塞页面首个请求。 */
    private Executor websiteSwitchesRefreshExecutor = ForkJoinPool.commonPool();

    /** AI 配置为低频变更数据：成功结果缓存 5 分钟，过期后返回 stale 并异步刷新。 */
    private static final long AI_CONFIG_LIST_CACHE_TTL_MS = 5 * 60 * 1000L;
    final ConcurrentHashMap<String, CachedAiConfigList> aiConfigListCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicBoolean> aiConfigRefreshLocks = new ConcurrentHashMap<>();
    private Executor aiConfigRefreshExecutor = ForkJoinPool.commonPool();

    private static class CachedWebsiteSwitches {
        final ResponseResult result;
        final long expireAt;

        CachedWebsiteSwitches(ResponseResult result, long expireAt) {
            this.result = result;
            this.expireAt = expireAt;
        }
    }

    static final class CachedAiConfigList {
        final ResponseResult result;
        final long expireAt;

        CachedAiConfigList(ResponseResult result, long expireAt) {
            this.result = result;
            this.expireAt = expireAt;
        }
    }

    // ================= 敏感词配置管�?=================

    /**
     * 查询敏感词配�?
/**
     *
     * @param param 查询参数
     * @return ResponseResult 敏感词配置信�?
     */
    @PostMapping("/sensitive/config/query")
    public ResponseResult querySensitiveConfig(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.querySensitiveConfig(param);
    }

    /**
     * 更新敏感词配�?
/**
     *
     * @param configData 敏感词配置数�?
     * @return ResponseResult 更新结果
     */
    @PostMapping("/sensitive/config/update")
    public ResponseResult updateSensitiveConfig(@RequestBody Map<String, Object> configData) {
        return baseServiceFeign.updateSensitiveConfig(configData);
    }

    // ================= 敏感词管�?=================

    /**
     * 查询敏感词列�?
/**
     *
     * @param param 查询参数（支持分页和筛选）
     * @return ResponseResult 敏感词列�?
     */
    @PostMapping("/sensitive/word/list")
    public ResponseResult querySensitiveWords(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.querySensitiveWords(param);
    }

    /**
     * 添加敏感�?
/**
     *
     * @param wordData 敏感词数�?
     * @return ResponseResult 添加结果
     */
    @PostMapping("/sensitive/word/add")
    public ResponseResult addSensitiveWord(@RequestBody Map<String, Object> wordData) {
        return baseServiceFeign.addSensitiveWord(wordData);
    }

    /**
     * 删除敏感�?
/**
     *
     * @param deleteData 删除参数（包含敏感词ID�?
     * @return ResponseResult 删除结果
     */
    @PostMapping("/sensitive/word/delete")
    public ResponseResult deleteSensitiveWord(@RequestBody Map<String, Object> deleteData) {
        return baseServiceFeign.deleteSensitiveWord(deleteData);
    }

    /**
     * 批量添加敏感�?
/**
     *
     * @param batchData 批量添加参数（包含敏感词数组�?
     * @return ResponseResult 添加结果
     */
    @PostMapping("/sensitive/word/batch-add")
    public ResponseResult batchAddSensitiveWords(@RequestBody Map<String, Object> batchData) {
        return baseServiceFeign.batchAddSensitiveWords(batchData);
    }

    /**
     * 查询所有敏感词
/**
     *
     * @return ResponseResult 所有敏感词列表
     */
    @PostMapping("/sensitive/word/all")
    public ResponseResult selectAllSensitiveWords() {
        return baseServiceFeign.selectAllSensitiveWords();
    }

    // ================= 敏感词分类管�?=================

    /**
     * 查询敏感词分�?
/**
     *
     * @param param 查询参数（支持分页和筛选）
     * @return ResponseResult 敏感词分类列�?
     */
    @PostMapping("/sensitive/classification/list")
    public ResponseResult querySensitiveClassification(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.querySensitiveClassification(param);
    }

    /**
     * 添加敏感词分�?
/**
     *
     * @param classificationData 分类数据
     * @return ResponseResult 添加结果
     */
    @PostMapping("/sensitive/classification/add")
    public ResponseResult addSensitiveClassification(@RequestBody Map<String, Object> classificationData) {
        return baseServiceFeign.addSensitiveClassification(classificationData);
    }

    /**
     * 删除敏感词分�?
/**
     *
     * @param deleteData 删除参数（包含分类ID�?
     * @return ResponseResult 删除结果
     */
    @PostMapping("/sensitive/classification/delete")
    public ResponseResult deleteSensitiveClassification(@RequestBody Map<String, Object> deleteData) {
        return baseServiceFeign.deleteSensitiveClassification(deleteData);
    }

    /**
     * 查询所有敏感词分类
/**
     *
     * @return ResponseResult 所有敏感词分类列表
     */
    @PostMapping("/sensitive/classification/all")
    public ResponseResult queryAllSensitiveClassification() {
        return baseServiceFeign.queryAllSensitiveClassification();
    }

    // ================= 敏感词命中记录管�?=================

    /**
     * 查询敏感词命中记�?
/**
     *
     * @param param 查询参数（支持分页和筛选）
     * @return ResponseResult 敏感词命中记录列�?
     */
    @PostMapping("/sensitive/record/list")
    public ResponseResult querySensitiveRecord(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.querySensitiveRecord(param);
    }

    /**
     * 添加敏感词命中记�?
/**
     *
     * @param recordData 记录数据
     * @return ResponseResult 添加结果
     */
    @PostMapping("/sensitive/record/add")
    public ResponseResult addSensitiveRecord(@RequestBody Map<String, Object> recordData) {
        return baseServiceFeign.addSensitiveRecord(recordData);
    }

    // ================= 系统配置管理 =================

    /**
     * 查询系统配置
/**
     *
     * @return ResponseResult 系统配置信息
     */
    @PostMapping("/sys-config/query")
    public ResponseResult querySysConfig() {
        return baseServiceFeign.querySysConfigSetting();
    }

    /**
     * 更新系统配置
/**
     *
     * @param configData 配置数据
     * @return ResponseResult 更新结果
     */
    @PostMapping("/sys-config/update")
    public ResponseResult updateSysConfig(@RequestBody Map<String, Object> configData) {
        return baseServiceFeign.updateSysConfigSetting(configData);
    }

    /**
     * 查询入驻设置
/**
     *
     * @param param 查询参数
     * @return ResponseResult 入驻设置信息
     */
    @PostMapping("/sys-config/join-setting/query")
    public ResponseResult queryJoinSetting(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.queryJoinSetting(param);
    }

    /**
     * 更新入驻设置
/**
     *
     * @param joinSettingData 入驻设置数据
     * @return ResponseResult 更新结果
     */
    @PostMapping("/sys-config/join-setting/update")
    public ResponseResult updateJoinSetting(@RequestBody Map<String, Object> joinSettingData) {
        return baseServiceFeign.updateJoinSetting(joinSettingData);
    }

    /**
     * 获取企业注册密码规则
/**
     *
     * @return ResponseResult 密码规则配置
     */
    @PostMapping("/sys-config/password-config")
    public ResponseResult getPasswordConfig() {
        return baseServiceFeign.getPasswordConfig();
    }

    /**
     * 根据configKey查询配置
/**
     *
     * @param configKey 配置�?
     * @return ResponseResult 配置�?
     */
    @GetMapping("/sys-config/config-key")
    public ResponseResult getConfigByKey(@RequestParam("configKey") String configKey) {
        return baseServiceFeign.selectByConfigKey(configKey);
    }

    @GetMapping("/sys-config/switch/query-language")
    public ResponseResult queryLanguageSwitch(@RequestParam("configKey") String configKey) {
        return baseServiceFeign.queryLanguageSwitch(configKey);
    }

    /** 更新开关（language_switch / auto_translate_switch 等） */
    @PostMapping("/sys-config/switch/upload-language")
    public ResponseResult uploadLanguageSwitch(@RequestBody Map<String, Object> body) {
        return baseServiceFeign.uploadLanguageSwitch(body == null ? new HashMap<>() : body);
    }

    @GetMapping("/index/detecting-im-status")
    public ResponseResult detectingImStatus(@RequestParam("configKey") String configKey) {
        return baseServiceFeign.detectingImStatus(configKey);
    }

    @PostMapping("/merchant-plus-rule/query")
    public ResponseResult queryMerchantPlusRule() {
        return baseServiceFeign.queryMerchantPlusRule();
    }

    /**
     * 更新配置启用状�?
/**
     *
     * @param configKey 配置�?
     * @param configEnable 启用状�?(1:启用, 0:禁用)
     * @return ResponseResult 更新结果
     */
    @GetMapping("/sys-config/config-enable")
    public ResponseResult updateConfigEnable(@RequestParam("configKey") String configKey,
                                           @RequestParam("configEnable") String configEnable) {
        return baseServiceFeign.updateConfigEnable(configKey, configEnable);
    }

    /**
     * 查询网站基础开关（国际化、多组织）
/**
     *
     * @return ResponseResult
     */
    @GetMapping("/sys-config/website-switches")
    public ResponseResult getWebsiteSwitches() {
        return buildWebsiteSwitchesResult();
    }

    /**
     * 查询网站基础开关（POST，与 GET 等价；便于走统一鉴权/代理或与仅允许 POST 的网关兼容）
/**
     *
     * @return ResponseResult
     */
    @PostMapping("/sys-config/website-switches/query")
    public ResponseResult getWebsiteSwitchesPost() {
        return buildWebsiteSwitchesResult();
    }

    /**
     * 组装网站开关查询结果（无锁设计，避免 synchronized 导致的请求排队雪崩）
/**
     * <p>
     * 设计思路：
     * 1. 缓存命中直接返回（volatile 读，无锁）
     * 2. 缓存过期时，用 CAS 保证只有一个线程去刷新
     * 3. 其他并发线程直接返回旧缓存（stale）或默认值，不阻塞
     * 4. Feign 调用 base 服务是简单 configKey 查询，5s 内必须返回
     *
     * @return ResponseResult 网站开关配置
     */
    private ResponseResult buildWebsiteSwitchesResult() {
        long now = System.currentTimeMillis();

        // 1. 缓存命中，直接返回（无锁快速路径）
        CachedWebsiteSwitches cache = cachedWebsiteSwitches;
        if (cache != null && cache.expireAt > now) {
            return cache.result;
        }

        // 2. 缓存过期时仅触发后台刷新，当前请求立即返回 stale 或默认值。
        if (refreshingWebsiteSwitches.compareAndSet(false, true)) {
            try {
                websiteSwitchesRefreshExecutor.execute(this::refreshWebsiteSwitches);
            } catch (RuntimeException e) {
                refreshingWebsiteSwitches.set(false);
            }
        }

        return cache != null ? cache.result : buildDefaultWebsiteSwitches();
    }

    private void refreshWebsiteSwitches() {
        long refreshedAt = System.currentTimeMillis();
        try {
            String internationalizationSwitch = baseServiceFeign.selectConfigValueByKey("internationalization_switch");
            String multiOrganizationSwitch = baseServiceFeign.selectConfigValueByKey("multi_organization_switch");
            String aiAssistantSwitch = baseServiceFeign.selectConfigValueByKey("aiComparison_switch");

            Map<String, Object> result = new HashMap<>();
            result.put("internationalizationEnabled", toBooleanSwitch(internationalizationSwitch, true));
            result.put("multiOrganizationEnabled", toBooleanSwitch(multiOrganizationSwitch, true));
            result.put("aiAssistantEnabled", toBooleanSwitch(aiAssistantSwitch, true));
            cachedWebsiteSwitches = new CachedWebsiteSwitches(
                    ResponseResult.success(result),
                    refreshedAt + WEBSITE_SWITCHES_CACHE_TTL_MS
            );
        } catch (Exception e) {
            if (cachedWebsiteSwitches == null) {
                cachedWebsiteSwitches = new CachedWebsiteSwitches(
                        buildDefaultWebsiteSwitches(),
                        refreshedAt + WEBSITE_SWITCHES_CACHE_TTL_MS
                );
            }
        } finally {
            refreshingWebsiteSwitches.set(false);
        }
    }

    /**
     * 构建网站开关默认值（全部开启）
/**
     *
     * @return ResponseResult 默认配置
     */
    private ResponseResult buildDefaultWebsiteSwitches() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("internationalizationEnabled", true);
        fallback.put("multiOrganizationEnabled", true);
        fallback.put("aiAssistantEnabled", true);
        return ResponseResult.success(fallback);
    }

    /**
     * 保存网站基础开关（国际化、多组织）
/**
     *
     * @param configData 开关数据
     * @return ResponseResult
     */
    @PostMapping("/sys-config/website-switches")
    public ResponseResult saveWebsiteSwitches(@RequestBody Map<String, Object> configData) {
        boolean internationalizationEnabled = toBoolean(configData.get("internationalizationEnabled"), true);
        boolean multiOrganizationEnabled = toBoolean(configData.get("multiOrganizationEnabled"), true);
        boolean aiAssistantEnabled = toBoolean(configData.get("aiAssistantEnabled"), true);

        Map<String, Object> internationalizationPayload = new HashMap<>();
        internationalizationPayload.put("configKey", "internationalization_switch");
        internationalizationPayload.put("configValue", internationalizationEnabled ? "1" : "0");
        ResponseResult internationalizationResult = baseServiceFeign.uploadLanguageSwitch(internationalizationPayload);
        if (internationalizationResult == null || internationalizationResult.getCode() != 1) {
            return internationalizationResult == null ? ResponseResult.fail("保存国际化开关失败") : internationalizationResult;
        }

        Map<String, Object> multiOrganizationPayload = new HashMap<>();
        multiOrganizationPayload.put("configKey", "multi_organization_switch");
        multiOrganizationPayload.put("configValue", multiOrganizationEnabled ? "1" : "0");
        ResponseResult multiOrganizationResult = baseServiceFeign.uploadLanguageSwitch(multiOrganizationPayload);
        if (multiOrganizationResult == null || multiOrganizationResult.getCode() != 1) {
            return multiOrganizationResult == null ? ResponseResult.fail("保存多组织开关失败") : multiOrganizationResult;
        }

        Map<String, Object> aiAssistantPayload = new HashMap<>();
        aiAssistantPayload.put("configKey", "aiComparison_switch");
        aiAssistantPayload.put("configValue", aiAssistantEnabled ? "1" : "0");
        ResponseResult aiAssistantResult = baseServiceFeign.uploadLanguageSwitch(aiAssistantPayload);
        if (aiAssistantResult == null || aiAssistantResult.getCode() != 1) {
            return aiAssistantResult == null ? ResponseResult.fail("保存 AI 导购入口开关失败") : aiAssistantResult;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("internationalizationEnabled", internationalizationEnabled);
        result.put("multiOrganizationEnabled", multiOrganizationEnabled);
        result.put("aiAssistantEnabled", aiAssistantEnabled);
        ResponseResult response = ResponseResult.success(result);
        cachedWebsiteSwitches = new CachedWebsiteSwitches(
                response,
                System.currentTimeMillis() + WEBSITE_SWITCHES_CACHE_TTL_MS
        );
        return response;
    }

    /**
     * 将任意对象解析为布尔值。
/**
     *
     * @param value        原始值
     * @param defaultValue 默认值
     * @return 布尔值
     */
    private boolean toBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        String text = String.valueOf(value).trim();
        if ("1".equals(text) || "true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text)) {
            return true;
        }
        if ("0".equals(text) || "false".equalsIgnoreCase(text) || "no".equalsIgnoreCase(text)) {
            return false;
        }
        return defaultValue;
    }

    /**
     * 将开关字符串转换为布尔值。
/**
     *
     * @param value        开关字符串
     * @param defaultValue 默认值
     * @return 布尔值
     */
    private boolean toBooleanSwitch(String value, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return toBoolean(value, defaultValue);
    }

    // ================= 组织管理 =================

    /**
     * 分页查询组织信息
/**
     *
     * @param param 查询参数
     * @return ResponseResult 组织信息列表
     */
    @PostMapping("/organization/queryOrganizationPage")
    public ResponseResult queryOrganizationPage(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.queryOrganizationPage(param);
    }

    /**
     * 查询组织树形结构
/**
     *
     * @param param 查询参数
     * @return ResponseResult 组织�?
     */
    @PostMapping("/organization/queryOrganizationTree")
    public ResponseResult queryOrganizationTree(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.queryOrganizationTree(param);
    }

    /**
     * 根据ID查询组织信息
/**
     *
     * @param orgId 组织ID
     * @return ResponseResult 组织信息
     */
    @GetMapping("/organization/queryOrganizationById")
    public ResponseResult queryOrganizationById(@RequestParam("orgId") String orgId) {
        return baseServiceFeign.queryOrganizationById(orgId);
    }

    /**
     * 新增组织
/**
     *
     * @param orgData 组织数据
     * @return ResponseResult 添加结果
     */
    @PostMapping("/organization/addOrganization")
    public ResponseResult addOrganization(@RequestBody Map<String, Object> orgData) {
        return baseServiceFeign.addOrganization(orgData);
    }

    /**
     * 更新组织信息
/**
     *
     * @param orgData 组织数据
     * @return ResponseResult 更新结果
     */
    @PostMapping("/organization/updateOrganization")
    public ResponseResult updateOrganization(@RequestBody Map<String, Object> orgData) {
        return baseServiceFeign.updateOrganization(orgData);
    }

    /**
     * 删除组织
/**
     *
     * @param orgId 组织ID
     * @return ResponseResult 删除结果
     */
    @PostMapping("/organization/deleteOrganization")
    public ResponseResult deleteOrganization(@RequestParam("orgId") String orgId) {
        return baseServiceFeign.deleteOrganization(orgId);
    }

    /**
     * 批量删除组织
/**
     *
     * @param orgIds 组织ID列表
     * @return ResponseResult 删除结果
     */
    @PostMapping("/organization/batchDeleteOrganization")
    public ResponseResult batchDeleteOrganization(@RequestBody java.util.List<String> orgIds) {
        return baseServiceFeign.batchDeleteOrganization(orgIds);
    }

    /**
     * 启用/禁用组织
/**
     *
     * @param orgId 组织ID
     * @param status 状态：1-启用�?-禁用
     * @return ResponseResult 更新结果
     */
    @PostMapping("/organization/updateOrganizationStatus")
    public ResponseResult updateOrganizationStatus(@RequestParam("orgId") String orgId,
                                                 @RequestParam("status") Integer status) {
        return baseServiceFeign.updateOrganizationStatus(orgId, status);
    }

    /**
     * 检查组织名称是否存�?
/**
     *
     * @param orgName 组织名称
     * @param orgId 组织ID（排除自己）
     * @return ResponseResult 是否存在
     */
    @GetMapping("/organization/checkOrgNameExists")
    public ResponseResult checkOrgNameExists(@RequestParam("orgName") String orgName,
                                           @RequestParam(required = false) String orgId) {
        return baseServiceFeign.checkOrgNameExists(orgName, orgId);
    }

    // ================= 短信配置管理接口 =================

    /**
     * 查询开启的短信配置类型
/**
     *
     * @return 短信配置类型
     */
    @PostMapping("/sms/querySmsStates")
    public ResponseResult<String> querySmsStates() {
        return baseServiceFeign.querySmsStates();
    }

    /**
     * 根据短信类型查询短信配置
/**
     *
     * @param type 短信类型
     * @return ResponseResult
     */
    @GetMapping("/sms/selectSMSInfoByType")
    public ResponseResult selectSMSInfoByType(@RequestParam("type") String type) {
        return baseServiceFeign.selectSMSInfoByType(type);
    }

    /**
     * 更新短信配置
/**
     *
     * @param param 短信配置参数
     * @return ResponseResult
     */
    @PostMapping("/sms/updateSmsConfigMap")
    public ResponseResult updateSmsConfig(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.updateSmsConfig(param);
    }

    /**
     * 查询短信配置列表
/**
     *
     * @return ResponseResult
     */
    @PostMapping("/sms/selectSMSInfoList")
    public ResponseResult selectSMSInfoList() {
        return baseServiceFeign.selectSMSInfoList();
    }

    /**
     * 保存短信设置（验证码有效期、记录有效期）
/**
     *
     * @param params 包含 codeExpireMinutes 和 recordExpireDays 的Map
     * @return ResponseResult
     */
    @PostMapping("/sms/saveSmsSetting")
    public ResponseResult saveSmsSetting(@RequestBody Map<String, Object> params) {
        return baseServiceFeign.saveSmsSetting(params);
    }

    /**
     * 查询短信设置
/**
     *
     * @return ResponseResult
     */
    @GetMapping("/sms/getSmsSetting")
    public ResponseResult getSmsSetting() {
        return baseServiceFeign.getSmsSetting();
    }

    // ================= 协议管理 =================

    /**
     * 查询协议列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 协议列表
     */
    @PostMapping("/protocol/list")
    public ResponseResult queryProtocolList(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.queryProtocolList(param);
    }

    /**
     * 保存或更新协议
/**
     *
     * @param param 协议数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/protocol/save")
    public ResponseResult saveProtocol(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.saveOrUpdateProtocol(param);
    }

    /**
     * 删除协议
/**
     *
     * @param param 包含协议ID的参数
     * @return ResponseResult 删除结果
     */
    @PostMapping("/protocol/delete")
    public ResponseResult deleteProtocol(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.deleteProtocol(param);
    }

    // ================= 快递公司管理 =================

    /**
     * 查询快递公司列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 快递公司列表
     */
    @PostMapping("/express/company/list")
    public ResponseResult queryExpressCompanyList(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.queryExpressCompanyList(param);
    }

    /**
     * 保存或更新快递公司
/**
     *
     * @param param 快递公司数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/express/company/save")
    public ResponseResult saveExpressCompany(@RequestBody Map<String, Object> param) {
        // 根据是否有id判断是新增还是更新
        if (param.get("id") != null) {
            return baseServiceFeign.updateExpressCompany(param);
        } else {
            return baseServiceFeign.addExpressCompany(param);
        }
    }

    /**
     * 删除快递公司
/**
     *
     * @param param 包含快递公司ID的参数
     * @return ResponseResult 删除结果
     */
    @PostMapping("/express/company/delete")
    public ResponseResult deleteExpressCompany(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.deleteExpressCompany(param);
    }

    // ================= 货币管理 =================

    /**
     * 查询货币列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 货币列表
     */
    @PostMapping("/currency/list")
    public ResponseResult queryCurrencyList(@RequestBody Map<String, Object> param) {
        normalizeListPageParam(param);
        if (param.get("name") != null && param.get("remark") == null) {
            param.put("remark", param.get("name"));
        }
        return baseServiceFeign.queryCurrencyList(param);
    }

    /**
     * 保存或更新货币
/**
     *
     * @param param 货币数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/currency/save")
    public ResponseResult saveCurrency(@RequestBody Map<String, Object> param) {
        normalizeCurrencySaveParam(param);
        if (param.get("id") != null) {
            return baseServiceFeign.updateCurrency(param);
        } else {
            return baseServiceFeign.insertCurrency(param);
        }
    }

    /**
     * 删除货币
/**
     *
     * @param param 包含货币ID的参数
     * @return ResponseResult 删除结果
     */
    @PostMapping("/currency/delete")
    public ResponseResult deleteCurrency(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.deleteCurrency(param);
    }

    // ================= 国家管理 =================

    /**
     * 查询国家列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 国家列表
     */
    @PostMapping("/country/list")
    public ResponseResult queryCountryList(@RequestBody Map<String, Object> param) {
        normalizeCountryListParam(param);
        normalizeListPageParam(param);
        return baseServiceFeign.queryCountryList(param);
    }

    /**
     * 保存或更新国家
/**
     *
     * @param param 国家数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/country/save")
    public ResponseResult saveCountry(@RequestBody Map<String, Object> param) {
        normalizeCountrySaveParam(param);
        return baseServiceFeign.updateCountry(param);
    }

    /**
     * 删除国家
/**
     *
     * @param param 包含国家ID的参数
     * @return ResponseResult 删除结果
     */
    @PostMapping("/country/delete")
    public ResponseResult deleteCountry(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.deleteCountry(param);
    }

    // ================= 语言包校正 =================

    /**
     * 查询语言包列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 语言包列表
     */
    @PostMapping("/language/list")
    public ResponseResult queryLanguageList(@RequestBody Map<String, Object> param) {
        normalizeLanguageListParam(param);
        normalizeListPageParam(param);
        return baseServiceFeign.queryLanguageList(param);
    }

    /**
     * 修正译文
/**
     *
     * @param param 修正数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/language/correct")
    public ResponseResult correctTranslation(@RequestBody Map<String, Object> param) {
        normalizeLanguageCorrectParam(param);
        return baseServiceFeign.correctTranslation(param);
    }

    /**
     * 根据中文原文和语言代码获取翻译
/**
     *
     * @param cnText 中文原文
     * @param languageCode 语言代码
     * @return ResponseResult 翻译结果
     */
    @GetMapping("/language/get")
    public ResponseResult getTranslation(@RequestParam("cnText") String cnText,
                                        @RequestParam("languageCode") String languageCode) {
        return baseServiceFeign.getTranslation(cnText, languageCode);
    }

    /**
     * 批量导入翻译数据
/**
     *
     * @param translations 翻译数据列表
     * @return ResponseResult 导入结果
     */
    @PostMapping("/language/batchImport")
    public ResponseResult batchImportTranslations(@RequestBody java.util.List<?> translations) {
        return baseServiceFeign.batchImportTranslations(translations);
    }

    /**
     * 删除翻译记录
/**
     *
     * @param id 记录ID
     * @return ResponseResult 删除结果
     */
    @PostMapping("/language/delete")
    public ResponseResult deleteTranslation(@RequestParam("id") Integer id) {
        return baseServiceFeign.deleteTranslation(id);
    }

    /** ================= AI配置管理 ================= */

    @PostMapping("/aiConfig/list")
    public ResponseResult queryAiConfigList(@RequestBody Map<String, Object> param) {
        Map<String, Object> query = param == null ? new HashMap<>() : new HashMap<>(param);
        normalizeListPageParam(query);
        String cacheKey = aiConfigCacheKey(query);
        long now = System.currentTimeMillis();
        CachedAiConfigList cached = aiConfigListCache.get(cacheKey);
        if (cached != null && cached.expireAt > now) {
            return cached.result;
        }
        if (cached != null) {
            refreshAiConfigListAsync(cacheKey, query);
            return cached.result;
        }

        ResponseResult result = baseServiceFeign.queryAiConfigList(query);
        cacheSuccessfulAiConfigList(cacheKey, result);
        return result;
    }

    @PostMapping("/aiConfig/enabled")
    public ResponseResult queryEnabledAiConfigs() {
        return baseServiceFeign.queryEnabledAiConfigs();
    }

    @GetMapping("/aiConfig/default")
    public ResponseResult getDefaultAiConfig() {
        return baseServiceFeign.getDefaultAiConfig();
    }

    @PostMapping("/aiConfig/save")
    public ResponseResult saveOrUpdateAiConfig(@RequestBody Map<String, Object> param) {
        return invalidateAiConfigListCacheIfSuccess(baseServiceFeign.saveOrUpdateAiConfig(param));
    }

    @PostMapping("/aiConfig/saveProvider")
    public ResponseResult saveAiProviderCredential(@RequestBody Map<String, Object> param) {
        return invalidateAiConfigListCacheIfSuccess(baseServiceFeign.saveAiProviderCredential(param));
    }

    @PostMapping("/aiConfig/delete")
    public ResponseResult deleteAiConfig(@RequestParam("id") Integer id) {
        return invalidateAiConfigListCacheIfSuccess(baseServiceFeign.deleteAiConfig(id));
    }

    @PostMapping("/aiConfig/toggle")
    public ResponseResult toggleAiConfigEnabled(@RequestParam("id") Integer id, 
                                                @RequestParam("isEnabled") Integer isEnabled) {
        return invalidateAiConfigListCacheIfSuccess(baseServiceFeign.toggleAiConfigEnabled(id, isEnabled));
    }

    @PostMapping("/aiConfig/setDefault")
    public ResponseResult setDefaultAiConfig(@RequestParam("id") Integer id) {
        return invalidateAiConfigListCacheIfSuccess(baseServiceFeign.setDefaultAiConfig(id));
    }

    @PostMapping("/aiConfig/test")
    public ResponseResult testAiConnection(@RequestParam("id") Integer id) {
        return baseServiceFeign.testAiConnection(id);
    }

    // ================= RSA密钥管理 =================

    /**
     * 获取RSA公钥
/**
     *
     * @return ResponseResult RSA公钥
     */
    @GetMapping("/sysConfig/rsa/publicKey")
    public ResponseResult getRSAPublicKey() {
        return baseServiceFeign.getRSAPublicKey();
    }

    /**
     * 获取RSA密钥配置（包含公钥和私钥）
/**
     *
     * @return ResponseResult RSA密钥配置
     */
    @GetMapping("/sysConfig/rsa/keys")
    public ResponseResult getRSAKeys() {
        return baseServiceFeign.getRSAKeys();
    }

    /**
     * 保存RSA密钥配置
/**
     *
     * @param keys 密钥配置数据
     * @return ResponseResult 操作结果
     */
    @PostMapping("/sysConfig/rsa/keys")
    public ResponseResult saveRSAKeys(@RequestBody Map<String, String> keys) {
        return baseServiceFeign.saveRSAKeys(keys);
    }

    /**
     * 生成RSA密钥对
/**
     *
     * @param keySize 密钥长度（默认2048）
     * @return ResponseResult 生成的密钥对
     */
    @PostMapping("/sysConfig/rsa/generate")
    public ResponseResult generateRSAKeys(@RequestParam(defaultValue = "2048") int keySize) {
        return baseServiceFeign.generateRSAKeys(keySize);
    }

    /**
     * 测试RSA加密解密
/**
     *
     * @param keys 密鑰配置数据
     * @return ResponseResult 测试结果
     */
    @PostMapping("/sysConfig/rsa/test")
    public ResponseResult testRSAEncryption(@RequestBody Map<String, String> keys) {
        return baseServiceFeign.testRSAEncryption(keys);
    }

    // ================= OSS存储配置 =================

    /**
     * 查询所有OSS存储配置列表
/**
     *
     * @return ResponseResult OSS配置列表
     */
    @PostMapping("/ossConfig/list")
    public ResponseResult queryAllOssConfig() {
        return baseServiceFeign.queryAllOssConfig();
    }

    /**
     * 查询指定类型的OSS配置
/**
     *
     * @param param 包含 token 和 ossType 的参数Map
     * @return ResponseResult 指定类型OSS配置
     */
    @PostMapping("/ossConfig/detail")
    public ResponseResult selectOSSInfo(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.selectOSSInfo(param);
    }

    /**
     * 新增或更新OSS配置（存在则更新，不存在则添加）
/**
     *
     * @param param OSS配置数据，包含 token
     * @return ResponseResult 操作结果
     */
    @PostMapping("/ossConfig/save")
    public ResponseResult updateOssConfig(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.updateOssConfig(param);
    }

    /**
     * 修改OSS配置启用状态
/**
     *
     * @param param 包含 token、ossId、ossStates的参数Map
     * @return ResponseResult 操作结果
     */
    @PostMapping("/ossConfig/updateStates")
    public ResponseResult updateOssStates(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.updateOssStates(param);
    }

    /**
     * 存储监控概览
     */
    @PostMapping("/ossConfig/monitor")
    public ResponseResult queryStorageMonitor() {
        return baseServiceFeign.queryStorageMonitor();
    }

    /**
     * 素材库文件列表
     */
    @PostMapping("/storage/files/list")
    public ResponseResult queryStorageFileList(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.queryStorageFileList(param);
    }

    /**
     * 删除素材库文件
     */
    @PostMapping("/storage/files/delete")
    public ResponseResult deleteStorageFiles(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.deleteStorageFiles(param);
    }

    /**
     * 登记已上传文件到素材库
     */
    @PostMapping("/storage/files/register")
    public ResponseResult registerStorageFile(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.registerStorageFile(param);
    }

    /** ================= 缓存设置 ================= */

    @PostMapping("/cache/settings/get")
    public ResponseResult getCacheSettings() {
        return baseServiceFeign.getCacheSettings();
    }

    @PostMapping("/cache/settings/save")
    public ResponseResult saveCacheSettings(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.saveCacheSettings(param);
    }

    @PostMapping("/cache/settings/test")
    public ResponseResult testCacheConnection() {
        return baseServiceFeign.testCacheConnection();
    }

    // ================= Dashboard 用户偏好 =================

    /**
     * 查询当前用户 Dashboard 偏好
     */
    @PostMapping("/dashboard/pref/get")
    public ResponseResult getDashboardPref() {
        return baseServiceFeign.getDashboardPref();
    }

    /**
     * 保存当前用户 Dashboard 偏好
     */
    @PostMapping("/dashboard/pref/save")
    public ResponseResult saveDashboardPref(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.saveDashboardPref(param);
    }

    /**
     * 重置当前用户 Dashboard 偏好
     */
    @PostMapping("/dashboard/pref/reset")
    public ResponseResult resetDashboardPref() {
        return baseServiceFeign.resetDashboardPref();
    }

    /** 统一分页参数：兼容前端 size / pageSize，避免 limit 为空导致 base 服务 NPE */
    private void normalizeListPageParam(Map<String, Object> param) {
        if (param == null) {
            return;
        }
        if (param.get("limit") == null) {
            Object size = param.get("size");
            if (size == null) {
                size = param.get("pageSize");
            }
            if (size != null) {
                param.put("limit", size);
            }
        }
        if (param.get("page") == null) {
            param.put("page", 1);
        }
        if (param.get("limit") == null) {
            param.put("limit", 10);
        }
    }

    /** 兼容旧版前端字段名：name/code/symbol/rate */
    private void normalizeCurrencySaveParam(Map<String, Object> param) {
        if (param == null) {
            return;
        }
        copyIfAbsent(param, "remark", "name");
        copyIfAbsent(param, "currencyCode", "code");
        copyIfAbsent(param, "currencySymbol", "symbol");
        copyIfAbsent(param, "exchangeRate", "rate");
    }

    private void copyIfAbsent(Map<String, Object> param, String targetKey, String sourceKey) {
        if (param.get(targetKey) == null && param.get(sourceKey) != null) {
            param.put(targetKey, param.get(sourceKey));
        }
    }

    String aiConfigCacheKey(Map<String, Object> param) {
        return param == null ? "{}" : new TreeMap<>(param).toString();
    }

    private void refreshAiConfigListAsync(String cacheKey, Map<String, Object> query) {
        AtomicBoolean refreshLock = aiConfigRefreshLocks.computeIfAbsent(cacheKey, ignored -> new AtomicBoolean());
        if (!refreshLock.compareAndSet(false, true)) {
            return;
        }
        Map<String, Object> refreshQuery = new HashMap<>(query);
        aiConfigRefreshExecutor.execute(() -> {
            try {
                ResponseResult refreshed = baseServiceFeign.queryAiConfigList(refreshQuery);
                cacheSuccessfulAiConfigList(cacheKey, refreshed);
            } catch (Exception ignored) {
                // 保留上一次真实成功结果，下一次过期请求会再次尝试刷新。
            } finally {
                refreshLock.set(false);
            }
        });
    }

    private void cacheSuccessfulAiConfigList(String cacheKey, ResponseResult result) {
        if (result != null && result.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
            aiConfigListCache.put(cacheKey,
                    new CachedAiConfigList(result, System.currentTimeMillis() + AI_CONFIG_LIST_CACHE_TTL_MS));
        }
    }

    private ResponseResult invalidateAiConfigListCacheIfSuccess(ResponseResult result) {
        if (result != null && result.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
            aiConfigListCache.clear();
        }
        return result;
    }

    /** 国家列表：兼容前端 name 字段 */
    private void normalizeCountryListParam(Map<String, Object> param) {
        if (param == null) {
            return;
        }
        copyIfAbsent(param, "countryName", "name");
    }

    /** 国家保存：兼容 name / phoneCode / isCommon */
    private void normalizeCountrySaveParam(Map<String, Object> param) {
        if (param == null) {
            return;
        }
        copyIfAbsent(param, "countryName", "name");
        copyIfAbsent(param, "internationalDialingCode", "phoneCode");
        if (param.get("countryCommonUse") == null && param.get("isCommon") != null) {
            Object raw = param.get("isCommon");
            boolean common = Boolean.TRUE.equals(raw)
                    || "true".equalsIgnoreCase(String.valueOf(raw))
                    || "1".equals(String.valueOf(raw));
            param.put("countryCommonUse", common ? 1 : 2);
        }
    }

    /** 语言包列表：兼容 language 字段 */
    private void normalizeLanguageListParam(Map<String, Object> param) {
        if (param == null) {
            return;
        }
        copyIfAbsent(param, "languageCode", "language");
    }

    /** 语言包修正：兼容 correctedText 字段 */
    private void normalizeLanguageCorrectParam(Map<String, Object> param) {
        if (param == null) {
            return;
        }
        copyIfAbsent(param, "correctedTranslation", "correctedText");
    }

    // ================= 移动端页面模板 =================

    /**
     * 查询移动端页面模板库。
     * 供 admin「商城管理 → 移动端装修 → 模板选择」使用。
     *
     * @return 模板列表（code / name / summary / themeColor / style / columns）
     */
    @PostMapping("/page/template/list")
    public ResponseResult queryPageTemplates() {
        return baseServiceFeign.queryPageTemplates();
    }

    /**
     * 查询某页面当前使用的模板 code。
     *
     * @param param 含 pageId
     * @return 当前 templateCode，未设置时为空串
     */
    @PostMapping("/page/template/current")
    public ResponseResult queryPageTemplateCode(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.queryPageTemplateCode(param);
    }

    /**
     * 为某页面选择移动端模板（按页面生效）。
     *
     * @param param 含 pageId 与 templateCode；templateCode 传空串表示取消套用
     * @return 是否成功
     */
    @PostMapping("/page/template/apply")
    public ResponseResult applyPageTemplate(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.applyPageTemplate(param);
    }

    /**
     * 查询组件化页面列表（模板选择页需先选页面）。
     *
     * @param param 查询参数
     * @return 页面列表
     */
    @PostMapping("/page/modularizationd/list")
    public ResponseResult queryModularizationdAll(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.queryModularizationdAll(param);
    }
}
