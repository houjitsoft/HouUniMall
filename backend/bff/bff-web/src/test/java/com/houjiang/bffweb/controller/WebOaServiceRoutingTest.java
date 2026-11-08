package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.support.PluginServiceResolver;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.*;

class WebOaServiceRoutingTest {
    @Test
    void legacyFileRoutesUseOaOwnership() {
        var controller = controller(new MockEnvironment().withProperty("feign.oa.url", "https://oa.example"));
        assertEquals("https://oa.example/plugin/oa/files/1", ReflectionTestUtils.invokeMethod(controller, "resolveOaUrl", "/api/oa/files/1"));
        assertEquals("https://oa.example/plugin/oa/files/1", ReflectionTestUtils.invokeMethod(controller, "resolveOaUrl", "/oa/files/1"));
    }

    @Test
    void missingOaServiceReturns503ForStreamingAndImport() {
        var controller = controller(new MockEnvironment().withProperty("feign.mall.url", "http://mall"));
        var response = new MockHttpServletResponse();
        ReflectionTestUtils.invokeMethod(controller, "proxyFileStream", "/api/oa/files/1", new MockHttpServletRequest(), response);
        assertEquals(503, response.getStatus());
        ResponseResult<?> result = ReflectionTestUtils.invokeMethod(controller, "proxySalaryImportMultipart",
                new MultipartFile[0], "2026-09", false, "org-1", new MockHttpServletRequest());
        assertEquals(503, result.getCode());
    }

    private WebOaController controller(MockEnvironment environment) {
        var controller = new WebOaController();
        ReflectionTestUtils.setField(controller, "pluginServiceResolver", new PluginServiceResolver(environment, (DiscoveryClient) null));
        return controller;
    }
}
