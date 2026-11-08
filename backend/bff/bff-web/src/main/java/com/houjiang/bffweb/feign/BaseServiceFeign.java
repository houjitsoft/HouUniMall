package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 基础服务Feign客户端
/**
 * 
 * @description 调用paas中台基础服务(base)的接口
 * @author Amos
 * @date 2024-12-20
 */
/**
 * base 服务 Feign（支持通过 -Dfeign.base.url=http://localhost:8214 直连，默认走 Nacos 服务发现）。
 */
@FeignClient(name = "base", path = "/", fallback = BaseServiceFeignFallback.class)
public interface BaseServiceFeign {

    /** PC 首页楼层及楼层广告（公开查询） */
    @GetMapping("/web/index/all/adpage/floor")
    ResponseResult queryAllAdpageFloor();

    // ================= 系统配置管理接口 =================
    
    /**
     * 查询系统配置
     */
    @PostMapping("/base/sysConfig/setting/query")
    ResponseResult querySysConfigSetting();
    
    /**
     * 更新系统配置
     */
    @PostMapping("/base/sysConfig/setting/update")
    ResponseResult updateSysConfigSetting(@RequestBody Map<String, Object> param);
    
    /**
     * 查询入驻设置
     */
    @PostMapping("/base/sysConfig/joinSetting/query")
    ResponseResult queryJoinSetting(@RequestBody Map<String, Object> param);
    
    /**
     * 更新入驻设置
     */
    @PostMapping("/base/sysConfig/joinSetting/update")
    ResponseResult updateJoinSetting(@RequestBody Map<String, Object> param);
    
    /**
     * 获取密码规则
     */
    @PostMapping("/base/sysConfig/business/passwordConfig")
    ResponseResult getPasswordConfig();
    
    /**
     * 根据configKey查询配置
     */
    @GetMapping("/base/sysConfig/selectByConfigKey")
    ResponseResult selectByConfigKey(@RequestParam("configKey") String configKey);

    @GetMapping("/base/sysConfig/switch/queryLanguage")
    ResponseResult queryLanguageSwitch(@RequestParam("configKey") String configKey);

    @GetMapping("/web/index/detectingImStatus")
    ResponseResult detectingImStatus(@RequestParam("configKey") String configKey);

    @PostMapping("/base/merchantPlusRule/query")
    ResponseResult queryMerchantPlusRule();

    /**
     * 根据configKey查询配置值（字符串）
     */
    @GetMapping("/base/sysConfig/selectConfigValueByKey")
    String selectConfigValueByKey(@RequestParam("configKey") String configKey);
    
    /**
     * 更新配置启用状态
     */
    @GetMapping("/base/sysConfig/updateConfig")
    ResponseResult updateConfigEnable(@RequestParam("configKey") String configKey, 
                                      @RequestParam("configEnable") String configEnable);

    /**
     * 更新单个开关配置
     */
    @PostMapping("/base/sysConfig/switch/uploadLanguage")
    ResponseResult uploadLanguageSwitch(@RequestBody Map<String, Object> param);

    /**
     * 按 configKey 写入配置。键不存在时插入，已存在时更新值。
     */
    @PostMapping("/base/sysConfig/upsertConfigValue")
    ResponseResult upsertConfigValue(@RequestBody Map<String, Object> param);

    // ================= 敏感词管理接口=================
    
    /**
     * 查询敏感词配置
     */
    @PostMapping("/baseSensitiveConfig/query")
    ResponseResult querySensitiveConfig(@RequestBody Map<String, Object> param);
    
    /**
     * 更新敏感词配置
     */
    @PostMapping("/baseSensitiveConfig/update")
    ResponseResult updateSensitiveConfig(@RequestBody Map<String, Object> param);
    
    /**
     * 查询敏感词列表
     */
    @PostMapping("/baseSensitive/query")
    ResponseResult querySensitiveWords(@RequestBody Map<String, Object> param);
    
    /**
     * 添加敏感词
     */
    @PostMapping("/baseSensitive/add")
    ResponseResult addSensitiveWord(@RequestBody Map<String, Object> param);
    
