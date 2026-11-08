package com.houjiang.merchant.controller.finance;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.merchant.common.exception.OperateException;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlatformFinanceControllerTest {

    @Test
    void exposesOnlyThePlatformFinanceRoutesRequiredByCloud() {
        RequestMapping root = PlatformFinanceController.class.getAnnotation(RequestMapping.class);
        assertEquals("/merchant/platform-finance", root.value()[0]);
        assertEquals("hasRole('MALL_PLATFORM_FINANCE')", PlatformFinanceController.class.getAnnotation(PreAuthorize.class).value());

        Set<String> routes = Arrays.stream(PlatformFinanceController.class.getDeclaredMethods())
                .filter(method -> method.getAnnotation(PostMapping.class) != null)
                .map(PlatformFinanceControllerTest::route)
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "POST /center",
                "POST /refund/stats",
                "POST /refund/list",
                "POST /refund/retry",
                "POST /shop/balance/list",
                "POST /user/wallet/list",
                "POST /user/recharge/list"
        ), routes);
    }

    private static String route(Method method) {
        PostMapping post = method.getAnnotation(PostMapping.class);
        if (post != null) return "POST " + post.value()[0];
        throw new AssertionError("缺少 HTTP 路由注解: " + method.getName());
    }

    @Test
    void ignoresBlankOptionalFiltersAndKeepsZeroStatus() {
        assertEquals(null, PlatformFinanceController.integer(Map.of("status", "  "), "status"));
        assertEquals(0, PlatformFinanceController.integer(Map.of("status", 0), "status"));
    }

    @Test
    void keepsMerchantFailureMessageInUnifiedResponseContract() {
        ResponseResult<Object> response = PlatformFinanceController.handleBaseException(
                new OperateException("非失败状态不能重新退款"));

        assertEquals(0, response.getCode());
        assertEquals("非失败状态不能重新退款", response.getMessage());
    }
}
