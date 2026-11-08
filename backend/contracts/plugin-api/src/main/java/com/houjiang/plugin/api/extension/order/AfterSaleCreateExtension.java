package com.houjiang.plugin.api.extension.order;

import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.math.BigDecimal;

/**
 * 售后单创建扩展点：退款 / 退货 / 换货申请前的校验。<b>before* 语义</b>，只能读、可 {@code abort}。
 *
 * <p>扩展点只能调整 {@code approveAmount}（可退金额上限内），不得改写 {@code paidAmount}（只读输入）。
 * 契约测试不变量：{@code 0 <= approveAmount <= paidAmount}。
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface AfterSaleCreateExtension extends ExtensionPoint<AfterSaleCreateExtension.Context> {

    final class Context extends ExtensionContext {
        /** 原订单（只读） */
        public Object order;
        /** 订单明细（只读） */
        public Object orderItem;
        /** 售后类型：REFUND=仅退款 / RETURN=退货退款 / EXCHANGE=换货 */
        public String type;
        /** 退款数量 */
        public Integer quantity;
        /** 该明细实付金额（只读） */
        public BigDecimal paidAmount;
        /** 核定可退金额（扩展点可改写，须落在 [0, paidAmount]） */
        public BigDecimal approveAmount;
        /** 用户填写原因（只读） */
        public String reason;
        public java.util.Map<String, Object> params = new java.util.HashMap<>();
    }

    /**
     * 执行前钩子（售后单创建）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(AfterSaleCreateExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（售后单创建）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(AfterSaleCreateExtension.Context ctx) {
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
    default boolean supports(AfterSaleCreateExtension.Context ctx) {
        return true;
    }

    class Noop implements AfterSaleCreateExtension {
        @Override
        public void execute(Context ctx) { /* 默认不拦截 */
        }
    }
}
