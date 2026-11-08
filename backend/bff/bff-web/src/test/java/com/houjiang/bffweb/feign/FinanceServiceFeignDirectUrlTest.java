package com.houjiang.bffweb.feign;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FinanceServiceFeignDirectUrlTest {

    @Test
    void financeUsesItsOwnDiscoveryNameOrExplicitAddress() {
        FeignClient client = FinanceServiceFeign.class.getAnnotation(FeignClient.class);
        assertNotNull(client);
        assertEquals("finance", client.value());
        assertEquals("${feign.finance.url:}", client.url());
        assertEquals("/plugin/finance", client.path());
    }
}
