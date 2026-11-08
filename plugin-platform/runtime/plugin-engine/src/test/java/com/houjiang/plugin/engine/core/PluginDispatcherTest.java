package com.houjiang.plugin.engine.core;

import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.engine.security.PluginPermissionEvaluator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PluginDispatcherTest {

    static class Endpoint {
        public String ping() {
            return "pong";
        }

        @PostMapping("/upload")
        public String upload(@RequestPart("file") MultipartFile file,
                             @RequestHeader("X-Org-Id") String orgId) {
            return file.getOriginalFilename() + ":" + orgId;
        }

        @GetMapping("/download")
        public ResponseEntity<byte[]> download() {
            return ResponseEntity.status(206).body(new byte[]{1, 2, 3});
        }

        @GetMapping("/response")
        public String response(HttpServletResponse response) {
            response.setHeader("X-Plugin", "demo");
            return "ok";
        }

        @GetMapping("/defaults")
        public String defaults(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") int size) {
            return page + ":" + size;
        }

        @GetMapping("/forbidden")
        public String forbidden() {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "missing plugin permission");
        }
    }

    private PluginDispatcher dispatcher(PluginPermissionEvaluator permissions) throws Exception {
        RouteRegistry routes = new RouteRegistry();
        RouteRegistry.RouteEntry entry = new RouteRegistry.RouteEntry();
        entry.code = "demo";
        entry.path = "/ping";
        entry.httpMethod = "GET";
        entry.bean = new Endpoint();
        entry.method = Endpoint.class.getMethod("ping");
        entry.permission = "demo:ping";
        routes.register(entry);

        register(routes, "upload", "POST", Endpoint.class.getMethod("upload", MultipartFile.class, String.class), "demo:upload");
        register(routes, "download", "GET", Endpoint.class.getMethod("download"), "demo:download");
        register(routes, "response", "GET", Endpoint.class.getMethod("response", HttpServletResponse.class), "demo:response");
        register(routes, "defaults", "GET",
                Endpoint.class.getMethod("defaults", Integer.class, int.class), "demo:defaults");
        register(routes, "forbidden", "GET", Endpoint.class.getMethod("forbidden"), "demo:forbidden");

        PluginRegistry plugins = new PluginRegistry();
        plugins.putState("demo", PluginState.ACTIVE);
        return new PluginDispatcher(routes, plugins, permissions);
    }

    private static void register(RouteRegistry routes, String path, String method,
                                  java.lang.reflect.Method target, String permission) {
        RouteRegistry.RouteEntry entry = new RouteRegistry.RouteEntry();
        entry.code = "demo";
        entry.path = "/" + path;
        entry.httpMethod = method;
        entry.bean = new Endpoint();
        entry.method = target;
        entry.permission = permission;
        routes.register(entry);
    }

    private HttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/plugin/demo/ping");
    }

    /** 构造一条「未声明权限」路由的分发器，用于验证未声明权限的策略。 */
    private PluginDispatcher dispatcherWithUndeclaredPermission(PluginPermissionEvaluator permissions,
                                                               boolean requireDeclared) throws Exception {
        RouteRegistry routes = new RouteRegistry();
        register(routes, "ping", "GET", Endpoint.class.getMethod("ping"), "");
        PluginRegistry plugins = new PluginRegistry();
        plugins.putState("demo", PluginState.ACTIVE);
        return new PluginDispatcher(routes, plugins, permissions, requireDeclared);
    }

    @Test
    void undeclaredRoutePermissionKeepsLoginOnlyBehaviourByDefault() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);

        ResponseEntity<?> response =
                dispatcherWithUndeclaredPermission(permissions, false).dispatch("demo", request());

        assertEquals(200, response.getStatusCode().value(), "默认兼容策略：未声明权限的路由仅校验登录态");
        // 空权限不得交给求值器——求值器对空权限是 fail-closed 的，由路由层显式决策
        verify(permissions, never()).hasPermission(anyString());
    }

    @Test
    void undeclaredRoutePermissionIsRejectedWhenFailClosedEnabled() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);

        ResponseEntity<?> response =
                dispatcherWithUndeclaredPermission(permissions, true).dispatch("demo", request());

        assertEquals(403, response.getStatusCode().value(),
                "require-declared-route-permission=true 时未声明权限的路由必须拒绝");
    }

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(false);

        ResponseEntity<?> response = dispatcher(permissions).dispatch("demo", request());

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void rejectsMissingRoutePermission() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        when(permissions.hasPermission("demo:ping")).thenReturn(false);

        ResponseEntity<?> response = dispatcher(permissions).dispatch("demo", request());

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void dispatchesAuthorizedRequest() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        when(permissions.hasPermission("demo:ping")).thenReturn(true);

        ResponseEntity<?> response = dispatcher(permissions).dispatch("demo", request());

        assertEquals(200, response.getStatusCode().value());
        assertEquals("pong", response.getBody());
    }

    @Test
    void resolvesMultipartPartsAndHeaders() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        when(permissions.hasPermission(anyString())).thenReturn(true);

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/plugin/demo/upload");
        request.addFile(new MockMultipartFile("file", "hello.txt", "text/plain", new byte[]{1}));
        request.addHeader("X-Org-Id", "org-1");

        ResponseEntity<?> response = dispatcher(permissions).dispatch("demo", request, new MockHttpServletResponse());

        assertEquals(200, response.getStatusCode().value());
        assertEquals("hello.txt:org-1", response.getBody());
    }

    @Test
    void preservesResponseEntityAndServletResponseHeaders() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        when(permissions.hasPermission(anyString())).thenReturn(true);

        MockHttpServletRequest downloadRequest = new MockHttpServletRequest("GET", "/plugin/demo/download");
        ResponseEntity<?> download = dispatcher(permissions).dispatch("demo", downloadRequest, new MockHttpServletResponse());
        assertEquals(206, download.getStatusCode().value());
        assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) download.getBody());

        MockHttpServletRequest responseRequest = new MockHttpServletRequest("GET", "/plugin/demo/response");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ResponseEntity<?> response = dispatcher(permissions).dispatch("demo", responseRequest, servletResponse);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("demo", servletResponse.getHeader("X-Plugin"));
    }

    // ------------------------------------------------------------------
    // M4：升级排空窗口（方案 §5.3 关键约定 3）
    // ------------------------------------------------------------------

    @Test
    void drainingPluginReturns503WithRetryAfter() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        PluginDrainGate gate = new PluginDrainGate(10);
        gate.begin("demo", 5000);

        ResponseEntity<?> response = dispatcherWithDrainGate(permissions, gate).dispatch("demo", request());

        assertEquals(503, response.getStatusCode().value(),
                "排空期必须回 503：404 会让前端以为接口被删，500 会让前端以为插件有 bug");
        assertEquals("10", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    void drainWindowDoesNotLeakToOtherPlugins() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        when(permissions.hasPermission(anyString())).thenReturn(true);
        PluginDrainGate gate = new PluginDrainGate(10);
        gate.begin("other", 5000);

        ResponseEntity<?> response = dispatcherWithDrainGate(permissions, gate).dispatch("demo", request());

        assertEquals(200, response.getStatusCode().value(), "升级 A 插件不能让 B 插件不可用");
    }

    @Test
    void withoutDrainGateBehaviourIsUnchanged() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        when(permissions.hasPermission(anyString())).thenReturn(true);

        ResponseEntity<?> response = dispatcher(permissions).dispatch("demo", request());

        assertEquals(200, response.getStatusCode().value(), "未接入排空闸门时保持 M4 之前的行为");
    }

    private PluginDispatcher dispatcherWithDrainGate(PluginPermissionEvaluator permissions,
                                                     PluginDrainGate gate) throws Exception {
        RouteRegistry routes = new RouteRegistry();
        register(routes, "ping", "GET", Endpoint.class.getMethod("ping"), "demo:ping");
        PluginRegistry plugins = new PluginRegistry();
        plugins.putState("demo", PluginState.ACTIVE);
        return new PluginDispatcher(routes, plugins, permissions, false, gate);
    }
    @Test
    void appliesRequestParamDefaultValuesWhenQueryParametersAreMissing() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        when(permissions.hasPermission(anyString())).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/plugin/demo/defaults");
        ResponseEntity<?> response = dispatcher(permissions).dispatch("demo", request);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("1:10", response.getBody());
    }

    @Test
    void preservesResponseStatusFromPluginControllerWithoutHostExceptionWrapping() throws Exception {
        PluginPermissionEvaluator permissions = mock(PluginPermissionEvaluator.class);
        when(permissions.isAuthenticated()).thenReturn(true);
        when(permissions.hasPermission(anyString())).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/plugin/demo/forbidden");
        ResponseEntity<?> response = dispatcher(permissions).dispatch("demo", request);
        assertEquals(403, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(403, body.get("code"));
        assertEquals("missing plugin permission", body.get("message"));
        assertEquals(null, body.get("data"));
    }

}
