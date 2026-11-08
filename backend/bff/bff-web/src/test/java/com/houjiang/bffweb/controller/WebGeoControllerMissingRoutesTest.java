package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebGeoControllerMissingRoutesTest {
    @Test
    void proxiesProjectDeletion() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);

        controller.deleteWorkflowProject(9L);

        verify(feign).deleteWorkflowProject(9L);
    }

    @Test
    void forwardsProjectListQueryParametersForLegacyClients() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> params = Map.of("orgId", "org-7", "projectId", 3L);

        controller.workflowProjects(params);

        verify(feign).workflowProjects(params);
    }

    @Test
    void proxiesRankingRefresh() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> body = Map.of("projectId", 3L);

        controller.refreshRanking(body);

        verify(feign).refreshRanking(body);
    }

    @Test
    void proxiesCrawlerPolicyChangelog() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> params = Map.of("projectId", 3L);

        controller.intelligencePolicyChangelog(params);

        verify(feign).intelligencePolicyChangelog(params);
    }

    @Test
    void proxiesNapReport() {
        GeoServiceFeign feign = mock(GeoServiceFeign.class);
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> params = Map.of("projectId", 3L);

        controller.localNapReport(params);

        verify(feign).localNapReport(params);
    }

    @Test
    void declaresEveryLocalClosureRouteAtTheBffBoundary() {
        Set<String> routes = new HashSet<>();
        for (Method method : WebGeoController.class.getDeclaredMethods()) {
            addRoutes(routes, "GET", method.getAnnotation(GetMapping.class));
            addRoutes(routes, "POST", method.getAnnotation(PostMapping.class));
            addRoutes(routes, "PUT", method.getAnnotation(PutMapping.class));
            addRoutes(routes, "DELETE", method.getAnnotation(DeleteMapping.class));
        }

        assertTrue(routes.contains("GET /local/stores"));
        assertTrue(routes.contains("GET /local/bindings"));
        assertTrue(routes.contains("GET /local/providers/status"));
        assertTrue(routes.contains("GET /local/sync-jobs"));
        assertTrue(routes.contains("GET /local/nap/audits"));
        assertTrue(routes.contains("GET /local/nap/issues"));
        assertTrue(routes.contains("GET /local/monitor/queries"));
        assertTrue(routes.contains("GET /local/monitor/plan"));
        assertTrue(routes.contains("GET /local/monitor/models"));
        assertTrue(routes.contains("GET /local/monitor/runs"));
        assertTrue(routes.contains("GET /local/monitor/results"));
        assertTrue(routes.contains("GET /local/monitor/trends"));
    }

    private static void addRoutes(Set<String> routes, String method, GetMapping mapping) {
        if (mapping != null) routes.addAll(Arrays.stream(mapping.value()).map(path -> method + " " + path).toList());
    }

    private static void addRoutes(Set<String> routes, String method, PostMapping mapping) {
        if (mapping != null) routes.addAll(Arrays.stream(mapping.value()).map(path -> method + " " + path).toList());
    }

    private static void addRoutes(Set<String> routes, String method, PutMapping mapping) {
        if (mapping != null) routes.addAll(Arrays.stream(mapping.value()).map(path -> method + " " + path).toList());
    }

    private static void addRoutes(Set<String> routes, String method, DeleteMapping mapping) {
        if (mapping != null) routes.addAll(Arrays.stream(mapping.value()).map(path -> method + " " + path).toList());
    }

    @Test
    void forwardsCurrentProjectToModelDiagnosis() throws Exception {
        final String[] calledMethod = new String[1];
        final Object[][] calledArgs = new Object[1][];
        GeoServiceFeign feign = (GeoServiceFeign) java.lang.reflect.Proxy.newProxyInstance(
            GeoServiceFeign.class.getClassLoader(), new Class<?>[]{GeoServiceFeign.class},
            (proxy, method, args) -> {
                calledMethod[0] = method.getName();
                calledArgs[0] = args;
                return null;
            });
        WebGeoController controller = new WebGeoController(feign);
        Map<String, Object> params = Map.of("projectId", 3L);

        Method route = WebGeoController.class.getMethod("modelsDiagnosisAlias", String.class, Map.class);
        route.invoke(controller, "tongyi", params);

        assertEquals("modelsDiagnosisAlias", calledMethod[0]);
        assertNotNull(calledArgs[0]);
        assertArrayEquals(new Object[]{"tongyi", params}, calledArgs[0]);
    }
}
