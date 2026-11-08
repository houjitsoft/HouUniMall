package com.houjiang.goods.plugin;

import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * 商品取价扩展点网关（goods 域，R1.1 / §1.1 薄网关）。
 *
 * <p>职责边界：只做「业务对象 ↔ {@link PriceCalcExtension.Context} 映射 + abort → {@link PluginAbortException}
 * + 领域夹取」；超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理，
 * 本类不重复实现。
 *
 * <p><b>after* 语义</b>：官方价（含千客千价）算完后回调扩展点，回读 {@code finalPrice} 并写回。
 * 领域夹取：最终价必须落在 {@code [0, basePrice]}——二开写入负价或高于原价都属异常，
 * 直接采信会让用户倒贴或多付，故夹取并告警。
 *
 * @author Amos
 */
public class PriceCalcGateway {

    /** 扩展点短名，用于中断异常与日志。 */
    public static final String POINT = "PriceCalc";

    private static final Logger log = LoggerFactory.getLogger(PriceCalcGateway.class);

    private final ExtensionGatewaySupport support;

    public PriceCalcGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /** 取价结果。 */
    public record Result(BigDecimal finalPrice, boolean aborted, String abortReason) {
    }

    /**
     * 执行商品取价。
     *
     * @param skuId     商品 / SKU 标识，可为 {@code null}
     * @param basePrice 官方计算出的原价（只读输入），为 {@code null} 时按 0 处理
     * @param extra     透传参数（用户、门店等），可为 {@code null}
     */
    public Result calc(Long skuId, BigDecimal basePrice, Map<String, Object> extra) {
        BigDecimal base = basePrice == null ? BigDecimal.ZERO : basePrice;
        PriceCalcExtension.Context ctx = new PriceCalcExtension.Context();
        ctx.skuId = skuId;
        ctx.basePrice = base;
        ctx.finalPrice = base;
        if (extra != null) {
            ctx.extra = extra;
        }

        support.invoke(PriceCalcExtension.class, ctx);

        if (ctx.isAborted()) {
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
        return new Result(clamp(ctx.finalPrice, base), false, null);
    }

    /** 领域夹取：最终价 ∈ [0, basePrice]。 */
    private BigDecimal clamp(BigDecimal finalPrice, BigDecimal base) {
        if (finalPrice == null) {
            return base;
        }
        if (finalPrice.signum() < 0) {
            log.warn("[goods.plugin] 取价扩展点返回负价，已夹为 0：raw={}, base={}", finalPrice, base);
            return BigDecimal.ZERO;
        }
        if (finalPrice.compareTo(base) > 0) {
            log.warn("[goods.plugin] 取价扩展点返回价高于原价，已夹为原价：raw={}, base={}", finalPrice, base);
            return base;
        }
        return finalPrice;
    }
}
