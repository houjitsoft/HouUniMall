package com.houjiang.goods.plugin;

import com.houjiang.plugin.api.extension.goods.StockChangeExtension;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 库存变动扩展点网关（goods 域，R1.1 / §1.1 薄网关）。
 *
 * <p><b>after* 语义</b>：库存扣减 / 回滚<b>发生并落库后</b>执行，只做同步与通知，
 * 不回写业务对象，也不再中断主链路。二开实现只能在其上追加副作用（同步外部仓储 / 通知 / 记账），
 * 入参 {@code skuId} / {@code delta} / {@code type} / {@code order} 是既成事实，不可改写。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class StockChangeGateway {

    /** 扩展点短名。 */
    public static final String POINT = "StockChange";

    /** 出库（扣减）。 */
    public static final String TYPE_OUT = "OUT";
    /** 入库（增加）。 */
    public static final String TYPE_IN = "IN";
    /** 回滚。 */
    public static final String TYPE_ROLLBACK = "ROLLBACK";

    private static final Logger log = LoggerFactory.getLogger(StockChangeGateway.class);

    private final ExtensionGatewaySupport support;

    public StockChangeGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /**
     * 库存变动后通知扩展点。
     *
     * @param skuId 商品 / SKU 标识
     * @param delta 变动量（扣减为负、增加为正）
     * @param type  变动类型：{@link #TYPE_OUT} / {@link #TYPE_IN} / {@link #TYPE_ROLLBACK}
     * @param order 关联订单对象，可为 {@code null}
     */
    public void onChange(Long skuId, Integer delta, String type, Object order) {
        StockChangeExtension.Context ctx = new StockChangeExtension.Context();
        ctx.skuId = skuId;
        ctx.delta = delta;
        ctx.type = type;
        ctx.order = order;

        support.invoke(StockChangeExtension.class, ctx);
        if (log.isDebugEnabled()) {
            log.debug("[goods.plugin] 库存变动已通知二开：skuId={}, delta={}, type={}", skuId, delta, type);
        }
    }
}
