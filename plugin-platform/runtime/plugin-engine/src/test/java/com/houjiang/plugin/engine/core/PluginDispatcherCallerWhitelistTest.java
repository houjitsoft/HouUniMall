package com.houjiang.plugin.engine.core;

import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.engine.security.PluginPermissionEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 插件间调用方白名单（{@code @HouRoute(allowCallers)}）的分发期判定。
 *
 * <p>背景：分发器原先只校验「当前用户有没有权限」，<b>没有调用方插件身份</b>——
 * 插件 A 只要用户有权限就能调插件 B 的接口。本套用例锁定新加的调用方判定，
 * 并特别覆盖「前端伪造来源头无效」，避免白名单被一行 {@code fetch} 绕过。
 *
 * @author HouCloud
 */
class PluginDispatcherCallerWhitelistTest {

    static class Endpoint {
        public String ping() {
            return "pong";
        }
    }

    private static PluginPermissionEvaluator permissions() {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        when(permissions.hasPermission(anyString())).thenReturn(true);
        return permissions;
    }

    /** 建一条 demo 插件的 /ping 路由，allowCallers 可为 null（= 不限制）。 */
    private static RouteRegistry routes(Set<String> allowCallers) throws Exception {
        RouteRegistry routes = new RouteRegistry();
        RouteRegistry.RouteEntry entry = new RouteRegistry.RouteEntry();
        entry.code = "demo";
        entry.path = "/ping";
        entry.httpMethod = "GET";
        entry.bean = new Endpoint();
        entry.method = Endpoint.class.getMethod("ping");
        entry.permission = "demo:ping";
        entry.allowCallers = allowCallers;
        routes.register(entry);
        return routes;
    }

    private static PluginDispatcher dispatcher(Set<String> allowCallers,
                                               boolean enforceCallerWhitelist) throws Exception {
        PluginRegistry plugins = new PluginRegistry();
        plugins.putState("demo", PluginState.ACTIVE);
        return new PluginDispatcher(routes(allowCallers), plugins, permissions(),
                false, null, enforceCallerWhitelist);
    }

    /** 宿主侧请求：不带任何可信 attribute。 */
    private static MockHttpServletRequest hostRequest() {
        return new MockHttpServletRequest("GET", "/plugin/demo/ping");
    }

