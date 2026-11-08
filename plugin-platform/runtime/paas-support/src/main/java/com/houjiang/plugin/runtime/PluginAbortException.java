package com.houjiang.plugin.runtime;

import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;

/**
 * 扩展点业务中断异常（R1 / §1.1）。
 *
 * <p>{@code ExtensionContext#abort(reason)} 是<b>业务语义</b>（限购不通过、库存不足、风控拦截等），
 * 不是故障。领域网关在 {@code ExtensionGatewaySupport#invoke} 返回后检查
 * {@code ctx.isAborted()}，若为真则抛出本异常，由全局异常处理器按业务错误码回传前端。
 *
 * <p>错误码固定为 {@link BizCodeEnume#PLUGIN_ABORT}（70012，旧码 7001），{@code reason} 原样透传，
 * 便于运营定位是哪个扩展点因何中断。
 *
 * <p><b>与故障的区别</b>：扩展点抛异常 / 超时属<b>故障</b>，走引擎降级策略
 * （IGNORE / ABORT / FALLBACK），由 {@code ExtensionGatewaySupport} 统一处理，
 * 与本异常无关。
 *
 * @author HouCloud
 */
public class PluginAbortException extends DefinedException {

    private static final long serialVersionUID = 7001L;

    /** 触发中断的扩展点短名，如 {@code PriceCalc}。 */
    private final String point;

    /** 扩展点调用 {@code ctx.abort(reason)} 传入的中断原因。 */
    private final String reason;

    /**
     * 唯一构造器。
     *
     * @param point  扩展点短名（如 {@code "PriceCalc"}）
     * @param reason 中断原因，直接透传 {@code ctx.getAbortReason()}
     */
    public PluginAbortException(String point, String reason) {
        super(BizCodeEnume.PLUGIN_ABORT.getCode(), "[" + point + "] " + reason);
        this.point = point;
        this.reason = reason;
    }

    /** @return 触发中断的扩展点短名 */
    public String getPoint() {
        return point;
    }

    /** @return 中断原因 */
    public String getReason() {
        return reason;
    }
}
