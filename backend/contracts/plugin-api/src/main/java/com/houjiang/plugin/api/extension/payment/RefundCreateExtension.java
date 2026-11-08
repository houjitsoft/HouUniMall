package com.houjiang.plugin.api.extension.payment;

import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.math.BigDecimal;

/**
 * 退款单创建扩展点：发起退款前的校验与金额核定。<b>before* 语义</b>，可 {@code abort}。
 *
 * <p>与 {@link PayCallbackExtension}（支付成功回调后）互为反向链路。
 * 契约测试不变量：{@code 0 <= refundAmount <= paidAmount}，金额精度不得超过 2 位小数。
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface RefundCreateExtension extends ExtensionPoint<RefundCreateExtension.Context> {

    final class Context extends ExtensionContext {
        public String orderNo;
        /** 订单实付金额（只读） */
        public BigDecimal paidAmount;
        /** 本次退款金额（扩展点可改写，须落在 [0, paidAmount]） */
        public BigDecimal refundAmount;
        /** 退款方式：ORIGINAL=原路退回 / BALANCE=退到余额 / MANUAL=人工线下 */
        public String refundWay;
        public String reason;
        public java.util.Map<String, Object> params = new java.util.HashMap<>();
    }

    /**
     * 执行前钩子（退款创建）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(RefundCreateExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（退款创建）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(RefundCreateExtension.Context ctx) {
        /* 默认不做收尾 */
    }

    /**
     * 宿主询问实现是否关心本次上下文；返回 {@code false} 时宿主可快速跳过本实现。
     *
     * <p>默认返回 {@code true}（关心）。用于避免为无关业务场景加载重实现。
     *
     * @param ctx 扩展点上下文
     * @return 是否需要执行
     * @since 1.0
     */
    default boolean supports(RefundCreateExtension.Context ctx) {
        return true;
    }

    class Noop implements RefundCreateExtension {
        @Override
        public void execute(Context ctx) { /* 默认不拦截 */
        }
    }
}
