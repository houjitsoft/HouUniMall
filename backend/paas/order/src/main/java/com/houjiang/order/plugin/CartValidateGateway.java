package com.houjiang.order.plugin;

import com.houjiang.plugin.api.extension.order.CartValidateExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 购物车校验扩展点网关（order 域，R1.2 / §1.1 薄网关）。
 *
 * <p><b>before* 语义</b>：加购 / 改数量 / 结算前的校验与拦截，只读、可中断，不修改购物车。
 * 典型用途：限购、地区不可售、混装限制。{@code ctx.abort(reason)} → 抛 {@link PluginAbortException}。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理。
 *
 * @author Amos
 */
public class CartValidateGateway {

    /** 扩展点短名。 */
    public static final String POINT = "CartValidate";

    /** 加购。 */
    public static final String ACTION_ADD = "ADD";
    /** 改数量。 */
    public static final String ACTION_UPDATE = "UPDATE";
    /** 结算。 */
    public static final String ACTION_CHECKOUT = "CHECKOUT";

    private static final Logger log = LoggerFactory.getLogger(CartValidateGateway.class);

    private final ExtensionGatewaySupport support;

    public CartValidateGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /**
     * 校验购物车操作。
     *
     * @param cart   购物车对象（只读），可为 {@code null}
     * @param items  购物车行（只读），可为 {@code null}
     * @param action 操作类型：{@link #ACTION_ADD} / {@link #ACTION_UPDATE} / {@link #ACTION_CHECKOUT}，缺省按加购
     * @param params 透传参数（限购规则等），可为 {@code null}
     */
    public void validate(Object cart, List<Object> items, String action, Map<String, Object> params) {
        CartValidateExtension.Context ctx = new CartValidateExtension.Context();
        ctx.cart = cart;
        if (items != null) {
            ctx.items = items;
        }
        ctx.action = action == null ? ACTION_ADD : action;
        if (params != null) {
            ctx.params = params;
        }

        support.invoke(CartValidateExtension.class, ctx);

        if (ctx.isAborted()) {
            log.info("[order.plugin] 购物车校验被二开拦截：action={}, reason={}", ctx.action, ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
    }
}
