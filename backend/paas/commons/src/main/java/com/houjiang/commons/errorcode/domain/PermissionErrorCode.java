package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * PERMISSION（域 31）权限与角色错误码，号段 31001-31999。
 *
 * <p>解决的问题（team-lead 裁决 1）：cmsadmin 的 VALID_CODES 白名单把旧码 <b>11007</b>
 * （BizCodeEnume 里是「权限不足」）当成合法返回值静默放行，
 * 导致用户点了没权限的按钮后「什么都没发生也不知道为什么」。
 * 本域的 {@link #PERMISSION_ACCESS_DENIED}（31005）是本码的正式归属，
 * cmsadmin 必须显式提示「你没有该操作权限，请联系管理员」并停留原页，不再静默。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum PermissionErrorCode implements ErrorCodeDef {

    /**
     * 角色不存在（角色被删除或 ID 无效）。
     *
     * <p>触发：按角色 ID 查不到定义。用户看到「角色不存在或已被删除」。
     *
     * <p>排查：确认角色 ID 与 permission 模块的角色表。
     */
    ROLE_NOT_FOUND(31001, ErrorMeta.builder(31001, "角色不存在或已被删除")
            .devMessage("按角色 ID 未查到角色定义")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 权限参数不合法（菜单 ID 列表为空等）。
     *
     * <p>触发：授权接口参数校验失败。用户看到「权限配置有误，请检查后重试」。
     *
     * <p>排查：确认前端提交的菜单/权限 ID 列表非空且合法。
     */
    PERMISSION_PARAM_INVALID(31002, ErrorMeta.builder(31002, "权限配置有误，请检查后重试")
            .devMessage("授权接口参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 权限不足（越权、角色不足、菜单未授权）。
     *
     * <p>触发：鉴权判定当前用户不具备该操作权限。用户看到「你没有该操作权限，请联系管理员」，
     * 前端弹红色 Toast 并<b>停留原页</b>，绝不跳登录页。
     *
     * <p>排查：确认用户角色与菜单授权；这是 WARN 级别，说明系统健康、是授权配置问题。
     * <b>旧码 11007 从 cmsadmin 的放行白名单中移除，不再静默。</b>
     */
    PERMISSION_ACCESS_DENIED(31005, ErrorMeta.builder(31005, "你没有该操作权限，请联系管理员")
            .devMessage("鉴权判定当前用户不具备该操作权限")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .troubleshooting("1. 确认用户所属角色；2. 确认菜单/按钮授权是否包含该操作；3. 确认数据权限范围。")
            .legacyCodes(403, 11007)
            .since("v1.2.0")
            .remark("cmsadmin 白名单码 11007 的新归属；本轮起停止静默放行，改为显式 Toast；"
                    + "HTTP 裸码 403 一并收口到本码")
            .build()),

    /**
     * 数据范围越权（跨组织/跨商户访问数据）。
     *
     * <p>触发：数据权限校验不通过。用户看到「你没有该数据的访问权限」。
     *
     * <p>排查：确认当前组织上下文与数据归属是否一致。
     */
    DATA_SCOPE_DENIED(31006, ErrorMeta.builder(31006, "你没有该数据的访问权限")
            .devMessage("数据权限范围校验未通过")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 菜单不存在（菜单 ID 无效或未注册）。
     *
     * <p>触发：按菜单 ID 查不到注册信息。用户看到「菜单不存在或已下线」。
     *
     * <p>排查：确认菜单注册表与前端路由是否同步。
     */
    MENU_NOT_FOUND(31007, ErrorMeta.builder(31007, "菜单不存在或已下线")
            .devMessage("按菜单 ID 未查到注册信息")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 角色仍被引用（有用户绑定该角色时不允许删除）。
     *
     * <p>触发：删除角色时存在关联用户。用户看到「该角色下还有用户，无法删除」。
     *
     * <p>排查：先解绑用户再删除；这是保护性拦截。
     */
    ROLE_IN_USE(31008, ErrorMeta.builder(31008, "该角色下还有用户，无法删除")
            .devMessage("角色存在关联用户，删除被拒绝")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17037)
            .since("v1.2.0")
            .build()),

    UCENTER_RESOURCE_NOT_FOUND(31009, ErrorMeta.builder(31009, "会员权限记录不存在或已失效")
            .devMessage("ucenter 空间、成员或邀请记录不存在")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404).exposeToUser(true)
            .since("v1.4.0").build()),

    UCENTER_SPACE_CONFLICT(31010, ErrorMeta.builder(31010, "当前权限空间状态不允许该操作")
            .devMessage("ucenter 空间或成员状态发生冲突")
            .level(ErrorLevel.WARN).retryable(true)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409).exposeToUser(true)
            .since("v1.4.0").build()),

    UCENTER_INVITATION_EXPIRED(31011, ErrorMeta.builder(31011, "邀请已过期，请联系空间所有者重新邀请")
            .devMessage("ucenter 邀请超过七天有效期")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409).exposeToUser(true)
            .since("v1.4.0").build()),

    UCENTER_INVITATION_TRANSITION_INVALID(31012, ErrorMeta.builder(31012, "邀请已处理，请刷新后查看")
            .devMessage("ucenter 邀请状态不允许重复流转")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409).exposeToUser(true)
            .since("v1.4.0").build())
    ;

    private final int code;
    private final ErrorMeta meta;

    PermissionErrorCode(int code, ErrorMeta meta) {
        this.code = code;
        this.meta = meta;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public ErrorMeta meta() {
        return meta;
    }
}
