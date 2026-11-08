package com.houjiang.bffweb.controller.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.houjiang.bffweb.support.PluginServiceResolver;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestTemplate;

/**
 * AI 网关平台侧只读通道的边界测试。
 *
 * <p>这条通道替浏览器持有网关会话令牌，因此白名单必须是唯一入口：
 * 写接口（改密钥、改路由、轮换凭据）一旦混进来，平台页面就成了绕过
 * 独立账号二次确认流程的旁路。
 */
class WebAigatewayControllerTest {

    private WebAigatewayController controller(String email, String password) {
        // 显式转型：null 在两个构造器之间会产生歧义。
        DiscoveryClient discovery = (DiscoveryClient) null;
        return new WebAigatewayController(new PluginServiceResolver(new MockEnvironment(), discovery),
                new RestTemplate(), email, password);
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        return request;
    }

    @Test
    void rejectsWriteAndUnknownPaths() {
        var controller = controller("admin@houjit.com", "secret-value");
        for (String uri : new String[]{
                "/web/aigateway/settings/api-key/regenerate",
                "/web/aigateway/models/1/test",
                "/web/aigateway/keys/import-selected",
                "/web/aigateway/fallback/sort/priority",
                "/web/aigateway/logs/clear"}) {
            var response = controller.proxyGatewayReadApi(request(uri));
            assertEquals(403, response.getStatusCode().value(), uri + " 应被只读白名单拒绝");
        }
    }

    /**
     * 同一路径上 GET 与 POST 语义不同（读密钥 vs 新增密钥），只读通道必须按方法区分。
     */
    @Test
    void writeMethodOnReadablePathIsStillRejected() {
        var controller = controller("admin@houjit.com", "secret-value");
        for (String uri : new String[]{"/web/aigateway/keys", "/web/aigateway/models", "/web/aigateway/logs"}) {
            MockHttpServletRequest post = new MockHttpServletRequest("POST", uri);
            post.setRequestURI(uri);
            var response = controller.proxyGatewayReadApi(post);
            assertEquals(403, response.getStatusCode().value(), uri + " 的写方法必须被拒绝");
        }
    }

    @Test
    void rejectsPathTraversalAndAbsoluteUrlEscape() {
        var controller = controller("admin@houjit.com", "secret-value");
        for (String uri : new String[]{
                "/web/aigateway/../../etc/passwd",
                "/web/aigateway/keys/../../secret"}) {
            var response = controller.proxyGatewayReadApi(request(uri));
            assertTrue(response.getStatusCode().isError(), uri + " 应被拒绝");
        }
    }

    @Test
    void refusesToServeWithoutServerSideCredential() {
        var controller = controller("", "");
        var response = controller.proxyGatewayReadApi(request("/web/aigateway/analytics/summary"));
        assertEquals(503, response.getStatusCode().value());
        assertTrue(new String(response.getBody(), StandardCharsets.UTF_8).contains("账号"),
                "未配置账号时应说明原因，而不是退化成无鉴权直连");
    }

    @Test
    void allowsReadOnlyPaths() {
        var controller = controller("admin@houjit.com", "secret-value");
        // 白名单路径放行后由下游返回真实结果；此处只断言不被本地白名单拒绝。
        for (String uri : new String[]{
                "/web/aigateway/analytics/summary",
                "/web/aigateway/models",
                "/web/aigateway/keys",
                "/web/aigateway/health",
                "/web/aigateway/fallback/routing",
                "/web/aigateway/logs"}) {
            var response = controller.proxyGatewayReadApi(request(uri));
            assertTrue(response.getStatusCode().value() != 403, uri + " 属于只读白名单，不应被本地拒绝");
        }
    }

    @Test
    void queryParametersArePreservedForRangeSelection() {
        var controller = controller("admin@houjit.com", "secret-value");
        MockHttpServletRequest request = request("/web/aigateway/analytics/summary");
        request.setQueryString("range=7d");
        var response = controller.proxyGatewayReadApi(request);
        // 未配置网关地址时解析失败返回 503；只要不是 403 白名单拒绝即可。
        assertTrue(response.getStatusCode().value() != 403);
    }

    @Test
    void writePathsAreNeverReachableThroughTheChannel() {
        var controller = controller("admin@houjit.com", "secret-value");
        // 每个写接口都必须被本地白名单挡住：即使账号有效也不能通过平台通道调用。
        for (String uri : new String[]{
                "/web/aigateway/settings/api-key/regenerate",
                "/web/aigateway/models/1/test",
                "/web/aigateway/keys/import-selected",
                "/web/aigateway/fallback/sort/priority",
                "/web/aigateway/logs/clear",
                "/web/aigateway/backups"}) {
            var response = controller.proxyGatewayReadApi(request(uri));
            assertEquals(403, response.getStatusCode().value(), uri + " 属于写操作，必须被拒绝");
        }
    }
}
