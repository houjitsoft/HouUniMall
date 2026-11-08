package com.houjiang.commons.exception;

import com.houjiang.commons.errorcode.domain.LogisticsErrorCode;
import com.houjiang.commons.errorcode.domain.OrderErrorCode;
import com.houjiang.commons.errorcode.domain.RiderErrorCode;

/**
 * RiderCodeEnume —— 统一错误码的<b>引用入口</b>，不再是第二套码源。
 *
 * <p>第二阶段重构后的形态：
 * <ul>
 *   <li>每一个常量的 code / msg 都<b>直接取自</b> {@code com.houjiang.commons.errorcode.domain}
 *       下的统一码枚举（{@code XxxErrorCode.CONST.code()} / {@code .meta().userMessage()}），
 *       不手写数字。统一码改了，本枚举自动跟着变，不可能漂移。</li>
 *   <li>只包含<b>活项</b>：在 backend 与 plugins 的真实代码里被调用过的常量。
 *       没人引用的死项已全部删除 —— 死项留着就是第二套码源，等于没重构。</li>
 *   <li>标注 {@code @deprecated} 的常量仅供存量调用点过渡，
 *       新代码请直接使用对应的 {@code XxxErrorCode}。</li>
 *   <li>成功语义的常量<b>不属于错误码体系</b>，码值保持历史值不变，
 *       不能编成 5 位错误码（会把成功判成失败）。</li>
 * </ul>
 *
 * <p>运行时<b>不做任何旧码→新码翻译</b>：业务代码拿到的码值就是统一码，
 * wire 上不再出现历史码值（成功语义的 0/1/2 等除外）。
 *
 * @author Amos
 * @since v1.0.0（历史枚举）/ v1.3.0（改为引用统一码）
 */
public enum RiderCodeEnume {

    /**
     * 订单不存在。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_NOT_EXIST(OrderErrorCode.ORDER_NOT_FOUND.code(), OrderErrorCode.ORDER_NOT_FOUND.meta().userMessage()),

    /**
     * 订单未完结，无法删除！。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_ORDER_NOT_COMPLETED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_ORDER_NOT_COMPLETED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_NOT_COMPLETED(LogisticsErrorCode.LOGISTICS_ORDER_NOT_COMPLETED.code(), LogisticsErrorCode.LOGISTICS_ORDER_NOT_COMPLETED.meta().userMessage()),

    /**
     * 不能重复确认！。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_CONFIRM_DUPLICATE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_CONFIRM_DUPLICATE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CANNOT_CONFIRM_REPEATEDLY(LogisticsErrorCode.LOGISTICS_CONFIRM_DUPLICATE.code(), LogisticsErrorCode.LOGISTICS_CONFIRM_DUPLICATE.meta().userMessage()),

    /**
     * 退款中,不能确认！。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_REFUND_IN_PROGRESS}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_REFUND_IN_PROGRESS}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    REFUND_IN_PROGRESS(OrderErrorCode.ORDER_REFUND_IN_PROGRESS.code(), OrderErrorCode.ORDER_REFUND_IN_PROGRESS.meta().userMessage()),

    /**
     * 退款中,不能确认！。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_REFUND_IN_PROGRESS}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_REFUND_IN_PROGRESS}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_REFUNDED(OrderErrorCode.ORDER_REFUND_IN_PROGRESS.code(), OrderErrorCode.ORDER_REFUND_IN_PROGRESS.meta().userMessage()),

    /**
     * 订单已被绑定！。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_ORDER_BOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_ORDER_BOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_HAS_BEEN_BOUND(LogisticsErrorCode.LOGISTICS_ORDER_BOUND.code(), LogisticsErrorCode.LOGISTICS_ORDER_BOUND.meta().userMessage()),

    /**
     * 订单不能被绑定，请检查订单！。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_ORDER_BIND_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_ORDER_BIND_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_CANNOT_BOUND(LogisticsErrorCode.LOGISTICS_ORDER_BIND_NOT_ALLOWED.code(), LogisticsErrorCode.LOGISTICS_ORDER_BIND_NOT_ALLOWED.meta().userMessage()),

    /**
     * 订单已完成！。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_ORDER_COMPLETED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_ORDER_COMPLETED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_COMPLETED(LogisticsErrorCode.LOGISTICS_ORDER_COMPLETED.code(), LogisticsErrorCode.LOGISTICS_ORDER_COMPLETED.meta().userMessage()),

    /**
     * 订单有误，请联系管理员！。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_ORDER_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_ORDER_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_ERROR(LogisticsErrorCode.LOGISTICS_ORDER_ERROR.code(), LogisticsErrorCode.LOGISTICS_ORDER_ERROR.meta().userMessage()),

    /**
     * 该订单不是同城配送订单，请检查！。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_NOT_SAME_CITY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_NOT_SAME_CITY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDERS_DELIVERED_DIFFERENT_CITIES(LogisticsErrorCode.LOGISTICS_NOT_SAME_CITY.code(), LogisticsErrorCode.LOGISTICS_NOT_SAME_CITY.meta().userMessage()),

    /**
     * 订单已点击配送！。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_DELIVERY_STARTED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_DELIVERY_STARTED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_CLICKED_DELIVERY(LogisticsErrorCode.LOGISTICS_DELIVERY_STARTED.code(), LogisticsErrorCode.LOGISTICS_DELIVERY_STARTED.meta().userMessage()),

    /**
     * 只能提现到骑手自己的账户中。
     *
     * <p>引用统一码 {@link RiderErrorCode#RIDER_WITHDRAW_ACCOUNT_INVALID}，码值与文案均由该枚举提供。
     * 历史旧码 11006 与 BizCodeEnume 的「用户未登录」冲突，故按骑手语义单独落此码。
     *
     * @deprecated 请直接使用 {@link RiderErrorCode#RIDER_WITHDRAW_ACCOUNT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CAN_ONLY_WITHDRAW_ONESELF_ACCOUNT(RiderErrorCode.RIDER_WITHDRAW_ACCOUNT_INVALID.code(),
            RiderErrorCode.RIDER_WITHDRAW_ACCOUNT_INVALID.meta().userMessage()),

    /** 历史哨兵项：原枚举用 End 标记常量列表结束，保留以兼容反射式遍历。 */
    End(999999, "-----");

    private final int code;

    private final String msg;

    RiderCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
