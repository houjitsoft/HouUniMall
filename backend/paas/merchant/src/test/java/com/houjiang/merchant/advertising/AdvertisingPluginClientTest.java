package com.houjiang.merchant.advertising;

import com.houjiang.merchant.service.AdvertisingPluginClient;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AdvertisingPluginClientTest {

    @Test
    void internalInterceptorReplacesBrowserAuthorizationWithOnlyServiceToken() {
        RequestTemplate template = new RequestTemplate();
        template.header("Authorization", "Bearer browser-shop-token");

        new AdvertisingPluginClient.InternalTokenConfiguration()
                .advertisingInternalToken(" service-secret ")
                .apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
        assertEquals(java.util.List.of("service-secret"),
                java.util.List.copyOf(template.headers().get("X-Internal-Job-Token")));
    }
}
