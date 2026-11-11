package com.houjiang.plugin.api.extension.order;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.math.BigDecimal;

/**
 * 订单取消后扩展点：资源回收通知。<b>after* 语义</b>，旁路通知、不得中断取消流程。
 *
 * <p><b>动机</b>：订单取消时，各促销/补贴类插件占用的资源必须回收——
 * 优惠券要退、补贴额度要释放、积分要返还。这些资源全部登记在插件自己的账本里，
 * 宿主不知道也不该知道。本扩展点只负责把「这一单取消了」广播出去，
 * 谁登记过谁自己来回收，幂等由插件保证。
 *
 * <p><b>旁路语义</b>：宿主调用本扩展点<b>不抛异常、不回滚</b>——订单取消是既成事实，
 * 插件回收失败只记日志与告警，由对账任务兜底，绝不能让用户「取消失败」。
 *
 * @author Amos
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface OrderCancelAfterExtension extends ExtensionPoint<OrderCancelAfterExtension.Context> {

    final class Context extends ExtensionContext {
        /** 订单号（只读） */
        public String orderNo;
        /** 支付单号（同批多单时相同，只读） */
        public String paymentNumber;
        /** 买家用户 ID（只读） */
        public Long userId;
        /** 取消方：1 买家 / 2 卖家 / 3 系统（只读） */
        public Integer cancelIdentity;
        /** 订单实付金额（元，只读，可为 null 表示未支付取消） */
        public BigDecimal paidAmount;
        /** 插件可回写的信息（如本次回收金额），宿主只记录不采信 */
        public java.util.Map<String, Object> result = new java.util.HashMap<>();
    }

    /**
     * 执行前钩子（订单取消后）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(OrderCancelAfterExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（订单取消后）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(OrderCancelAfterExtension.Context ctx) {
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
    default boolean supports(OrderCancelAfterExtension.Context ctx) {
        return true;
    }

    class Noop implements OrderCancelAfterExtension {
        @Override
        public void execute(Context ctx) { /* 旁路通知，默认无操作 */ }
    }
}
