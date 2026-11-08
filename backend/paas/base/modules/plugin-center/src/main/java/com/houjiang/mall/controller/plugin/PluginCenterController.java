package com.houjiang.mall.controller.plugin;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.util.PluginManageGate;
import com.houjiang.commons.util.SiteInstanceResolver;
import com.houjiang.plugin.api.host.PluginHostApi;
import com.houjiang.mall.model.plugin.PluginCenterItem;
import com.houjiang.mall.service.plugin.PluginCenterService;
import com.houjiang.mall.service.plugin.PluginScaffoldService;
import com.houjiang.mall.service.plugin.StandalonePluginCodes;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 平台插件中心。
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/plugin-center")
public class PluginCenterController {

    // 独立服务编码统一取 StandalonePluginCodes（单一事实来源），不再在本控制器另存一份。
    private static final Set<String> STANDALONE_SERVICES = StandalonePluginCodes.CODES;

    // 插件管理权限码不再本地定义：统一取 PluginManageGate 的常量，
    // 避免「同一套安全判定抄三份」时改一处漏两处。

    private final PluginCenterService pluginCenterService;
    private final PluginLifecycleManager lifecycleManager;
    private final PluginHostApi pluginHostApi;
    private final PluginScaffoldService pluginScaffoldService;

    /**
     * 高危动作是否强制校验管理权限（默认 <b>开</b>，理由见 {@link #manageGate()}）。
     */
    @org.springframework.beans.factory.annotation.Value("${houcloud.plugin.require-manage-authority:true}")
    private boolean requireManageAuthority;

    /** 创建插件：真实生成 legacy/architecture-before-2026-09-20/plugin-services/{code}-service 骨架并写目录/市场库。 */
    @PostMapping("/create")
    public ResponseResult create(@RequestBody Map<String, Object> params) {
        ResponseResult denied = manageGate();
        if (denied != null) {
            return denied;
        }
        try {
            return ResponseResult.success(pluginScaffoldService.createPlugin(params));
        } catch (IllegalArgumentException e) {
            log.warn("创建插件参数不合法: {}", e.getMessage());
            return ResponseResult.fail(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("创建插件冲突: {}", e.getMessage());
            return ResponseResult.fail(409, e.getMessage());
        } catch (Exception e) {
            log.error("创建插件失败", e);
            return ResponseResult.fail(500, "创建插件失败: " + e.getMessage());
        }
    }

    @PostMapping("/list")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> params) {
        try {
            // instanceId 允许缺省：旧版前端未传时按不过滤站点授权兜底，避免整页插件列表为空。
            return ResponseResult.success(pluginCenterService.listPluginsWithCategories(params));
        } catch (Exception e) {
            log.error("查询插件列表失败", e);
            return ResponseResult.fail(500, "查询插件列表失败: " + e.getMessage());
        }
    }

    /** 登录用户共用的分类字典，不要求插件管理权限。 */
    @PostMapping("/categories")
    public ResponseResult categories() {
        if (!SecurityContextUtils.isAuthenticated()) return ResponseResult.fail(401, "请先登录");
        return ResponseResult.success(pluginCenterService.listCategories());
    }

    /** permission 菜单管理调用；仍使用原调用者的精确菜单编辑权限。 */
    @PostMapping("/catalog/category/update")
    public ResponseResult updateCategory(@RequestBody Map<String, Object> params) {
        if (!SecurityContextUtils.isAuthenticated()) return ResponseResult.fail(401, "请先登录");
        if (!SecurityContextUtils.hasPermission("shell:setting:permission:menu:edit")) {
            return ResponseResult.fail(403, "缺少菜单编辑权限");
        }
        try {
            Object cateId = params == null ? null : params.get("cateId");
            if (cateId == null) throw new IllegalArgumentException("分类ID不能为空");
            pluginCenterService.updateCategory(requiredCode(params), Integer.parseInt(cateId.toString()));
            return ResponseResult.success();
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(400, e.getMessage());
        }
    }

    @PostMapping("/summary")
    public ResponseResult summary() {
        try {
            return ResponseResult.success(pluginCenterService.summary());
        } catch (Exception e) {
            log.error("查询插件概览失败", e);
            return ResponseResult.fail(500, "查询插件概览失败: " + e.getMessage());
        }
    }

