package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WebGeoControllerBrandCatalogRoutesTest {

    @Test
    void exposesEveryBrandCatalogRouteOnControllerAndFeignClient() {
        assertRoute(GetMapping.class, "/brand/products");
        assertRoute(PostMapping.class, "/brand/products");
        assertRoute(PutMapping.class, "/brand/products/{id}");
        assertRoute(DeleteMapping.class, "/brand/products/{id}");
        assertRoute(GetMapping.class, "/brand/competitors");
        assertRoute(PostMapping.class, "/brand/competitors");
        assertRoute(PutMapping.class, "/brand/competitors/{id}");
        assertRoute(DeleteMapping.class, "/brand/competitors/{id}");
        assertRoute(PostMapping.class, "/brand/catalog-analysis");
        assertRoute(GetMapping.class, "/brand/catalog-analysis/status");
    }

    @Test
    void forwardsBrandCatalogRequestsWithoutChangingProjectContext() throws Exception {
        String[] calledMethod = new String[1];
        Object[][] calledArgs = new Object[1][];
        GeoServiceFeign feign = (GeoServiceFeign) Proxy.newProxyInstance(
            GeoServiceFeign.class.getClassLoader(),
            new Class<?>[]{GeoServiceFeign.class},
            (proxy, method, args) -> {
                calledMethod[0] = method.getName();
                calledArgs[0] = args;
                return null;
            }
        );
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> project = Map.of("projectId", 3L);
        Map<String, Object> body = Map.of("projectId", 3L, "name", "HouGEO");

        assertForwarded(controller, "brandProducts", new Class<?>[]{Map.class},
            new Object[]{project}, "brandProducts", calledMethod, calledArgs);
        assertForwarded(controller, "createBrandProduct", new Class<?>[]{Map.class},
            new Object[]{body}, "createBrandProduct", calledMethod, calledArgs);
        assertForwarded(controller, "updateBrandProduct", new Class<?>[]{Long.class, Map.class},
            new Object[]{7L, body}, "updateBrandProduct", calledMethod, calledArgs);
        assertForwarded(controller, "deleteBrandProduct", new Class<?>[]{Long.class, Map.class},
            new Object[]{7L, project}, "deleteBrandProduct", calledMethod, calledArgs);
        assertForwarded(controller, "brandCompetitors", new Class<?>[]{Map.class},
            new Object[]{project}, "brandCompetitors", calledMethod, calledArgs);
        assertForwarded(controller, "createBrandCompetitor", new Class<?>[]{Map.class},
            new Object[]{body}, "createBrandCompetitor", calledMethod, calledArgs);
        assertForwarded(controller, "updateBrandCompetitor", new Class<?>[]{Long.class, Map.class},
            new Object[]{8L, body}, "updateBrandCompetitor", calledMethod, calledArgs);
        assertForwarded(controller, "deleteBrandCompetitor", new Class<?>[]{Long.class, Map.class},
            new Object[]{8L, project}, "deleteBrandCompetitor", calledMethod, calledArgs);
        assertForwarded(controller, "triggerBrandCatalogAnalysis", new Class<?>[]{Map.class},
            new Object[]{project}, "triggerBrandCatalogAnalysis", calledMethod, calledArgs);
        assertForwarded(controller, "brandCatalogAnalysisStatus", new Class<?>[]{Map.class},
            new Object[]{project}, "brandCatalogAnalysisStatus", calledMethod, calledArgs);
    }

    private static void assertRoute(Class<? extends Annotation> annotationType, String path) {
        assertDoesNotThrow(() -> findRoute(WebGeoController.class, annotationType, path),
            () -> "BFF controller route is missing: " + path);
        assertDoesNotThrow(() -> findRoute(GeoServiceFeign.class, annotationType, path),
            () -> "GEO Feign route is missing: " + path);
    }

    private static Method findRoute(Class<?> type, Class<? extends Annotation> annotationType, String path) {
        for (Method method : type.getDeclaredMethods()) {
            Annotation annotation = method.getAnnotation(annotationType);
            if (annotation == null) continue;
            String[] values;
            if (annotation instanceof GetMapping mapping) values = mapping.value();
            else if (annotation instanceof PostMapping mapping) values = mapping.value();
            else if (annotation instanceof PutMapping mapping) values = mapping.value();
            else values = ((DeleteMapping) annotation).value();
            if (values.length > 0 && path.equals(values[0])) return method;
        }
        throw new AssertionError("Missing route " + path + " on " + type.getSimpleName());
    }

    private static void assertForwarded(
        WebGeoController controller,
        String controllerMethod,
        Class<?>[] parameterTypes,
        Object[] args,
        String expectedFeignMethod,
        String[] calledMethod,
        Object[][] calledArgs
    ) throws Exception {
        Method method = assertDoesNotThrow(
            () -> WebGeoController.class.getMethod(controllerMethod, parameterTypes),
            () -> "BFF controller method is missing: " + controllerMethod
        );
        method.invoke(controller, args);
        assertEquals(expectedFeignMethod, calledMethod[0]);
        assertArrayEquals(args, calledArgs[0]);
    }
}
