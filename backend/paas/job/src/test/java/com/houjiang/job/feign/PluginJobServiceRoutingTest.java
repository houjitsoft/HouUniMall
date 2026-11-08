package com.houjiang.job.feign;

import com.houjiang.job.feign.support.InternalJobFeignConfiguration;
import feign.RequestTemplate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import static org.junit.jupiter.api.Assertions.*;

class PluginJobServiceRoutingTest {
    @Test
    void taskClientsCallOwningServicesAndRetainProtectedPluginPaths() {
        for (var entry : Map.<Class<?>, String>of(OaServiceClient.class, "oa", GeoServiceClient.class, "geo").entrySet()) {
            FeignClient client = entry.getKey().getAnnotation(FeignClient.class);
            assertEquals(entry.getValue(), client.name());
            assertEquals("${feign." + entry.getValue() + ".url:}", client.url());
            assertArrayEquals(new Class<?>[]{InternalJobFeignConfiguration.class}, client.configuration());
            for (var method : entry.getKey().getDeclaredMethods()) {
                assertTrue(method.getAnnotation(PostMapping.class).value()[0]
                        .startsWith("/plugin/" + entry.getValue() + "/job/"));
            }
        }
    }

    @Test
    void internalTokenIsStillAddedByDedicatedClientConfiguration() {
        var template = new RequestTemplate();
        new InternalJobFeignConfiguration().internalJobTokenRequestInterceptor(" test-secret ").apply(template);
        assertEquals(java.util.List.of("test-secret"), java.util.List.copyOf(template.headers().get("X-Internal-Job-Token")));
        assertNull(InternalJobFeignConfiguration.class.getAnnotation(org.springframework.context.annotation.Configuration.class));
    }
}
