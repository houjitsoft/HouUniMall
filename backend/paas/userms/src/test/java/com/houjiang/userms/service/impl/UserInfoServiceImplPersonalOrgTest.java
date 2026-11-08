package com.houjiang.userms.service.impl;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.feign.BaseServiceClient;
import com.houjiang.userms.service.UserOrganizationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 注册后自动保障个人空间组织：注册接口直接下发 token，会话里必须带上组织成员关系，
 * 否则用户拿到 token 后所有按组织隔离的功能都会被判为「组织未确定」。
 */
class UserInfoServiceImplPersonalOrgTest {

    private UserInfoServiceImpl service(BaseServiceClient base, UserOrganizationService organizations) {
        UserInfoServiceImpl service = new UserInfoServiceImpl();
        ReflectionTestUtils.setField(service, "baseServiceClient", base);
        ReflectionTestUtils.setField(service, "userOrganizationService", organizations);
        return service;
    }

    @Test
    void bindsPersonalOrganizationAndWritesItIntoSession() {
        BaseServiceClient base = mock(BaseServiceClient.class);
        UserOrganizationService organizations = mock(UserOrganizationService.class);
        UserInfoServiceImpl service = service(base, organizations);

        Map<String, Object> orgData = new HashMap<>();
        orgData.put("orgId", "12");
        orgData.put("orgCode", "PERSONAL_42");
        orgData.put("isNewOrg", true);
        when(base.ensurePersonalOrganization(anyMap())).thenReturn(ResponseResult.success(orgData));
        when(organizations.createUserOrganization(anyMap())).thenReturn(ResponseResult.success("组织绑定成功"));

        List<Map<String, Object>> orgList = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("org_id", "12");
        row.put("is_primary", 1);
        orgList.add(row);
        when(organizations.getUserOrganizations("42")).thenReturn(ResponseResult.success(orgList));

        UserInfoVO session = new UserInfoVO();
        ReflectionTestUtils.invokeMethod(service, "ensurePersonalOrganization", "42", "19900002026", session);

        assertEquals("12", session.getOrgId());
        assertEquals(1, session.getOrgList().size());
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(organizations).createUserOrganization(captor.capture());
        assertEquals("42", captor.getValue().get("userId"));
        assertEquals("12", captor.getValue().get("orgId"));
        // 个人空间不抢占主组织，否则企业用户登记公司后默认组织会一直停在个人空间
        assertEquals(0, captor.getValue().get("isPrimary"));
    }

    @Test
    void toleratesCallWithoutSession() {
        BaseServiceClient base = mock(BaseServiceClient.class);
        UserOrganizationService organizations = mock(UserOrganizationService.class);
        UserInfoServiceImpl service = service(base, organizations);

        Map<String, Object> orgData = new HashMap<>();
        orgData.put("orgId", "12");
        when(base.ensurePersonalOrganization(anyMap())).thenReturn(ResponseResult.success(orgData));
        when(organizations.createUserOrganization(anyMap())).thenReturn(ResponseResult.success("组织绑定成功"));
        when(organizations.getUserOrganizations("42")).thenReturn(ResponseResult.success(new ArrayList<>()));

        // 企业注册走这条：不下发 token，组织关系等下次登录时按库里补全
        ReflectionTestUtils.invokeMethod(service, "ensurePersonalOrganization", "42", "某某公司", null);

        verify(organizations).createUserOrganization(argThat(m -> "12".equals(m.get("orgId"))));
    }

    @Test
    void leavesSessionUnchangedWhenOrganizationCreationFails() {
        BaseServiceClient base = mock(BaseServiceClient.class);
        UserOrganizationService organizations = mock(UserOrganizationService.class);
        UserInfoServiceImpl service = service(base, organizations);

        when(base.ensurePersonalOrganization(anyMap())).thenReturn(ResponseResult.error("创建个人空间失败"));

        UserInfoVO session = new UserInfoVO();
        ReflectionTestUtils.invokeMethod(service, "ensurePersonalOrganization", "42", "19900002026", session);

        assertNull(session.getOrgId());
        assertNull(session.getOrgList());
        verify(organizations, never()).createUserOrganization(any());
    }
}
