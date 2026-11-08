package com.houjiang.goods.plugin;

import com.houjiang.plugin.api.extension.goods.StockLockExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * 库存预占扩展点网关（goods 域，R1.1 / §1.1 薄网关）。
 *
 * <p><b>before* 语义</b>：在库存变动（预占 / 释放 / 确认扣减）<b>发生前</b>执行，只读、可中断，
 * 不得改写库存结果。{@code ctx.abort(reason)} → 抛 {@link PluginAbortException}，
 * 由业务层回滚并原样回传原因。本类只做映射与中断转译；超时 / 熔断 / 降级 / 可观测
 * 由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class StockLockGateway {

    /** 扩展点短名，用于中断异常与日志。 */
    public static final String POINT = "StockLock";

    /** 预占。 */
    public static final String TYPE_LOCK = "LOCK";
    /** 释放。 */
    public static final String TYPE_UNLOCK = "UNLOCK";
    /** 确认扣减。 */
    public static final String TYPE_CONFIRM = "CONFIRM";

    private static final Logger log = LoggerFactory.getLogger(StockLockGateway.class);

    private final ExtensionGatewaySupport support;

    public StockLockGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /** 预占结果。 */
    public record Result(boolean aborted, String abortReason) {
    }

    /**
     * 在库存变动前校验。
     *
     * @param skuId     商品 / SKU 标识，可为 {@code null}
     * @param quantity  本次变动数量（正数），可为 {@code null}
     * @param type      变动类型：{@link #TYPE_LOCK} / {@link #TYPE_UNLOCK} / {@link #TYPE_CONFIRM}，缺省按预占
     * @param available 当前可用库存（只读输入）
     * @param orderNo   关联订单号，可为 {@code null}
     * @param params    透传参数（限购规则等），可为 {@code null}
     */
    public Result lock(Long skuId, Integer quantity, String type, Integer available,
                       String orderNo, Map<String, Object> params) {
        StockLockExtension.Context ctx = new StockLockExtension.Context();
        ctx.skuId = skuId;
        ctx.quantity = quantity;
        ctx.type = type == null ? TYPE_LOCK : type;
        ctx.available = available;
        ctx.orderNo = orderNo;
        if (params != null) {
            ctx.params = params;
        }

        support.invoke(StockLockExtension.class, ctx);

        if (ctx.isAborted()) {
            log.info("[goods.plugin] 库存预占被二开拦截：skuId={}, type={}, reason={}",
                    skuId, ctx.type, ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
        return new Result(false, null);
    }
}
