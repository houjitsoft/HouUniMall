package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.MallServiceFeign;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebMallFinanceRoutesTest {

    private static final Set<String> PLATFORM_FINANCE_ROUTES = Set.of(
            "/finance/center",
            "/finance/refund/stats",
            "/finance/refund/list",
            "/finance/refund/retry",
            "/finance/shop/balance/list",
            "/finance/user/wallet/list",
            "/finance/user/recharge/list"
    );

    @Test
    void exposesAllPlatformFinanceRoutesOnBffAndMallFeign() {
        Set<String> controller = postRoutes(WebMallController.class);
        Set<String> feign = postRoutes(MallServiceFeign.class).stream()
                .filter(path -> path.startsWith("/mall/finance/"))
                .map(path -> path.substring("/mall".length()))
                .collect(Collectors.toSet());

        assertTrue(controller.containsAll(PLATFORM_FINANCE_ROUTES));
        assertTrue(feign.containsAll(PLATFORM_FINANCE_ROUTES));
    }

    private static Set<String> postRoutes(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(mapping -> mapping != null && mapping.value().length > 0)
                .map(mapping -> mapping.value()[0])
                .collect(Collectors.toSet());
    }
}