    /**
     * 删除敏感词
     */
    @PostMapping("/baseSensitive/delete")
    ResponseResult deleteSensitiveWord(@RequestBody Map<String, Object> param);
    
    /**
     * 批量添加敏感词
     */
    @PostMapping("/baseSensitive/addList")
    ResponseResult batchAddSensitiveWords(@RequestBody Map<String, Object> param);
    
    /**
     * 查询所有敏感词
     */
    @PostMapping("/baseSensitive/selectAllSensitive")
    ResponseResult selectAllSensitiveWords();

    // ================= 敏感词分类管理接口=================
    
    /**
     * 查询敏感词分类
     */
    @PostMapping("/baseSensitiveClassification/query")
    ResponseResult querySensitiveClassification(@RequestBody Map<String, Object> param);
    
    /**
     * 添加敏感词分类
     */
    @PostMapping("/baseSensitiveClassification/add")
    ResponseResult addSensitiveClassification(@RequestBody Map<String, Object> param);
    
    /**
     * 删除敏感词分类
     */
    @PostMapping("/baseSensitiveClassification/delete")
    ResponseResult deleteSensitiveClassification(@RequestBody Map<String, Object> param);
    
    /**
     * 查询所有敏感词分类
     */
    @PostMapping("/baseSensitiveClassification/queryAll")
    ResponseResult queryAllSensitiveClassification();

    // ================= 敏感词命中记录接口=================
    
    /**
     * 查询敏感词命中记录
     */
    @PostMapping("/baseSensitiveRecord/query")
    ResponseResult querySensitiveRecord(@RequestBody Map<String, Object> param);
    
    /**
     * 添加敏感词命中记录
     */
    @PostMapping("/baseSensitiveRecord/addFeign")
    ResponseResult addSensitiveRecord(@RequestBody Map<String, Object> param);

    // ================= 组织管理接口 =================
    
    /**
     * 分页查询组织信息
     */
    @PostMapping("/organization/queryOrganizationPage")
    ResponseResult queryOrganizationPage(@RequestBody Map<String, Object> param);
    
    /**
     * 查询组织树形结构
     */
    @PostMapping("/organization/queryOrganizationTree")
    ResponseResult queryOrganizationTree(@RequestBody Map<String, Object> param);
    
    /**
     * 根据ID查询组织信息
     */
    @GetMapping("/organization/queryOrganizationById")
    ResponseResult queryOrganizationById(@RequestParam("orgId") String orgId);
    
    /**
     * 新增组织
     */
    @PostMapping("/organization/addOrganization")
    ResponseResult addOrganization(@RequestBody Map<String, Object> param);
    
    /**
     * 更新组织信息
     */
    @PostMapping("/organization/updateOrganization")
    ResponseResult updateOrganization(@RequestBody Map<String, Object> param);
    
    /**
     * 删除组织
     */
    @PostMapping("/organization/deleteOrganization")
    ResponseResult deleteOrganization(@RequestParam("orgId") String orgId);
    
    /**
     * 批量删除组织
     */
    @PostMapping("/organization/batchDeleteOrganization")
    ResponseResult batchDeleteOrganization(@RequestBody List<String> orgIds);
    
    /**
     * 启用/禁用组织
     */
    @PostMapping("/organization/updateOrganizationStatus")
    ResponseResult updateOrganizationStatus(@RequestParam("orgId") String orgId, @RequestParam("status") Integer status);
    
    /**
     * 检查组织名称是否存在
     */
    @GetMapping("/organization/checkOrgNameExists")
    ResponseResult checkOrgNameExists(@RequestParam("orgName") String orgName, @RequestParam(required = false) String orgId);

    // ================= 短信配置管理接口 =================
    
    /**
     * 查询开启的短信配置类型
     */
    @PostMapping("/base/sms/querySmsStates")
    ResponseResult<String> querySmsStates();
    
    /**
     * 根据短信类型查询短信配置
     */
    @GetMapping("/base/sms/selectSMSInfoByType")
    ResponseResult selectSMSInfoByType(@RequestParam("type") String type);
    
