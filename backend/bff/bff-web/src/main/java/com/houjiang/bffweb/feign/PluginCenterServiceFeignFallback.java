package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Fail-closed fallback for the base plugin center control plane. */
@Component
public class PluginCenterServiceFeignFallback implements PluginCenterServiceFeign {
    private ResponseResult unavailable() {
        return ResponseResult.fail("插件中心服务暂时不可用");
    }

    @Override public ResponseResult pluginCenterList(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult pluginCenterCategories() { return unavailable(); }
    @Override public ResponseResult pluginCenterSummary() { return unavailable(); }
    @Override public ResponseResult pluginCenterDetail(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult pluginCenterInstall(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult pluginCenterUninstall(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult pluginCenterToggle(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult pluginCenterSaveConfig(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult pluginCenterCreate(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketList(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketCategories() { return unavailable(); }
    @Override public ResponseResult marketDetail(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketFrameworkVersion() { return unavailable(); }
    @Override public ResponseResult marketVersions(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketCatalogUpdates(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketInstall(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketDownloadReport(String token, Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketDownloadStats(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketRemoteCatalog() { return unavailable(); }
    @Override public ResponseResult marketRemoteAcquire(String token, Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketRemoteLicenseReport(String token, Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketLicenseActivate(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketLicenseStatus(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketLicenseMine(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketLicenseUnbind(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketOrderCreate(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketOrderPay(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketOrderSync(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketOrderDetail(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketOrderCancel(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketOrderMine(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketPayConfig(Map<String, Object> p) { return unavailable(); }
    @Override public ResponseResult marketPaySandboxConfirm(Map<String, Object> p) { return unavailable(); }
    @Override public String marketPayNotify(String c, Map<String, String> p, String b) { return "fail:base插件中心服务不可用"; }
    @Override public ResponseResult marketPayAdminView(Map<String, Object> p, String t) { return unavailable(); }
    @Override public ResponseResult marketPayAdminSave(Map<String, Object> p, String t) { return unavailable(); }
    @Override public ResponseResult marketPayAdminDelete(Map<String, Object> p, String t) { return unavailable(); }
    @Override public ResponseResult marketPayAdminReload(Map<String, Object> p, String t) { return unavailable(); }
    @Override public ResponseResult serviceDelivery(Map<String,Object> params) { return ResponseResult.fail(503,"插件中心暂时不可用"); }
    @Override public ResponseResult serviceInstall(Map<String,Object> params) { return ResponseResult.fail(503,"插件中心暂时不可用"); }
    @Override public ResponseResult serviceUpgrade(Map<String,Object> params) { return ResponseResult.fail(503,"插件中心暂时不可用"); }
    @Override public ResponseResult serviceTaskStatus(Map<String,Object> params) { return ResponseResult.fail(503,"插件中心暂时不可用"); }
    @Override public ResponseResult serviceHistory() { return ResponseResult.fail(503,"插件中心暂时不可用"); }
}
