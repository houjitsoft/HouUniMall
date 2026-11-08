package com.houjiang.plugin.api.extension.goods;

import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

/**
 * 库存预占扩展点：下单预占 / 释放 / 确认扣减。<b>before* 语义</b>，可 {@code abort}，不得改写库存结果。
 *
 * <p>与 {@link StockChangeExtension} 的区别：本点在库存变动<b>发生前</b>执行，用于校验可售、限购、
 * 外部仓储可用量；{@code StockChangeExtension} 在变动<b>发生后</b>执行，只做同步与通知。
 *
 * <p>二开需要在预占失败时给出面向用户的中文原因（如「该商品每人限购 2 件」），
 * 原因会由宿主原样带到业务异常中。
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface StockLockExtension extends ExtensionPoint<StockLockExtension.Context> {

    final class Context extends ExtensionContext {
        public Long skuId;
        /** 本次变动数量（正数） */
        public Integer quantity;
        /** 变动类型：LOCK=预占 / UNLOCK=释放 / CONFIRM=确认扣减 */
        public String type;
        /** 当前可用库存（只读输入，扩展点不得改写） */
        public Integer available;
        public String orderNo;
        public java.util.Map<String, Object> params = new java.util.HashMap<>();
    }

    /**
     * 执行前钩子（库存锁定）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(StockLockExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（库存锁定）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(StockLockExtension.Context ctx) {
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
    default boolean supports(StockLockExtension.Context ctx) {
        return true;
    }

    class Noop implements StockLockExtension {
        @Override
        public void execute(Context ctx) { /* 默认不拦截 */
        }
    }
}
