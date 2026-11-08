package com.houjiang.commons.errorcode;

/**
 * 前端默认展示动作：后端只声明「这类错误应该怎么呈现」，前端按动作分支执行（PRD §6.1）。
 *
 * <p>解决的问题：过去前端对每个错误码各写一套 if-else，
 * 12 个端各有独立 <code>utils/request.*</code>，统一策略无处落地（PRD 痛点 6）。
 * 现在前端只需实现一个 <code>presentError(action, text)</code> 分发器。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum UiAction {

    /** 顶部 Toast 提示（通用）。 */
    TOAST,

    /** 表单行内错误：字段下方红字 + 自动聚焦首个错误字段。 */
    INLINE,

    /** 清 token 并跳转登录页（业务声明 skipAuthRedirect 时降级为仅 reject）。 */
    REDIRECT_LOGIN,

    /** 整页错误页。 */
    ERROR_PAGE,

    /** 静默重试 1-2 次（指数退避），仍失败再降级为 Toast。 */
    SILENT_RETRY,

    /** 空状态占位：列表/详情区出「没有找到相关数据」，不弹 Toast。 */
    EMPTY_STATE,

    /** 模态对话框（冲突类，带刷新入口）。 */
    DIALOG,

    /** 不展示任何 UI，仅 reject（业务声明 skipErrorMessage 或预期降级场景）。 */
    NONE
}
