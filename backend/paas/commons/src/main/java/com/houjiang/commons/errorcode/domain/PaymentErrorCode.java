package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * PAYMENT（域 44）支付错误码，号段 44001-44999。
 *
 * <p>解决的问题：支付是全站安全等级最高的链路，任何具体原因（余额、限额、风控、签名）
 * 都不应回显给用户或浏览器（PRD Q4 / §6.3）。本域的 userMessage 一律泛化，
 * {@code exposeToUser=false} 的码前端必须出类别兜底文案。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum PaymentErrorCode implements ErrorCodeDef {

    /**
     * 支付单不存在。
     *
     * <p>触发：按支付单号查不到记录。用户看到「支付单不存在，请重新发起支付」。
     *
     * <p>排查：确认支付单号与所属订单。
     */
    PAYMENT_ORDER_NOT_FOUND(44001, ErrorMeta.builder(44001, "支付单不存在，请重新发起支付")
            .devMessage("按支付单号未查到记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 支付参数不合法。
     *
     * <p>触发：发起支付参数校验失败。用户看到「支付信息有误，请重新发起」。
     *
     * <p>排查：确认金额、订单号、支付方式是否合法。
     */
    PAYMENT_PARAM_INVALID(44002, ErrorMeta.builder(44002, "支付信息有误，请重新发起")
            .devMessage("支付参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 支付状态不允许（已支付的订单不允许重复支付）。
     *
     * <p>触发：对已支付/已关闭的支付单再次发起支付。用户看到「该订单无需再次支付」。
     *
     * <p>排查：确认支付单状态机；已支付应引导查看订单。
     */
    PAYMENT_STATE_NOT_ALLOWED(44003, ErrorMeta.builder(44003, "该订单无需再次支付")
            .devMessage("支付单状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14045)
            .since("v1.2.0")
            .build()),

    /**
     * 重复支付通知（同一回调重复到达）。
     *
     * <p>触发：回调幂等键命中。用户侧无感知，前端不提示。
     *
     * <p>排查：这是幂等保护，服务端直接返回成功；重复通知不应产生二次入账。
     */
    PAYMENT_DUPLICATE_NOTIFY(44004, ErrorMeta.builder(44004, "支付结果已处理")
            .devMessage("支付回调幂等键重复命中")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.NONE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(false)
            .since("v1.2.0")
            .remark("uiAction=NONE：前端不提示，避免用户以为支付出问题")
            .build()),

    /**
     * 支付金额超限（超出渠道单笔/单日限额）。
     *
     * <p>触发：金额超过渠道限额。用户看到「支付金额超出限制，请分笔支付」。
     *
     * <p>排查：确认渠道限额配置；不回显具体限额数字。
     */
    PAYMENT_AMOUNT_EXCEEDED(44006, ErrorMeta.builder(44006, "支付金额超出限制，请分笔支付")
            .devMessage("金额超过支付渠道限额")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(false)
            .since("v1.2.0")
            .build()),

    /**
     * 支付渠道调用失败。
     *
     * <p>触发：调用微信/支付宝下单失败。用户看到「支付发起失败，请稍后重试」。
     *
     * <p>排查：<b>渠道返回的原始错误描述禁止出屏</b>，只在日志里查。
     */
    PAYMENT_CHANNEL_CALL_FAILED(44008, ErrorMeta.builder(44008, "支付发起失败，请稍后重试")
            .devMessage("支付渠道下单接口返回错误，原文见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(false)
            .troubleshooting("1. 从日志取渠道返回码；2. 确认商户号与密钥有效；3. 确认网络出口可达。")
            .legacyCodes(17024)
            .since("v1.2.0")
            .build()),

    /**
     * 支付配置缺失（商户号、密钥、回调地址未配置）。
     *
     * <p>触发：发起支付前发现配置为空。用户看到「支付未开通，请联系管理员」。
     *
     * <p>排查：补配置后重启服务。FATAL 级别需人工介入。
     */
    PAYMENT_CONFIG_MISSING(44009, ErrorMeta.builder(44009, "支付未开通，请联系管理员")
            .devMessage("支付渠道配置为空（商户号 / 密钥 / 回调地址）")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(false)
            .troubleshooting("1. 检查支付渠道配置是否下发；2. 补配置后重启服务；3. 确认回调地址外网可达。")
            .since("v1.2.0")
            .build()),

    /**
     * 支付回调验签失败。
     *
     * <p>触发：回调签名校验未通过。用户侧无感知，服务端只记日志并拒绝。
     *
     * <p>排查：<b>绝不告诉调用方具体哪一步错了</b>；确认密钥版本与平台证书是否更新。
     */
    PAYMENT_SIGN_VERIFY_FAILED(44010, ErrorMeta.builder(44010, "支付结果校验未通过")
            .devMessage("回调验签失败，签名细节不出日志")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.NONE).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(false)
            .troubleshooting("1. 确认平台证书/密钥是否轮换；2. 核对验签算法版本；3. 排查是否存在伪造回调。")
            .since("v1.2.0")
            .build()),

    /**
     * 退款失败。
     *
     * <p>触发：调用渠道退款失败。用户看到「退款失败，请联系客服处理」。
     *
     * <p>排查：资金类错误，需人工核对退款单状态后再决定重试。
     */
    PAYMENT_REFUND_FAILED(44011, ErrorMeta.builder(44011, "退款失败，请联系客服处理")
            .devMessage("渠道退款调用失败，渠道原文见服务端日志")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(false)
            .troubleshooting("1. 查退款单状态与渠道返回码；2. 确认商户余额充足；3. 必要时人工退款。")
            .since("v1.2.0")
            .build()),
    /**
     * 账户余额不足，请充值后重试。
     *
     * <p>触发/排查：可用余额小于本次扣减金额。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_BALANCE_NOT_ENOUGH(44012, ErrorMeta.builder(44012, "账户余额不足，请充值后重试")
            .devMessage("可用余额小于本次扣减金额")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14050, 17005)
            .since("v1.3.0")
            .build()),

    /**
     * 尚未设置支付密码，请先设置。
     *
     * <p>触发/排查：支付前未设置支付密码。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_PASSWORD_NOT_SET(44013, ErrorMeta.builder(44013, "尚未设置支付密码，请先设置")
            .devMessage("支付前未设置支付密码")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11078, 17014)
            .since("v1.3.0")
            .build()),

    /**
     * 支付密码错误，请重新输入。
     *
     * <p>触发/排查：支付密码校验不通过。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_PASSWORD_ERROR(44014, ErrorMeta.builder(44014, "支付密码错误，请重新输入")
            .devMessage("支付密码校验不通过")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17009, 17015, 17016)
            .since("v1.3.0")
            .build()),

    /**
     * 密码错误次数过多，请稍后再试。
     *
     * <p>触发/排查：连续输错达到锁定阈值。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_PASSWORD_LOCKED(44015, ErrorMeta.builder(44015, "密码错误次数过多，请稍后再试")
            .devMessage("连续输错达到锁定阈值")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.AUTH)
            .httpSemantic(HttpSemantic.TOO_MANY_REQUESTS_429)
            .exposeToUser(true)
            .legacyCodes(10067, 17026)
            .since("v1.3.0")
            .build()),

    /**
     * 转账失败，请稍后重试。
     *
     * <p>触发/排查：账户间转账执行失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_TRANSFER_FAILED(44016, ErrorMeta.builder(44016, "转账失败，请稍后重试")
            .devMessage("账户间转账执行失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(17013)
            .since("v1.3.0")
            .build()),

    /**
     * 资产变更失败，请稍后重试。
     *
     * <p>触发/排查：账户资产流水写入失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_ASSET_UPDATE_FAILED(44017, ErrorMeta.builder(44017, "资产变更失败，请稍后重试")
            .devMessage("账户资产流水写入失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(17011, 17012)
            .since("v1.3.0")
            .build()),

    /**
     * 提现申请失败，请稍后重试。
     *
     * <p>触发/排查：提现申请提交失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_WITHDRAW_FAILED(44018, ErrorMeta.builder(44018, "提现申请失败，请稍后重试")
            .devMessage("提现申请提交失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(17008, 17019)
            .since("v1.3.0")
            .build()),

    /**
     * 提现申请已提交，请等待审核。
     *
     * <p>触发/排查：存在在途提现申请。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_WITHDRAW_PENDING(44019, ErrorMeta.builder(44019, "提现申请已提交，请等待审核")
            .devMessage("存在在途提现申请")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17007)
            .since("v1.3.0")
            .build()),

    /**
     * 提现金额格式有误，请重新输入。
     *
     * <p>触发/排查：提现金额不满足格式或限额。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_WITHDRAW_AMOUNT_INVALID(44020, ErrorMeta.builder(44020, "提现金额格式有误，请重新输入")
            .devMessage("提现金额不满足格式或限额")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(17006)
            .since("v1.3.0")
            .build()),

    /**
     * 充值失败，请稍后重试。
     *
     * <p>触发/排查：充值流程执行失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_RECHARGE_FAILED(44021, ErrorMeta.builder(44021, "充值失败，请稍后重试")
            .devMessage("充值流程执行失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(17003)
            .since("v1.3.0")
            .build()),

    /**
     * 充值卡不可用，请联系客服。
     *
     * <p>触发/排查：充值卡状态不允许使用。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_RECHARGE_CARD_INVALID(44022, ErrorMeta.builder(44022, "充值卡不可用，请联系客服")
            .devMessage("充值卡状态不允许使用")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17001, 17004)
            .since("v1.3.0")
            .build()),

    /**
     * 有未确认结算单，请先确认。
     *
     * <p>触发/排查：存在未确认的结算单据。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_SETTLEMENT_UNCONFIRMED(44023, ErrorMeta.builder(44023, "有未确认结算单，请先确认")
            .devMessage("存在未确认的结算单据")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17021)
            .since("v1.3.0")
            .build()),

    /**
     * 不能向本人账号转账。
     *
     * <p>触发/排查：转账收款方与付款方相同。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_TRANSFER_SELF_FORBIDDEN(44024, ErrorMeta.builder(44024, "不能向本人账号转账")
            .devMessage("转账收款方与付款方相同")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17032)
            .since("v1.3.0")
            .build()),

    /**
     * 收款账号不存在，请确认。
     *
     * <p>触发/排查：按账号查不到收款方账户。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PAYMENT_TARGET_ACCOUNT_NOT_FOUND(44025, ErrorMeta.builder(44025, "收款账号不存在，请确认")
            .devMessage("按账号查不到收款方账户")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(17033)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    PaymentErrorCode(int code, ErrorMeta meta) {
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
