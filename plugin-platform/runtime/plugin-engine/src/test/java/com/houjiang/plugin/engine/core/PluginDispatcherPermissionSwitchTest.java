package com.houjiang.plugin.engine.core;

import com.houjiang.plugin.engine.PluginEngineAutoConfiguration;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 死开关接线测试：{@code require-declared-route-permission} 从
 * 「配置键 → {@link PluginEngineProperties} → {@link PluginEngineAutoConfiguration} → {@link PluginDispatcher}」
 * 全程可达。
 *
 * <p>背景：该开关原先只在 {@code PluginDispatcher} 内部存在且构造器硬编码为 {@code false}，
 * 配置里没有对应字段，因此配不上去、实际 fail-open。本测试证明补上配置字段并在 autoconfig
 * 喂入后：① 默认 {@code false} 行为不变；② 置 {@code true} 时未声明权限的路由被 fail-closed 拒绝。
 *
 * <p>测试走 {@code PluginEngineAutoConfiguration.pluginDispatcher(...)} 接线，而非直接 {@code new}，
 * 确保验证的是「配置是否真的喂进分发器」这一缺口本身。
 */
class PluginDispatcherPermissionSwitchTest {

    static class Endpoint {
        public String ping() {
            return "pong";
        }
    }

    /** 用 autoconfig 的 {@code pluginDispatcher(...)} 装配分发器，注册一条「未声明权限」路由。 */
    private static PluginDispatcher wiredDispatcher(PluginEngineProperties properties) throws Exception {
        RouteRegistry routes = new RouteRegistry();
        RouteRegistry.RouteEntry entry = new RouteRegistry.RouteEntry();
        entry.code = "demo";
        entry.path = "/ping";
        entry.httpMethod = "GET";
        entry.bean = new Endpoint();
        entry.method = Endpoint.class.getMethod("ping");
        entry.permission = ""; // 未声明权限
        routes.register(entry);

        PluginRegistry plugins = new PluginRegistry();
        plugins.putState("demo", PluginState.ACTIVE);

        PluginPermissionEvaluator evaluator = new PluginPermissionEvaluator() {
            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public boolean hasPermission(String permission) {
                return false;
            }
        };
        // 未接入排空闸门与装载闸门：getIfAvailable() 返回 null，分发器应保持 M4 之前的行为
        @SuppressWarnings("unchecked")
        org.springframework.beans.factory.ObjectProvider<PluginDrainGate> noGate =
                org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class);
        @SuppressWarnings("unchecked")
        org.springframework.beans.factory.ObjectProvider<PluginBootGate> noBootGate =
                org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class);
        return new PluginEngineAutoConfiguration()
                .pluginDispatcher(routes, plugins, evaluator, properties, noGate, noBootGate);
    }

    private static MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/plugin/demo/ping");
    }

    @Test
    void switchDefaultsToFalse() {
        assertFalse(new PluginEngineProperties().isRequireDeclaredRoutePermission(),
                "死开关默认必须为 false（pm/activity/crm 未补权限声明，默认开即大面积 403）");
    }

    @Test
    void configKeyBindsToFieldViaRelaxedBinding() {
        PluginEngineProperties bound = new Binder(new MapConfigurationPropertySource(
                Map.of("houcloud.plugin.require-declared-route-permission", "true")))
                .bind("houcloud.plugin", Bindable.of(PluginEngineProperties.class))
                .orElseThrow(() -> new IllegalStateException("应能绑定出 PluginEngineProperties"));
        assertTrue(bound.isRequireDeclaredRoutePermission(),
                "配置键 houcloud.plugin.require-declared-route-permission 必须可达该字段");
    }

    @Test
    void defaultWiringKeepsLoginOnlyBehaviour() throws Exception {
        ResponseEntity<?> response = wiredDispatcher(new PluginEngineProperties()).dispatch("demo", request());
        assertEquals(200, response.getStatusCode().value(),
                "默认 false：未声明权限的路由保持历史兼容（仅校验登录态）");
    }

    @Test
    void enabledWiringRejectsUndeclaredRoute() throws Exception {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setRequireDeclaredRoutePermission(true);
        ResponseEntity<?> response = wiredDispatcher(properties).dispatch("demo", request());
        assertEquals(403, response.getStatusCode().value(),
                "置 true 后：未声明权限的路由必须 fail-closed 拒绝");
    }
}
