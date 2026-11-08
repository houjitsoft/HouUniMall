package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OaServiceFeignFallbackTest {

    private static final String MESSAGE = "薪资阶梯规则服务暂时不可用，请稍后重试";

    @Test
    void tierConfigurationFallbacksPutUnavailableReasonInMessageOnly() {
        OaServiceFeignFallback fallback = new OaServiceFeignFallback();

        assertUnavailable(fallback.getNoProjectAcceptanceRuleConfig());
        assertUnavailable(fallback.saveNoProjectAcceptanceRuleConfig(Map.of()));
    }

    private void assertUnavailable(ResponseResult<?> response) {
        assertEquals(MESSAGE, response.getMessage());
        assertNull(response.getData());
    }
}
