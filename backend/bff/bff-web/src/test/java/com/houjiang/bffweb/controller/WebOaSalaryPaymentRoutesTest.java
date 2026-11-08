package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.OaServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class WebOaSalaryPaymentRoutesTest {

    @Test
    void paymentRoutePassesRequestThroughToExactOaRoute() throws Exception {
        assertPassThrough(
                "/salary/payslip/payment/{id}",
                "/api/oa/salary/payslip/payment/{id}",
                Map.of("paymentAmount", "123.45", "remark", "首款"));
    }

    @Test
    void paymentRecordsRoutePassesRequestThroughToExactOaRoute() throws Exception {
        assertPassThrough(
                "/salary/payslip/payment-records/{id}",
                "/api/oa/salary/payslip/payment-records/{id}",
                Map.of("orgId", "org-a"));
    }

    @Test
    void paymentRouteNormalizesNullBodyToEmptyMap() throws Exception {
        assertPassThrough(
                "/salary/payslip/payment/{id}",
                "/api/oa/salary/payslip/payment/{id}",
                null,
                Map.of());
    }

    @Test
    void paymentRecordsRouteNormalizesNullBodyToEmptyMap() throws Exception {
        assertPassThrough(
                "/salary/payslip/payment-records/{id}",
                "/api/oa/salary/payslip/payment-records/{id}",
                null,
                Map.of());
    }

    private void assertPassThrough(String bffPath, String oaPath, Map<String, Object> body) throws Exception {
        assertPassThrough(bffPath, oaPath, body, body);
    }

    private void assertPassThrough(String bffPath, String oaPath, Map<String, Object> body,
                                   Map<String, Object> expectedBody) throws Exception {
        ResponseResult<Map<String, Object>> downstream = ResponseResult.success(Map.of("ok", true));
        CapturedInvocation captured = new CapturedInvocation();
        OaServiceFeign feign = (OaServiceFeign) Proxy.newProxyInstance(
                OaServiceFeign.class.getClassLoader(),
                new Class<?>[]{OaServiceFeign.class},
                (proxy, method, args) -> {
                    captured.method = method;
                    captured.args = args;
                    return downstream;
                });
        WebOaController controller = new WebOaController();
        ReflectionTestUtils.setField(controller, "oaServiceFeign", feign);

        Method route = requirePostRoute(bffPath);
        Object result = route.invoke(controller, 42L, body);

        assertSame(downstream, result);
        assertNotNull(captured.method, "BFF 路由没有委托 OA Feign");
        PostMapping mapping = captured.method.getAnnotation(PostMapping.class);
        assertNotNull(mapping, "Feign 方法缺少 POST 映射");
        assertEquals(oaPath, mapping.value()[0]);
        assertArrayEquals(new Object[]{42L, expectedBody}, captured.args);
    }

    private Method requirePostRoute(String path) {
        Method method = Arrays.stream(WebOaController.class.getDeclaredMethods())
                .filter(candidate -> {
                    PostMapping mapping = candidate.getAnnotation(PostMapping.class);
                    return mapping != null && Arrays.asList(mapping.value()).contains(path);
                })
                .findFirst()
                .orElse(null);
        assertNotNull(method, "缺少 POST /web/oa" + path);
        return method;
    }

    private static final class CapturedInvocation {
        private Method method;
        private Object[] args;
    }
}
