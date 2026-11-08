package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/** Feign contract for the base-owned plugin center control plane. */
@FeignClient(name = "base", contextId = "pluginCenterServiceFeign", path = "/",
        fallback = PluginCenterServiceFeignFallback.class)
public interface PluginCenterServiceFeign {

    @PostMapping("/base/plugin-center/list")
    ResponseResult pluginCenterList(@RequestBody(required = false) Map<String, Object> params);

    @PostMapping("/base/plugin-center/categories")
    ResponseResult pluginCenterCategories();

    @PostMapping("/base/plugin-center/summary")
    ResponseResult pluginCenterSummary();

    @PostMapping("/base/plugin-center/detail")
    ResponseResult pluginCenterDetail(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/install")
    ResponseResult pluginCenterInstall(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/uninstall")
    ResponseResult pluginCenterUninstall(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/toggle")
    ResponseResult pluginCenterToggle(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/config/save")
    ResponseResult pluginCenterSaveConfig(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/create")
    ResponseResult pluginCenterCreate(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/list")
    ResponseResult marketList(@RequestBody(required = false) Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/categories")
    ResponseResult marketCategories();

    @PostMapping("/base/plugin-center/market/detail")
    ResponseResult marketDetail(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/versions")
    ResponseResult marketVersions(@RequestBody(required = false) Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/catalog/updates")
    ResponseResult marketCatalogUpdates(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/install")
    ResponseResult marketInstall(@RequestBody Map<String, Object> params);

    /**
     * 站点上报一次插件下载（别的安装人打过来，没有平台登录态；令牌在 base 侧 fail-closed 校验）。
     */
    @PostMapping("/base/plugin-center/market/download/report")
    ResponseResult marketDownloadReport(@RequestHeader(value = "X-Platform-Token", required = false) String token,
                                        @RequestBody Map<String, Object> params);

    /**
     * 读取插件真实下载次数（聚合值，免登录）。
     */
    @PostMapping("/base/plugin-center/market/download/stats")
    ResponseResult marketDownloadStats(@RequestBody(required = false) Map<String, Object> params);

    /**
     * 平台对外市场目录（2026-10-29）：别的安装人的站点实时从这里读插件商品。
     * 免平台登录态——插件商品是公开信息，站点私有状态由站点自己合并。
     */
    @PostMapping("/base/plugin-center/market/remote/catalog")
    ResponseResult marketRemoteCatalog();

    /**
     * 站点换票安装包（2026-10-30）：站点市场在线安装走平台制品分发，
     * X-Platform-Token 在 base 侧 fail-closed 校验，令牌绑站点、10 分钟限次。
     */
    @PostMapping("/base/plugin-center/market/remote/acquire")
    ResponseResult marketRemoteAcquire(@RequestHeader(value = "X-Platform-Token", required = false) String token,
                                       @RequestBody(required = false) Map<String, Object> params);

    /**
     * 站点购买授权上报（2026-10-30）：站点发证后异步上报授权副本，
     * 平台按 sourceOrderNo 幂等落库；与 acquire 同令牌门。
     */
    @PostMapping("/base/plugin-center/market/remote/license/report")
    ResponseResult marketRemoteLicenseReport(@RequestHeader(value = "X-Platform-Token", required = false) String token,
                                             @RequestBody(required = false) Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/license/activate")
    ResponseResult marketLicenseActivate(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/license/status")
    ResponseResult marketLicenseStatus(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/license/mine")
    ResponseResult marketLicenseMine(@RequestBody(required = false) Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/license/unbind")
    ResponseResult marketLicenseUnbind(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/order/create")
    ResponseResult marketOrderCreate(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/order/pay")
    ResponseResult marketOrderPay(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/order/sync")
    ResponseResult marketOrderSync(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/order/detail")
    ResponseResult marketOrderDetail(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/order/cancel")
    ResponseResult marketOrderCancel(@RequestBody Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/order/mine")
    ResponseResult marketOrderMine(@RequestBody(required = false) Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/pay/config")
    ResponseResult marketPayConfig(@RequestBody(required = false) Map<String, Object> params);

    @PostMapping("/base/plugin-center/market/pay/sandbox/confirm")
    ResponseResult marketPaySandboxConfirm(@RequestBody(required = false) Map<String, Object> params);

    @PostMapping(value = "/base/plugin-center/market/pay/notify/{channel}", consumes = MediaType.ALL_VALUE)
    String marketPayNotify(@PathVariable("channel") String channel,
                           @RequestParam Map<String, String> params,
                           @RequestBody(required = false) String body);

    @PostMapping("/base/plugin-center/market/pay/admin/view")
    ResponseResult marketPayAdminView(@RequestBody(required = false) Map<String, Object> params,
                                      @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                      String adminToken);

    @PostMapping("/base/plugin-center/market/pay/admin/save")
    ResponseResult marketPayAdminSave(@RequestBody(required = false) Map<String, Object> params,
                                      @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                      String adminToken);

    @PostMapping("/base/plugin-center/market/pay/admin/delete")
    ResponseResult marketPayAdminDelete(@RequestBody(required = false) Map<String, Object> params,
                                        @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                        String adminToken);

    @PostMapping("/base/plugin-center/market/pay/admin/reload")
    ResponseResult marketPayAdminReload(@RequestBody(required = false) Map<String, Object> params,
                                        @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                        String adminToken);
    @PostMapping("/base/plugin-center/service/delivery")
    ResponseResult serviceDelivery(@RequestBody Map<String, Object> params);
    @PostMapping("/base/plugin-center/service/install")
    ResponseResult serviceInstall(@RequestBody Map<String, Object> params);
    @PostMapping("/base/plugin-center/service/upgrade")
    ResponseResult serviceUpgrade(@RequestBody Map<String, Object> params);
    @PostMapping("/base/plugin-center/service/task/status")
    ResponseResult serviceTaskStatus(@RequestBody Map<String, Object> params);
    @PostMapping("/base/plugin-center/service/history")
    ResponseResult serviceHistory();
}
