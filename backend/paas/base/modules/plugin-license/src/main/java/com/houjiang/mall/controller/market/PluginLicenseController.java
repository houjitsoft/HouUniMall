package com.houjiang.mall.controller.market;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.util.SiteInstanceResolver;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.service.market.PluginLicenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 插件授权（{@code /base/plugin-center/market/license/**}）。
 *
 * <p><b>暴露面说明（重要）</b>：
 * <ul>
 *   <li><b>租户侧</b>（经 BFF 转发给 Cloud 后台）：{@code /activate}、{@code /status}、
 *       {@code /mine}（本站点已授权列表）、{@code /unbind}（解绑本站点绑定）。
 *       <b>一律不信任请求体里的 {@code instanceId}</b>，改用
 *       {@link SiteInstanceResolver#requireAndOverride(Map)} 从登录态解析并在服务端覆写，
 *       否则任何登录用户都能伪造成别人的站点、把他人未绑定的授权码据为己有（F-19）。</li>
 *   <li><b>平台侧</b>：{@code /issue}（收款后发证）与 {@code /revoke}（吊销），
 *       <b>不对外暴露</b>，只允许运维或后续「平台管理端」经内网调用；{@code /list} 同理。
 *       这三个接口<b>默认 fail-closed</b>（无 {@code shell:plugin:license:admin} 权限直接 403）：
 *       已核实前端全库无任何 {@code license/issue|license/list|license/revoke} 调用，
 *       BFF 的 {@code MallServiceFeign} 也只暴露 activate/status/mine/unbind 四个租户接口，
 *       即没有合法调用方，收紧不会打断业务。</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/plugin-center/market/license")
public class PluginLicenseController {

    /** 平台侧接口的权限码 */
    private static final String LICENSE_ADMIN_PERMISSION = "shell:plugin:license:admin";

    private final PluginLicenseService pluginLicenseService;

    /**
     * 平台侧接口是否要求 {@code shell:plugin:license:admin} 权限。默认 {@code true}（fail-closed）：
     * 这三个接口零合法调用方，宁可拒绝也不留口子；确需内网运维直连时显式置 false。
     */
    @Value("${houcloud.plugin.require-license-admin-authority:true}")
    private boolean requireLicenseAdminAuthority;

    /**
     * 平台侧闸门：放行返回 {@code null}，拒绝返回 403 结果。
     */
    private ResponseResult requireLicenseAdmin() {
        if (!requireLicenseAdminAuthority) {
            return null;
        }
        if (!SecurityContextUtils.hasPermission(LICENSE_ADMIN_PERMISSION)) {
            log.warn("拒绝平台侧插件授权接口调用：缺少权限 {}", LICENSE_ADMIN_PERMISSION);
            return ResponseResult.fail(403, "无权操作平台侧插件授权接口");
        }
        return null;
    }

    /**
     * 签发授权（平台侧）：线下收款后为某插件签发授权码。
     *
     * <p>参数：{@code pluginCode}（必填）、{@code buyerId}、{@code instanceId}、
     * {@code licenseType}（PAID/TRIAL，默认 PAID）、{@code validDays}（为空或 &lt;=0 表示永久）。
     */
    @PostMapping("/issue")
    public ResponseResult issue(@RequestBody Map<String, Object> params) {
        ResponseResult denied = requireLicenseAdmin();
        if (denied != null) {
            return denied;
        }
        try {
            String pluginCode = requiredText(params, "插件编码不能为空", "pluginCode", "code");
            return ResponseResult.success(toView(pluginLicenseService.issue(
                    pluginCode,
                    longValue(params.get("buyerId")),
                    text(params.get("instanceId")),
                    text(params.get("licenseType")),
                    intValue(params.get("validDays")))));
        } catch (Exception e) {
            log.error("签发插件授权失败", e);
            return ResponseResult.fail(500, "签发插件授权失败: " + e.getMessage());
        }
    }

    /**
     * 授权列表（平台侧排障）：按插件查看全部授权记录（含已吊销/已过期）。
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> params) {
        ResponseResult denied = requireLicenseAdmin();
        if (denied != null) {
            return denied;
        }
        try {
            String pluginCode = params == null ? null : text(params.get("pluginCode"));
            return ResponseResult.success(pluginLicenseService.listByPlugin(pluginCode));
        } catch (Exception e) {
            log.error("查询插件授权列表失败", e);
            return ResponseResult.fail(500, "查询插件授权列表失败: " + e.getMessage());
        }
    }

    /**
     * 吊销授权（平台侧）。
     */
    @PostMapping("/revoke")
    public ResponseResult revoke(@RequestBody Map<String, Object> params) {
        ResponseResult denied = requireLicenseAdmin();
        if (denied != null) {
            return denied;
        }
        try {
            pluginLicenseService.revoke(requiredText(params, "授权码不能为空", "licenseKey", "key"));
            return ResponseResult.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("吊销插件授权失败", e);
            return ResponseResult.fail(500, "吊销插件授权失败: " + e.getMessage());
        }
    }

    /**
     * 激活授权（租户侧，经 BFF 暴露）：凭授权码激活，可选绑定本站点实例。
     *
     * <p>参数：{@code licenseKey}（必填）、{@code pluginCode}（可选）。
     * {@code instanceId} 由服务端按登录态解析，不再取请求体。
     */
    @PostMapping("/activate")
    public ResponseResult activate(@RequestBody Map<String, Object> params) {
        try {
            String licenseKey = requiredText(params, "授权码不能为空", "licenseKey", "key");
            // 先 override 再取值：请求体里的 instanceId 一律不可信，必须换成服务端解析结果
            String instanceId = SiteInstanceResolver.requireAndOverride(params);
            // 在线购买签发的授权带唯一买方：激活时用登录态的组织 ID 做归属校验，
            // 防止泄漏的授权码被别的站点抢先绑定（线下手工发码无来源订单，不受此限）。
            Long buyerId = SiteInstanceResolver.resolveBuyerIdOrNull();
            return ResponseResult.success(toView(pluginLicenseService.activate(
                    text(params.get("pluginCode")),
                    licenseKey,
                    instanceId,
                    buyerId)));
        } catch (Exception e) {
            log.error("激活插件授权失败", e);
            return ResponseResult.fail(500, "激活插件授权失败: " + e.getMessage());
        }
    }

    /**
     * 授权状态（租户侧，经 BFF 暴露）：该插件本站点是否已授权、到期时间等。
     */
    @PostMapping("/status")
    public ResponseResult status(@RequestBody Map<String, Object> params) {
        try {
            String pluginCode = requiredText(params, "插件编码不能为空", "pluginCode", "code");
            String instanceId = SiteInstanceResolver.requireAndOverride(params);
            return ResponseResult.success(pluginLicenseService.statusOf(pluginCode, instanceId));
        } catch (Exception e) {
            log.error("查询插件授权状态失败", e);
            return ResponseResult.fail(500, "查询插件授权状态失败: " + e.getMessage());
        }
    }

    /**
     * 本站点已授权列表（租户侧，经 BFF 暴露）：只返回绑定了本实例的授权，用于「授权管理」页。
     *
     * <p>身份由服务端解析（{@link SiteInstanceResolver}），请求体里的 {@code instanceId} 会被忽略；
     * 解析不出站点实例时直接报错，不退化成「不限实例」查询。
     */
    @PostMapping("/mine")
    public ResponseResult mine(@RequestBody(required = false) Map<String, Object> params) {
        try {
            String instanceId = SiteInstanceResolver.requireAndOverride(params);
            List<Map<String, Object>> view = pluginLicenseService.listMine(instanceId)
                    .stream().map(this::toView).collect(Collectors.toList());
            return ResponseResult.success(view);
        } catch (Exception e) {
            log.error("查询本站点授权列表失败", e);
            return ResponseResult.fail(500, "查询本站点授权列表失败: " + e.getMessage());
        }
    }

    /**
     * 解绑授权（租户侧，经 BFF 暴露）：释放本实例与授权码的绑定，不清空授权本身。
     *
     * <p>参数：{@code licenseKey}（必填）、{@code instanceId}（必填，须与当前绑定实例一致）。
     * 插件仍处于安装状态时会拒绝解绑。
     */
    @PostMapping("/unbind")
    public ResponseResult unbind(@RequestBody Map<String, Object> params) {
        try {
            String licenseKey = requiredText(params, "授权码不能为空", "licenseKey", "key");
            String instanceId = SiteInstanceResolver.requireAndOverride(params);
            return ResponseResult.success(toView(pluginLicenseService.unbind(licenseKey, instanceId)));
        } catch (Exception e) {
            log.error("解绑插件授权失败", e);
            return ResponseResult.fail(500, "解绑插件授权失败: " + e.getMessage());
        }
    }

    private Map<String, Object> toView(MarketPluginLicense license) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pluginCode", license.getPluginCode());
        view.put("licenseKey", license.getLicenseKey());
        view.put("licenseType", license.getLicenseType());
        view.put("buyerId", license.getBuyerId());
        view.put("instanceId", license.getInstanceId());
        view.put("maxInstances", license.getMaxInstances());
        view.put("maintenanceUntil", license.getMaintenanceUntil());
        view.put("maintenanceActive", com.houjiang.mall.service.market.OfficialMaintenancePolicy.active(license, java.time.LocalDateTime.now()));
        view.put("expireTime", license.getExpireTime() == null ? null : license.getExpireTime().toString());
        view.put("permanent", license.getExpireTime() == null);
        view.put("status", license.getStatus());
        return view;
    }

    private String requiredText(Map<String, Object> params, String message, String... keys) {
        String value = firstText(params, keys);
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String firstText(Map<String, Object> params, String... keys) {
        if (params == null) {
            return null;
        }
        for (String key : keys) {
            String value = text(params.get(key));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private Long longValue(Object value) {
        String text = text(value);
        if (text == null) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数值格式不正确: " + text);
        }
    }

    private Integer intValue(Object value) {
        String text = text(value);
        if (text == null) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数值格式不正确: " + text);
        }
    }
}
