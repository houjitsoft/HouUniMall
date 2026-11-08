package com.houjiang.order.plugin;

import com.houjiang.plugin.api.extension.order.OrderCreateBeforeExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * 下单前扩展点网关（order 域，R1.2 / §1.1 薄网关）。
 *
 * <p><b>before* 语义</b>：在订单入参组装后、写库前执行，只读、可中断，不修改下单结果。
 * {@code ctx.abort(reason)} → 抛 {@link PluginAbortException}，由业务层回滚事务、原样回传原因。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class OrderCreateBeforeGateway {

    /** 扩展点短名。 */
    public static final String POINT = "OrderCreateBefore";

    private static final Logger log = LoggerFactory.getLogger(OrderCreateBeforeGateway.class);

    private final ExtensionGatewaySupport support;

    public OrderCreateBeforeGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /**
     * 下单前校验。
     *
     * @param order  待创建订单（只读），可为 {@code null}
     * @param params 入参快照，供二开读取用于校验，可为 {@code null}
     */
    public void validateBefore(Object order, Map<String, Object> params) {
        OrderCreateBeforeExtension.Context ctx = new OrderCreateBeforeExtension.Context();
        ctx.order = order;
        if (params != null) {
            ctx.params = params;
        }

        support.invoke(OrderCreateBeforeExtension.class, ctx);

        if (ctx.isAborted()) {
            log.info("[order.plugin] 下单被二开前置拦截：reason={}", ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
    }
}
