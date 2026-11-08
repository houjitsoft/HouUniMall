package com.houjiang.base.controller;

import com.houjiang.base.security.BasePermissionGuard;
import com.houjiang.base.service.OrganizationService;
import com.houjiang.commons.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class OrganizationControllerAuthorizationTest {

    @Test
    void queryIsDeniedWithoutViewPermission() throws Exception {
        OrganizationController controller = controller();
        OrganizationService service = mock(OrganizationService.class);
        BasePermissionGuard guard = mock(BasePermissionGuard.class);
        ReflectionTestUtils.setField(controller, "organizationService", service);
        ReflectionTestUtils.setField(controller, "permissionGuard", guard);
        when(guard.require("shell:organization:view")).thenReturn(ResponseResult.fail(403, "无组织查看权限"));

        mvc(controller).perform(post("/organization/queryOrganizationPage").contentType("application/json")
                        .content("{}"))
                .andExpect(jsonPath("$.code").value(403));
        verifyNoInteractions(service);
    }

    @Test
    void queryAppliesCurrentOrganizationScopeWhenAuthorized() throws Exception {
        OrganizationController controller = controller();
        OrganizationService service = mock(OrganizationService.class);
        BasePermissionGuard guard = mock(BasePermissionGuard.class);
        ReflectionTestUtils.setField(controller, "organizationService", service);
        ReflectionTestUtils.setField(controller, "permissionGuard", guard);
        when(guard.require("shell:organization:view")).thenReturn(null);
        when(service.queryOrganizationPage(any())).thenReturn(ResponseResult.success());

        mvc(controller).perform(post("/organization/queryOrganizationPage").contentType("application/json")
                        .content("{}"))
                .andExpect(jsonPath("$.code").value(1));
        verify(guard).applyOrganizationScope(any());
        verify(service).queryOrganizationPage(any());
    }

    @Test
    void addIsDeniedWithoutAddPermission() throws Exception {
        OrganizationController controller = controller();
        OrganizationService service = mock(OrganizationService.class);
        BasePermissionGuard guard = mock(BasePermissionGuard.class);
        ReflectionTestUtils.setField(controller, "organizationService", service);
        ReflectionTestUtils.setField(controller, "permissionGuard", guard);
        when(guard.require("shell:organization:add")).thenReturn(ResponseResult.fail(403, "无组织新增权限"));

        mvc(controller).perform(post("/organization/addOrganization").contentType("application/json")
                        .content("{\"orgName\":\"test\",\"orgCode\":\"T\"}"))
                .andExpect(jsonPath("$.code").value(403));
        verifyNoInteractions(service);
    }

    private OrganizationController controller() {
        return new OrganizationController();
    }

    private MockMvc mvc(OrganizationController controller) {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }
}