    /** 插件内部调用请求：由宿主可信过滤器写入调用方身份。 */
    private static MockHttpServletRequest callerRequest(String pluginCode) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/plugin/demo/ping");
        request.setAttribute(PluginDispatcher.ATTRIBUTE_TRUSTED_PLUGIN_CALLER, pluginCode);
        return request;
    }

    @Test
    void undeclaredAllowCallersKeepsLegacyBehaviourWhenSwitchIsOff() throws Exception {
        PluginDispatcher dispatcher = dispatcher(null, false);

        assertEquals(200, dispatcher.dispatch("demo", hostRequest()).getStatusCode().value());
        assertEquals(200, dispatcher.dispatch("demo", callerRequest("erp")).getStatusCode().value(),
                "默认 false 时必须保持历史行为，否则会打断既有的插件间 Feign 调用");
    }

    @Test
    void undeclaredAllowCallersRejectsPluginCallersWhenSwitchIsOn() throws Exception {
        PluginDispatcher dispatcher = dispatcher(null, true);

        assertEquals(200, dispatcher.dispatch("demo", hostRequest()).getStatusCode().value(),
                "fail-closed 只挡插件间调用，宿主用户不受影响");
        assertEquals(403, dispatcher.dispatch("demo", callerRequest("erp")).getStatusCode().value());
    }

    @Test
    void declaredAllowCallersOnlyAdmitsNamedPlugin() throws Exception {
        PluginDispatcher dispatcher = dispatcher(Set.of("erp"), false);

        assertEquals(200, dispatcher.dispatch("demo", callerRequest("erp")).getStatusCode().value());
        ResponseEntity<?> denied = dispatcher.dispatch("demo", callerRequest("crm"));
        assertEquals(403, denied.getStatusCode().value());
        assertEquals("该插件接口不允许当前调用方访问", ((Map<?, ?>) denied.getBody()).get("error"));
    }

    @Test
    void declaredAllowCallersExcludesHostUnlessExplicitlyListed() throws Exception {
        PluginDispatcher hostOnly = dispatcher(Set.of("host"), false);
        assertEquals(200, hostOnly.dispatch("demo", hostRequest()).getStatusCode().value());
        assertEquals(403, hostOnly.dispatch("demo", callerRequest("erp")).getStatusCode().value());

        PluginDispatcher both = dispatcher(Set.of("erp", "host"), false);
        assertEquals(200, both.dispatch("demo", hostRequest()).getStatusCode().value());
        assertEquals(200, both.dispatch("demo", callerRequest("erp")).getStatusCode().value());
    }

    @Test
    void wildcardAllowsAnyPluginButNotHost() throws Exception {
        PluginDispatcher dispatcher = dispatcher(Set.of("*"), false);

        assertEquals(200, dispatcher.dispatch("demo", callerRequest("erp")).getStatusCode().value());
        assertEquals(200, dispatcher.dispatch("demo", callerRequest("anything")).getStatusCode().value());
        assertEquals(403, dispatcher.dispatch("demo", hostRequest()).getStatusCode().value(),
                "* 只代表任意插件，宿主必须显式写 host，避免一次通配把接口完全放开");
    }

    /**
     * 前端伪造来源头必须无效：插件产物与宿主同处一个 JS 运行时，
     * 任何人都能加 {@code X-Plugin-Caller}。采信请求头等于没有校验。
     */
    @Test
    void forgedCallerHeaderIsIgnored() throws Exception {
        PluginDispatcher dispatcher = dispatcher(Set.of("erp"), false);

        MockHttpServletRequest forged = hostRequest();
        forged.addHeader("X-Plugin-Caller", "erp");

        assertEquals(403, dispatcher.dispatch("demo", forged).getStatusCode().value(),
                "只带来源头、没有服务端可信 attribute，必须按宿主判定并拒绝");
    }

    /**
     * 宿主内部链路（job 服务凭内部任务令牌）是一个独立的调用方代号 {@code internal}：
     * 它既不是浏览器用户（host），也不是另一个插件。声明只允许 internal 的定时接口，
     * 浏览器用户不得直连触发。
     */
    @Test
    void internalCallerIsDistinctFromHostAndPlugins() throws Exception {
        PluginDispatcher internalOnly = dispatcher(Set.of(PluginDispatcher.CALLER_INTERNAL), true);

        MockHttpServletRequest jobRequest = hostRequest();
        PluginDispatcher.markCaller(jobRequest, PluginDispatcher.CALLER_INTERNAL);
        assertEquals(200, internalOnly.dispatch("demo", jobRequest).getStatusCode().value(),
                "job 链路写入 internal 身份后必须放行");
        assertEquals(403, internalOnly.dispatch("demo", hostRequest()).getStatusCode().value(),
                "声明只允许 internal 时，浏览器用户不得直连内部定时接口");
        assertEquals(403, internalOnly.dispatch("demo", callerRequest("erp")).getStatusCode().value(),
                "internal 不等于任意插件");
    }

    /**
     * {@code markCaller} 是宿主演进过滤器写入身份的唯一入口，必须做归一与清理：
     * 大小写 / 空白不能让同一调用方变成两种身份，传空必须退回宿主。
     */
    @Test
    void markCallerNormalizesCaseAndClearsOnBlank() {
        MockHttpServletRequest request = hostRequest();
        PluginDispatcher.markCaller(request, "  ERP ");
        assertEquals("erp", request.getAttribute(PluginDispatcher.ATTRIBUTE_TRUSTED_PLUGIN_CALLER));

        PluginDispatcher.markCaller(request, "   ");
        assertNull(request.getAttribute(PluginDispatcher.ATTRIBUTE_TRUSTED_PLUGIN_CALLER),
                "传空必须清除身份，退回宿主判定");
    }

    @Test
    void configKeyBindsToEnforceCallerWhitelistField() {
        PluginEngineProperties bound = new Binder(new MapConfigurationPropertySource(
                Map.of("houcloud.plugin.enforce-caller-whitelist", "true")))
                .bind("houcloud.plugin", Bindable.of(PluginEngineProperties.class))
                .orElseThrow(() -> new IllegalStateException("应能绑定出 PluginEngineProperties"));

        assertTrue(bound.isEnforceCallerWhitelist(),
                "配置键 houcloud.plugin.enforce-caller-whitelist 必须可达该字段，否则又是一个死开关");
        assertFalse(new PluginEngineProperties().isEnforceCallerWhitelist(),
                "默认必须为 false（存量插件未声明 allowCallers，默认开即打断既有插件间调用）");
    }
}
