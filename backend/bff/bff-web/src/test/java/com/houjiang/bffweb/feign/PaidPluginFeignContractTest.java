package com.houjiang.bffweb.feign;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaidPluginFeignContractTest {

    @Test
    void cmsCompatibilityFeignUsesOwningService() throws Exception {
        FeignClient client = UcmsServiceFeign.class.getAnnotation(FeignClient.class);
        assertEquals("cms", client.name());
        assertEquals("${feign.cms.url:}", client.url());
        assertEquals("/plugin/cms", client.path());

        Method statistics = UcmsServiceFeign.class.getMethod("getUcmsStatistics", java.util.Map.class);
        assertEquals("/statistics/dashboard", statistics.getAnnotation(PostMapping.class).value()[0]);
    }
}
