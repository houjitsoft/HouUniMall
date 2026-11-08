package com.houjiang.plugin.api.extension.order;

import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

/**
 * 购物车校验扩展点：加购 / 结算前的校验与拦截。<b>before* 语义</b>，只能读、可 {@code abort}，不修改购物车。
 *
 * <p>典型用途：限购、地区不可售、混装限制、跨境商品与身份认证。
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface CartValidateExtension extends ExtensionPoint<CartValidateExtension.Context> {

    final class Context extends ExtensionContext {
        /** 购物车（只读） */
        public Object cart;
        /** 购物车行（只读） */
        public java.util.List<Object> items = new java.util.ArrayList<>();
        /** 操作类型：ADD=加购 / UPDATE=改数量 / CHECKOUT=结算 */
        public String action;
        public java.util.Map<String, Object> params = new java.util.HashMap<>();
    }

    /**
     * 执行前钩子（购物车校验）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(CartValidateExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（购物车校验）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(CartValidateExtension.Context ctx) {
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
    default boolean supports(CartValidateExtension.Context ctx) {
        return true;
    }

    class Noop implements CartValidateExtension {
        @Override
        public void execute(Context ctx) { /* 默认不拦截 */
        }
    }
}
