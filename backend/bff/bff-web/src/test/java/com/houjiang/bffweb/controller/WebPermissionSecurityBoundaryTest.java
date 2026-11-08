package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.config.GlobalExceptionHandler;
import com.houjiang.bffweb.feign.PermissionServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.commons.errorcode.domain.UpstreamErrorCode;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebPermissionSecurityBoundaryTest {

    @Test
    void preservesPermissionServiceUnauthorizedHttpStatusAndBodyCode() throws Exception {
        PermissionServiceFeign feign = mock(PermissionServiceFeign.class);
        when(feign.getUserMenus(anyMap())).thenThrow(feignFailure(401));
        when(feign.getUserPermissions(anyMap())).thenThrow(feignFailure(401));
        when(feign.checkUserPermission(anyMap())).thenThrow(feignFailure(401));
        when(feign.getSubMenuTree(anyMap())).thenThrow(feignFailure(401));
        when(feign.checkPageAccess(anyMap())).thenThrow(feignFailure(401));
        when(feign.getPageButtonPermissions(anyMap())).thenThrow(feignFailure(401));
        when(feign.batchCheckButtonPermissions(anyMap())).thenThrow(feignFailure(401));
        MockMvc mockMvc = mockMvc(feign);

        Map<String, String> requests = Map.of(
                "/web/permission/user/menus", "{}",
                "/web/permission/user/permissions", "{}",
                "/web/permission/user/check", "{\"permission\":\"shell:setting:permission\"}",
                "/web/permission/submenu/tree", "{\"parentMenuId\":1}",
                "/web/permission/page/access/check", "{\"pagePath\":\"/setting/permission/menu\"}",
                "/web/permission/page/buttons", "{\"pagePath\":\"/setting/permission/menu\"}",
                "/web/permission/page/buttons/batch-check", "{\"permissions\":[\"shell:setting:permission:menu:add\"]}"
        );
        for (Map.Entry<String, String> request : requests.entrySet()) {
            mockMvc.perform(post(request.getKey())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request.getValue()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Test
    void preservesPermissionServiceSecurityAndDependencyStatuses() throws Exception {
        for (int expectedStatus : List.of(400, 403, 503)) {
            PermissionServiceFeign feign = mock(PermissionServiceFeign.class);
            when(feign.getUserPermissions(anyMap())).thenThrow(feignFailure(expectedStatus));

            mockMvc(feign).perform(post("/web/permission/user/permissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(expectedStatus))
                    .andExpect(jsonPath("$.code").value(expectedStatus))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Test
    void turnsPermissionFallbackCodeIntoMatchingHttpStatus() throws Exception {
        PermissionServiceFeign feign = mock(PermissionServiceFeign.class);
        when(feign.getUserPermissions(anyMap()))
                .thenReturn(ResponseResult.fail(503, "权限服务暂时不可用"));

        mockMvc(feign).perform(post("/web/permission/user/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(503));
    }

    @Test
    void routesAllPagePermissionSelfServiceRequestsToPermissionService() throws Exception {
        PermissionServiceFeign feign = mock(PermissionServiceFeign.class);
        when(feign.checkPageAccess(anyMap())).thenReturn(ResponseResult.success(true));
        when(feign.getPageButtonPermissions(anyMap())).thenReturn(ResponseResult.success(List.of()));
        when(feign.batchCheckButtonPermissions(anyMap())).thenReturn(ResponseResult.success(Map.of()));
        MockMvc mockMvc = mockMvc(feign);

        Map<String, String> requests = Map.of(
                "/web/permission/page/access/check", "{\"pagePath\":\"/setting/permission/menu\"}",
                "/web/permission/page/buttons", "{\"pagePath\":\"/setting/permission/menu\"}",
                "/web/permission/page/buttons/batch-check", "{\"permissions\":[\"shell:setting:permission:menu:add\"]}"
        );
        for (Map.Entry<String, String> request : requests.entrySet()) {
            mockMvc.perform(post(request.getKey())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request.getValue()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1));
        }

        verify(feign).checkPageAccess(anyMap());
        verify(feign).getPageButtonPermissions(anyMap());
        verify(feign).batchCheckButtonPermissions(anyMap());
    }

    @Test
    void mapsNonSelfServiceFeignFailuresToUnifiedUpstreamCode() throws Exception {
        PermissionServiceFeign feign = mock(PermissionServiceFeign.class);
        when(feign.getRoleList(anyMap())).thenThrow(feignFailure(401));

        mockMvc(feign).perform(post("/web/permission/role/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(UpstreamErrorCode.UPSTREAM_CALL_FAILED.code()));
    }

    @Test
    void mapsUnexpectedSelfServiceFeignFailuresToUnifiedUpstreamCode() throws Exception {
        PermissionServiceFeign feign = mock(PermissionServiceFeign.class);
        when(feign.getUserPermissions(anyMap())).thenThrow(feignFailure(500));

        mockMvc(feign).perform(post("/web/permission/user/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(UpstreamErrorCode.UPSTREAM_CALL_FAILED.code()));
    }

    private MockMvc mockMvc(PermissionServiceFeign feign) {
        WebPermissionController controller = new WebPermissionController();
        ReflectionTestUtils.setField(controller, "permissionServiceFeign", feign);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private FeignException feignFailure(int status) {
        Request request = Request.create(
                Request.HttpMethod.POST,
                "/permission/user/permissions",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null);
        Response response = Response.builder()
                .status(status)
                .reason("permission security failure")
                .request(request)
                .build();
        return FeignException.errorStatus("PermissionServiceFeign#getUserPermissions", response);
    }
}
