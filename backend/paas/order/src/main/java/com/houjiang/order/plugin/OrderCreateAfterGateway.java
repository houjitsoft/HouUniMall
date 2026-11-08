package com.houjiang.order.plugin;

import com.houjiang.plugin.api.extension.order.OrderCreateAfterExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * 下单后扩展点网关（order 域，R1.2 / §1.1 薄网关）。
 *
 * <p><b>after* 语义</b>：订单落库后执行，二开可补充 {@code params} 与 {@code result}
 * （如外部系统同步结果），但不得替换已落库的 {@code order}。宿主把 {@code params}
 * 传入响应数据容器后，二开补充的字段会随响应回传。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class OrderCreateAfterGateway {

    /** 扩展点短名。 */
    public static final String POINT = "OrderCreateAfter";

    private static final Logger log = LoggerFactory.getLogger(OrderCreateAfterGateway.class);

    private final ExtensionGatewaySupport support;

    public OrderCreateAfterGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /**
     * 下单后回调。
     *
     * @param order  已创建订单（只读，既成事实），可为 {@code null}
     * @param result 下单结果，可为 {@code null}
     * @param params 可写回容器（宿主响应数据），二开可补充字段，可为 {@code null}
     */
    public void afterCreate(Object order, Object result, Map<String, Object> params) {
        OrderCreateAfterExtension.Context ctx = new OrderCreateAfterExtension.Context();
        ctx.order = order;
        ctx.result = result;
        if (params != null) {
            ctx.params = params;
        }

        support.invoke(OrderCreateAfterExtension.class, ctx);

        if (ctx.isAborted()) {
            log.info("[order.plugin] 下单后被二开中断：reason={}", ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
    }
}
