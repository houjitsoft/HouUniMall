package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * SECURITY（域 26）安全与风控错误码，号段 26001-26999。
 *
 * <p>解决的问题：安全类错误的响应必须<b>泛化</b>——绝不能告诉攻击者「是签名错了还是黑名单命中」。
 * 本域所有码的 userMessage 一律使用「操作失败，请联系管理员」这类不含具体原因的文案，
 * 详情只在 devMessage 与日志里。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum SecurityErrorCode implements ErrorCodeDef {

    /**
     * 签名校验失败（参数签名不匹配）。
     *
     * <p>触发：回调或开放接口的签名校验未通过。用户看到「请求校验失败，请重试」。
     *
     * <p>排查：确认签名算法与密钥版本；注意时钟偏移。<b>不告诉调用方具体哪一步错了。</b>
     */
    SECURITY_SIGNATURE_INVALID(26002, ErrorMeta.builder(26002, "请求校验失败，请重试")
            .devMessage("参数签名校验未通过，签名细节不出日志")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(false)
            .legacyCodes(10018)
            .since("v1.2.0")
            .remark("exposeToUser=false：前端一律出 PERMISSION 兜底文案，禁止回显具体原因")
            .build()),

    /**
     * 禁止访问（风控或黑名单拦截）。
     *
     * <p>触发：风控规则判定为风险请求。用户看到「操作失败，请联系管理员」。
     *
     * <p>排查：查风控命中的规则；误拦需风控侧加白。
     */
    SECURITY_ACCESS_FORBIDDEN(26005, ErrorMeta.builder(26005, "操作失败，请联系管理员")
            .devMessage("风控规则命中，请求被拒绝")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(false)
            .since("v1.2.0")
            .build()),

    /**
     * 凭证非法（token 格式错误、被篡改）。
     *
     * <p>触发：JWT 解析失败或签名校验不过。用户看到「登录已失效，请重新登录」。
     *
     * <p>排查：旧码 10002（token非法）在 BizCodeEnume 里文案是「token非法」，直出技术术语；
     * 本码改为用户可理解的文案。前端按 AUTH/REDIRECT_LOGIN 处理。
     */
    SECURITY_TOKEN_ILLEGAL(26010, ErrorMeta.builder(26010, "登录已失效，请重新登录")
            .devMessage("凭证解析失败或签名校验未通过")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.REDIRECT_LOGIN).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.UNAUTHORIZED_401)
            .exposeToUser(false)
            .since("v1.2.0")
            .remark("旧码 10002 由 AUTH 域 30004 认领，本码不重复认领，避免 10002 变成歧义码")
            .build()),

    /**
     * 风控拒绝（高风险操作被拦截）。
     *
     * <p>触发：风控引擎返回拒绝。用户看到「操作存在风险，已被拦截」。
     *
     * <p>排查：查风控决策明细；不放宽标准，只做误拦复核。
     */
    SECURITY_RISK_REJECTED(26011, ErrorMeta.builder(26011, "操作存在风险，已被拦截")
            .devMessage("风控引擎决策为拒绝")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(false)
            .since("v1.2.0")
            .build()),

    /**
     * IP 被封禁。
     *
     * <p>触发：来源 IP 命中封禁名单。用户看到「当前网络不允许访问，请联系管理员」。
     *
     * <p>排查：确认是否为共享出口 IP 误封。
     */
    SECURITY_IP_BLOCKED(26012, ErrorMeta.builder(26012, "当前网络不允许访问，请联系管理员")
            .devMessage("来源 IP 命中封禁名单")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(false)
            .troubleshooting("1. 查封禁名单来源；2. 共享出口 IP 误封需加白。")
            .since("v1.2.0")
            .build()),

    /**
     * 敏感操作被拦截（需二次确认/审批但缺失）。
     *
     * <p>触发：删除、资金、权限变更等高危操作缺少二次校验。用户看到「该操作需要审批，请联系管理员」。
     *
     * <p>排查：确认审批流配置；这是保护性拦截。
     */
    SECURITY_SENSITIVE_OPERATION_BLOCKED(26013, ErrorMeta.builder(26013, "该操作需要审批，请联系管理员")
            .devMessage("高危操作缺少二次确认或审批流未配置")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(false)
            .since("v1.2.0")
            .build()),
    /**
     * 内容包含敏感词，请修改。
     *
     * <p>触发/排查：文本命中敏感词库被拦截。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    SECURITY_SENSITIVE_WORD(26014, ErrorMeta.builder(26014, "内容包含敏感词，请修改")
            .devMessage("文本命中敏感词库被拦截")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(10096)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    SecurityErrorCode(int code, ErrorMeta meta) {
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
