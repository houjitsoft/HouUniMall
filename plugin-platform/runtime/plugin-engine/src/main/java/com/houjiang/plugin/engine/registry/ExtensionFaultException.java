package com.houjiang.plugin.engine.registry;

/**
 * 扩展点故障中断（引擎内部异常，R3.2）。
 *
 * <p>当某个扩展点发生故障（抛异常 / 超时），且其降级策略为 {@code ABORT}
 * （fail-closed，锁定集：金额 / 库存 / 支付等）时，{@link ExtensionInvoker} 抛出本异常。
 *
 * <p>引擎位于 {@code plugin-platform}，不依赖宿主基础库 {@code commons}，因此不能直接抛出
 * {@code DefinedException}；宿主侧网关（{@code ExtensionGatewaySupport}）会捕获本异常并
 * 转译为携带业务错误码 7002 的 {@code PluginFaultException}。
 *
 * @author HouCloud
 */
public class ExtensionFaultException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 发生故障的扩展点短名，如 {@code PriceCalc}。 */
    private final String point;

    /** 发生故障的实现来源，如插件编码 {@code custom-goods}。 */
    private final String source;

    /**
     * 构造器。
     *
     * @param point   扩展点短名
     * @param source  故障实现来源
     * @param message 故障描述
     * @param cause   底层原因，可为 {@code null}
     */
    public ExtensionFaultException(String point, String source, String message, Throwable cause) {
        super(message, cause);
        this.point = point;
        this.source = source;
    }

    /** @return 发生故障的扩展点短名 */
    public String getPoint() {
        return point;
    }

    /** @return 发生故障的实现来源 */
    public String getSource() {
        return source;
    }
}
