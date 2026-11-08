package com.houjiang.bffweb.controller.plugin;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.houjiang.bffweb.support.ForwardDiagnostics;
import com.houjiang.bffweb.support.BasePluginCenterUrlResolver;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

/**
 * BFF 制品下载转发单测。
 *
 * <p>核心断言：<b>前端塞进请求体的 instanceId / userId 绝不能出现在转发给 mall 的报文里</b>。
 * 只要这两个字段不外泄，mall 侧就只能用登录 Token 解析出的身份，伪造无从发生。
 *
 * @author Amos
 * @since M2
 */
class PluginArtifactProxyControllerTest {

    /**
     * mall 直连地址（测试用，避免依赖 Nacos）。
     */
    private static final String BASE_URL = "http://base-test";

    @Test
    void downloadUrlForwardsOnlyCodeVersionAndDropsIdentity() throws Exception {
        RestTemplate restTemplate = PluginArtifactProxyController.createRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/artifact/download-url"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    String json = new String(((MockClientHttpRequest) request).getBodyAsBytes(),
                            StandardCharsets.UTF_8);
                    assertTrue(json.contains("\"pluginCode\":\"coupon-demo\""), "插件编码必须转发");
                    assertTrue(json.contains("\"version\":\"1.0.0\""), "版本必须转发");
                    assertFalse(json.contains("instanceId"), "前端伪造的 instanceId 绝不能透传");
                    assertFalse(json.contains("userId"), "前端伪造的 userId 绝不能透传");
                })
                .andRespond(withSuccess("{\"code\":1,\"message\":\"ok\",\"data\":{\"url\":\"u\"}}",
                        MediaType.APPLICATION_JSON));

        PluginArtifactProxyController controller =
                new PluginArtifactProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/web/plugin-center/artifact/download-url");
        request.addHeader("Authorization", "Bearer abc");
        request.setContent(("{\"pluginCode\":\"coupon-demo\",\"version\":\"1.0.0\","
                + "\"instanceId\":\"attacker-org\",\"userId\":\"999\"}")
                .getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadUrl(request, response);

        server.verify();
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"url\":\"u\""));
    }

    @Test
    void downloadUrlRejectsMissingVersion() throws Exception {
        PluginArtifactProxyController controller =
                new PluginArtifactProxyController(BASE_URL, new RestTemplate());
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/web/plugin-center/artifact/download-url");
        request.setContent("{\"pluginCode\":\"coupon-demo\"}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadUrl(request, response);

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":3013"));
    }

    /**
     * ★P0 回归（2026-09 生产缺陷）：上游返回业务错误码时必须原样透传，不能变成 502。
     *
     * <p>修复前 <code>RestTemplate</code> 用默认的 {@code DefaultResponseErrorHandler}，
     * 4xx/5xx 一律抛 {@code HttpStatusCodeException}，{@code ResponseExtractor} 根本不执行，
     * 于是 3007/3008/3009 这些六态授权矩阵的错误码在公网上统一表现为 502，
     * 前端无法区分「没登录 / 没购买 / 授权过期 / 令牌过期」。本用例就是那次缺陷的守门员：
     * <b>不修必红</b>。
     */
    @Test
    void downloadUrl透传上游403与错误体而不是502() throws Exception {
        String upstreamBody = "{\"code\":3002,\"message\":\"当前站点未购买该插件\",\"data\":null}";
        RestTemplate restTemplate = PluginArtifactProxyController.createRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/artifact/download-url"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(upstreamBody));

        PluginArtifactProxyController controller =
                new PluginArtifactProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/web/plugin-center/artifact/download-url");
        request.setContent("{\"pluginCode\":\"demo\",\"version\":\"1.0.0\"}"
                .getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadUrl(request, response);

        server.verify();
        assertEquals(403, response.getStatus(), "上游 403 必须原样透传，502 会让前端无法区分业务错误");
        assertTrue(response.getContentAsString().contains("\"code\":3002"),
                "上游错误体必须原样透传，前端靠 code 分支");
    }

    /**
     * 同一个缺陷在回源端点上的表现：令牌过期（3007）也必须透传，而不是 502 + 空 body。
     */
    @Test
    void raw透传上游403与错误体而不是502() throws Exception {
        String upstreamBody = "{\"code\":3007,\"message\":\"下载链接已过期\",\"data\":null}";
        RestTemplate restTemplate = PluginArtifactProxyController.createRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/artifact/raw?token=expired.token"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(upstreamBody));

        PluginArtifactProxyController controller =
                new PluginArtifactProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/web/plugin-center/artifact/raw");
        request.setQueryString("token=expired.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.raw(request, response);

        server.verify();
        assertEquals(403, response.getStatus(), "令牌过期的业务码不能退化成 502");
        assertTrue(response.getContentAsString().contains("\"code\":3007"));
    }

    /**
     * 连接不上 / 超时这类真正的 IO 故障才是 502（与「上游返回了错误码」区分开）。
     */
    @Test
    void 连接失败才是502() throws Exception {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.execute(anyString(), any(HttpMethod.class), any(),
                any(ResponseExtractor.class)))
                .thenThrow(new ResourceAccessException("Connection refused: http://base-test"));

        PluginArtifactProxyController controller =
                new PluginArtifactProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/web/plugin-center/artifact/download-url");
        request.setContent("{\"pluginCode\":\"demo\",\"version\":\"1.0.0\"}"
                .getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadUrl(request, response);

        assertEquals(502, response.getStatus());
        assertTrue(response.getContentAsString().contains("500"), "网关故障统一回 500 业务码 + HTTP 502");
    }

    /**
     * ★P0 回归（2026-09 生产 502）：<b>配置存在时必须用配置，不能被注册中心带偏</b>。
     *
     * <p>修复前转发器是「注册中心优先」：既有 Feign 吃 {@code feign.mall.url}
     * （{@code 127.0.0.1:8214}）一切正常，转发器却吃 Nacos 里 mall 的注册地址
     * （{@code 127.0.0.1:8214}）连不通，于是所有插件转发一律 502。
     * 本用例把「配置优先」钉死：<b>注册中心返回了另一个地址也必须以配置为准</b>。
     *
     * <p>这个 bug 之所以长期没人发现，是因为所有 {@code /web/mall/**} 都要登录，
     * 未登录一律 11006 被鉴权拦掉，从来没真正转发过 mall 一步。
     */
    @Test
    void 配置存在时优先用配置而不是注册中心() throws Exception {
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("base")).thenReturn(
                List.of(new DefaultServiceInstance("i-1", "mall", "127.0.0.1", 8214, false)));
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        // 关键断言：请求必须打到配置里的回环地址，不是注册中心那个地址
        server.expect(requestTo("http://127.0.0.1:8214/base/plugin-center/artifact/raw?token=abc.def"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("PKfake".getBytes(StandardCharsets.UTF_8),
                        MediaType.valueOf("application/java-archive")));

        PluginArtifactProxyController controller = new PluginArtifactProxyController(
                new BasePluginCenterUrlResolver("http://127.0.0.1:8214", discovery), restTemplate);
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/web/plugin-center/artifact/raw");
        request.setQueryString("token=abc.def");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.raw(request, response);

        server.verify();
        assertEquals(200, response.getStatus());
    }

    /**
     * 配置为空时才回退到注册中心——回退路径本身不能坏。
     */
    @Test
    void 配置为空时回退到注册中心地址() throws Exception {
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("base")).thenReturn(
                List.of(new DefaultServiceInstance("i-1", "mall", "127.0.0.1", 8214, false)));
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://127.0.0.1:8214/base/plugin-center/artifact/raw?token=abc.def"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("PKfake".getBytes(StandardCharsets.UTF_8),
                        MediaType.valueOf("application/java-archive")));

        PluginArtifactProxyController controller = new PluginArtifactProxyController(
                new BasePluginCenterUrlResolver("", discovery), restTemplate);
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/web/plugin-center/artifact/raw");
        request.setQueryString("token=abc.def");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.raw(request, response);

        server.verify();
    }

    /**
     * 转发失败必须落进诊断队列（否则 502 在 X99 上永远是黑盒），且令牌要打码。
     */
    @Test
    void 转发失败会记入诊断队列且令牌被打码() throws Exception {
        ForwardDiagnostics diagnostics = new ForwardDiagnostics();
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.execute(anyString(), any(HttpMethod.class), any(RequestCallback.class),
                any(ResponseExtractor.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        PluginArtifactProxyController controller = new PluginArtifactProxyController(
                new BasePluginCenterUrlResolver("http://127.0.0.1:8214", (DiscoveryClient) null, diagnostics), restTemplate);
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/web/plugin-center/artifact/raw");
        request.setQueryString("token=SECRET");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.raw(request, response);

        assertEquals(502, response.getStatus());
        assertEquals(1, diagnostics.recentFailures(10).size());
        String url = diagnostics.recentFailures(10).get(0).url();
        assertTrue(url.contains("token=***"), "令牌必须打码: " + url);
        assertFalse(url.contains("SECRET"), "令牌绝不能出现在诊断输出里");
        assertEquals("config", diagnostics.recentFailures(10).get(0).source());
    }

    @Test
    void rawStreamsBytesFromMall() throws Exception {
        byte[] jar = "PKfake-jar".getBytes(StandardCharsets.UTF_8);
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/artifact/raw?token=abc.def"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jar, MediaType.valueOf("application/java-archive")));

        PluginArtifactProxyController controller =
                new PluginArtifactProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/web/plugin-center/artifact/raw");
        request.setQueryString("token=abc.def");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.raw(request, response);

        server.verify();
        assertEquals(200, response.getStatus());
        assertArrayEquals(jar, response.getContentAsByteArray(),
                "制品必须原样透传，不能被重新编码或截断");
    }
}
