package com.houjiang.plugin.update.controller;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.PluginManageGate;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.identity.PluginIdentityResolver;
import com.houjiang.plugin.update.exception.UpdateRejectedException;
import com.houjiang.plugin.update.model.InstallResult;
import com.houjiang.plugin.update.model.TaskStatus;
import com.houjiang.plugin.update.model.UpdateOutcome;
import com.houjiang.plugin.update.precheck.PrecheckResult;
import com.houjiang.plugin.update.risk.UpgradeRiskService;
import com.houjiang.plugin.update.service.PluginUpdateService;
import com.houjiang.mall.service.plugin.StandalonePluginCodes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端组接口（PaaS 内部 {@code /base/plugin-center/update/**}，方案 §4.4）。
 *
 * <p>M3 落地四个：{@code install} / {@code precheck} / {@code task/status} / {@code history}。
 * M4 补上 {@code upgrade} 与 {@code rollback}（方案 §4.4 客户端组）。
 *
 * <p><b>身份字段一律不接前端传值</b>：与 {@code /base/plugin-center/artifact/download-url} 同一原则，
 * 站点实例（instanceId）由服务端从登录态解析，请求体里带了一概丢弃并记 warn——
 * 否则任何登录用户都能把安装动作挂到别人的站点上，授权体系直接失效。
 *
 * <p><b>为什么本组接口不进鉴权白名单</b>：下载回源（{@code /base/plugin-center/artifact/raw}）靠令牌自证，
 * 而安装是<b>写操作</b>且要落站点安装态，必须确认是登录用户本人发起，
 * 因此走 {@code MallAdminTokenAuthenticationFilter} 的正常鉴权（未登录 → 3001）。
 *
 * @author Amos
 * @since M3
 */
@Slf4j
@RestController
@RequestMapping("/base/plugin-center/update")
public class PluginUpdateController {

    // 独立服务编码统一取 StandalonePluginCodes（单一事实来源），不再在本控制器另存一份。
    private static final Set<String> STANDALONE_SERVICES = StandalonePluginCodes.CODES;

    private final PluginUpdateService updateService;
    private final UpgradeRiskService upgradeRiskService;

    /**
     * 构造控制器。
     *
     * @param updateService 安装编排服务
     * @param upgradeRiskService 升级风险报告
     */
    public PluginUpdateController(PluginUpdateService updateService, UpgradeRiskService upgradeRiskService) {
        this.updateService = Objects.requireNonNull(updateService, "PluginUpdateService 不能为空");
        this.upgradeRiskService = Objects.requireNonNull(upgradeRiskService, "UpgradeRiskService 不能为空");
    }

    // 权限码与闸门判定统一收口在 PluginManageGate（commons）：插件中心、插件更新、插件市场
    // 三处共用同一份实现，避免「同一套安全判定抄三份，改一处漏两处」。
    // 权限码取插件中心菜单真实使用的 shell:addon（hou_permission_menu id=239），
    // 兼容早期菜单 id=4 的 shell:addons；不用自造码——自造码会让所有角色匹配不上，
    // 闸门一开就锁死整条安装链路。

    /**
     * 高危动作是否强制校验管理权限（与 {@code PluginCenterController} 同一开关）。
     */
    @org.springframework.beans.factory.annotation.Value("${houcloud.plugin.require-manage-authority:true}")
    private boolean requireManageAuthority;

    /**
     * 写动作的授权闸门。
     *
     * <p>本机 JVM 是<b>共享</b>的：一次安装/卸载会影响该实例上所有租户可见的插件行为，
     * 因此不能只凭「已登录」放行。
     *
     * <p>开关默认 <b>true</b>（fail-closed）。此前默认 false 是为了分两步上线，现已用真实
     * 管理员登录态核实过权限集合确实包含 {@code shell:addon}，可以安全开启：开着但不生效
     * 等于本次权限收口零收益，而租户子账号凭登录态就能卸载别人正在用的插件。
     *
     * @return 通过返回 null；被拦截时返回可直接响应的失败结果
     */
    private ResponseResult<Object> manageGate() {
        return PluginManageGate.check(requireManageAuthority);
    }

    /**
     * 安装插件。
     *
     * @param params  请求体（{@code pluginCode} 必填，{@code version} 可选）
     * @param request 原始请求（取 IP / UA 做下载留痕）
     * @return 安装结果；失败时 data 为 null
     */
    @PostMapping("/install")
    public ResponseResult<Object> install(@RequestBody(required = false) Map<String, Object> params,
                                          HttpServletRequest request) {
        ResponseResult<Object> denied = manageGate();
        if (denied != null) {
            return denied;
        }
        PluginIdentity identity = requireLogin();
        if (identity == null) {
            return ResponseResult.fail(PluginDistErrorCode.NOT_LOGIN, "未登录或登录已过期");
        }
        rejectClientSuppliedIdentity(params);
        String code = text(params, "pluginCode");
        if (STANDALONE_SERVICES.contains(code)) return ResponseResult.fail(409, "独立服务必须走 /base/plugin-center/service/install");
        try {
            InstallResult result = updateService.install(identity,
                    text(params, "pluginCode"),
                    text(params, "version"),
                    resolveClientIp(request),
                    request == null ? null : request.getHeader("User-Agent"));
            return ResponseResult.success(result);
        } catch (UpdateRejectedException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("安装插件失败", e);
            return ResponseResult.fail(500, "安装插件失败，请联系管理员查看服务端日志");
        }
    }

    /**
     * 升级插件（M4）。
     *
     * <p>成功时也可能带有 {@code rolledBack=true}：升级失败后自动回滚成功会返回
     * <b>200 + ROLLED_BACK</b> 而不是失败码——系统没有被留在坏状态，
     * 但必须让前端知道「这次升级没成，已退回原版本」。
     *
     * @param params  请求体（{@code pluginCode} 必填，{@code targetVersion} 可选）
     * @param request 原始请求（取 IP / UA 做下载留痕）
     * @return 升级结果
     */
    @PostMapping("/upgrade")
    public ResponseResult<Object> upgrade(@RequestBody(required = false) Map<String, Object> params,
                                          HttpServletRequest request) {
        ResponseResult<Object> denied = manageGate();
        if (denied != null) {
            return denied;
        }
        PluginIdentity identity = requireLogin();
        if (identity == null) {
            return ResponseResult.fail(PluginDistErrorCode.NOT_LOGIN, "未登录或登录已过期");
        }
        rejectClientSuppliedIdentity(params);
        String code = text(params, "pluginCode");
        if (STANDALONE_SERVICES.contains(code)) return ResponseResult.fail(409, "独立服务必须走 /base/plugin-center/service/upgrade");
        try {
            UpdateOutcome result = updateService.upgrade(identity,
                    text(params, "pluginCode"),
                    text(params, "targetVersion"),
                    resolveClientIp(request),
                    request == null ? null : request.getHeader("User-Agent"));
            return ResponseResult.success(result);
        } catch (UpdateRejectedException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("升级插件失败", e);
            return ResponseResult.fail(500, "升级插件失败，请联系管理员查看服务端日志");
        }
    }

    /**
     * 卸载插件（编排版，带任务流水与补偿）。
     *
     * <p>与 {@code /base/plugin-center/uninstall}（旧链路，只写状态位 + 删菜单）的分工：
     * 本接口会走排空窗口、释放 ClassLoader、关闭插件子上下文、回收版本目录、
     * 清除迁移审计、同步站点目录，并在任务表留痕——卸载失败可以在
     * {@code /base/plugin-center/update/history} 里查到具体阶段与原因。
     *
     * <p>参数：{@code pluginCode}（必填）、{@code purgeData}（可选，默认 false）。
     * {@code purgeData=true} 时把插件业务表按 {@code plugin.yaml} 的 {@code tables.prefix}
     * {@code RENAME} 成 {@code _bak_<时间戳>}；<b>默认不回收业务数据</b>，因为删表不可逆。
     *
     * @param params 请求体
     * @return 卸载结果
     */
    @PostMapping("/uninstall")
    public ResponseResult<Object> uninstall(@RequestBody(required = false) Map<String, Object> params) {
        ResponseResult<Object> denied = manageGate();
        if (denied != null) {
            return denied;
        }
        PluginIdentity identity = requireLogin();
        if (identity == null) {
            return ResponseResult.fail(PluginDistErrorCode.NOT_LOGIN, "未登录或登录已过期");
        }
        rejectClientSuppliedIdentity(params);
        String code = text(params, "pluginCode");
        if (STANDALONE_SERVICES.contains(code)) return ResponseResult.fail(409, "独立服务必须由安装代理管理容器，禁止走 PF4J 卸载链路");
        try {
            UpdateOutcome result = updateService.uninstall(identity,
                    text(params, "pluginCode"),
                    bool(params, "purgeData"));
            return ResponseResult.success(result);
        } catch (UpdateRejectedException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("卸载插件失败", e);
            return ResponseResult.fail(500, "卸载插件失败，请联系管理员查看服务端日志");
        }
    }

    /**
     * 回滚插件（M4）。
     *
     * <p><b>豁免授权与下架状态</b>：回滚是履约动作，授权过期、版本已下架也必须允许退回到
     * 本机已安装过的版本——买了却不能用是最坏结果。具体豁免逻辑在服务层，
     * 这里只负责转达参数（{@code targetVersion} 为空时由服务端取登记的回滚锚点）。
     *
     * @param params 请求体（{@code pluginCode} 必填，{@code targetVersion} 可选）
     * @return 回滚结果
     */
    @PostMapping("/rollback")
    public ResponseResult<Object> rollback(@RequestBody(required = false) Map<String, Object> params) {
        ResponseResult<Object> denied = manageGate();
        if (denied != null) {
            return denied;
        }
        PluginIdentity identity = requireLogin();
        if (identity == null) {
            return ResponseResult.fail(PluginDistErrorCode.NOT_LOGIN, "未登录或登录已过期");
        }
        rejectClientSuppliedIdentity(params);
        try {
            UpdateOutcome result = updateService.rollback(text(params, "pluginCode"),
                    text(params, "targetVersion"),
                    identity.getInstanceId(),
                    identity.getUserId());
            return ResponseResult.success(result);
        } catch (UpdateRejectedException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("回滚插件失败", e);
            return ResponseResult.fail(500, "回滚插件失败，请联系管理员查看服务端日志");
        }
    }

    /**
     * 兼容性预检（P0-9）。
     *
     * <p>这里是 <b>200 + {@code pass=false}</b> 而不是 fail：预检是<b>问询</b>不是动作，
     * 「不兼容」是它的正常答案之一，前端靠 {@code data.pass} 与 {@code data.reason} 分支，
     * 用失败码会让「预检成功返回不兼容」这个最常见的路径看起来像接口报错。
     *
     * @param params 请求体（{@code pluginCode} 必填，{@code version} 可选）
     * @return 预检结果
     */
    @PostMapping("/precheck")
    public ResponseResult<Object> precheck(@RequestBody(required = false) Map<String, Object> params) {
        if (requireLogin() == null) {
            return ResponseResult.fail(PluginDistErrorCode.NOT_LOGIN, "未登录或登录已过期");
        }
        rejectClientSuppliedIdentity(params);
        String pluginCode = text(params, "pluginCode");
        if (pluginCode == null || pluginCode.isBlank()) {
            return ResponseResult.fail(PluginDistErrorCode.MANIFEST_INVALID, "pluginCode 不能为空");
        }
        PrecheckResult result = updateService.precheck(pluginCode, text(params, "version"));
        return ResponseResult.success(result);
    }

    /**
     * 升级前风险报告（强制展示：将备份 / 可能不可逆迁移 / 二开冲突）。
     *
     * <p>问询接口：始终 200；{@code blocking=true} 表示兼容性预检未通过，前端应禁止确认升级。
     */
    @PostMapping("/upgrade-risk")
    public ResponseResult<Object> upgradeRisk(@RequestBody(required = false) Map<String, Object> params) {
        if (requireLogin() == null) {
            return ResponseResult.fail(PluginDistErrorCode.NOT_LOGIN, "未登录或登录已过期");
        }
        rejectClientSuppliedIdentity(params);
        String pluginCode = text(params, "pluginCode");
        if (pluginCode == null || pluginCode.isBlank()) {
            return ResponseResult.fail(PluginDistErrorCode.MANIFEST_INVALID, "pluginCode 不能为空");
        }
        String target = text(params, "targetVersion");
        if (target == null || target.isBlank()) {
            target = text(params, "version");
        }
        return ResponseResult.success(upgradeRiskService.report(pluginCode, target, text(params, "hostService")));
    }

    /**
     * 查询任务状态。
     *
     * @param params 请求体（{@code taskId} 或 {@code pluginCode}）
     * @return 任务状态
     */
    @PostMapping("/task/status")
    public ResponseResult<Object> taskStatus(@RequestBody(required = false) Map<String, Object> params) {
        PluginIdentity identity = requireLogin();
        if (identity == null) {
            return ResponseResult.fail(PluginDistErrorCode.NOT_LOGIN, "未登录或登录已过期");
        }
        TaskStatus status = updateService.taskStatus(text(params, "taskId"),
                text(params, "pluginCode"), identity.getInstanceId());
        if (status == null) {
            return ResponseResult.fail(PluginDistErrorCode.ARTIFACT_NOT_FOUND, "任务不存在");
        }
        return ResponseResult.success(status);
    }

    /**
     * 任务历史（分页）。
     *
     * @param params 请求体（{@code pluginCode} 可选，{@code page} / {@code pageSize} 可选）
     * @return 分页结果
     */
    @PostMapping("/history")
    public ResponseResult<Object> history(@RequestBody(required = false) Map<String, Object> params) {
        PluginIdentity identity = requireLogin();
        if (identity == null) {
            return ResponseResult.fail(PluginDistErrorCode.NOT_LOGIN, "未登录或登录已过期");
        }
        return ResponseResult.success(updateService.history(text(params, "pluginCode"),
                identity.getInstanceId(),
                intValue(params, "page", 1),
                intValue(params, "pageSize", 20)));
    }

    /**
     * 取登录态，未登录返回 null。
     *
     * @return 调用方身份
     */
    private static PluginIdentity requireLogin() {
        return PluginIdentityResolver.fromCurrentContext();
    }

    /**
     * 请求体里出现身份字段一律丢弃并告警。
     *
     * @param params 请求体
     */
    private static void rejectClientSuppliedIdentity(Map<String, Object> params) {
        if (params != null && (params.containsKey("instanceId") || params.containsKey("userId"))) {
            log.warn("安装请求携带了客户端身份字段，已忽略并以登录态为准");
        }
    }

    /**
     * 取客户端 IP：优先 X-Forwarded-For（经过 nginx），再退回 TCP 对端地址。
     *
     * @param request 原始请求，可为 null
     * @return 客户端 IP，可能为 null
     */
    private static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 取请求体文本字段。
     *
     * @param params 请求体
     * @param key    字段名
     * @return 文本值，缺失返回 null
     */
    /**
     * 取请求体里的布尔字段。
     *
     * <p>只认 {@code true/false/1/0/yes/no}（大小写不敏感），<b>不认其它任何值</b>：
     * 卸载的 {@code purgeData} 是要删数据的开关，把 "false" 以外的字符串一律当 true
     * 等于「传错参数就删表」，这个方向上的歧义必须偏向保守。
     *
     * @param params 请求体
     * @param key    字段名
     * @return 布尔值；缺失或无法识别时返回 false
     */
    private static boolean bool(Map<String, Object> params, String key) {
        if (params == null) {
            return false;
        }
        Object value = params.get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean flag) {
            return flag;
        }
        String text = value.toString().trim();
        return "true".equalsIgnoreCase(text)
                || "1".equals(text)
                || "yes".equalsIgnoreCase(text)
                || "on".equalsIgnoreCase(text);
    }

    private static String text(Map<String, Object> params, String key) {
        if (params == null) {
            return null;
        }
        Object value = params.get(key);
        return value == null ? null : value.toString();
    }

    /**
     * 取请求体整型字段。
     *
     * @param params   请求体
     * @param key      字段名
     * @param fallback 缺省值
     * @return 整型值
     */
    private static int intValue(Map<String, Object> params, String key, int fallback) {
        if (params == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(params.get(key)));
        } catch (Exception e) {
            return fallback;
        }
    }
}
