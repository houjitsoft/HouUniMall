package com.houjiang.plugin.runtime;

import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;

/**
 * 扩展点故障中断异常（R3.2 / §1.1）。
 *
 * <p>当扩展点发生<b>故障</b>（抛异常 / 超时），且该扩展点的降级策略为
 * {@code ABORT}（fail-closed，锁定集：金额 / 库存 / 支付等）时，引擎的
 * {@code ExtensionInvoker} 抛出引擎内部的 {@code ExtensionFaultException}，
 * 由 {@link com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport} 统一转译为本异常，
 * 携带宿主业务错误码 {@link BizCodeEnume#PLUGIN_FAULT}（70013，旧码 7002）回传业务层。
 *
 * <p>这是「宁可让主链路失败，也不带着错误的金额 / 库存继续」的兜底：
 * 例如价格扩展点故障时，绝不允许按错误的最终价成交。
 *
 * <p>IGNORE 类扩展点故障时不会抛出本异常（引擎记录并忽略）；FALLBACK 类故障时回落官方取值，
 * 同样不抛。
 *
 * @author HouCloud
 */
public class PluginFaultException extends DefinedException {

    private static final long serialVersionUID = 7002L;

    /** 发生故障的扩展点短名，如 {@code PriceCalc}。 */
    private final String point;

    /** 故障描述（异常信息 / 超时说明）。 */
    private final String reason;

    /**
     * 构造器。
     *
     * @param point  扩展点短名
     * @param reason 故障描述
     * @param cause  底层故障原因，可为 {@code null}
     */
    public PluginFaultException(String point, String reason, Throwable cause) {
        super(BizCodeEnume.PLUGIN_FAULT.getCode(), "[" + point + "] " + reason);
        this.point = point;
        this.reason = reason;
        if (cause != null) {
            initCause(cause);
        }
    }

    /** @return 发生故障的扩展点短名 */
    public String getPoint() {
        return point;
    }

    /** @return 故障描述 */
    public String getReason() {
        return reason;
    }
}
