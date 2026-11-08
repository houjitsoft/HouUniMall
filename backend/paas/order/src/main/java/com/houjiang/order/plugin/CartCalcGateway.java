package com.houjiang.order.plugin;

import com.houjiang.plugin.api.extension.order.CartCalcExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * 购物车算价扩展点网关（order 域，R1.2 / §1.1 薄网关）。
 *
 * <p><b>after* 语义</b>：官方算价完成后调整金额，回读 {@code discountAmount} / {@code payAmount}。
 * 契约不变量：{@code payAmount = goodsAmount - discountAmount}，{@code 0 <= discountAmount <= goodsAmount}。
 * 领域夹取：折扣夹在 {@code [0, goodsAmount]}，再据夹取后的折扣重算应付，避免二开把金额算成负数。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class CartCalcGateway {

    /** 扩展点短名。 */
    public static final String POINT = "CartCalc";

    private static final Logger log = LoggerFactory.getLogger(CartCalcGateway.class);

    private final ExtensionGatewaySupport support;

    public CartCalcGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /** 算价结果。 */
    public record Result(BigDecimal discountAmount, BigDecimal payAmount, boolean aborted, String abortReason) {
    }

    /**
     * 执行购物车算价。
     *
     * @param cart        购物车对象（只读），可为 {@code null}
     * @param goodsAmount 官方商品总额（只读），为 {@code null} 时按 0 处理
     * @param extra       透传参数，可为 {@code null}
     */
    public Result calc(Object cart, BigDecimal goodsAmount, Map<String, Object> extra) {
        BigDecimal goods = goodsAmount == null ? BigDecimal.ZERO : goodsAmount;
        CartCalcExtension.Context ctx = new CartCalcExtension.Context();
        ctx.cart = cart;
        ctx.goodsAmount = goods;
        ctx.discountAmount = BigDecimal.ZERO;
        ctx.payAmount = goods;
        if (extra != null) {
            ctx.extra = extra;
        }

        support.invoke(CartCalcExtension.class, ctx);

        if (ctx.isAborted()) {
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
        BigDecimal discount = clampDiscount(ctx.discountAmount, goods);
        return new Result(discount, goods.subtract(discount), false, null);
    }

    /** 领域夹取：折扣 ∈ [0, goodsAmount]。 */
    private BigDecimal clampDiscount(BigDecimal discountAmount, BigDecimal goodsAmount) {
        if (discountAmount == null) {
            return BigDecimal.ZERO;
        }
        if (discountAmount.signum() < 0) {
            log.warn("[order.plugin] 购物车算价返回负折扣，已夹为 0：raw={}, goodsAmount={}",
                    discountAmount, goodsAmount);
            return BigDecimal.ZERO;
        }
        if (discountAmount.compareTo(goodsAmount) > 0) {
            log.warn("[order.plugin] 购物车算价折扣超过商品总额，已夹为总额：raw={}, goodsAmount={}",
                    discountAmount, goodsAmount);
            return goodsAmount;
        }
        return discountAmount;
    }
}
