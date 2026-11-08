package com.houjiang.bffweb.controller.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * BFF 安装链路转发单测。
 *
 * <p>与 M2 的制品转发同一条铁律：<b>前端塞进请求体的 instanceId / userId 绝不能出现在转发给 mall 的报文里</b>。
 * 安装比下载更敏感——它会写站点安装态，一旦身份可被伪造，攻击者就能把插件装到别人的站点上。
 *
 * @author Amos
 * @since M3
 */
class PluginUpdateProxyControllerTest {

    /**
     * mall 直连地址（测试用，避免依赖 Nacos）。
     */
    private static final String BASE_URL = "http://base-test";

    @Test
    void installForwardsBusinessFieldsOnlyAndDropsIdentity() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/update/install"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    String json = new String(((MockClientHttpRequest) request).getBodyAsBytes(),
                            StandardCharsets.UTF_8);
                    assertTrue(json.contains("\"pluginCode\":\"demo\""), "插件编码必须转发");
                    assertTrue(json.contains("\"version\":\"2.0.0\""), "版本必须转发");
                    assertFalse(json.contains("instanceId"), "前端伪造的 instanceId 绝不能透传");
                    assertFalse(json.contains("userId"), "前端伪造的 userId 绝不能透传");
                })
                .andRespond(withSuccess("{\"code\":1,\"message\":\"ok\",\"data\":{\"taskId\":\"upd-1\"}}",
                        MediaType.APPLICATION_JSON));

        PluginUpdateProxyController controller = new PluginUpdateProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/plugin-center/update/install");
        request.addHeader("Authorization", "Bearer abc");
        request.setContent(("{\"pluginCode\":\"demo\",\"version\":\"2.0.0\","
                + "\"instanceId\":\"attacker-org\",\"userId\":\"999\"}")
                .getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.install(request, response);

        server.verify();
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("upd-1"));
    }

    @Test
    void precheckForwardsAndKeepsPassFlag() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/update/precheck"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"code\":1,\"message\":\"ok\","
                        + "\"data\":{\"pass\":false,\"reason\":3014,\"detail\":\"框架版本不足\"}}",
                        MediaType.APPLICATION_JSON));

        PluginUpdateProxyController controller = new PluginUpdateProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/plugin-center/update/precheck");
        request.setContent("{\"pluginCode\":\"demo\",\"version\":\"2.0.0\"}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.precheck(request, response);

        server.verify();
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"reason\":3014"),
                "预检不通过仍是 200，原因放在 data 里，前端据此禁用按钮");
    }

    @Test
    void historyForwardsPaginationOnly() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/update/history"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    String json = new String(((MockClientHttpRequest) request).getBodyAsBytes(),
                            StandardCharsets.UTF_8);
                    assertTrue(json.contains("\"page\":2"), "分页参数必须转发");
                    assertFalse(json.contains("instanceId"), "身份字段绝不能透传");
                })
                .andRespond(withSuccess("{\"code\":1,\"message\":\"ok\",\"data\":{\"list\":[],\"total\":0}}",
                        MediaType.APPLICATION_JSON));

        PluginUpdateProxyController controller = new PluginUpdateProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/plugin-center/update/history");
        request.setContent(("{\"pluginCode\":\"demo\",\"page\":2,\"pageSize\":10,"
                + "\"instanceId\":\"attacker-org\"}").getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.history(request, response);

        server.verify();
        assertEquals(200, response.getStatus());
    }

    /**
     * 上游返回业务错误码时必须原样透传（同 M2 制品转发的 P0 缺陷：默认 ErrorHandler 会让
     * 4xx/5xx 变异常，最后统一退化成 502，前端就分不清「依赖缺失 3015」和「网关挂了」）。
     */
    @Test
    void install透传上游403与错误体而不是502() throws Exception {
        String upstreamBody = "{\"code\":3015,\"message\":\"依赖插件 erp 未安装\",\"data\":null}";
        RestTemplate restTemplate = PluginUpdateProxyController.createRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/update/install"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(upstreamBody));

        PluginUpdateProxyController controller = new PluginUpdateProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/plugin-center/update/install");
        request.setContent("{\"pluginCode\":\"demo\",\"version\":\"2.0.0\"}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.install(request, response);

        server.verify();
        assertEquals(403, response.getStatus(), "安装被拒的业务码必须原样透传");
        assertTrue(response.getContentAsString().contains("\"code\":3015"));
    }

    @Test
    void buildForwardBodyKeepsWhitelistOnly() {
        var body = PluginUpdateProxyController.buildForwardBody(new java.util.LinkedHashMap<>(
                java.util.Map.of("pluginCode", " demo ", "taskId", " t-1 ", "instanceId", "x")));

        assertEquals("demo", body.get("pluginCode"));
        assertEquals("t-1", body.get("taskId"));
        assertFalse(body.containsKey("instanceId"));
        assertFalse(body.containsKey("userId"));
    }

    // ------------------------------------------------------------------
    // M4：upgrade / rollback 转发
    // ------------------------------------------------------------------

    @Test
    void upgradeForwardsTargetVersionAndDropsIdentity() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/update/upgrade"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    String json = new String(((MockClientHttpRequest) request).getBodyAsBytes(),
                            StandardCharsets.UTF_8);
                    assertTrue(json.contains("\"targetVersion\":\"3.0.0\""), "升级目标版本必须转发（否则只能升到 latest）");
                    assertTrue(json.contains("\"pluginCode\":\"demo\""));
                    assertFalse(json.contains("instanceId"), "前端伪造的 instanceId 绝不能透传");
                })
                .andRespond(withSuccess("{\"code\":1,\"message\":\"ok\",\"data\":{}}",
                        MediaType.APPLICATION_JSON));

        PluginUpdateProxyController controller = new PluginUpdateProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/plugin-center/update/upgrade");
        request.setContent(("{\"pluginCode\":\"demo\",\"targetVersion\":\"3.0.0\","
                + "\"instanceId\":\"attacker-org\"}")
                .getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.upgrade(request, response);

        server.verify();
        assertEquals(200, response.getStatus());
    }

    @Test
    void rollbackIsForwardedToMall() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(BASE_URL + "/base/plugin-center/update/rollback"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"code\":1,\"message\":\"ok\",\"data\":{}}",
                        MediaType.APPLICATION_JSON));

        PluginUpdateProxyController controller = new PluginUpdateProxyController(BASE_URL, restTemplate);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/plugin-center/update/rollback");
        request.setContent("{\"pluginCode\":\"demo\"}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.rollback(request, response);

        server.verify();
        assertEquals(200, response.getStatus());
    }
}
