package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * BaseServiceFeign降级处理类
/**
 * 
 * @description 当base服务不可用时，提供降级处理
 * @author Amos
 * @date 2024-12-20
 */
@Component
public class BaseServiceFeignFallback implements BaseServiceFeign {

    @Override
    public ResponseResult queryAllAdpageFloor() {
        return ResponseResult.success(List.of());
    }

    @Override
    public ResponseResult querySysConfigSetting() {
        return ResponseResult.fail("系统配置服务暂时不可用");
    }

    @Override
    public ResponseResult updateSysConfigSetting(Map<String, Object> param) {
        return ResponseResult.fail("系统配置服务暂时不可用");
    }

    @Override
    public ResponseResult queryJoinSetting(Map<String, Object> param) {
        return ResponseResult.fail("系统配置服务暂时不可用");
    }

    @Override
    public ResponseResult updateJoinSetting(Map<String, Object> param) {
        return ResponseResult.fail("系统配置服务暂时不可用");
    }

    @Override
    public ResponseResult getPasswordConfig() {
        return ResponseResult.fail("系统配置服务暂时不可用");
    }

    @Override
    public ResponseResult selectByConfigKey(String configKey) {
        return ResponseResult.fail("系统配置服务暂时不可用");
    }

    @Override
    public ResponseResult queryLanguageSwitch(String configKey) {
        return ResponseResult.success();
    }

    @Override
    public ResponseResult detectingImStatus(String configKey) {
        return ResponseResult.success();
    }

    @Override
    public ResponseResult queryMerchantPlusRule() {
        return ResponseResult.success(0);
    }

    @Override
    public String selectConfigValueByKey(String configKey) {
        return null;
    }

    @Override
    public ResponseResult updateConfigEnable(String configKey, String configEnable) {
        return ResponseResult.fail("系统配置服务暂时不可用");
    }

    @Override
    public ResponseResult uploadLanguageSwitch(Map<String, Object> param) {
        return ResponseResult.fail("系统配置服务暂时不可用");
    }

    @Override
    public ResponseResult upsertConfigValue(Map<String, Object> param) {
        return ResponseResult.fail("系统配置服务暂时不可用");
    }

