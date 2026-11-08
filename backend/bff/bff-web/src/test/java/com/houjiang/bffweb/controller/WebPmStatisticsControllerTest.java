package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.config.FeignRequestInterceptor;
import com.houjiang.bffweb.feign.PmServiceFeign;
import com.houjiang.bffweb.feign.PmServiceFeignFallback;
import com.houjiang.bffweb.util.ResponseResult;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebPmStatisticsControllerTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void proxiesStatisticsAndOptionsWithoutReplacingDownstreamFailures() throws Exception {
        PmServiceFeign feign = mock(PmServiceFeign.class);
        WebPmController controller = controllerWith(feign);
        Map<String, Object> query = Map.of("orgId", 7L, "dateField", "createTime");
        Map<String, Object> options = Map.of("orgId", 7L);
        ResponseResult<Map<String, Object>> forbidden = ResponseResult.success(403, "无项目访问权限", Map.of());
        ResponseResult<List<String>> optionResult = ResponseResult.success(List.of("project-1"));
        when(feign.getTaskStatistics(query)).thenReturn(forbidden);
        when(feign.getTaskStatisticsOptions(options)).thenReturn(optionResult);

        assertSame(forbidden, controller.getTaskStatistics(query));
        assertSame(optionResult, controller.getTaskStatisticsOptions(options));
        verify(feign).getTaskStatistics(query);
        verify(feign).getTaskStatisticsOptions(options);
    }

    @Test
    void streamsExcelStatusHeadersAndBodyUnchanged() throws Exception {
        PmServiceFeign feign = mock(PmServiceFeign.class);
        WebPmController controller = controllerWith(feign);
        Map<String, Object> params = Map.of("orgId", 7L);
        byte[] workbook = "xlsx-content".getBytes(StandardCharsets.UTF_8);
        Response downstream = Response.builder()
                .status(200)
                .reason("OK")
                .request(Request.create(Request.HttpMethod.GET,
                        "/pm/statistics/task/export", Map.of(), null,
                        StandardCharsets.UTF_8, null))
                .headers(Map.of(
                        "Content-Type", List.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                        "Content-Disposition", List.of("attachment; filename=task-statistics.xlsx")))
                .body(workbook)
                .build();
        when(feign.exportTaskStatistics(params)).thenReturn(downstream);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.exportTaskStatistics(params, response);

        assertEquals(200, response.getStatus());
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.getHeader("Content-Type"));
        assertEquals("attachment; filename=task-statistics.xlsx",
                response.getHeader("Content-Disposition"));
        assertEquals("Content-Disposition", response.getHeader("Access-Control-Expose-Headers"));
        assertArrayEquals(workbook, response.getContentAsByteArray());
    }

    @Test
    void forwardsSelectedOrganizationToPmRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        request.addHeader("X-Org-Id", " 7 ");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RequestTemplate template = new RequestTemplate();
        template.target("http://pm");
        template.uri("/pm/statistics/task");

        new FeignRequestInterceptor().apply(template);

        assertEquals(List.of("Bearer token"), List.copyOf(template.headers().get("Authorization")));
        assertEquals(List.of("7"), List.copyOf(template.headers().get("X-Org-Id")));
        assertEquals(List.of("bff-web"), List.copyOf(template.headers().get("X-Hou-Gateway")));
    }

    @Test
    void exportFallbackUsesServiceUnavailableStatus() {
        Response response = new PmServiceFeignFallback().exportTaskStatistics(Map.of("orgId", 7L));

        assertEquals(503, response.status());
    }

    private static WebPmController controllerWith(PmServiceFeign feign) throws Exception {
        WebPmController controller = new WebPmController();
        Field field = WebPmController.class.getDeclaredField("pmServiceFeign");
        field.setAccessible(true);
        field.set(controller, feign);
        return controller;
    }
}
