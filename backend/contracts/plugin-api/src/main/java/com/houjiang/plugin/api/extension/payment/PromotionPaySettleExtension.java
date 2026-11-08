package com.houjiang.plugin.api.extension.payment;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 促销支付落库扩展点：支付成功后通知促销插件完成状态流转（如拼团成团）。
 *
 * <p><b>背景</b>：原先 Core 的 {@code payment} 各支付实现（微信/支付宝/Stripe/Paypal/余额）
 * 在支付成功回调中硬编码调用 {@code updatePinTuanPayStart}。促销插件未安装时该调用失败，
 * 会把"促销插件缺失"放大成"支付回调异常"。
 *
 * <p><b>解耦方式</b>：支付成功后由宿主回调本扩展点；无插件注册则跳过，支付主流程不受影响。
 *
 * <p><b>幂等要求</b>：支付渠道可能重复回调，实现方必须保证同一 {@code orderNo} 重复执行
 * 不产生重复数据（以订单号 + 支付流水号做幂等键）。
 *
 * @author Amos
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface PromotionPaySettleExtension extends ExtensionPoint<PromotionPaySettleExtension.Context> {

    final class Context extends ExtensionContext {

        /** 商城订单号 */
        public String orderNo;

        /** 促销类型：PIN_TUAN 拼团 等 */
        public String promotionType;

        /** 支付方式：WXPAY / ALIPAY 等 */
        public String payMethod;

        /** 渠道支付流水号（幂等键之一） */
        public String channelTradeNo;

        /** 实付金额 */
        public BigDecimal paidAmount = BigDecimal.ZERO;

        /** 支付完成时间（yyyy-MM-dd HH:mm:ss） */
        public String paidTime;

        /** 透传参数（拼团 ID 等） */
        public Map<String, Object> extra = new HashMap<>();
    }

    /**
     * 执行前钩子（促销支付结算）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(PromotionPaySettleExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（促销支付结算）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(PromotionPaySettleExtension.Context ctx) {
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
    default boolean supports(PromotionPaySettleExtension.Context ctx) {
        return true;
    }

    class Noop implements PromotionPaySettleExtension {
        @Override
        public void execute(Context ctx) { /* 默认无促销落库动作 */
        }
    }
}
