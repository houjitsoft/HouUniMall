package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * AUTH（域 30）认证登录错误码，号段 30001-30999。
 *
 * <p>解决的问题（PRD 痛点 1）：同一个数字在不同枚举里是两件事 ——
 * 11006 在 BizCodeEnume 是「用户未登录」、在 RiderCodeEnume 是「只能提现到骑手自己的账户中」；
 * 11007 在 BizCodeEnume 是「权限不足」、在 RiderCodeEnume 是「短信验证码错误」。
 * admin 端因此用 <code>res.code === 11006</code> 触发「清 token + 强跳登录页」，
 * 骑手提现填错账户会被踢出登录。本域用独立号段重写认证语义，前端只认 AUTH 类别做跳登录，
 * 不再靠数字和 message 关键词猜。
 *
 * <p><b>11006 已按模块拆分认领</b>：主站 / admin 语义落在 {@link #USER_NOT_LOGGED_IN}（30802），
 * 骑手语义落在 RIDER 域 {@code RIDER_WITHDRAW_ACCOUNT_INVALID}（35010）。两个码都登记了
 * {@code legacyCodes = {11006}}，因此该旧码判定为 {@code AMBIGUOUS} —— <b>这是刻意的</b>：
 * 若把 11006 自动映射成「未登录」，骑手提现填错账户仍会被踢出登录，BUG 就还在。
 * 11007 仍只由 PERMISSION 域 31005 认领（主站语义），骑手侧短信码语义走 {@link #SMS_CODE_INVALID}（30006）。
 * 详见《码表.md》附录 B 与《开发手册.md》「游离码认领」小节。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum AuthErrorCode implements ErrorCodeDef {

    /**
     * 账号不存在（手机号/邮箱未注册）。
     *
     * <p>触发：登录时查不到账号。用户看到「账号不存在，请确认后重试」。
     *
     * <p>排查：确认用户是否注册；注册入口是否正常。本码不触发跳登录。
     */
    ACCOUNT_NOT_FOUND(30001, ErrorMeta.builder(30001, "账号不存在，请确认后重试")
            .devMessage("登录标识未匹配到账号记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .legacyCodes(10005)
            .since("v1.2.0")
            .remark("不触发跳登录；历史上 11002 在两套枚举里语义不同，本码只覆盖「账号不存在」")
            .build()),

    /**
     * 缺少凭证（请求未携带 token）。
     *
     * <p>触发：需要登录的接口没有 token。用户看到「登录已失效，请重新登录」，前端清 token 并跳登录页。
     *
     * <p>排查：确认前端是否漏传 token；业务声明 skipAuthRedirect 时不跳页。
     */
    TOKEN_MISSING(30002, ErrorMeta.builder(30002, "登录已失效，请重新登录")
            .devMessage("请求未携带凭证")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.REDIRECT_LOGIN).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.UNAUTHORIZED_401)
            .exposeToUser(false)
            .legacyCodes(10029)
            .since("v1.2.0")
            .remark("AUTH 类别是唯一允许触发「清 token + 跳登录」的类别")
            .build()),

    /**
     * 凭证已过期（JWT 超过有效期或 Redis 中的会话已失效）。
     *
     * <p>触发：{@code ExpiredJwtException} 或会话过期。用户看到「登录已过期，请重新登录」。
     *
     * <p>排查：确认 token 有效期配置与 Redis 会话 TTL 是否一致。
     */
    TOKEN_EXPIRED(30003, ErrorMeta.builder(30003, "登录已过期，请重新登录")
            .devMessage("凭证超过有效期或服务端会话已失效")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.REDIRECT_LOGIN).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.UNAUTHORIZED_401)
            .exposeToUser(false)
            .since("v1.2.0")
            .build()),

    /**
     * 凭证非法（格式错误、被篡改、签名不匹配）。
     *
     * <p>触发：JWT 解析或验签失败。用户看到「凭证无效，请重新登录」。
     *
     * <p>排查：旧码 10002（token非法）文案直出技术术语，本码改为用户可理解文案；
     * 与 SECURITY 域 26010 的区别是本码触发跳登录。
     */
    TOKEN_ILLEGAL(30004, ErrorMeta.builder(30004, "凭证无效，请重新登录")
            .devMessage("凭证格式错误、被篡改或验签失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.REDIRECT_LOGIN).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.UNAUTHORIZED_401)
            .exposeToUser(false)
            .legacyCodes(10002)
            .since("v1.2.0")
            .build()),

    /**
     * 账号或密码错误。
     *
     * <p>触发：密码校验失败。用户看到「账号或密码错误，请重新输入」。
     *
     * <p>排查：连续失败会触发风控；不告诉用户是账号错还是密码错。
     */
    CREDENTIAL_INVALID(30005, ErrorMeta.builder(30005, "账号或密码错误，请重新输入")
            .devMessage("密码校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.UNAUTHORIZED_401)
            .exposeToUser(false)
            .legacyCodes(10099, 11003)
            .since("v1.2.0")
            .remark("历史码 11003 跨枚举冲突（BizCodeEnume=账号密码错 / RiderCodeEnume=已审核通过），故不认领")
            .build()),

    /**
     * 短信验证码错误或已过期。
     *
     * <p>触发：登录/绑定手机号时校验短信码失败。用户看到「验证码不正确或已过期，请重新获取」。
     *
     * <p>排查：先看 Redis 中 sms:{phone} 是否已过期（默认 5 分钟）；再确认短信网关回执。
     */
    SMS_CODE_INVALID(30006, ErrorMeta.builder(30006, "验证码不正确或已过期，请重新获取")
            .devMessage("短信验证码校验失败或已过期")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(11004, 11013)
            .since("v1.2.0")
            .remark("历史码 11007 在 RiderCodeEnume 中才是短信码错误，本码独立登记以消除歧义")
            .build()),

    /**
     * 账号已被冻结（禁用、风控锁定）。
     *
     * <p>触发：账号状态为冻结。用户看到「账号已被冻结，请联系管理员」。
     *
     * <p>排查：确认冻结来源（管理员操作还是风控）；解冻需管理员处理。
     */
    ACCOUNT_FROZEN(30007, ErrorMeta.builder(30007, "账号已被冻结，请联系管理员")
            .devMessage("账号状态为冻结/禁用")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(false)
            .legacyCodes(11010)
            .since("v1.2.0")
            .build()),

    /**
     * 手机号已被注册。
     *
     * <p>触发：注册时手机号已存在。用户看到「该手机号已注册，请直接登录」。
     *
     * <p>排查：引导用户走登录或找回密码流程。
     */
    PHONE_ALREADY_REGISTERED(30008, ErrorMeta.builder(30008, "该手机号已注册，请直接登录")
            .devMessage("注册手机号已存在")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 账号尚未审核通过。
     *
     * <p>触发：账号处于待审核/审核中状态即尝试登录或操作。用户看到「账号尚未审核通过，请耐心等待」。
     *
     * <p>排查：确认审核流状态；这是正常业务流程，不是故障。
     */
    ACCOUNT_NOT_APPROVED(30009, ErrorMeta.builder(30009, "账号尚未审核通过，请耐心等待")
            .devMessage("账号审核状态未完成")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .legacyCodes(11009)
            .since("v1.2.0")
            .build()),

    /**
     * 登录态不可用（旧码 10086 / {@code GlobalExceptionEnum.NO_FORBIDDEN}）。
     *
     * <p>触发：鉴权拦截判定当前登录态不可用并抛 NO_FORBIDDEN。用户看到「登录失败，请重新登录」。
     *
     * <p>排查：确认会话存储中的登录态是否已失效、多端互踢策略是否开启、登录凭证是否被并发刷新顶掉。
     *
     * <p><b>游离码认领说明</b>：pc / webpos / rider 三端历史上靠 {@code code === 10086} 清登录态并跳登录页，
     * 但 10086 此前不在统一码表内。本码认领 10086，三端改为认 {@code category=AUTH} 或本码。
     * <b>注意</b>：10086 在 BizCodeEnume 里另有「快递公司名称和首字母需小于 30 个字符」的语义，
     * 在 paas-userms 里被当成「没有权限」使用 —— 这两种用法必须分别改用 VALIDATION 域 21002
     * 与 PERMISSION 域 31005，本码只认领登录语义。
     */
    LOGIN_FAILED(30801, ErrorMeta.builder(30801, "登录失败，请重新登录")
            .devMessage("鉴权拦截抛出 NO_FORBIDDEN（旧码 10086），当前登录态不可用")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.REDIRECT_LOGIN).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.UNAUTHORIZED_401)
            .exposeToUser(true)
            .legacyCodes(10086)
            .troubleshooting("确认会话存储中的登录态是否已失效或被清理；确认多端互踢策略是否开启；"
                    + "确认登录凭证是否被并发刷新顶掉；base 侧 10086 曾用于「凭证失效，请重新获取」，与本码同语义，"
                    + "userms 侧 10086「没有权限」须改判到 PERMISSION 域 31005")
            .since("v1.2.0")
            .remark("域内扩展槽 801；认领游离旧码 10086（GlobalExceptionEnum.NO_FORBIDDEN），pc/webpos/rider 三端凭此清登录态")
            .build()),

    /**
     * 用户未登录（admin / 主站语义，旧码 11006）。
     *
     * <p>触发：{@code JwtAuthenticationTokenFilter} 在会话存储中取不到登录凭证。
     * 用户看到「请先登录后再操作」，前端清 token 并跳登录页。
     *
     * <p>排查：确认请求是否携带凭证、会话是否过期；熔断器开启时 Feign 会在独立线程执行，
     * 请求上下文丢失会导致凭证传不到下游而被误判为未登录（见 bff-web {@code bootstrap.yml}）。
     *
     * <p><b>游离码认领说明</b>：11006 是跨枚举冲突码 —— 主站侧是「用户未登录」，
     * 骑手侧（{@code RiderCodeEnume}）是「只能提现到本人账户」。本码与 RIDER 域 35010
     * <b>同时认领 11006</b>，因此该旧码判定为 {@code AMBIGUOUS}，反向索引不产出 1:1 映射：
     * 若自动映射成「未登录」，骑手提现填错账户仍会被踢出登录，这正是本体系要修的 BUG。
     * 主站 / admin 侧请直接用本码，不要用 11006 反查。
     */
    USER_NOT_LOGGED_IN(30802, ErrorMeta.builder(30802, "请先登录后再操作")
            .devMessage("会话存储中取不到登录凭证（旧码 11006 的主站语义）")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.REDIRECT_LOGIN).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.UNAUTHORIZED_401)
            .exposeToUser(true)
            .legacyCodes(401, 11006, 11079)
            .troubleshooting("确认请求是否携带凭证、会话是否已过期；"
                    + "熔断器开启时 Feign 在独立线程执行会丢失请求上下文，导致凭证传不到下游而被误判为未登录；"
                    + "确认 11006 是否来自骑手侧（骑手侧语义为「只能提现到本人账户」，对应 RIDER 域 35010）")
            .since("v1.2.0")
            .remark("域内扩展槽 802；认领冲突旧码 11006 的主站语义，与 RIDER 域 35010 共同认领 → AMBIGUOUS，禁止 1:1 自动映射")
            .build()),

    /**
     * 登录凭证失效（旧码 10016 / {@code BizCodeEnume.TOKEN_IS_INVALID}「token失效,请重新登陆」）。
     *
     * <p>触发：服务端校验 token 判定失效（无法或不需区分「过期」与「非法」时使用本码）。
     * 用户看到「登录信息已失效，请重新登录」，前端清 token 并跳登录页。
     *
     * <p>排查：先区分是「过期」还是「非法」——能区分时直接用 {@link #TOKEN_EXPIRED}（30003）
     * 或 {@link #TOKEN_ILLEGAL}（30004）；网关层把 token 透传丢了也会落到本码。
     *
     * <p><b>游离码认领说明</b>：10016 在 rider / sellerapp / imchat 三端被并列进
     * {@code [10012, 10016, 10086]} 当未授权处理。本轮逐个核实后确认：10016 在
     * {@code BizCodeEnume} 里的定义就是「token失效,请重新登陆」，且 imchat 确有
     * {@code code === 10016 → 跳登录} 的分支，属<b>真·认证失效</b>，故认领到 AUTH 域，
     * 判定为 {@code EXACT}（1:1 映射，与 10012「系统异常」不同，10012 不是认证码）。
     *
     * <p><b>不跟着数组一起判 AUTH</b>：同一数组里的 10012 是「系统异常，请联系客服」，
     * 与认证无关，不认领到本域（见 11002 / 11006 的教训）。
     * 另：activity 插件曾把 10016 当「未选择优惠券类型 / 未选择商品」返回，属参数校验误用，
     * 已改判到 VALIDATION 域 21002。
     */
    TOKEN_INVALID(30803, ErrorMeta.builder(30803, "登录信息已失效，请重新登录")
            .devMessage("登录凭证已失效，无法继续代表当前用户（旧码 10016）")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.REDIRECT_LOGIN).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.UNAUTHORIZED_401)
            .exposeToUser(true)
            .legacyCodes(10016, 10094)
            .troubleshooting("确认请求链路是否把 token 透传到了下游：bff-web 未注入 token 时 order 侧会取到 null 并直接返回本码；"
                    + "确认会话存储中的凭证是否已过期（过期用 30003）或验签失败（非法用 30004）；"
                    + "确认 Feign 熔断未在独立线程执行导致请求上下文丢失；"
                    + "若业务方本意是参数校验（如 activity 插件的「未选择优惠券类型」），须改用 VALIDATION 域 21002")
            .since("v1.2.0")
            .remark("域内扩展槽 803；认领游离旧码 10016（BizCodeEnume.TOKEN_IS_INVALID），rider/sellerapp/imchat 三端凭此跳登录")
            .build()),
    /**
     * 账号已被禁用，请联系管理员。
     *
     * <p>触发/排查：账号被停用后仍发起请求。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ACCOUNT_DISABLED(30010, ErrorMeta.builder(30010, "账号已被禁用，请联系管理员")
            .devMessage("账号被停用后仍发起请求")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .legacyCodes(10098)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    AuthErrorCode(int code, ErrorMeta meta) {
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
