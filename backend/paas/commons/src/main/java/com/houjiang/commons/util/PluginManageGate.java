package com.houjiang.commons.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 插件管理类高危动作的授权闸门（单一事实来源）。
 *
 * <p><b>为什么要有这个类</b>：闸门逻辑原先在 {@code PluginCenterController} 与
 * {@code PluginUpdateController} 各写了一份（连权限码常量都是各定义一遍），
 * 插件市场那扇门更是根本没接。同一套安全判定抄多份，改一处漏一处是必然的——
 * 本次全检中「只锁了一扇门」的 B5 就是这么来的。这里收口成一份，三个控制器都来问它。
 *
 * <p><b>为什么设计成静态工具而不是 Spring Bean</b>：本类位于 {@code commons}，
 * 而各个 paas 模块的组件扫描路径并不一致；做成 Bean 一旦某个模块没扫到就是启动失败。
 * 开关值由调用方自己用 {@code @Value} 注入后传进来，既保留了配置能力，又不引入扫描依赖。
 *
 * <p><b>权限码为什么是 {@code shell:addon} 而不是自造一个</b>：它对应菜单表里真实存在的
 * 「插件中心」菜单（{@code hou_permission_menu}，perms = shell:addon），已绑定给管理角色。
 * 凭空造一个码会让所有角色都匹配不上，闸门一开就把插件安装整条路锁死——那是把安全改进做成生产事故。
 *
 * @author Amos
 */
@Slf4j
public final class PluginManageGate {

    /** 插件中心菜单权限码（当前口径）。 */
    public static final String PLUGIN_MANAGE_AUTHORITY = "shell:addon";

    /** 历史权限码：部分老角色的菜单登记用的是复数形式，一并认。 */
    public static final String PLUGIN_MANAGE_AUTHORITY_LEGACY = "shell:addons";

    private PluginManageGate() {
    }

    /**
     * 当前会话是否具备插件管理权限（不做响应，只给需要「按身份分流」的接口用）。
     *
     * <p>典型用法：结算/分账接口要区分「运营看全平台」与「开发者只看自己」。
     * 那种场景需要的是布尔判断，而不是一个可直接返回的失败响应。
     * 通配口径与 {@link #check(boolean)} 一致：超管会话的 {@code ["*"]} 必须放行。
     */
    public static boolean hasAuthority() {
        return SecurityContextUtils.hasPermission(PLUGIN_MANAGE_AUTHORITY)
                || SecurityContextUtils.hasPermission(PLUGIN_MANAGE_AUTHORITY_LEGACY)
                || SecurityContextUtils.hasPermission("*")
                || SecurityContextUtils.hasPermission("*:*");
    }

    /**
     * 校验调用方是否具备插件管理权限。
     *
     * <p>本机 JVM 是<b>共享</b>的：一次安装/卸载会影响该实例上所有租户可见的插件行为，
     * 因此不能只凭「已登录」放行。
     *
     * <p>超管会话（is_admin=1 且无 RBAC 角色）的权限集是字面 {@code ["*"]}，精确比对
     * {@code shell:addon} 单项必拒（2026-10-27 OCR 403 同款坑，见 PluginPublishController
     * 的 adminGate 注释）。对齐平台约定：角色命中或显式通配（{@code *} / {@code *:*}），
     * 任一命中放行。
     *
     * @param requireManageAuthority 开关值，来自 {@code houcloud.plugin.require-manage-authority}；
     *                               为 false 时闸门整体旁路（保留紧急降级能力）
     * @return 通过返回 {@code null}；被拦截时返回可直接响应的失败结果
     */
    public static ResponseResult<Object> check(boolean requireManageAuthority) {
        if (!requireManageAuthority) {
            return null;
        }
        if (!SecurityContextUtils.hasPermission(PLUGIN_MANAGE_AUTHORITY)
                && !SecurityContextUtils.hasPermission(PLUGIN_MANAGE_AUTHORITY_LEGACY)
                && !SecurityContextUtils.hasPermission("*")
                && !SecurityContextUtils.hasPermission("*:*")) {
            log.warn("拦截插件管理动作：调用方缺少 {} 权限", PLUGIN_MANAGE_AUTHORITY);
            return ResponseResult.fail(403, "缺少插件管理权限，无法执行该操作");
        }
        return null;
    }
}
