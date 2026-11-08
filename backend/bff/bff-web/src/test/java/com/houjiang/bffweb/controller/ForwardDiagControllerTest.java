package com.houjiang.bffweb.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.houjiang.bffweb.support.ForwardDiagnostics;
import com.houjiang.bffweb.support.MallBaseUrlResolver;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * {@link ForwardDiagController} 单测（P0 502 排查端点）。
 *
 * <p>重点：鉴权必须对、输出必须区分「失败」与「地址解析」、令牌必须打码。
 *
 * @author Amos
 * @since M4 补丁（502 排查）
 */
class ForwardDiagControllerTest {

    /**
     * 与 {@code /web/admin/restart} 同一套令牌（测试里用注入的已知值）。
     */
    private static final String TOKEN = "test-admin-token";

    private ForwardDiagnostics diagnostics;

    private ForwardDiagController controller;

    @BeforeEach
    void setUp() {
        diagnostics = new ForwardDiagnostics();
        MallBaseUrlResolver resolver =
                new MallBaseUrlResolver("http://127.0.0.1:8220", (DiscoveryClient) null, diagnostics);
        controller = new ForwardDiagController(diagnostics, resolver);
        ReflectionTestUtils.setField(controller, "adminToken", TOKEN);
    }

    @Test
    void 无令牌返回401() {
        Map<String, Object> body = controller.diag(new MockHttpServletRequest("GET", "/web/admin/forward-diag"), 20);

        assertEquals(401, body.get("code"));
    }

    @Test
    void 令牌错误返回401() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/web/admin/forward-diag");
        request.addHeader("X-Admin-Token", "wrong");

        Map<String, Object> body = controller.diag(request, 20);

        assertEquals(401, body.get("code"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void 令牌正确时返回失败与解析明细() {
        diagnostics.recordFailure("http://127.0.0.1:8220/mall/artifact/raw?token=abc", "nacos",
                new ConnectException("Connection refused"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/web/admin/forward-diag");
        request.addHeader("X-Admin-Token", TOKEN);

        Map<String, Object> body = controller.diag(request, 20);

        assertEquals(0, body.get("code"));
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertEquals("http://127.0.0.1:8220", data.get("configuredUrl"));
        assertEquals("config", data.get("currentSource"));
        List<ForwardDiagnostics.Entry> failures =
                (List<ForwardDiagnostics.Entry>) data.get("failures");
        assertEquals(1, failures.size());
        assertEquals("java.net.ConnectException", failures.get(0).errType());
        assertEquals("Connection refused", failures.get(0).errMsg());
        // 令牌必须已被打码——诊断输出会被转发、截图，不能指望每个看的人都守得住鉴权
        assertEquals("http://127.0.0.1:8220/mall/artifact/raw?token=***", failures.get(0).url());
        assertNotNull(data.get("resolved"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void limit被夹在1到容量上限之间() {
        for (int i = 0; i < 5; i++) {
            diagnostics.recordResolved("http://127.0.0.1:8220/" + i, "config");
        }
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/web/admin/forward-diag");
        request.addHeader("X-Admin-Token", TOKEN);

        Map<String, Object> body = controller.diag(request, 9999);

        Map<String, Object> data = (Map<String, Object>) body.get("data");
        List<ForwardDiagnostics.Entry> resolved = (List<ForwardDiagnostics.Entry>) data.get("resolved");
        assertEquals(5, resolved.size());
        assertTrue(resolved.stream().allMatch(e -> "config".equals(e.source())));
    }

    @Test
    void 无诊断数据时也能正常返回() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/web/admin/forward-diag");
        request.addHeader("X-Admin-Token", TOKEN);

        Map<String, Object> body = controller.diag(request, 20);

        assertEquals(0, body.get("code"));
    }
}