    @Override
    public ResponseResult querySensitiveConfig(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult updateSensitiveConfig(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult querySensitiveWords(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult addSensitiveWord(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult deleteSensitiveWord(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult batchAddSensitiveWords(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult selectAllSensitiveWords() {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult querySensitiveClassification(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult addSensitiveClassification(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult deleteSensitiveClassification(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult queryAllSensitiveClassification() {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult querySensitiveRecord(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    @Override
    public ResponseResult addSensitiveRecord(Map<String, Object> param) {
        return ResponseResult.fail("敏感词服务暂时不可用");
    }

    /** ================= 组织管理降级处理 ================= */
    
    @Override
    public ResponseResult queryOrganizationPage(Map<String, Object> param) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }
    
    @Override
    public ResponseResult queryOrganizationTree(Map<String, Object> param) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }
    
    @Override
    public ResponseResult queryOrganizationById(String orgId) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }
    
    @Override
    public ResponseResult addOrganization(Map<String, Object> param) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }
    
    @Override
    public ResponseResult updateOrganization(Map<String, Object> param) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }
    
    @Override
    public ResponseResult deleteOrganization(String orgId) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }
    
    @Override
    public ResponseResult batchDeleteOrganization(List<String> orgIds) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }
    
    @Override
    public ResponseResult updateOrganizationStatus(String orgId, Integer status) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }
    
    @Override
    public ResponseResult checkOrgNameExists(String orgName, String orgId) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }

    @Override
    public ResponseResult<String> querySmsStates() {
        return ResponseResult.success("default");
    }

    @Override
    public ResponseResult selectSMSInfoByType(String type) {
        return ResponseResult.fail("短信配置服务暂时不可用");
    }

    @Override
    public ResponseResult updateSmsConfig(Map<String, Object> param) {
        return ResponseResult.fail("短信配置服务暂时不可用");
    }

    @Override
    public ResponseResult selectSMSInfoList() {
        return ResponseResult.fail("短信配置服务暂时不可用");
    }

    @Override
    public ResponseResult saveSmsSetting(Map<String, Object> params) {
        return ResponseResult.fail("短信设置服务暂时不可用");
    }

    @Override
    public ResponseResult getSmsSetting() {
        return ResponseResult.fail("短信设置服务暂时不可用");
    }

    @Override
    public ResponseResult queryProtocolList(Map<String, Object> param) {
        return ResponseResult.fail("协议管理服务暂时不可用");
    }

    @Override
    public ResponseResult saveOrUpdateProtocol(Map<String, Object> param) {
        return ResponseResult.fail("协议管理服务暂时不可用");
    }

    @Override
    public ResponseResult deleteProtocol(Map<String, Object> param) {
        return ResponseResult.fail("协议管理服务暂时不可用");
    }

    @Override
    public ResponseResult queryExpressCompanyList(Map<String, Object> param) {
        return ResponseResult.fail("快递公司管理服务暂时不可用");
    }

    @Override
    public ResponseResult addExpressCompany(Map<String, Object> param) {
        return ResponseResult.fail("快递公司管理服务暂时不可用");
    }

    @Override
    public ResponseResult updateExpressCompany(Map<String, Object> param) {
        return ResponseResult.fail("快递公司管理服务暂时不可用");
    }

    @Override
    public ResponseResult deleteExpressCompany(Map<String, Object> param) {
        return ResponseResult.fail("快递公司管理服务暂时不可用");
    }

    @Override
    public ResponseResult getExpressInfoById(Integer expressId) {
        return ResponseResult.fail("快递公司管理服务暂时不可用");
    }

    @Override
    public ResponseResult queryCurrencyList(Map<String, Object> param) {
        return ResponseResult.fail("货币管理服务暂时不可用");
    }

    @Override
    public ResponseResult insertCurrency(Map<String, Object> param) {
        return ResponseResult.fail("货币管理服务暂时不可用");
    }

    @Override
    public ResponseResult updateCurrency(Map<String, Object> param) {
        return ResponseResult.fail("货币管理服务暂时不可用");
    }

    @Override
    public ResponseResult deleteCurrency(Map<String, Object> param) {
        return ResponseResult.fail("货币管理服务暂时不可用");
    }

    @Override
    public ResponseResult queryCountryList(Map<String, Object> param) {
        return ResponseResult.fail("国家管理服务暂时不可用");
    }

    @Override
    public ResponseResult updateCountry(Map<String, Object> param) {
        return ResponseResult.fail("国家管理服务暂时不可用");
    }

    @Override
    public ResponseResult deleteCountry(Map<String, Object> param) {
        return ResponseResult.fail("国家管理服务暂时不可用");
    }

    @Override
    public ResponseResult queryLanguageList(Map<String, Object> param) {
        return ResponseResult.fail("语言包翻译服务暂时不可用");
    }

    @Override
    public ResponseResult correctTranslation(Map<String, Object> param) {
        return ResponseResult.fail("语言包翻译服务暂时不可用");
    }

    @Override
    public ResponseResult getTranslation(String cnText, String languageCode) {
        return ResponseResult.fail("语言包翻译服务暂时不可用");
    }

    @Override
    public ResponseResult batchImportTranslations(List<?> translations) {
        return ResponseResult.fail("语言包翻译服务暂时不可用");
    }

    @Override
    public ResponseResult deleteTranslation(Integer id) {
        return ResponseResult.fail("语言包翻译服务暂时不可用");
    }

    @Override
    public ResponseResult queryAiConfigList(Map<String, Object> param) {
        return ResponseResult.fail("AI配置服务暂时不可用");
    }

    @Override
    public ResponseResult queryEnabledAiConfigs() {
        return ResponseResult.fail("AI配置服务暂时不可用");
    }

    @Override
    public ResponseResult getDefaultAiConfig() {
        return ResponseResult.fail("AI配置服务暂时不可用");
    }

    @Override
    public ResponseResult saveOrUpdateAiConfig(Map<String, Object> param) {
        return ResponseResult.fail("AI配置服务暂时不可用");
    }

    @Override
    public ResponseResult saveAiProviderCredential(Map<String, Object> param) {
        return ResponseResult.fail("AI配置服务暂时不可用");
    }

    @Override
    public ResponseResult deleteAiConfig(Integer id) {
        return ResponseResult.fail("AI配置服务暂时不可用");
    }

    @Override
    public ResponseResult toggleAiConfigEnabled(Integer id, Integer isEnabled) {
        return ResponseResult.fail("AI配置服务暂时不可用");
    }

    @Override
    public ResponseResult setDefaultAiConfig(Integer id) {
        return ResponseResult.fail("AI配置服务暂时不可用");
    }

    @Override
    public ResponseResult testAiConnection(Integer id) {
        return ResponseResult.fail("AI配置服务暂时不可用");
    }

    @Override
    public ResponseResult getRSAPublicKey() {
        return ResponseResult.fail("RSA密钥管理服务暂时不可用");
    }

    @Override
    public ResponseResult getRSAKeys() {
        return ResponseResult.fail("RSA密钥管理服务暂时不可用");
    }

    @Override
    public ResponseResult saveRSAKeys(Map<String, String> keys) {
        return ResponseResult.fail("RSA密钥管理服务暂时不可用");
    }

    @Override
    public ResponseResult generateRSAKeys(int keySize) {
        return ResponseResult.fail("RSA密钥管理服务暂时不可用");
    }

    @Override
    public ResponseResult testRSAEncryption(Map<String, String> keys) {
        return ResponseResult.fail("RSA密鑰管理服务暂时不可用");
    }

    @Override
    public ResponseResult queryAllOssConfig() {
        return ResponseResult.fail("OSS存储配置服务暂时不可用");
    }

    @Override
    public ResponseResult selectOSSInfo(Map<String, Object> param) {
        return ResponseResult.fail("OSS存储配置服务暂时不可用");
    }

    @Override
    public ResponseResult updateOssConfig(Map<String, Object> param) {
        return ResponseResult.fail("OSS存储配置服务暂时不可用");
    }

    @Override
    public ResponseResult updateOssStates(Map<String, Object> param) {
        return ResponseResult.fail("OSS存储配置服务暂时不可用");
    }

    @Override
    public ResponseResult queryStorageMonitor() {
        return ResponseResult.fail("OSS存储监控服务暂时不可用");
    }

    @Override
    public ResponseResult queryStorageFileList(Map<String, Object> param) {
        return ResponseResult.fail("存储文件服务暂时不可用");
    }

    @Override
    public ResponseResult deleteStorageFiles(Map<String, Object> param) {
        return ResponseResult.fail("存储文件服务暂时不可用");
    }

    @Override
    public ResponseResult registerStorageFile(Map<String, Object> param) {
        return ResponseResult.fail("存储文件服务暂时不可用");
    }

    @Override
    public ResponseResult getCacheSettings() {
        return ResponseResult.fail("缓存设置服务暂时不可用");
    }

    @Override
    public ResponseResult saveCacheSettings(Map<String, Object> param) {
        return ResponseResult.fail("缓存设置服务暂时不可用");
    }

    @Override
    public ResponseResult testCacheConnection() {
        return ResponseResult.fail("缓存设置服务暂时不可用");
    }

    @Override
    public ResponseResult getDashboardPref() {
        return ResponseResult.fail("Dashboard服务暂时不可用");
    }

    @Override
    public ResponseResult saveDashboardPref(Map<String, Object> param) {
        return ResponseResult.fail("Dashboard服务暂时不可用");
    }

    @Override
    public ResponseResult resetDashboardPref() {
        return ResponseResult.fail("Dashboard服务暂时不可用");
    }

    @Override
    public ResponseResult databaseListPatches() {
        return ResponseResult.fail("数据库管理服务暂时不可用");
    }

    @Override
    public ResponseResult databaseReadPatch(String patchId) {
        return ResponseResult.fail("数据库管理服务暂时不可用");
    }

    @Override
    public ResponseResult databaseList() {
        return ResponseResult.fail("数据库管理服务暂时不可用");
    }

    @Override
    public ResponseResult databaseListTables(String dbName) {
        return ResponseResult.fail("数据库管理服务暂时不可用");
    }

    @Override
    public ResponseResult databaseListColumns(String dbName, String tableName) {
        return ResponseResult.fail("数据库管理服务暂时不可用");
    }

    @Override
    public ResponseResult databaseExecute(Map<String, Object> body) {
        return ResponseResult.fail("数据库管理服务暂时不可用");
    }

    @Override
    public ResponseResult databaseExecutePatch(String patchId, Map<String, Object> body) {
        return ResponseResult.fail("数据库管理服务暂时不可用");
    }

    @Override
    public ResponseResult databaseImportWecom(Map<String, Object> body) {
        return ResponseResult.fail("数据库管理服务暂时不可用");
    }

    @Override
    public ResponseResult recognizeFile(org.springframework.web.multipart.MultipartFile file, String description) {
        return ResponseResult.fail("文件识别服务暂时不可用");
    }

    @Override
    public ResponseResult confirmRecognize(Map<String, Object> params) {
        return ResponseResult.fail("文件识别服务暂时不可用");
    }

    @Override
    public ResponseResult getFileImportCatalog() {
        return ResponseResult.fail(503, "智能入库目录服务暂时不可用");
    }

    @Override
    public ResponseResult recognizeFileImport(org.springframework.web.multipart.MultipartFile file,
                                              String systemKey, String description, String fileUrl,
                                              String fileHash, String draftNo) {
        return ResponseResult.fail(503, "智能入库识别服务暂时不可用");
    }

    @Override
    public ResponseResult resumeFileImportDraft() {
        return ResponseResult.fail(503, "智能入库草稿服务暂时不可用");
    }

    @Override
    public ResponseResult getFileImportDraft(String draftNo) {
        return ResponseResult.fail(503, "智能入库草稿服务暂时不可用");
    }

    @Override
    public ResponseResult updateFileImportItem(Long itemId, Map<String, Object> request) {
        return ResponseResult.fail(503, "智能入库草稿服务暂时不可用");
    }

    @Override
    public ResponseResult prepareFileImportItem(Long itemId) {
        return ResponseResult.fail(503, "智能入库草稿服务暂时不可用");
    }

    @Override
    public ResponseResult completeFileImportItem(Long itemId, Map<String, Object> request) {
        return ResponseResult.fail(503, "智能入库草稿服务暂时不可用");
    }

    // ================= 注册协议定义与版本 =================

    /**
     * base 不可用时返回空列表：注册页将回退到内置文案并继续要求用户勾选，不阻断注册流程。
     */
    @Override
    public ResponseResult agreementList(Map<String, Object> param) {
        return ResponseResult.success(java.util.List.of());
    }

    @Override
    public ResponseResult agreementLatestVersions(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult agreementAdminPage(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult agreementAdminSave(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult agreementAdminDelete(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult agreementPersistSiteEula(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult agreementVersionList(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult agreementVersionSave(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult agreementVersionPublish(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult agreementVersionOffline(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult agreementVersionDelete(Map<String, Object> param) {
        return ResponseResult.fail(503, "协议服务暂时不可用");
    }

    @Override
    public ResponseResult officialDownloadSendSms(Map<String, Object> param) {
        return ResponseResult.fail(503, "短信验证服务暂时不可用");
    }

    @Override
    public ResponseResult officialDownloadVerifySms(Map<String, Object> param) {
        return ResponseResult.fail(503, "短信验证服务暂时不可用");
    }

    @Override
    public ResponseResult officialDownloadTicket(Map<String, Object> param) {
        return ResponseResult.fail(503, "官方下载服务暂时不可用");
    }

    @Override
    public ResponseResult officialDownloadArtifacts() {
        return ResponseResult.fail(503, "发行版本服务暂时不可用");
    }

    @Override
    public ResponseResult queryPageTemplates() {
        return ResponseResult.fail(503, "页面模板服务暂时不可用");
    }

    @Override
    public ResponseResult queryPageTemplateCode(Map<String, Object> param) {
        return ResponseResult.fail(503, "页面模板服务暂时不可用");
    }

    @Override
    public ResponseResult applyPageTemplate(Map<String, Object> param) {
        return ResponseResult.fail(503, "页面模板服务暂时不可用");
    }

    @Override
    public ResponseResult queryModularizationdAll(Map<String, Object> param) {
        return ResponseResult.fail(503, "页面装修服务暂时不可用");
    }

    // ================= 平台侧：站点协议签署记录回传 =================

    @Override
    public ResponseResult platformConsentReport(String token, Map<String, Object> param) {
        return ResponseResult.fail(503, "基础服务暂时不可用，协议签署记录暂未回传平台");
    }

    @Override
    public ResponseResult platformConsentPage(Map<String, Object> param) {
        return ResponseResult.fail(503, "基础服务暂时不可用，无法查询平台协议签署记录");
    }

    @Override
    public ResponseResult platformConsentSites(Map<String, Object> param) {
        return ResponseResult.fail(503, "基础服务暂时不可用，无法查询已接入站点");
    }
}
