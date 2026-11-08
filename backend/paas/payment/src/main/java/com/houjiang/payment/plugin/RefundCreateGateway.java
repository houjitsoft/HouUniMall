package com.houjiang.payment.plugin;

import com.houjiang.plugin.api.extension.payment.RefundCreateExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 退款单创建扩展点的宿主薄网关（{@code before*} 语义，可中断）。
 *
 * <p><b>做什么</b>：在「退款单写库 / 发起渠道退款之前」让二开做校验与金额核定。
 * 典型用法是限额、风控、白名单校验，并在必要时 {@code ctx.abort("原因")} 阻断退款。
 *
 * <p><b>{@code before*} 语义</b>：
 * <ul>
 *   <li>插件 {@code abort} → 网关抛 {@link PluginAbortException}（7001），
 *       宿主在写库前中断，<b>退款单不会被创建</b>；</li>
 *   <li>插件可改写 {@code refundAmount}，网关做<b>领域夹取</b>后回写：
 *       夹到 {@code [0, paidAmount]} 且精度不超过 2 位小数（契约不变量
 *       {@code 0 <= refundAmount <= paidAmount}）。</li>
 * </ul>
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class RefundCreateGateway {

    private static final Logger log = LoggerFactory.getLogger(RefundCreateGateway.class);

    private static final String POINT = "RefundCreate";

    /** 金额精度：分。 */
    private static final int AMOUNT_SCALE = 2;

    private final ExtensionGatewaySupport support;

    public RefundCreateGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /** 退款核定结果：改写后的退款金额与透传参数。 */
    public record Decision(BigDecimal refundAmount, Map<String, Object> params) {
    }

    /**
     * 退款创建前触发扩展点。
     *
     * @param orderNo      商城订单号
     * @param paidAmount   订单实付金额（只读上界），{@code null} 视为 0
     * @param refundAmount 本次退款金额（插件可改写）
     * @param refundWay    退款方式：ORIGINAL / BALANCE / MANUAL
     * @param reason       退款原因
     * @param params       透传参数，可为 {@code null}
     * @return 夹取后的退款金额与（可能被插件补充的）透传参数
     * @throws PluginAbortException 当插件显式中断退款时抛出
     */
    public Decision beforeRefund(String orderNo,
                                 BigDecimal paidAmount,
                                 BigDecimal refundAmount,
                                 String refundWay,
                                 String reason,
                                 Map<String, Object> params) {
        BigDecimal paid = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        BigDecimal amount = refundAmount == null ? BigDecimal.ZERO : refundAmount;

        RefundCreateExtension.Context ctx = new RefundCreateExtension.Context();
        ctx.orderNo = orderNo;
        ctx.paidAmount = paid;
        ctx.refundAmount = amount;
        ctx.refundWay = refundWay;
        ctx.reason = reason;
        if (params != null) {
            ctx.params = new HashMap<>(params);
        }

        support.invoke(RefundCreateExtension.class, ctx);

        if (ctx.isAborted()) {
            log.warn("[ext] point={} result=abort orderNo={} reason={}",
                    POINT, orderNo, ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }

        BigDecimal clamped = clampRefund(ctx.refundAmount, paid);
        Map<String, Object> outParams = ctx.params == null ? Map.of() : ctx.params;
        return new Decision(clamped, outParams);
    }

    /**
     * 退款金额领域夹取：落在 {@code [0, paidAmount]}，并四舍五入到 2 位小数。
     *
     * <p>插件写入负数或超过实付金额都属异常，直接采信会让平台倒贴或多退，故夹取并告警。
     * 保持包可见（{@code static}）以便单元测试直接断言边界行为。
     *
     * @param raw        插件返回的退款金额（可能为 {@code null}）
     * @param paidAmount 订单实付金额
     * @return 夹取后的退款金额
     */
    static BigDecimal clampRefund(BigDecimal raw, BigDecimal paidAmount) {
        BigDecimal paid = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        if (paid.signum() < 0) {
            paid = BigDecimal.ZERO;
        }
        if (raw == null) {
            return BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        if (raw.signum() < 0) {
            log.warn("[ext] point={} 插件返回负退款金额，已夹为 0：raw={}", POINT, raw);
            return BigDecimal.ZERO.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        if (raw.compareTo(paid) > 0) {
            log.warn("[ext] point={} 退款金额超过实付金额，已夹为实付：raw={}, paid={}", POINT, raw, paid);
            return paid.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        return raw.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }
}
