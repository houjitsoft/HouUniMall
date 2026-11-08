package com.houjiang.payment.plugin;

import com.houjiang.plugin.api.extension.payment.PromotionPaySettleExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 促销支付落库扩展点的宿主薄网关（{@code after*} 语义）。
 *
 * <p><b>做什么</b>：支付成功后通知促销类插件完成状态流转（如拼团成团、秒杀订单核销），
 * 替代原先 Core 各支付实现里<b>硬编码</b>调用 {@code updatePinTuanPayStart} 的写法——
 * 促销插件未安装时不再把「插件缺失」放大成「支付回调异常」。
 *
 * <p><b>为什么 {@code after*}</b>：本扩展点在支付成功<b>落库之后</b>触发，属结果通知；
 * 插件不应改写支付金额 / 状态。网关仅在插件显式 {@code abort} 时抛 {@link PluginAbortException}（7001）。
 *
 * <p><b>幂等</b>：支付渠道可能重复回调。宿主在调用本网关前已做「收银台订单已是 SUCCESS 则跳过」，
 * 因此同一 {@code orderNo} + {@code channelTradeNo} 不会被重复通知；插件侧仍应以
 * （订单号 + 支付流水号）作为幂等键二次兜底（契约要求，见接口 Javadoc）。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class PromotionPaySettleGateway {

    private static final Logger log = LoggerFactory.getLogger(PromotionPaySettleGateway.class);

    private static final String POINT = "PromotionPaySettle";

    private final ExtensionGatewaySupport support;

    public PromotionPaySettleGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /**
     * 支付成功后触发促销落库扩展点。
     *
     * @param orderNo        商城订单号（幂等键之一）
     * @param promotionType  促销类型（如 PIN_TUAN），未知时可传 {@code null}
     * @param payMethod      支付方式（WXPAY / ALIPAY / BALANCE…）
     * @param channelTradeNo 渠道支付流水号（幂等键之一）
     * @param paidAmount     实付金额，{@code null} 视为 0
     * @param paidTime       支付完成时间（yyyy-MM-dd HH:mm:ss），可为 {@code null}
     * @param extra          透传参数（如拼团 ID），可为 {@code null}
     * @throws PluginAbortException 当插件显式中断时抛出
     */
    public void settle(String orderNo,
                       String promotionType,
                       String payMethod,
                       String channelTradeNo,
                       BigDecimal paidAmount,
                       String paidTime,
                       Map<String, Object> extra) {
        PromotionPaySettleExtension.Context ctx = new PromotionPaySettleExtension.Context();
        ctx.orderNo = orderNo;
        ctx.promotionType = promotionType;
        ctx.payMethod = payMethod;
        ctx.channelTradeNo = channelTradeNo;
        ctx.paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        ctx.paidTime = paidTime;
        if (extra != null) {
            ctx.extra = new HashMap<>(extra);
        }

        support.invoke(PromotionPaySettleExtension.class, ctx);

        if (ctx.isAborted()) {
            log.warn("[ext] point={} result=abort orderNo={} reason={}",
                    POINT, orderNo, ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
    }
}