    /**
     * 更新短信配置
     */
    @PostMapping("/base/sms/updateSmsConfigMap")
    ResponseResult updateSmsConfig(@RequestBody Map<String, Object> param);

    /**
     * 查询短信配置列表
     */
    @PostMapping("/base/sms/selectSMSInfoList")
    ResponseResult selectSMSInfoList();
    
    /**
     * 保存短信设置（验证码有效期、记录有效期）
     */
    @PostMapping("/base/sms/saveSmsSetting")
    ResponseResult saveSmsSetting(@RequestBody Map<String, Object> params);
    
    /**
     * 查询短信设置
     */
    @GetMapping("/base/sms/getSmsSetting")
    ResponseResult getSmsSetting();

    // ================= 协议管理接口 =================
    
    /**
     * 查询协议列表
     */
    @PostMapping("/base/protocol/queryList")
    ResponseResult queryProtocolList(@RequestBody Map<String, Object> param);
    
    /**
     * 保存或更新协议
     */
    @PostMapping("/base/protocol/saveOrUpdate")
    ResponseResult saveOrUpdateProtocol(@RequestBody Map<String, Object> param);
    
    /**
     * 删除协议
     */
    @PostMapping("/base/protocol/delete")
    ResponseResult deleteProtocol(@RequestBody Map<String, Object> param);

    // ================= 快递公司管理接口 =================
    
    /**
     * 查询快递公司列表
     */
    @PostMapping("/base/express/list/query")
    ResponseResult queryExpressCompanyList(@RequestBody Map<String, Object> param);
    
    /**
     * 新增快递公司
     */
    @PostMapping("/base/express/add")
    ResponseResult addExpressCompany(@RequestBody Map<String, Object> param);
    
    /**
     * 修改快递公司
     */
    @PostMapping("/base/express/update")
    ResponseResult updateExpressCompany(@RequestBody Map<String, Object> param);
    
    /**
     * 删除快递公司
     */
    @PostMapping("/base/express/delete")
    ResponseResult deleteExpressCompany(@RequestBody Map<String, Object> param);

    /**
     * 根据ID查询快递公司详情
     */
    @GetMapping("/base/express/getInfoById")
    ResponseResult getExpressInfoById(@RequestParam("expressId") Integer expressId);

    // ================= 货币管理接口 =================
    
    /**
     * 查询货币列表
     */
    @PostMapping("/base/currencySymbol/list")
    ResponseResult queryCurrencyList(@RequestBody Map<String, Object> param);
    
    /**
     * 新增货币
     */
    @PostMapping("/base/currencySymbol/insert")
    ResponseResult insertCurrency(@RequestBody Map<String, Object> param);
    
    /**
     * 更新货币
     */
    @PostMapping("/base/currencySymbol/update")
    ResponseResult updateCurrency(@RequestBody Map<String, Object> param);
    
    /**
     * 删除货币
     */
    @PostMapping("/base/currencySymbol/delete")
    ResponseResult deleteCurrency(@RequestBody Map<String, Object> param);

    // ================= 国家管理接口 =================
    
    /**
     * 查询国家列表
     */
    @PostMapping("/baseCountry/queryCountryAll")
    ResponseResult queryCountryList(@RequestBody Map<String, Object> param);
    
    /**
     * 更新国家
     */
    @PostMapping("/baseCountry/updateCountry")
    ResponseResult updateCountry(@RequestBody Map<String, Object> param);
    
    /**
     * 删除国家
     */
    @PostMapping("/baseCountry/deleteCountry")
    ResponseResult deleteCountry(@RequestBody Map<String, Object> param);

    // ================= 语言包翻译接口 =================
    
    /**
     * 分页查询语言包翻译列表
     */
    @PostMapping("/base/languageTranslation/list")
    ResponseResult queryLanguageList(@RequestBody Map<String, Object> param);
    
    /**
     * 修正译文
     */
    @PostMapping("/base/languageTranslation/correct")
    ResponseResult correctTranslation(@RequestBody Map<String, Object> param);
    
    /**
     * 根据中文原文和语言代码获取翻译
     */
    @GetMapping("/base/languageTranslation/get")
    ResponseResult getTranslation(@RequestParam("cnText") String cnText, @RequestParam("languageCode") String languageCode);
    
