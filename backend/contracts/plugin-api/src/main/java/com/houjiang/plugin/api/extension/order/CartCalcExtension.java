package com.houjiang.plugin.api.extension.order;

import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.math.BigDecimal;

/**
 * 购物车算价扩展点：在官方算价完成后调整金额。<b>after* 语义</b>，写回 {@code discountAmount} / {@code payAmount}。
 *
 * <p>不变量（契约测试强制）：{@code payAmount = goodsAmount - discountAmount}，
 * 且 {@code 0 <= discountAmount <= goodsAmount}，{@code payAmount >= 0}。
 * 扩展点只能改折扣与应付，不得改写 {@code goodsAmount}（只读输入）。
 */
@ExtensionStability(value = ExtensionStability.Stability.STABLE, since = "1.0",
        note = "购物车算价是下单前置链路的核心扩展点，宿主已真实接入并由 OrderExtensionGatewayTest 守护契约。破坏性变更必须跨两个宿主版本缓冲。")
public interface CartCalcExtension extends ExtensionPoint<CartCalcExtension.Context> {

    final class Context extends ExtensionContext {
        /** 购物车（只读） */
        public Object cart;
        /** 商品总额（官方计算，只读） */
        public BigDecimal goodsAmount;
        /** 优惠总额（扩展点可改写，须落在 [0, goodsAmount]） */
        public BigDecimal discountAmount = BigDecimal.ZERO;
        /** 应付金额（扩展点可改写，须等于 goodsAmount - discountAmount） */
        public BigDecimal payAmount;
        public java.util.Map<String, Object> extra = new java.util.HashMap<>();
    }

    /**
     * 执行前钩子（购物车算价）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(CartCalcExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（购物车算价）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(CartCalcExtension.Context ctx) {
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
    default boolean supports(CartCalcExtension.Context ctx) {
        return true;
    }

    class Noop implements CartCalcExtension {
        @Override
        public void execute(Context ctx) { /* 默认不调价 */
        }
    }
}
