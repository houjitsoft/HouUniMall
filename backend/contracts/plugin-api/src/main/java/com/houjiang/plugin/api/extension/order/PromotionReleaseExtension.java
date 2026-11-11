package com.houjiang.plugin.api.extension.order;

import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

/**
 * 促销资源释放扩展点：下单失败后的占用补偿回收。<b>after* 语义</b>，旁路、不抛异常。
 *
 * <p><b>动机</b>：占用类促销（国补额度、秒杀资格、限量券）在下单成功前就写了插件自己的
 * 账本。宿主后续任何一步失败（额度被抢、券已使用、自购拦截、插件超时误判…）都会回滚
 * 订单，但<b>插件账本的写入不受宿主事务管辖</b>——不显式通知释放，额度就被废单白占
 * （2026-11-11 E2E 实测泄漏）。本扩展点就是「这一单作废了，占过资源的自己退」的通知。
 *
 * <p><b>幂等</b>：同一 orderNo 可能重复通知（多处失败出口 + 取消订单），插件必须幂等；
 * 该单没登记过资源时直接返回零开销。
 *
 * @author Amos
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface PromotionReleaseExtension extends ExtensionPoint<PromotionReleaseExtension.Context> {

    final class Context extends ExtensionContext {
        /** 支付单号 / 占用幂等键前缀（只读） */
        public String orderNo;
        /** 买家用户 ID（只读） */
        public Long userId;
        /** 释放原因（只读，面向日志）：如 occupy-rejected / order-create-failed / cancel */
        public String reason;
        /** 插件可回写的信息，宿主只记录不采信 */
        public java.util.Map<String, Object> result = new java.util.HashMap<>();
    }

    class Noop implements PromotionReleaseExtension {
        @Override
        public void execute(Context ctx) { /* 旁路通知，默认无操作 */ }
    }
}
