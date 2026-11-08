package com.houjiang.auth.plugin;

import com.houjiang.plugin.api.extension.auth.LoginAfterExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LoginAfterGateway} 契约：插件写入的 attrs 出现在网关结果里；无插件返回空；abort → 业务异常。
 */
class LoginAfterGatewayTest {

    private static ExtensionGatewaySupport support(LoginAfterExtension... extensions) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(0);
        ExtensionRegistry registry = new ExtensionRegistry();
        int priority = 0;
        for (LoginAfterExtension extension : extensions) {
            registry.register(LoginAfterExtension.class, extension, "custom-" + priority, priority, "official");
            priority += 10;
        }
        return new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));
    }

    @Test
    void 无插件时返回空属性() {
        Map<String, Object> attrs = new LoginAfterGateway(support()).afterLogin(1L, "TKN", null);

        assertTrue(attrs.isEmpty());
    }

    @Test
    void 插件写入的attrs出现在结果里() {
        LoginAfterExtension ext = ctx -> {
            ctx.attrs.put("riskLevel", "LOW");
            ctx.attrs.put("welcomeBack", true);
        };

        Map<String, Object> attrs = new LoginAfterGateway(support(ext)).afterLogin(1L, "TKN", null);

        assertEquals("LOW", attrs.get("riskLevel"));
        assertEquals(true, attrs.get("welcomeBack"));
    }

    @Test
    void 预置属性被保留且可被插件读取() {
        LoginAfterExtension ext = ctx -> ctx.attrs.put("seenUserId", ctx.userId);
        Map<String, Object> attrs = new LoginAfterGateway(support(ext))
                .afterLogin(42L, "TKN", Map.of("traceId", "abc"));

        assertEquals(42L, attrs.get("seenUserId"));
        assertEquals("abc", attrs.get("traceId"));
    }

    @Test
    void 插件中断时抛可识别的业务异常() {
        LoginAfterExtension ext = ctx -> ctx.abort("账号异地登录");

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> new LoginAfterGateway(support(ext)).afterLogin(1L, "TKN", null));

        assertEquals("LoginAfter", ex.getPoint());
        assertEquals("账号异地登录", ex.getReason());
    }
}
