package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * USER（域 32）用户与会员错误码，号段 32001-32999。
 *
 * <p>解决的问题：用户域的错误过去与 11 段（认证）混在一起，
 * 「会员不存在」和「未登录」共用相近的数字，前端分支极易误判。本域独立登记用户实体类错误。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum UserErrorCode implements ErrorCodeDef {

    /**
     * 用户不存在（会员已注销或 ID 无效）。
     *
     * <p>触发：按用户 ID 查不到记录。用户看到「用户不存在或已注销」。
     *
     * <p>排查：确认用户 ID 与 userms 表；注销用户不可恢复。
     */
    USER_NOT_FOUND(32001, ErrorMeta.builder(32001, "用户不存在或已注销")
            .devMessage("按用户 ID 未查到会员记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(11002)
            .troubleshooting("确认用户 ID 与会员表记录；注销用户不可恢复。"
                    + "注意旧码 11002 在骑手侧语义为「正在审核」，对应 USER 域 32801，勿混用")
            .since("v1.2.0")
            .remark("认领冲突旧码 11002 的主站语义（BizCodeEnume.USER_DOES_NOT_EXIST）；"
                    + "骑手侧语义见 USER 域 32801，两者共同认领 11002 → AMBIGUOUS")
            .build()),

    /**
     * 用户资料参数不合法（昵称、头像等）。
     *
     * <p>触发：资料更新接口参数校验失败。用户看到「资料填写有误，请检查后重试」。
     *
     * <p>排查：确认前端与后端的字段长度、格式约定一致。
     */
    USER_PARAM_INVALID(32002, ErrorMeta.builder(32002, "资料填写有误，请检查后重试")
            .devMessage("用户资料参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(11076)
            .since("v1.2.0")
            .build()),

    /**
     * 用户状态不允许（已注销、已拉黑）。
     *
     * <p>触发：对当前状态的用户执行了不允许的操作。用户看到「当前用户状态不允许该操作」。
     *
     * <p>排查：确认用户状态流转是否合法。
     */
    USER_STATE_NOT_ALLOWED(32003, ErrorMeta.builder(32003, "当前用户状态不允许该操作")
            .devMessage("用户状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 手机号已被其它账号占用。
     *
     * <p>触发：绑定手机号时冲突。用户看到「该手机号已被其它账号使用」。
     *
     * <p>排查：引导用户更换手机号或走账号合并流程。
     */
    USER_DUPLICATE_PHONE(32004, ErrorMeta.builder(32004, "该手机号已被其它账号使用")
            .devMessage("手机号唯一约束冲突")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11016, 11040)
            .since("v1.2.0")
            .build()),

    /**
     * 查询不到用户数据（筛选条件无命中）。
     *
     * <p>触发：用户列表查询返回空。用户看到空状态占位，不弹 Toast。
     *
     * <p>排查：这是 INFO 级别，正常业务结果。
     */
    USER_QUERY_RESULT_EMPTY(32007, ErrorMeta.builder(32007, "没有找到符合条件的用户")
            .devMessage("用户列表查询无命中")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 会员等级不存在。
     *
     * <p>触发：按等级 ID 查不到配置。用户看到「会员等级不存在，请联系管理员」。
     *
     * <p>排查：确认等级配置是否已维护。
     */
    USER_LEVEL_NOT_FOUND(32010, ErrorMeta.builder(32010, "会员等级不存在，请联系管理员")
            .devMessage("按等级 ID 未查到会员等级配置")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 收货地址不存在。
     *
     * <p>触发：按地址 ID 查不到记录。用户看到「收货地址不存在，请重新选择」。
     *
     * <p>排查：确认地址是否被删除；前端应刷新地址列表。
     */
    USER_ADDRESS_NOT_FOUND(32011, ErrorMeta.builder(32011, "收货地址不存在，请重新选择")
            .devMessage("按地址 ID 未查到收货地址")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 余额不足。
     *
     * <p>触发：扣减余额时可用余额不够。用户看到「余额不足，请先充值」。
     *
     * <p>排查：确认余额计算是否包含冻结金额。
     */
    USER_BALANCE_NOT_ENOUGH(32012, ErrorMeta.builder(32012, "余额不足，请先充值")
            .devMessage("可用余额小于扣减金额")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 账号正在审核中（旧码 11002 的骑手侧语义）。
     *
     * <p>触发：账号处于「正在审核」状态即尝试登录或操作。用户看到「账号正在审核中，请稍后再试」。
     *
     * <p>排查：确认审核流状态与驳回原因；审核通过后需重新登录以刷新状态缓存。
     *
     * <p><b>游离码认领说明</b>：11002 是跨枚举冲突码 —— {@code BizCodeEnume} 里是
     * {@code USER_DOES_NOT_EXIST("用户不存在,请先注册再登录")}，
     * {@code RiderCodeEnume} 里是 {@code UNDER_REVIEW("正在审核！")}。
     * 「用户不存在」语义由 {@link #USER_NOT_FOUND}（32001）认领，「正在审核」语义由本码认领，
     * 两者都登记 {@code legacyCodes = {11002}}，故该旧码判定为 {@code AMBIGUOUS}，不做 1:1 自动映射。
     */
    USER_UNDER_REVIEW(32801, ErrorMeta.builder(32801, "账号正在审核中，请稍后再试")
            .devMessage("账号处于审核中状态，不允许登录或执行业务操作")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .legacyCodes(11002)
            .troubleshooting("确认审核流当前状态与驳回原因；审核通过后需重新登录以刷新状态缓存；"
                    + "确认调用方是骑手侧还是主站侧（主站侧 11002 语义为「用户不存在」，对应 USER 域 32001）")
            .since("v1.2.0")
            .remark("域内扩展槽 801；认领冲突旧码 11002 的骑手侧语义，与 USER 域 32001 共同认领 → AMBIGUOUS，禁止 1:1 自动映射")
            .build()),
    /**
     * 该邮箱已被其它账号使用。
     *
     * <p>触发/排查：注册/换绑时邮箱唯一键冲突。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_DUPLICATE_EMAIL(32005, ErrorMeta.builder(32005, "该邮箱已被其它账号使用")
            .devMessage("注册/换绑时邮箱唯一键冲突")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11016)
            .since("v1.3.0")
            .build()),

    /**
     * 积分不足，请调整数量。
     *
     * <p>触发/排查：可用积分小于本次扣减。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_POINTS_NOT_ENOUGH(32013, ErrorMeta.builder(32013, "积分不足，请调整数量")
            .devMessage("可用积分小于本次扣减")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(10050)
            .since("v1.3.0")
            .build()),

    /**
     * 获取用户信息失败，请稍后重试。
     *
     * <p>触发/排查：查询会员资料异常。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_QUERY_FAILED(32015, ErrorMeta.builder(32015, "获取用户信息失败，请稍后重试")
            .devMessage("查询会员资料异常")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(11033)
            .since("v1.3.0")
            .build()),

    /**
     * 注册失败，请稍后重试。
     *
     * <p>触发/排查：会员注册流程失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_REGISTER_FAILED(32016, ErrorMeta.builder(32016, "注册失败，请稍后重试")
            .devMessage("会员注册流程失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(11011, 11033)
            .since("v1.3.0")
            .build()),

    /**
     * 该会员等级下还有会员，无法删除。
     *
     * <p>触发/排查：删除会员等级时等级下仍有用户。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_LEVEL_IN_USE(32101, ErrorMeta.builder(32101, "该会员等级下还有会员，无法删除")
            .devMessage("删除会员等级时等级下仍有用户")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11080)
            .since("v1.3.0")
            .build()),

    /**
     * 该用户尚未在平台注册。
     *
     * <p>触发/排查：按手机号/邮箱查不到平台账号。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_NOT_PLATFORM_REGISTERED(32103, ErrorMeta.builder(32103, "该用户尚未在平台注册")
            .devMessage("按手机号/邮箱查不到平台账号")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(11081)
            .since("v1.3.0")
            .build()),

    /**
     * 账号尚未绑定微信，请先绑定。
     *
     * <p>触发/排查：需要微信身份但账号未绑定。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_WECHAT_NOT_BOUND(32104, ErrorMeta.builder(32104, "账号尚未绑定微信，请先绑定")
            .devMessage("需要微信身份但账号未绑定")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17059)
            .since("v1.3.0")
            .build()),

    /**
     * 收藏夹还是空的。
     *
     * <p>触发/排查：收藏列表查询无记录。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_FAVORITE_EMPTY(32105, ErrorMeta.builder(32105, "收藏夹还是空的")
            .devMessage("收藏列表查询无记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .legacyCodes(11036)
            .since("v1.3.0")
            .build()),

    /**
     * 原账号验证失败，请重试。
     *
     * <p>触发/排查：验证旧手机号或邮箱失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_PHONE_VERIFY_FAILED(32107, ErrorMeta.builder(32107, "原账号验证失败，请重试")
            .devMessage("验证旧手机号或邮箱失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11039)
            .since("v1.3.0")
            .build()),

    /**
     * 更换绑定手机失败，请重试。
     *
     * <p>触发/排查：换绑手机号流程失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    USER_PHONE_CHANGE_FAILED(32108, ErrorMeta.builder(32108, "更换绑定手机失败，请重试")
            .devMessage("换绑手机号流程失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(11043)
            .since("v1.3.0")
            .build()),


    USER_ACCOUNT_ABNORMAL(32802, ErrorMeta.builder(32802, "账号异常，请联系客服")
            .devMessage("账号状态异常（冻结/风控），不允许本次操作")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(11008)
            .build()),
    ;

    private final int code;
    private final ErrorMeta meta;

    UserErrorCode(int code, ErrorMeta meta) {
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
