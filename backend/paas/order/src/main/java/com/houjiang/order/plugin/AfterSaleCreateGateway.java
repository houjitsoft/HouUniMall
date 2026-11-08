package com.houjiang.order.plugin;

import com.houjiang.plugin.api.extension.order.AfterSaleCreateExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * 售后单创建扩展点网关（order 域，R1.2 / §1.1 薄网关）。
 *
 * <p><b>before* 语义</b>：退款 / 退货 / 换货申请前的校验，只读、可中断，只能调整
 * {@code approveAmount}，不得改写 {@code paidAmount}（只读输入）。领域夹取：
 * 核定可退金额夹在 {@code [0, paidAmount]}，契约不变量 {@code 0 <= approveAmount <= paidAmount}。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class AfterSaleCreateGateway {

    /** 扩展点短名。 */
    public static final String POINT = "AfterSaleCreate";

    /** 仅退款。 */
    public static final String TYPE_REFUND = "REFUND";
    /** 退货退款。 */
    public static final String TYPE_RETURN = "RETURN";
    /** 换货。 */
    public static final String TYPE_EXCHANGE = "EXCHANGE";

    private static final Logger log = LoggerFactory.getLogger(AfterSaleCreateGateway.class);

    private final ExtensionGatewaySupport support;

    public AfterSaleCreateGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /** 核定结果。 */
    public record Result(BigDecimal approveAmount, boolean aborted, String abortReason) {
    }

    /**
     * 售后创建前校验。
     *
     * @param order         原订单（只读），可为 {@code null}
     * @param orderItem     订单明细（只读），可为 {@code null}
     * @param type          售后类型：{@link #TYPE_REFUND} / {@link #TYPE_RETURN} / {@link #TYPE_EXCHANGE}
     * @param quantity      退款数量，可为 {@code null}
     * @param paidAmount    该明细实付金额（只读），为 {@code null} 时按 0 处理
     * @param approveAmount 核定可退金额（可改写），为 {@code null} 时按实付金额处理
     * @param reason        用户填写原因（只读），可为 {@code null}
     * @param params        透传参数，可为 {@code null}
     */
    public Result beforeCreate(Object order, Object orderItem, String type, Integer quantity,
                               BigDecimal paidAmount, BigDecimal approveAmount, String reason,
                               Map<String, Object> params) {
        BigDecimal paid = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        BigDecimal approve = approveAmount == null ? paid : approveAmount;

        AfterSaleCreateExtension.Context ctx = new AfterSaleCreateExtension.Context();
        ctx.order = order;
        ctx.orderItem = orderItem;
        ctx.type = type == null ? TYPE_REFUND : type;
        ctx.quantity = quantity;
        ctx.paidAmount = paid;
        ctx.approveAmount = approve;
        ctx.reason = reason;
        if (params != null) {
            ctx.params = params;
        }

        support.invoke(AfterSaleCreateExtension.class, ctx);

        if (ctx.isAborted()) {
            log.info("[order.plugin] 售后申请被二开拦截：type={}, reason={}", ctx.type, ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
        return new Result(clampApprove(ctx.approveAmount, paid), false, null);
    }

    /** 领域夹取：核定可退金额 ∈ [0, paidAmount]。 */
    private BigDecimal clampApprove(BigDecimal approveAmount, BigDecimal paidAmount) {
        if (approveAmount == null) {
            return paidAmount;
        }
        if (approveAmount.signum() < 0) {
            log.warn("[order.plugin] 售后核定金额为负，已夹为 0：raw={}, paidAmount={}", approveAmount, paidAmount);
            return BigDecimal.ZERO;
        }
        if (approveAmount.compareTo(paidAmount) > 0) {
            log.warn("[order.plugin] 售后核定金额超过实付，已夹为实付：raw={}, paidAmount={}",
                    approveAmount, paidAmount);
            return paidAmount;
        }
        return approveAmount;
    }
}
