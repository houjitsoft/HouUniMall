package com.houjiang.bffweb.controller.plugincenter;

import com.houjiang.bffweb.feign.PluginCenterServiceFeign;
import com.houjiang.bffweb.service.AgreementConsentService;
import com.houjiang.bffweb.controller.WebAgreementController;
import com.houjiang.bffweb.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件市场（Cloud 后台）。
 *
 * <p>只做转发：请求经 Feign 打到 base 的 {@code /base/plugin-center/market/**}。
 * 与插件中心（{@code /web/plugin-center/**}）并列——前者是「市场/分发」，
 * 后者是「本站点已装插件的管理」。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/web/plugin-center/market")
@Tag(name = "插件市场", description = "第三方插件的浏览、定价与在线安装")
public class PluginMarketController {

    private final PluginCenterServiceFeign pluginCenterServiceFeign;
    private final AgreementConsentService agreementConsentService;

    @PostMapping("/list")
    @Operation(summary = "市场插件列表（含分类）")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> params) {
        return pluginCenterServiceFeign.marketList(params == null ? Map.of() : params);
    }

    @PostMapping("/categories")
    @Operation(summary = "市场分类")
    public ResponseResult categories() {
        return pluginCenterServiceFeign.marketCategories();
    }

    @PostMapping("/detail")
    @Operation(summary = "市场插件详情（含版本）")
    public ResponseResult detail(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketDetail(params == null ? Map.of() : params);
    }

    /**
     * 租户视角的已发布版本列表。
     *
     * <p>为什么单独开一个接口：{@code /detail} 只在详情里带一版摘要，
     * 市场详情页要列出「可装的其它历史版本」时拿不到全量，前端只能拿到什么显示什么。
     * 这里按 SemVer 降序返回该插件的全部已发布版本，并标出与当前框架版本不兼容的项。
     */
    @PostMapping("/versions")
    @Operation(summary = "市场插件的已发布版本列表（SemVer 降序 + 兼容性标记）")
    public ResponseResult versions(@RequestBody(required = false) Map<String, Object> params) {
        return pluginCenterServiceFeign.marketVersions(params == null ? Map.of() : params);
    }

    /**
     * 目录「有更新」判定（P0-10 / M5）：插件中心用它在「已装旧版」上显示「有更新」。
     */
    @PostMapping("/catalog/updates")
    @Operation(summary = "批量判定已装插件是否有更新（SemVer 比较 + 授权/框架门控）")
    public ResponseResult catalogUpdates(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketCatalogUpdates(params == null ? Map.of() : params);
    }

    @PostMapping("/install")
    @Operation(summary = "在线安装市场插件（下载 → 校验 → 落盘 → 登记安装态）")
    public ResponseResult install(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketInstall(params == null ? Map.of() : params);
    }

    /**
     * 站点上报一次插件下载（免登录）。
     *
     * <p>调用方是<b>别的安装人</b>的站点后端：它没有本平台登录态，只带共享令牌
     * {@code X-Platform-Token}（令牌在 base 侧 fail-closed 校验）。这条链路与
     * 「平台自己人查市场」方向相反，别混。
     */
    @PostMapping("/download/report")
    @Operation(summary = "站点上报插件下载次数（免登录，令牌鉴权在 base 侧）")
    public ResponseResult downloadReport(@RequestBody(required = false) Map<String, Object> params,
                                         HttpServletRequest request) {
        String token = request == null ? null : request.getHeader("X-Platform-Token");
        if (token == null || token.isBlank()) {
            return ResponseResult.fail(401, "缺少站点上报令牌");
        }
        return pluginCenterServiceFeign.marketDownloadReport(token.trim(),
                params == null ? Map.of() : params);
    }

    /**
     * 读取插件真实下载次数（聚合值，免登录）。
     *
     * <p>别的安装人据此把「全网真实下载次数」展示在自己站点里；只返回次数与站点数，
     * 不含站点标识明细。
     */
    @PostMapping("/download/stats")
    @Operation(summary = "读取插件真实下载次数（聚合值，免登录）")
    public ResponseResult downloadStats(@RequestBody(required = false) Map<String, Object> params) {
        return pluginCenterServiceFeign.marketDownloadStats(params == null ? Map.of() : params);
    }

    /**
     * 平台对外市场目录（2026-10-29 架构口径，免登录）。
     *
     * <p>调用方是「别的安装人」的站点后端：开源包本地只有已安装插件的表、
     * 不带插件商品快照，站点市场页的插件商品实时从本端点读取（平台上新/改价/下架，
     * 所有站点的市场页立即生效）。只回公开插件商品信息，站点私有状态由站点合并。</p>
     */
    @PostMapping("/remote/catalog")
    @Operation(summary = "平台对外市场目录（全量在架插件商品+分类+套餐+版本，免登录）")
    public ResponseResult remoteCatalog() {
        return pluginCenterServiceFeign.marketRemoteCatalog();
    }

    /**
     * 站点换票插件安装包（2026-10-30，免登录）。
     *
     * <p>调用方是「别的安装人」的站点后端：它没有本平台登录态，只带共享令牌
     * {@code X-Platform-Token}（令牌在 base 侧 fail-closed 校验）。插件商品 /
     * 版本审核 / 制品存在的权威判定全在平台侧；站点经本端点换到限时令牌后，
     * 从既有 {@code /web/plugin-center/artifact/raw} 流式拉包。</p>
     */
    @PostMapping("/remote/acquire")
    @Operation(summary = "站点换取插件安装包下载令牌（免登录，令牌鉴权在 base 侧）")
    public ResponseResult remoteAcquire(@RequestBody(required = false) Map<String, Object> params,
                                        HttpServletRequest request) {
        String token = request == null ? null : request.getHeader("X-Platform-Token");
        if (token == null || token.isBlank()) {
            return ResponseResult.fail(401, "缺少站点令牌");
        }
        return pluginCenterServiceFeign.marketRemoteAcquire(token.trim(),
                params == null ? Map.of() : params);
    }

    /**
     * 站点购买授权上报（2026-10-30，免登录）：站点在线购买付费插件发证后异步上报，
     * 平台落授权副本——acquire 换票按 (pluginCode, siteCode) 校验副本放行付费插件，
     * 打通「站点购买 → 平台派发」闭环。与 remote/acquire 同一道门：
     * 共享令牌在 BFF 透传、base 侧 fail-closed 校验。
     */
    @PostMapping("/remote/license/report")
    @Operation(summary = "站点购买授权上报平台（免登录，令牌鉴权在 base 侧）")
    public ResponseResult remoteLicenseReport(@RequestBody(required = false) Map<String, Object> params,
                                              HttpServletRequest request) {
        String token = request == null ? null : request.getHeader("X-Platform-Token");
        if (token == null || token.isBlank()) {
            return ResponseResult.fail(401, "缺少站点令牌");
        }
        return pluginCenterServiceFeign.marketRemoteLicenseReport(token.trim(),
                params == null ? Map.of() : params);
    }

    @PostMapping("/license/activate")
    @Operation(summary = "激活付费插件授权（凭授权码，可选绑定站点实例）")
    public ResponseResult licenseActivate(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketLicenseActivate(params == null ? Map.of() : params);
    }

    @PostMapping("/license/status")
    @Operation(summary = "查询本站点某插件的授权状态")
    public ResponseResult licenseStatus(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketLicenseStatus(params == null ? Map.of() : params);
    }

    @PostMapping("/license/mine")
    @Operation(summary = "本站点已激活授权列表（授权管理页）")
    public ResponseResult licenseMine(@RequestBody(required = false) Map<String, Object> params) {
        return pluginCenterServiceFeign.marketLicenseMine(params == null ? Map.of() : params);
    }

    @PostMapping("/license/unbind")
    @Operation(summary = "解绑本站点授权（释放实例绑定，授权本身仍有效）")
    public ResponseResult licenseUnbind(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketLicenseUnbind(params == null ? Map.of() : params);
    }

    // ---------- 在线购买：插件订单 ----------

    @PostMapping("/order/create")
    @Operation(summary = "插件下单（生成待支付订单）")
    public ResponseResult orderCreate(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> body = params == null ? new HashMap<>() : new HashMap<>(params);
        String token = WebAgreementController.resolveToken(request);
        String userId = agreementConsentService.resolveUserId(token);
        if (userId == null || userId.isEmpty()) {
            return ResponseResult.fail(401, "登录状态已失效，请重新登录");
        }
        AgreementConsentService.ValidateResult consent = agreementConsentService.validateAndRecordPluginPurchase(
                userId,
                agreementConsentService.resolveUsername(token),
                body.get("agreements"),
                WebAgreementController.getClientIp(request),
                request.getHeader("User-Agent"),
                "web");
        if (!consent.isOk()) {
            return ResponseResult.error(consent.getMessage());
        }
        // 同意明细只给 BFF 留痕，不透传给 base 下单接口
        body.remove("agreements");
        return pluginCenterServiceFeign.marketOrderCreate(body);
    }

    @PostMapping("/order/pay")
    @Operation(summary = "发起插件订单支付（经商城收银台获取支付参数）")
    public ResponseResult orderPay(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketOrderPay(params == null ? Map.of() : params);
    }

    @PostMapping("/order/sync")
    @Operation(summary = "同步订单支付状态，成功则自动签发授权")
    public ResponseResult orderSync(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketOrderSync(params == null ? Map.of() : params);
    }

    @PostMapping("/order/detail")
    @Operation(summary = "插件订单详情")
    public ResponseResult orderDetail(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketOrderDetail(params == null ? Map.of() : params);
    }

    @PostMapping("/order/cancel")
    @Operation(summary = "取消待支付插件订单")
    public ResponseResult orderCancel(@RequestBody Map<String, Object> params) {
        return pluginCenterServiceFeign.marketOrderCancel(params == null ? Map.of() : params);
    }

    @PostMapping("/order/mine")
    @Operation(summary = "本站点插件订单列表")
    public ResponseResult orderMine(@RequestBody(required = false) Map<String, Object> params) {
        return pluginCenterServiceFeign.marketOrderMine(params == null ? Map.of() : params);
    }

    // ---------- 收费插件支付：配置自检 / 沙箱 / 渠道回调 ----------

    @PostMapping("/pay/config")
    @Operation(summary = "支付配置自检与价格套餐（不下发任何密钥）")
    public ResponseResult payConfig(@RequestBody(required = false) Map<String, Object> params) {
        return pluginCenterServiceFeign.marketPayConfig(params == null ? Map.of() : params);
    }

    @PostMapping("/pay/sandbox/confirm")
    @Operation(summary = "沙箱模拟支付（仅 pay.sandbox=true 可用，生产应关闭）")
    public ResponseResult sandboxConfirm(@RequestBody(required = false) Map<String, Object> params) {
        return pluginCenterServiceFeign.marketPaySandboxConfirm(params == null ? Map.of() : params);
    }

    /**
     * 支付渠道回调：<b>免登录</b>，且必须<b>原样透传</b>报文。
     *
     * <p>免登录的原因：渠道服务器不带本站 Token，安全性由回调验签保证（见 base 侧
     * {@code PluginPayChannel#parseAndVerify}）。
     * 原样透传的原因：签名是按原始报文算的，重新编码会导致验签失败。
     */
    @PostMapping(value = "/pay/notify/{channel}",
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    @Operation(summary = "支付渠道结果回调（免登录，靠验签准入）")
    public String payNotify(@PathVariable("channel") String channel,
                            @RequestParam(required = false) Map<String, String> params,
                            @RequestBody(required = false) String body) {
        try {
            return pluginCenterServiceFeign.marketPayNotify(channel,
                    params == null ? Map.of() : params, body);
        } catch (RuntimeException e) {
            log.error("支付回调转发失败: channel={}", channel, e);
            // 回失败：让渠道保留重发，避免这笔支付结果丢失
            return "fail:" + e.getMessage();
        }
    }

    // ---------- 支付配置管理（运营侧：需登录 + 管理令牌） ----------
    // 令牌在请求头 X-Pay-Admin-Token 里，BFF 原样透传，准入判断在 base 侧做——
    // 网关层不做权限判断，避免「BFF 说通过、mall 说不通过」两套口径。

    @PostMapping("/pay/admin/view")
    @Operation(summary = "支付配置总览：生效值（脱敏）+ 库中原始行 + 待办问题 + 可用键清单")
    public ResponseResult payAdminView(@RequestBody(required = false) Map<String, Object> params,
                                       @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                       String adminToken) {
        return pluginCenterServiceFeign.marketPayAdminView(params == null ? Map.of() : params, adminToken);
    }

    @PostMapping("/pay/admin/save")
    @Operation(summary = "新增或更新一条支付配置（密钥必须填 ${环境变量名}，明文拒绝落库）")
    public ResponseResult payAdminSave(@RequestBody(required = false) Map<String, Object> params,
                                       @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                       String adminToken) {
        return pluginCenterServiceFeign.marketPayAdminSave(params == null ? Map.of() : params, adminToken);
    }

    @PostMapping("/pay/admin/delete")
    @Operation(summary = "删除一条支付配置，回落到 Nacos/yml 基线")
    public ResponseResult payAdminDelete(@RequestBody(required = false) Map<String, Object> params,
                                         @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                         String adminToken) {
        return pluginCenterServiceFeign.marketPayAdminDelete(params == null ? Map.of() : params, adminToken);
    }

    @PostMapping("/pay/admin/reload")
    @Operation(summary = "重新加载支付配置（建表后首次使用时调用，无需重启）")
    public ResponseResult payAdminReload(@RequestBody(required = false) Map<String, Object> params,
                                         @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                         String adminToken) {
        return pluginCenterServiceFeign.marketPayAdminReload(params == null ? Map.of() : params, adminToken);
    }
}
