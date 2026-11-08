package com.houjiang.plugin.engine.core;

import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.engine.security.PluginPermissionEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 启动装载窗口：路由未命中时回 503 而不是 404（每次发版重启都会复现的假 404）。
 */
class PluginBootGateTest {

    private static PluginDispatcher dispatcher(RouteRegistry routes, PluginBootGate gate) {
        PluginRegistry plugins = new PluginRegistry();
        plugins.putState("geo", PluginState.ACTIVE);
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        return new PluginDispatcher(routes, plugins, permissions, false, null, false, gate);
    }

    private static MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/plugin/geo/workflow/projects");
    }

    @Test
    void unknownRouteIs404WhenBootGateIsReady() {
        PluginBootGate gate = new PluginBootGate();
        ResponseEntity<?> response = dispatcher(new RouteRegistry(), gate).dispatch("geo", request());

        assertEquals(404, response.getStatusCode().value(),
                "装载已结束：查不到路由就是真的没有这个接口，不能拿 503 掩盖");
        assertNull(response.getHeaders().get("Retry-After"));
    }

    @Test
    void unknownRouteIs503WhileBootGateIsLoading() {
        PluginBootGate gate = new PluginBootGate(7);
        gate.begin();
        ResponseEntity<?> response = dispatcher(new RouteRegistry(), gate).dispatch("geo", request());

        assertEquals(503, response.getStatusCode().value(),
                "装载窗口内路由表还没建起来，必须回 503 而不是 404");
        assertEquals("7", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    void bootGateClosesAfterComplete() {
        PluginBootGate gate = new PluginBootGate();
        gate.begin();
        gate.complete();

        assertEquals(404, dispatcher(new RouteRegistry(), gate).dispatch("geo", request())
                        .getStatusCode().value(),
                "装载结束（无论成功失败）后必须退回真实 404，否则会永久假 503");
    }

    @Test
    void bootGateExpiresSoPluginIsNeverStuckOn503() {
        // 装载线程被杀 / 卡死时窗口必须自然到期，不能把插件永久锁在 503。
        // 窗口给 1ms，断言只取「到期之后」这侧——「到期之前」由其它用例覆盖，
        // 否则 1ms 的窗口会在首次断言前就过期，用例自己变得不稳定。
        PluginBootGate gate = new PluginBootGate(10, 1L);
        gate.begin();
        sleepPastMaxWindow();

        assertEquals(false, gate.isLoading());
        assertEquals(404, dispatcher(new RouteRegistry(), gate).dispatch("geo", request())
                        .getStatusCode().value(),
                "窗口到期后必须退回真实 404，不能把插件永久锁在 503");
    }

    @Test
    void bootGateStaysLoadingUntilCompleted() {
        PluginBootGate gate = new PluginBootGate();
        gate.begin();
        assertEquals(true, gate.isLoading(), "装载未完成时窗口必须保持打开");
        // begin 幂等：重复调用不刷新开始时间，避免长装载把窗口无限续期
        gate.begin();
        gate.complete();
        assertEquals(false, gate.isLoading());
    }

    @Test
    void registeredRouteStillServedWhileLoading() throws Exception {
        RouteRegistry routes = new RouteRegistry();
        RouteRegistry.RouteEntry entry = new RouteRegistry.RouteEntry();
        entry.code = "geo";
        entry.path = "/workflow/projects";
        entry.httpMethod = "GET";
        entry.bean = new Object() {
            @SuppressWarnings("unused")
            public String list() {
                return "[]";
            }
        };
        entry.method = entry.bean.getClass().getMethod("list");
        entry.permission = "";
        routes.register(entry);

        PluginBootGate gate = new PluginBootGate();
        gate.begin();

        ResponseEntity<?> response = dispatcher(routes, gate).dispatch("geo", request());

        assertEquals(200, response.getStatusCode().value(),
                "已装载插件的路由不受装载窗口影响，后续插件还在装也不该挡住它");
    }

    private static void sleepPastMaxWindow() {
        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
