package com.houjiang.payment.plugin;

import com.houjiang.plugin.api.extension.payment.PayCallbackExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link PayCallbackGateway} 契约：{@code after*} 透传、无插件零副作用、abort → 业务异常。
 */
class PayCallbackGatewayTest {

    private static ExtensionGatewaySupport support(PayCallbackExtension... extensions) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(0);
        ExtensionRegistry registry = new ExtensionRegistry();
        int priority = 0;
        for (PayCallbackExtension extension : extensions) {
            registry.register(PayCallbackExtension.class, extension, "custom-" + priority, priority, "official");
            priority += 10;
        }
        return new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));
    }

    @Test
    void 无插件时零副作用且不抛异常() {
        new PayCallbackGateway(support()).afterPayCallback("T1", "WX_JSAPI", "SUCCESS", null);
    }

    @Test
    void 支付事实原样透传给插件() {
        List<String> seen = new ArrayList<>();
        PayCallbackExtension ext = ctx -> seen.add(ctx.tradeNo + "|" + ctx.channel + "|" + ctx.status);
        new PayCallbackGateway(support(ext)).afterPayCallback("T1", "WX_JSAPI", "SUCCESS", "payload");

        assertEquals(List.of("T1|WX_JSAPI|SUCCESS"), seen);
    }

    @Test
    void 插件中断时抛可识别的业务异常() {
        PayCallbackExtension ext = ctx -> ctx.abort("对账不一致");

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> new PayCallbackGateway(support(ext))
                        .afterPayCallback("T1", "WX_JSAPI", "SUCCESS", null));

        assertEquals("PayCallback", ex.getPoint());
        assertEquals("对账不一致", ex.getReason());
        assertEquals(7001, ex.getMessageCode());
    }
}
