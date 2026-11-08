package com.houjiang.order.plugin;

import com.houjiang.plugin.api.extension.order.FreightCalcExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 运费计算扩展点网关（order 域，R1.2 / §1.1 薄网关）。
 *
 * <p><b>after* 语义</b>：在官方基础运费算完后回调，回读 {@code freightFee} 并写回。
 * {@code baseFreight} 为官方算出的基础运费（只读）；二开在其之上叠加而不改写它，便于对账。
 * 领域夹取：最终运费 {@code >= 0}，防止二开写入负运费导致平台倒贴。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class FreightCalcGateway {

    /** 扩展点短名。 */
    public static final String POINT = "FreightCalc";

    private static final Logger log = LoggerFactory.getLogger(FreightCalcGateway.class);

    private final ExtensionGatewaySupport support;

    public FreightCalcGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /** 运费结果。 */
    public record Result(BigDecimal freightFee, boolean aborted, String abortReason) {
    }

    /**
     * 计算最终运费。
     *
     * @param skuId       商品 / SKU 标识，可为 {@code null}
     * @param weight      计费重量，可为 {@code null}
     * @param regionCode  收货地区编码，可为 {@code null}
     * @param baseFreight 官方基础运费（只读），为 {@code null} 时按 0 处理
     */
    public Result calc(Long skuId, BigDecimal weight, String regionCode, BigDecimal baseFreight) {
        BigDecimal base = baseFreight == null ? BigDecimal.ZERO : baseFreight;
        FreightCalcExtension.Context ctx = new FreightCalcExtension.Context();
        ctx.skuId = skuId;
        ctx.weight = weight;
        ctx.regionCode = regionCode;
        ctx.baseFreight = base;
        ctx.freightFee = base;

        support.invoke(FreightCalcExtension.class, ctx);

        if (ctx.isAborted()) {
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
        return new Result(clampFreight(ctx.freightFee, base), false, null);
    }

    /** 领域夹取：运费 >= 0；越界回落基础运费并告警。 */
    private BigDecimal clampFreight(BigDecimal freightFee, BigDecimal baseFreight) {
        if (freightFee == null) {
            return baseFreight;
        }
        if (freightFee.signum() < 0) {
            log.warn("[order.plugin] 运费扩展点返回负运费，已夹为 0：raw={}, base={}", freightFee, baseFreight);
            return BigDecimal.ZERO;
        }
        return freightFee;
    }
}
