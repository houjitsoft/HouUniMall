package com.houjiang.plugin.api.extension.order;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

/**
 * 下单后扩展点：通知 / 数据同步。after* 语义，可改结果（如补充字段）。
 */
@ExtensionStability(value = ExtensionStability.Stability.STABLE, since = "1.0",
        note = "下单后通知是交易主链路的核心扩展点，宿主已真实接入并由 OrderExtensionGatewayTest 守护契约。")
public interface OrderCreateAfterExtension extends ExtensionPoint<OrderCreateAfterExtension.Context> {

    final class Context extends ExtensionContext {
        /** 已创建订单（可写） */
        public Object order;
        /** 下单结果，插件可补充 */
        public Object result;
        public java.util.Map<String, Object> params = new java.util.HashMap<>();
    }

    /**
     * 执行前钩子（下单后）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(OrderCreateAfterExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（下单后）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(OrderCreateAfterExtension.Context ctx) {
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
    default boolean supports(OrderCreateAfterExtension.Context ctx) {
        return true;
    }

    class Noop implements OrderCreateAfterExtension {
        @Override
        public void execute(Context ctx) { /* 默认空实现 */
        }
    }
}