    /** 返回当前站点的插件详情、定价与购买状态。 */
    @PostMapping("/detail")
    public ResponseResult detail(@RequestBody Map<String, Object> params) {
        try {
            String instanceId = requiredInstance(params);
            return ResponseResult.success(pluginCenterService.detail(requiredCode(params), instanceId));
        } catch (Exception e) {
            log.error("查询插件详情失败", e);
            return ResponseResult.fail(500, "查询插件详情失败: " + e.getMessage());
        }
    }

    /**
     * 按当前站点实例的有效授权安装插件。
     *
     * <p><b>提示</b>：本接口是「写状态位 + 补菜单」的轻量链路，<b>不做下载、校验、建表、依赖检查</b>。
     * 完整能力在 {@code /base/plugin-center/update/install}（8 步流水线），新接入方请用后者。
     */
    @PostMapping("/install")
    public ResponseResult install(@RequestBody Map<String, Object> params) {
        ResponseResult denied = manageGate();
        if (denied != null) {
            return denied;
        }
        try {
            String code = requiredCode(params);
            if (isStandalone(code)) return ResponseResult.fail(409, "独立服务必须通过 /base/plugin-center/service/install 安装，禁止走 PF4J 插件链路");
            String instanceId = requiredInstance(params);
            return pluginCenterService.install(code, instanceId)
                    ? ResponseResult.success("安装成功")
                    : ResponseResult.fail(500, "安装失败");
        } catch (Exception e) {
            log.error("安装插件失败", e);
            return ResponseResult.fail(500, "安装插件失败: " + e.getMessage());
        }
    }

    /**
     * 卸载插件（轻量链路）。
     *
     * <p><b>提示</b>：本接口不做版本目录回收、迁移审计清理与业务数据归档。
     * 完整卸载编排见 {@code /base/plugin-center/update/uninstall}。
     */
    @PostMapping("/uninstall")
    public ResponseResult uninstall(@RequestBody Map<String, Object> params) {
        ResponseResult denied = manageGate();
        if (denied != null) {
            return denied;
        }
        try {
            // instanceId 由服务端按登录态解析：卸载后的授权回收与凭证清理都必须按站点收敛，
            // 让前端传会把「清谁的授权」交到请求方手里。
            String code = requiredCode(params);
            if (isStandalone(code)) return ResponseResult.fail(409, "独立服务必须由安装代理卸载，禁止走 PF4J 插件链路");
            String instanceId = SiteInstanceResolver.resolveAndOverrideOrNull(params);
            return pluginCenterService.uninstall(code, instanceId)
                    ? ResponseResult.success("卸载成功")
                    : ResponseResult.fail(500, "卸载失败");
        } catch (Exception e) {
            log.error("卸载插件失败", e);
            return ResponseResult.fail(500, "卸载插件失败: " + e.getMessage());
        }
    }

