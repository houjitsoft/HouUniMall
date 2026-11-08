package com.houjiang.tool.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

class ToolPluginPermissionConfigTest {
    final FakeRest rest = new FakeRest();
    final ToolPluginPermissionConfig.CurrentPermissionEvaluator evaluator =
            new ToolPluginPermissionConfig.CurrentPermissionEvaluator(rest, "http://permission");

    void login(String... authorities) {
        var granted = java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test-user", null, granted));
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer unit-test");
        request.addHeader("tenant-id", "1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    void rbac(String... perms) throws Exception {
        StringBuilder json = new StringBuilder("{\"code\":1,\"data\":[");
        for (int i = 0; i < perms.length; i++) {
            if (i > 0) json.append(',');
            json.append('"').append(perms[i]).append('"');
        }
        rest.response = new ObjectMapper().readTree(json.append("]}").toString());
    }

    @AfterEach
    void reset() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void oldSessionUsesCurrentExactGrant() throws Exception {
        login("shell:ocr:apps");
        rbac("shell:ocr:overview");
        assertTrue(evaluator.hasPermission("shell:ocr:overview"));
        assertFalse(evaluator.hasPermission("shell:ocr:users"));
        assertEquals("Bearer unit-test", rest.headers.getFirst("Authorization"));
        assertEquals("1", rest.headers.getFirst("tenant-id"));
        assertEquals(java.util.Map.of(), rest.body);
    }

    @Test
    void rootGrantDoesNotAuthorizeAllChildren() throws Exception {
        login("shell:ocr:apps");
        rbac("shell:ocr:apps");
        assertFalse(evaluator.hasPermission("shell:ocr:overview"));
    }

    /** 超级管理员会话权限就是 "*"，且没有任何 RBAC 角色菜单：必须放行（2026-10-27 线上超管 403 回归用例）。 */
    @Test
    void superAdminWildcardSessionIsHonored() throws Exception {
        login("*");
        rbac();
        assertTrue(evaluator.hasPermission("shell:ocr:overview"));
        assertTrue(evaluator.hasPermission("shell:ocr:recognizes"));
        assertEquals(0, rest.calls, "会话已有通配授权时不应再打权限服务");
    }

    @Test
    void rbacWildcardGrantIsHonored() throws Exception {
        login("shell:ocr:apps");
        rbac("shell:ocr:*");
        assertTrue(evaluator.hasPermission("shell:ocr:logins"));
    }

    @Test
    void sessionWithoutOcrGrantIsDenied() throws Exception {
        login("shell:mall:apps");
        rbac("shell:mall:apps");
        assertFalse(evaluator.hasPermission("shell:ocr:overview"));
    }

    @Test
    void unavailableOrMalformedPermissionServiceDenies() throws Exception {
        login("shell:ocr:apps");
        rest.failed = true;
        assertFalse(evaluator.hasPermission("shell:ocr:overview"));
        rest.failed = false;
        rbac("shell:ocr:overview");
        rest.response = new ObjectMapper().readTree("{\"code\":500,\"data\":[\"shell:ocr:overview\"]}");
        assertFalse(evaluator.hasPermission("shell:ocr:overview"));
    }

    @Test
    void anonymousAndMissingCredentialDenyWithoutRequest() {
        assertFalse(evaluator.hasPermission("shell:ocr:overview"));
        login();
        RequestContextHolder.resetRequestAttributes();
        assertFalse(evaluator.hasPermission("shell:ocr:overview"));
        assertEquals(0, rest.calls);
    }

    @Test
    void emptyPermissionDenies() {
        login();
        assertFalse(evaluator.hasPermission(""));
        assertFalse(evaluator.hasPermission(null));
    }

    @Test
    void nonOcrPermissionKeepsDelegateDecision() {
        login("*");
        assertTrue(evaluator.hasPermission("shell:cms:apps"));
        assertEquals(0, rest.calls);
    }

    static class FakeRest extends RestTemplate {
        JsonNode response;
        boolean failed;
        int calls;
        HttpHeaders headers;
        Object body;

        @Override
        public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> request,
                                              Class<T> type, Object... args) {
            calls++;
            headers = request.getHeaders();
            body = request.getBody();
            assertEquals("http://permission/permission/user/permissions", url);
            assertEquals(HttpMethod.POST, method);
            if (failed) throw new IllegalStateException("unavailable");
            return ResponseEntity.ok(type.cast(response));
        }

        { response = new ObjectMapper().createObjectNode(); }
    }
}