    /**
     * 批量导入翻译数据
     */
    @PostMapping("/base/languageTranslation/batchImport")
    ResponseResult batchImportTranslations(@RequestBody List<?> translations);
    
    /**
     * 删除翻译记录
     */
    @PostMapping("/base/languageTranslation/delete")
    ResponseResult deleteTranslation(@RequestParam("id") Integer id);

    // ================= AI配置管理接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/base/aiConfig/list")
    ResponseResult queryAiConfigList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/base/aiConfig/enabled")
    ResponseResult queryEnabledAiConfigs();
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/base/aiConfig/default")
    ResponseResult getDefaultAiConfig();
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/base/aiConfig/save")
    ResponseResult saveOrUpdateAiConfig(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/base/aiConfig/saveProvider")
    ResponseResult saveAiProviderCredential(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/base/aiConfig/delete")
    ResponseResult deleteAiConfig(@RequestParam("id") Integer id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/base/aiConfig/toggle")
    ResponseResult toggleAiConfigEnabled(@RequestParam("id") Integer id, @RequestParam("isEnabled") Integer isEnabled);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/base/aiConfig/setDefault")
    ResponseResult setDefaultAiConfig(@RequestParam("id") Integer id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/base/aiConfig/test")
    ResponseResult testAiConnection(@RequestParam("id") Integer id);

    // ================= RSA密钥管理接口 =================
    
    /**
     * 获取RSA公钥
     */
    @GetMapping("/base/sysConfig/rsa/publicKey")
    ResponseResult getRSAPublicKey();
    
    /**
     * 获取RSA密钥配置（包含公钥和私钥）
     */
    @GetMapping("/base/sysConfig/rsa/keys")
    ResponseResult getRSAKeys();
    
    /**
     * 保存RSA密钥配置
     */
    @PostMapping("/base/sysConfig/rsa/keys")
    ResponseResult saveRSAKeys(@RequestBody Map<String, String> keys);
    
    /**
     * 生成RSA密钥对
     */
    @PostMapping("/base/sysConfig/rsa/generate")
    ResponseResult generateRSAKeys(@RequestParam(defaultValue = "2048") int keySize);
    
    /**
     * 测试RSA加密解密
     */
    @PostMapping("/base/sysConfig/rsa/test")
    ResponseResult testRSAEncryption(@RequestBody Map<String, String> keys);

    // ================= OSS存储配置接口 =================

    /**
     * 查询所有OSS配置
     */
    @PostMapping("/base/ossConfig/queryAllOssConfig")
    ResponseResult queryAllOssConfig();

    /**
     * 查询指定类型的OSS配置信息
     */
    @PostMapping("/base/ossConfig/oss/selectOSSInfo")
    ResponseResult selectOSSInfo(@RequestBody Map<String, Object> param);

    /**
     * 更新OSS配置（存在则更新，不存在则添加）
     */
    @PostMapping("/base/ossConfig/updateOssConfig")
    ResponseResult updateOssConfig(@RequestBody Map<String, Object> param);

    /**
     * 修改OSS配置启用状态
     */
    @PostMapping("/base/ossConfig/updateOssStates")
    ResponseResult updateOssStates(@RequestBody Map<String, Object> param);

    /**
     * 存储监控概览
     */
    @PostMapping("/base/ossConfig/monitor")
    ResponseResult queryStorageMonitor();

    /**
     * 素材库文件列表
     */
    @PostMapping("/baseModularizationFlie/queryFileList")
    ResponseResult queryStorageFileList(@RequestBody Map<String, Object> param);

    /**
     * 删除素材库文件
     */
    @PostMapping("/baseModularizationFlie/delete")
    ResponseResult deleteStorageFiles(@RequestBody Map<String, Object> param);

    /**
     * 登记已上传文件
     */
    @PostMapping("/baseModularizationFlie/register")
    ResponseResult registerStorageFile(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/base/cache/settings/get")
    ResponseResult getCacheSettings();

    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/base/cache/settings/save")
    ResponseResult saveCacheSettings(@RequestBody Map<String, Object> param);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/base/cache/settings/test")
    ResponseResult testCacheConnection();

    // ================= Dashboard 用户偏好接口 =================

    /**
     * 查询当前用户 Dashboard 偏好
     */
    @PostMapping("/base/dashboard/pref/get")
    ResponseResult getDashboardPref();

    /**
     * 保存当前用户 Dashboard 偏好
     */
    @PostMapping("/base/dashboard/pref/save")
    ResponseResult saveDashboardPref(@RequestBody Map<String, Object> param);

    /**
     * 重置当前用户 Dashboard 偏好
     */
    @PostMapping("/base/dashboard/pref/reset")
    ResponseResult resetDashboardPref();

    // ================= 数据库脚本执行 =================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/base/database/patches")
    ResponseResult databaseListPatches();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/base/database/patch/{patchId}/content")
    ResponseResult databaseReadPatch(@PathVariable("patchId") String patchId);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/base/database/list")
    ResponseResult databaseList();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/base/database/{dbName}/tables")
    ResponseResult databaseListTables(@PathVariable("dbName") String dbName);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/base/database/{dbName}/tables/{tableName}/columns")
    ResponseResult databaseListColumns(@PathVariable("dbName") String dbName,
                                       @PathVariable("tableName") String tableName);

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/base/database/execute")
    ResponseResult databaseExecute(@RequestBody Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/base/database/patch/{patchId}/execute")
    ResponseResult databaseExecutePatch(@PathVariable("patchId") String patchId,
                                        @RequestBody(required = false) Map<String, Object> body);

    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @PostMapping("/base/database/wecom/import")
    ResponseResult databaseImportWecom(@RequestBody(required = false) Map<String, Object> body);

    // ================= 小匠帮你传 - 文件智能识别 =================

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping(value = "/base/file/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult recognizeFile(
        @RequestPart("file") MultipartFile file,
        @RequestParam(value = "description", required = false) String description
    );

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/base/file/confirm")
    ResponseResult confirmRecognize(@RequestBody Map<String, Object> params);

    @GetMapping("/base/file/import/catalog")
    ResponseResult getFileImportCatalog();

    @PostMapping(value = "/base/file/import/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult recognizeFileImport(
            @RequestPart("file") MultipartFile file,
            @RequestParam("systemKey") String systemKey,
            @RequestParam("description") String description,
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam("fileHash") String fileHash,
            @RequestParam(value = "draftNo", required = false) String draftNo);

    @GetMapping("/base/file/import/draft/resume")
    ResponseResult resumeFileImportDraft();

    @GetMapping("/base/file/import/draft/{draftNo}")
    ResponseResult getFileImportDraft(@PathVariable("draftNo") String draftNo);

    @PatchMapping("/base/file/import/item/{itemId}")
    ResponseResult updateFileImportItem(@PathVariable("itemId") Long itemId,
                                        @RequestBody Map<String, Object> request);

    @GetMapping("/base/file/import/item/{itemId}/prepare")
    ResponseResult prepareFileImportItem(@PathVariable("itemId") Long itemId);

    @PostMapping("/base/file/import/item/{itemId}/result")
    ResponseResult completeFileImportItem(@PathVariable("itemId") Long itemId,
                                          @RequestBody Map<String, Object> request);

    // ================= 注册协议定义与版本（协议可配置化） =================

    /**
     * 按场景查询启用中的协议及其当前生效版本正文（注册页/登录页使用，免登录）
     */
    @PostMapping("/base/agreement/list")
    ResponseResult agreementList(@RequestBody Map<String, Object> param);

    /**
     * 按协议编码批量查询当前生效版本
     */
    @PostMapping("/base/agreement/latestVersions")
    ResponseResult agreementLatestVersions(@RequestBody Map<String, Object> param);

    // ================= 平台侧：站点协议签署记录回传 =================

    /**
     * 站点回传签署记录到平台（平台侧落库，令牌由平台侧 fail-closed 校验）。
     */
    @PostMapping("/base/platform/consent/report")
    ResponseResult platformConsentReport(@RequestHeader("X-Platform-Token") String token,
                                          @RequestBody Map<String, Object> param);

    /**
     * 平台后台：各站点回传的签署记录分页。
     */
    @PostMapping("/base/platform/consent/page")
    ResponseResult platformConsentPage(@RequestBody Map<String, Object> param);

    /**
     * 平台后台：已接入回传的站点清单。
     */
    @PostMapping("/base/platform/consent/sites")
    ResponseResult platformConsentSites(@RequestBody(required = false) Map<String, Object> param);

    /**
     * 后台：协议定义分页
     */
    @PostMapping("/base/agreement/admin/page")
    ResponseResult agreementAdminPage(@RequestBody Map<String, Object> param);

    /**
     * 后台：新增或更新协议定义
     */
    @PostMapping("/base/agreement/admin/save")
    ResponseResult agreementAdminSave(@RequestBody Map<String, Object> param);

    /**
     * 后台：删除协议定义（连同全部版本）
     */
    @PostMapping("/base/agreement/admin/delete")
    ResponseResult agreementAdminDelete(@RequestBody Map<String, Object> param);

    /**
     * 写入站点开源 EULA 接受标记（accepted_eula_version / accepted_eula_time）
     */
    @PostMapping("/base/agreement/eula/persistSite")
    ResponseResult agreementPersistSiteEula(@RequestBody Map<String, Object> param);

    /**
     * 后台：查询某协议的版本列表
     */
    @PostMapping("/base/agreement/version/list")
    ResponseResult agreementVersionList(@RequestBody Map<String, Object> param);

    /**
     * 后台：新增或更新协议版本（草稿）
     */
    @PostMapping("/base/agreement/version/save")
    ResponseResult agreementVersionSave(@RequestBody Map<String, Object> param);

    /**
     * 后台：发布协议版本
     */
    @PostMapping("/base/agreement/version/publish")
    ResponseResult agreementVersionPublish(@RequestBody Map<String, Object> param);

    /**
     * 后台：下线协议版本
     */
    @PostMapping("/base/agreement/version/offline")
    ResponseResult agreementVersionOffline(@RequestBody Map<String, Object> param);

    /**
     * 后台：删除协议版本
     */
    @PostMapping("/base/agreement/version/delete")
    ResponseResult agreementVersionDelete(@RequestBody Map<String, Object> param);

    @PostMapping("/base/official-download/sms/send")
    ResponseResult officialDownloadSendSms(@RequestBody Map<String, Object> param);

    @PostMapping("/base/official-download/sms/verify")
    ResponseResult officialDownloadVerifySms(@RequestBody Map<String, Object> param);

    @PostMapping("/base/official-download/ticket")
    ResponseResult officialDownloadTicket(@RequestBody Map<String, Object> param);

    @GetMapping("/base/official-download/artifacts")
    ResponseResult officialDownloadArtifacts();

    // ================= 移动端页面模板 =================
    // 注意：baseModularizationd 的 Controller 未加 /base 前缀，
    // 线上实测 /baseModularizationd/** 返回 200、/base/baseModularizationd/** 返回 404，
    // 故此处 Feign path 不带 /base，与现状保持一致（改前缀属破坏性变更，需同步改前台）。

    /** 查询移动端页面模板库（后台「移动端装修 → 模板选择」用） */
    @PostMapping("/baseModularizationd/queryPageTemplates")
    ResponseResult queryPageTemplates();

    /** 查询某页面当前使用的模板 code */
    @PostMapping("/baseModularizationd/queryPageTemplateCode")
    ResponseResult queryPageTemplateCode(@RequestBody Map<String, Object> param);

    /** 为某页面选择移动端模板（templateCode 传空串表示取消套用） */
    @PostMapping("/baseModularizationd/applyPageTemplate")
    ResponseResult applyPageTemplate(@RequestBody Map<String, Object> param);

    /** 查询组件化页面列表（模板选择页需要先选页面） */
    @PostMapping("/baseModularizationd/queryModularizationdAll")
    ResponseResult queryModularizationdAll(@RequestBody Map<String, Object> param);
}
