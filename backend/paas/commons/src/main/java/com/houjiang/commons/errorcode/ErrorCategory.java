package com.houjiang.commons.errorcode;

/**
 * 错误类别：前端「未命中映射表」时的兜底依据，也是展示规则的一级分类（PRD §6.1）。
 *
 * <p>解决的问题：映射表不可能覆盖全部历史裸码，必须有一个稳定的兜底维度；
 * 同时「清 token 跳登录」这类高风险动作只能由 {@link #AUTH} 触发，
 * 不能像过去那样靠 <code>message.includes('未登录')</code> 猜。
 *
 * <p>共 9 类，与前端 {@code categoryFallback.js} 的 CATEGORY_TEXT / CATEGORY_UI 一一对应。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum ErrorCategory {

    /** 认证/登录态失效：清 token → 跳登录页。 */
    AUTH,

    /** 越权/角色不足：Toast 停留原页，绝不跳登录。 */
    PERMISSION,

    /** 参数校验失败：表单行内红字 + 自动聚焦首个错误字段。 */
    VALIDATION,

    /** 业务规则阻断（库存不足、状态不允许、重复提交）：Toast 3s。 */
    BIZ,

    /** 资源不存在/已删除/查询为空：空状态占位，不弹 Toast。 */
    NOT_FOUND,

    /** 版本冲突/重复键/并发互斥：Dialog 引导刷新。 */
    CONFLICT,

    /** Feign 下游失败、超时、第三方不可用：静默重试后再 Toast，禁止出后端原文。 */
    UPSTREAM,

    /** 系统级未知异常：通用 Toast，可展开错误码 + traceId。 */
    SYSTEM,

    /** 限流/幂等拦截：Toast + 倒计时禁用按钮。 */
    RATE_LIMIT
}
