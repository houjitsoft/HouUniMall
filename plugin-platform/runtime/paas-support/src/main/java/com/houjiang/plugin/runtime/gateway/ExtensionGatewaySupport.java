package com.houjiang.plugin.runtime.gateway;

import com.houjiang.plugin.engine.registry.ExtensionFaultException;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.runtime.PluginFaultException;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 宿主统一扩展点调用骨架（R1 / §1.1「薄网关三件套」的共享底座）。
 *
 * <p>所有领域网关（{@code PriceCalcGateway}、{@code OrderCreateBeforeGateway} 等）复用本类，
 * 把「调用 + 超时 + 熔断 + 降级 + 可观测 + 故障转译」收敛到一处；领域网关只负责
 * 「业务对象 ↔ Context 映射 + 领域夹取」。
 *
 * <p><b>语义边界</b>：
 * <ul>
 *   <li>{@code ctx.abort(reason)} 是<b>业务中断</b>，本类<b>不</b>抛异常；领域网关在
 *       {@code invoke} 返回后检查 {@code ctx.isAborted()} 并抛
 *       {@link com.houjiang.plugin.runtime.PluginAbortException}（70012，旧码 7001）。</li>
 *   <li>扩展点<b>故障</b>（抛异常 / 超时）由引擎按扩展点降级策略判定：{@code IGNORE}/{@code FALLBACK}
 *       记录并继续（不抛）；策略为 {@code ABORT} 时引擎抛 {@link ExtensionFaultException}，
 *       本类将其<b>类型转译</b>为 {@link PluginFaultException}（70013，旧码 7002）向业务层传播，做到 fail-closed。</li>
 * </ul>
 *
 * <p><b>职责边界（契约，架构师裁定）</b>：<b>本类是业务错误码的唯一出口</b>——只做
 * 「引擎内部异常 → 业务异常」的<b>类型转译</b>（{@link PluginFaultException} = 70013），
 * <b>不做降级策略判定</b>。「propagate vs swallow」是隔离语义，归属引擎
 * {@code ExtensionInvoker}/{@code ExtensionPolicyRegistry}（策略判定的唯一归宿），
 * 不在此处复制策略真相，避免引擎/宿主双份策略来源。
 *
 * @author HouCloud
 */
public class ExtensionGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(ExtensionGatewaySupport.class);

    private final ExtensionInvoker invoker;

    /**
     * 构造器。
     *
     * @param invoker 引擎统一调用模板，不可为 {@code null}
     */
    public ExtensionGatewaySupport(ExtensionInvoker invoker) {
        this.invoker = Objects.requireNonNull(invoker, "invoker");
    }

    /**
     * 调用一个扩展点。
     *
     * <p>超时 / 熔断 / 降级 / 可观测均在此完成；仅当锁定集扩展点发生故障（ABORT 策略）时，
     * 才会抛出 {@link PluginFaultException} 中断主链路。
     *
     * @param type 扩展点类型
     * @param ctx  扩展点上下文
     * @param <E>  扩展点类型
     * @param <C>  上下文类型
     */
    public <E extends ExtensionPoint<C>, C extends ExtensionContext> void invoke(Class<E> type, C ctx) {
        try {
            invoker.invoke(type, ctx);
        } catch (ExtensionFaultException fault) {
            String point = shortNameOf(type);
            log.error("[plugin-runtime] 扩展点故障中断（fail-closed）：point={}, source={}",
                    point, fault.getSource(), fault);
            throw new PluginFaultException(point, fault.getMessage(), fault);
        }
    }

    /** 扩展点短名：{@code PriceCalcExtension} → {@code PriceCalc}。 */
    private static String shortNameOf(Class<?> type) {
        String simpleName = type.getSimpleName();
        if (simpleName.endsWith("Extension") && simpleName.length() > "Extension".length()) {
            return simpleName.substring(0, simpleName.length() - "Extension".length());
        }
        return simpleName;
    }
}
