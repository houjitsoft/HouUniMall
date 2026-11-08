package com.houjiang.payment.plugin;

import com.houjiang.plugin.api.extension.payment.PayCallbackExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 支付回调扩展点的宿主薄网关（{@code after*} 语义）。
 *
 * <p><b>做什么</b>：把 Core 收银台回调链路里「支付渠道回调已被确认成功」这一事实，
 * 转成 {@link PayCallbackExtension.Context} 并交给引擎；二开可借此做打标 / 对账 / 风控后置处理，
 * Core 无需知道装了哪些插件。
 *
 * <p><b>为什么 {@code after*}</b>：本扩展点在「回调首次被确认成功」之后触发，属结果通知，
 * 插件不应改写支付状态。因此网关只在插件显式 {@code abort} 时把业务原因原样抛出
 * （{@link PluginAbortException}，码 7001），不做任何状态写回。
 *
 * <p><b>幂等</b>：支付渠道会重复回调，幂等由宿主在调用本网关<b>之前</b>完成
 * （收银台订单已是 SUCCESS 时直接 early-return，不会再次调用本网关），
 * 故同一回调重放不会产生二次副作用。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理，本类不重复实现。
 *
 * @author Amos
 */
public class PayCallbackGateway {

    private static final Logger log = LoggerFactory.getLogger(PayCallbackGateway.class);

    /** 扩展点短名，用于异常与日志定位。 */
    private static final String POINT = "PayCallback";

    private final ExtensionGatewaySupport support;

    public PayCallbackGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /**
     * 支付回调成功后触发扩展点。
     *
     * @param tradeNo         渠道支付流水号（幂等键之一），可为 {@code null}
     * @param channel         支付渠道 / 方式（WX_JSAPI、STRIPE、WXPAY…）
     * @param status          回调结果状态，已确认成功时固定为 {@code SUCCESS}
     * @param callbackPayload 原始回调载荷（可选，便于插件对账）
     * @throws PluginAbortException 当插件显式中断（{@code ctx.abort}）时抛出，原因原样透传
     */
    public void afterPayCallback(String tradeNo, String channel, String status, Object callbackPayload) {
        PayCallbackExtension.Context ctx = new PayCallbackExtension.Context();
        ctx.tradeNo = tradeNo;
        ctx.channel = channel;
        ctx.status = status;
        ctx.callbackPayload = callbackPayload;

        support.invoke(PayCallbackExtension.class, ctx);

        if (ctx.isAborted()) {
            log.warn("[ext] point={} result=abort reason={}", POINT, ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
    }
}