    @PostMapping("/toggle")
    public ResponseResult toggle(@RequestBody Map<String, Object> params) {
        try {
            String code = requiredCode(params);
            if (isStandalone(code)) return ResponseResult.fail(409, "独立服务的启停由容器编排和安装代理管理");
            boolean enabled = Boolean.parseBoolean(String.valueOf(params.getOrDefault("enabled", false)));
            // instanceId 改为服务端解析：原先直接读请求体，传别人的实例就能绕过「启用前的授权校验」，
            // 免费启用本站点未授权的付费插件。保留可空——旧版前端未传时仍按本站点库内授权校验。
            String instanceText = SiteInstanceResolver.resolveAndOverrideOrNull(params);
            return pluginCenterService.toggle(code, enabled, instanceText)
                    ? ResponseResult.success("操作成功")
                    : ResponseResult.fail(500, "操作失败");
        } catch (Exception e) {
            log.error("切换插件状态失败", e);
            return ResponseResult.fail(500, "切换插件状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/config/save")
    public ResponseResult saveConfig(@RequestBody Map<String, Object> params) {
        try {
            String configJson = String.valueOf(params.getOrDefault("configJson", "{}"));
            return pluginCenterService.saveConfig(requiredCode(params), configJson)
                    ? ResponseResult.success("保存成功")
                    : ResponseResult.fail(500, "保存失败");
        } catch (Exception e) {
            log.error("保存插件配置失败", e);
            return ResponseResult.fail(500, "保存插件配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/health")
    public ResponseResult health() {
        try {
            List<PluginCenterItem> items = pluginCenterService.listPlugins(Map.of());
            List<Map<String, Object>> health = items.stream().map(item -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("code", item.getCode());
                m.put("installed", item.getInstalled());
                m.put("enabled", item.getEnabled());
                m.put("healthy", healthy(item));
                return m;
            }).toList();
            return ResponseResult.success(health);
        } catch (Exception e) {
            log.error("查询插件健康状态失败", e);
            return ResponseResult.fail(500, "查询插件健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 按运行形态判定健康。
     *
     * <p>内嵌扩展看宿主进程里的 PF4J 运行态；独立服务（独立容器）不跑 PF4J，
     * 问 PF4J 只会永远得到「不健康」。独立服务的运行态由安装回执
     * （{@code hou_service_installation}）承载：回执在且状态为运行才算健康。
     *
     * <p>拿不到运行态证据时返回 false，不猜「健康」——把未知说成健康会掩盖真实故障。
     */
    private boolean healthy(PluginCenterItem item) {
        if (!item.getInstalled()) {
            return false;
        }
        if (isStandalone(item.getCode())) {
            return pluginCenterService.isServiceRunning(item.getCode());
        }
        return lifecycleManager.isHealthy(item.getCode());
    }

    @PostMapping("/capabilities")
    public ResponseResult capabilities() {
        try {
            return ResponseResult.success(pluginHostApi.activeCapabilities());
        } catch (Exception e) {
            log.error("查询插件能力失败", e);
            return ResponseResult.fail(500, "查询插件能力失败: " + e.getMessage());
        }
    }

    private static boolean isStandalone(String code) { return STANDALONE_SERVICES.contains(code); }

    private String requiredCode(Map<String, Object> params) {
        Object code = params == null ? null : params.get("pluginCode");
        if (code == null) {
            code = params == null ? null : params.get("code");
        }
        if (code == null || code.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("插件编码不能为空");
        }
        return code.toString().trim();
    }

    /**
     * 取站点实例标识（<b>服务端解析，不信任请求体</b>）。
     *
     * <p>旧实现直接读请求体里的 {@code instanceId}：那是<b>越权口子</b>——
     * 任何登录用户都能把请求伪造成别人的站点，用别人的授权去装付费插件，
     * 或者把安装记录写进别人站点造成数据污染。因此这里与 plugin-artifact /
     * plugin-update 保持一致：请求体里带身份字段一律丢弃并记 warn，
     * 实际身份从登录态解析（{@code orgId}，缺失时退化为 {@code u-{userId}}）。
     *
     * <p><b>为什么不是「有传就用、没传才解析」</b>：那种兼容写法等于漏洞还在，
     * 只是加了个默认值。身份归属类字段必须单一事实来源。
     *
     * @param params 请求体
     * @return 服务端解析出的站点实例标识
     * @throws IllegalArgumentException 未登录或无法识别站点时抛出
     */
    private String requiredInstance(Map<String, Object> params) {
        // 与 plugin-market / plugin-license 共用同一套服务端解析口径，避免三处各写一份慢慢长歪。
        return SiteInstanceResolver.requireAndOverride(params);
    }

    /**
     * 从登录态解析站点实例：优先 {@code orgId}（一个组织 = 一个站点实例），
     * 组织为空时退化为 {@code u-{userId}} 保证非空——空标识会让授权查询退化成「不限实例」。
     *
     * <p>实现已上移到 {@link SiteInstanceResolver}（commons），插件市场 / 授权 / 订单与本处共用；
     * 该方法仅保留为私有入口，避免改动散落各处的调用点。
     *
     * @return 站点实例标识；未登录返回 null
     */
    private static String resolveServerSideInstanceId() {
        return SiteInstanceResolver.resolveOrNull();
    }

    /**
     * 高危动作的授权闸门（受 {@code houcloud.plugin.require-manage-authority} 控制）。
     *
     * <p>判定实现收口在 {@link PluginManageGate}，与插件更新、插件市场共用同一份——
     * 三处各写一份时出现了「只锁了一扇门」的漏洞（全检 B5），故不再本地实现。
     *
     * @return 通过返回 null；被拦截时返回可直接响应的失败结果
     */
    private ResponseResult manageGate() {
        return PluginManageGate.check(requireManageAuthority);
    }
}
