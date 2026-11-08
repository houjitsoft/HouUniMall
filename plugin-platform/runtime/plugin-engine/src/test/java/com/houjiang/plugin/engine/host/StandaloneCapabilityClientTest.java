package com.houjiang.plugin.engine.host;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.plugin.api.host.PluginCapabilityCallException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * standalone 能力代理：验证「接口调用 → HTTP → 反序列化」这条链路，
 * 以及插件不可达 / 返回错误时能被识别为调用失败（而非静默当作无能力）。
 */
class StandaloneCapabilityClientTest {

    interface SampleCapability {
        String greet(String name);

        int count();

        void touch();

        BigDecimal price(String sku);
    }

    private static final String HOST = "http://plugin-host:9000";
    private static final String URL = HOST + StandaloneCapabilityClient.CAPABILITY_PATH;

    private static MockRestServiceServer server(RestTemplate restTemplate) {
        return MockRestServiceServer.bindTo(restTemplate).build();
    }

    private static StandaloneCapabilityClient client(RestTemplate restTemplate) {
        return new StandaloneCapabilityClient(restTemplate, new ObjectMapper());
    }

    @Test
    void 方法调用被转为HTTP请求并返回反序列化结果() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = server(restTemplate);
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"method\":\"greet\"")))
                .andRespond(withSuccess("{\"ok\":true,\"result\":\"hello\"}", MediaType.APPLICATION_JSON));

        SampleCapability capability = client(restTemplate).createProxy(HOST, SampleCapability.class);

        assertEquals("hello", capability.greet("world"));
        server.verify();
    }

    @Test
    void 请求体携带能力接口名与方法名() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = server(restTemplate);
        server.expect(requestTo(URL))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        SampleCapability.class.getName())))
                .andRespond(withSuccess("{\"ok\":true,\"result\":3}", MediaType.APPLICATION_JSON));

        assertEquals(3, client(restTemplate).createProxy(HOST, SampleCapability.class).count());
        server.verify();
    }

    @Test
    void 无返回值方法返回null() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = server(restTemplate);
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{\"ok\":true,\"result\":null}", MediaType.APPLICATION_JSON));

        client(restTemplate).createProxy(HOST, SampleCapability.class).touch();
        server.verify();
    }

    @Test
    void 复杂类型按声明类型反序列化() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = server(restTemplate);
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{\"ok\":true,\"result\":\"19.90\"}", MediaType.APPLICATION_JSON));

        BigDecimal price = client(restTemplate).createProxy(HOST, SampleCapability.class).price("SKU-1");

        assertEquals(0, new BigDecimal("19.90").compareTo(price));
        server.verify();
    }

    @Test
    void 插件返回业务失败时抛调用异常() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = server(restTemplate);
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{\"ok\":false,\"error\":\"活动已结束\"}", MediaType.APPLICATION_JSON));

        SampleCapability capability = client(restTemplate).createProxy(HOST, SampleCapability.class);

        PluginCapabilityCallException ex =
                assertThrows(PluginCapabilityCallException.class, () -> capability.greet("world"));
        assertEquals(true, ex.getMessage().contains("活动已结束"));
        server.verify();
    }

    @Test
    void 插件不可达时抛调用异常而不是静默返回空() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = server(restTemplate);
        server.expect(requestTo(URL)).andRespond(withServerError());

        SampleCapability capability = client(restTemplate).createProxy(HOST, SampleCapability.class);

        assertThrows(PluginCapabilityCallException.class, () -> capability.greet("world"));
        server.verify();
    }
}
