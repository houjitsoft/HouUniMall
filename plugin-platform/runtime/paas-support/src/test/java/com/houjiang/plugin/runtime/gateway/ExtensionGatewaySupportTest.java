package com.houjiang.plugin.runtime.gateway;

import com.houjiang.plugin.engine.config.ExtensionPolicyRegistry;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.observability.ExtensionMetrics;
import com.houjiang.plugin.engine.registry.ExtensionDisableSwitch;
import com.houjiang.plugin.engine.registry.ExtensionHealthTracker;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.PluginFaultException;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 宿主统一骨架 {@link ExtensionGatewaySupport} 单测（R1 / §1.1）。
 *
 * <p>验证冻结契约：锁定集（ABORT）故障 → 抛 {@link PluginFaultException}（70013）fail-closed；
 * IGNORE 点故障 → 不抛、主流程继续；{@code ctx.abort} 为业务语义，本骨架不在此抛
 * {@link PluginAbortException}（由领域网关在 invoke 返回后检查 {@code isAborted()} 抛出）。
 *
 * @author HouCloud
 */
class ExtensionGatewaySupportTest {

    /** 未知扩展点默认 ABORT（锁定集语义由策略表按短名判定，未知点 fail-closed）。 */
    interface Probe extends ExtensionPoint<ExtensionContext> {
    }

    static final class Throwing implements Probe {
        @Override
        public void execute(ExtensionContext context) {
            throw new IllegalStateException("插件炸了");
        }
    }

    static final class Aborting implements Probe {
        @Override
        public void execute(ExtensionContext context) {
            context.abort("超过单笔限额");
        }
    }

    private static PluginEngineProperties properties() {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(200);
        properties.setExtensionMaxConcurrent(4);
        return properties;
    }

    private static ExtensionInvoker invokerWith(ExtensionRegistry registry, ExtensionPolicyRegistry policy) {
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        ExtensionHealthTracker healthTracker = new ExtensionHealthTracker(disableSwitch, 5);
        return new ExtensionInvoker(registry, properties(), policy, healthTracker, new ExtensionMetrics());
    }

    @Test
    void abortPolicyFaultBecomesPluginFaultWithUnifiedCode70013() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new Throwing(), "custom-x", 10);
        ExtensionInvoker invoker = invokerWith(registry, new ExtensionPolicyRegistry());
        try {
            ExtensionGatewaySupport support = new ExtensionGatewaySupport(invoker);
            PluginFaultException fault = assertThrows(PluginFaultException.class,
                    () -> support.invoke(Probe.class, new ExtensionContext() {
                    }));
            assertEquals(70013, fault.getMessageCode(), "锁定集故障应为 PLUGIN_EXTENSION_FAULT=70013");
            assertEquals("Probe", fault.getPoint());
        } finally {
            invoker.shutdown();
        }
    }

    @Test
    void ignorePolicyFaultDoesNotThrow() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new Throwing(), "custom-x", 10);
        ExtensionPolicyRegistry policy = new ExtensionPolicyRegistry();
        policy.applyOverrides(Map.of("Probe", "IGNORE"));
        ExtensionInvoker invoker = invokerWith(registry, policy);
        try {
            ExtensionGatewaySupport support = new ExtensionGatewaySupport(invoker);
            // IGNORE：故障被隔离，主流程继续，不抛
            support.invoke(Probe.class, new ExtensionContext() {
            });
        } finally {
            invoker.shutdown();
        }
    }

    @Test
    void contextAbortIsNotThrownBySupport() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new Aborting(), "custom-x", 10);
        ExtensionInvoker invoker = invokerWith(registry, new ExtensionPolicyRegistry());
        try {
            ExtensionGatewaySupport support = new ExtensionGatewaySupport(invoker);
            ExtensionContext ctx = new ExtensionContext() {
            };
            support.invoke(Probe.class, ctx);
            assertTrue(ctx.isAborted(), "abort 是业务语义，由领域网关在返回后处理");
        } finally {
            invoker.shutdown();
        }
    }

    @Test
    void pluginAbortExceptionCarriesUnifiedCode70012AndReason() {
        PluginAbortException ex = new PluginAbortException("PriceCalc", "限购不通过");
        assertEquals(70012, ex.getMessageCode());
        assertEquals("PriceCalc", ex.getPoint());
        assertEquals("限购不通过", ex.getReason());
    }
}
