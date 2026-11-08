package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * ORDER（域 42）订单错误码，号段 42001-42999。
 *
 * <p>解决的问题：订单是全站最高频的失败点，但失败提示过去是「操作失败」+ 后端原文，
 * 用户不知道该等、该重下还是该找客服。本域按「状态不允许 / 重复提交 / 超时」等可行动语义分类。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum OrderErrorCode implements ErrorCodeDef {

    /**
     * 订单不存在。
     *
     * <p>触发：按订单号查不到记录。用户看到「订单不存在，请确认订单号」。
     *
     * <p>排查：确认订单号与所属用户；跨用户查询会被数据权限拦截。
     */
    ORDER_NOT_FOUND(42001, ErrorMeta.builder(42001, "订单不存在，请确认订单号")
            .devMessage("按订单号未查到订单记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(11012, 14025, 14072)
            .since("v1.2.0")
            .build()),

    /**
     * 下单参数不合法（收货地址缺失、商品列表为空）。
     *
     * <p>触发：下单接口参数校验失败。用户看到「下单信息不完整，请检查」。
     *
     * <p>排查：确认地址、商品、支付方式是否齐全。
     */
    ORDER_PARAM_INVALID(42002, ErrorMeta.builder(42002, "下单信息不完整，请检查")
            .devMessage("下单参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(14007)
            .since("v1.2.0")
            .build()),

    /**
     * 订单状态不允许（已发货的订单不能取消等）。
     *
     * <p>触发：对当前订单状态执行不允许的操作。用户看到「订单当前状态不允许该操作」。
     *
     * <p>排查：确认订单状态机；已发货应走售后流程。
     */
    ORDER_STATE_NOT_ALLOWED(42003, ErrorMeta.builder(42003, "订单当前状态不允许该操作")
            .devMessage("订单状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14002, 14070)
            .since("v1.2.0")
            .build()),

    /**
     * 重复提交订单（幂等键命中）。
     *
     * <p>触发：同一幂等键在窗口内重复下单。用户看到「订单正在处理，请勿重复提交」。
     *
     * <p>排查：这是保护性拦截，引导用户去订单列表查看结果。
     */
    ORDER_DUPLICATE_SUBMIT(42004, ErrorMeta.builder(42004, "订单正在处理，请勿重复提交")
            .devMessage("下单幂等键在窗口内重复命中")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 下单数量超出限购。
     *
     * <p>触发：超出活动限购或单笔上限。用户看到「购买数量超出限购」。
     *
     * <p>排查：确认限购规则；库存不足请用 INVENTORY 域 47006。
     */
    ORDER_QUANTITY_EXCEEDED(42006, ErrorMeta.builder(42006, "购买数量超出限购")
            .devMessage("下单数量超过限购规则")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(15016)
            .since("v1.2.0")
            .build()),

    /**
     * 查询不到订单数据（筛选条件无命中）。
     *
     * <p>触发：订单列表查询返回空。用户看到空状态占位，不弹 Toast。
     *
     * <p>排查：这是 INFO 级别，正常业务结果。
     */
    ORDER_QUERY_RESULT_EMPTY(42007, ErrorMeta.builder(42007, "没有找到符合条件的订单")
            .devMessage("订单列表查询无命中")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 取消订单失败。
     *
     * <p>触发：取消流程（含退款、回滚库存）失败。用户看到「取消订单失败，请稍后重试」。
     *
     * <p>排查：<b>注意资金与库存回滚是否完成一半</b>，需人工核对后再让用户重试。
     */
    ORDER_CANCEL_FAILED(42010, ErrorMeta.builder(42010, "取消订单失败，请稍后重试")
            .devMessage("取消流程失败，需核对库存与资金回滚状态")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 核对库存是否已回滚；2. 核对退款是否已发起；3. 确认 Seata 事务未悬挂后再重试。")
            .since("v1.2.0")
            .build()),

    /**
     * 发货失败。
     *
     * <p>触发：调用物流或写入运单失败。用户看到「发货失败，请稍后重试」。
     *
     * <p>排查：确认物流配置与运单号；可重试。
     */
    ORDER_DELIVERY_FAILED(42011, ErrorMeta.builder(42011, "发货失败，请稍后重试")
            .devMessage("物流下单或运单写入失败")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 订单已超时（未支付超时关闭）。
     *
     * <p>触发：支付超时，订单被自动关闭。用户看到「订单已超时关闭，请重新下单」。
     *
     * <p>排查：这是正常业务结果，引导用户重新下单。
     */
    ORDER_TIMEOUT_EXPIRED(42012, ErrorMeta.builder(42012, "订单已超时关闭，请重新下单")
            .devMessage("支付超时，订单被自动关闭")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14048)
            .since("v1.2.0")
            .build()),
    /**
     * 收货人信息不完整，请补充。
     *
     * <p>触发/排查：订单收货联系人信息缺失。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_ADDRESS_RECEIVER_EMPTY(42014, ErrorMeta.builder(42014, "收货人信息不完整，请补充")
            .devMessage("订单收货联系人信息缺失")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(16068)
            .since("v1.3.0")
            .build()),

    /**
     * 订单金额有误，请核对。
     *
     * <p>触发/排查：订单金额/运费与支付金额不一致。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_AMOUNT_INVALID(42015, ErrorMeta.builder(42015, "订单金额有误，请核对")
            .devMessage("订单金额/运费与支付金额不一致")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14026, 14073, 14074, 15015)
            .since("v1.3.0")
            .build()),

    /**
     * 订单退款中，请勿重复操作。
     *
     * <p>触发/排查：订单存在在途退款单。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_REFUND_IN_PROGRESS(42016, ErrorMeta.builder(42016, "订单退款中，请勿重复操作")
            .devMessage("订单存在在途退款单")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11015, 11016)
            .since("v1.3.0")
            .build()),

    /**
     * 订单退货中，请勿重复操作。
     *
     * <p>触发/排查：订单存在在途退货单。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_RETURN_IN_PROGRESS(42017, ErrorMeta.builder(42017, "订单退货中，请勿重复操作")
            .devMessage("订单存在在途退货单")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14059)
            .since("v1.3.0")
            .build()),

    /**
     * 订单当前状态无法核销。
     *
     * <p>触发/排查：过期/退款等状态下不允许核销。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_WRITE_OFF_NOT_ALLOWED(42018, ErrorMeta.builder(42018, "订单当前状态无法核销")
            .devMessage("过期/退款等状态下不允许核销")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14062, 14063, 14064)
            .since("v1.3.0")
            .build()),

    /**
     * 确认收货失败，请稍后重试。
     *
     * <p>触发/排查：收货确认流程执行失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_RECEIPT_FAILED(42019, ErrorMeta.builder(42019, "确认收货失败，请稍后重试")
            .devMessage("收货确认流程执行失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(14004, 14006)
            .since("v1.3.0")
            .build()),

    /**
     * 发票模板数量已达上限。
     *
     * <p>触发/排查：发票模板数量超出系统上限。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_INVOICE_TEMPLATE_LIMIT_EXCEEDED(42101, ErrorMeta.builder(42101, "发票模板数量已达上限")
            .devMessage("发票模板数量超出系统上限")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14023)
            .since("v1.3.0")
            .build()),

    /**
     * 发票地址数量已达上限。
     *
     * <p>触发/排查：发票地址数量超出系统上限。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_INVOICE_ADDRESS_LIMIT_EXCEEDED(42102, ErrorMeta.builder(42102, "发票地址数量已达上限")
            .devMessage("发票地址数量超出系统上限")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14024)
            .since("v1.3.0")
            .build()),

    /**
     * 请填写发票抬头。
     *
     * <p>触发/排查：开票时缺少发票抬头。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_INVOICE_TITLE_REQUIRED(42103, ErrorMeta.builder(42103, "请填写发票抬头")
            .devMessage("开票时缺少发票抬头")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(14051)
            .since("v1.3.0")
            .build()),

    /**
     * 还有未完成开票的订单，暂不能操作。
     *
     * <p>触发/排查：存在未完成开票的订单。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    ORDER_INVOICE_UNFINISHED(42104, ErrorMeta.builder(42104, "还有未完成开票的订单，暂不能操作")
            .devMessage("存在未完成开票的订单")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16014)
            .since("v1.3.0")
            .build()),


    ORDER_TIMEOUT_NOT_PAYABLE(42801, ErrorMeta.builder(42801, "该订单已超时，不能支付")
            .devMessage("订单超过支付时限，已不可继续支付")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(14047)
            .build()),
    ;

    private final int code;
    private final ErrorMeta meta;

    OrderErrorCode(int code, ErrorMeta meta) {
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
