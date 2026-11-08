package com.houjiang.userms.ucenter;

import com.houjiang.userms.ucenter.controller.UcenterController;
import com.houjiang.userms.ucenter.model.UcenterRole;
import com.houjiang.userms.ucenter.service.UcenterAuditService;
import com.houjiang.userms.ucenter.service.UcenterDomainException;
import com.houjiang.userms.ucenter.service.UcenterInvitationService;
import com.houjiang.userms.ucenter.service.UcenterPermissionService;
import com.houjiang.userms.ucenter.service.UcenterSpaceService;
import com.houjiang.userms.ucenter.vo.UcenterBootstrapVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UcenterControllerTest {
    private final UcenterSpaceService spaces = mock(UcenterSpaceService.class);
    private final UcenterPermissionService permissions = mock(UcenterPermissionService.class);
    private final UcenterInvitationService invitations = mock(UcenterInvitationService.class);
    private final UcenterAuditService audits = mock(UcenterAuditService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(
                new UcenterController(spaces, permissions, invitations, audits)).build();
    }

    @Test
    void bootstrapAndAccessCheckUseOnlyAuthenticatedServiceContext() throws Exception {
        UcenterBootstrapVO bootstrap = new UcenterBootstrapVO(
                new UcenterBootstrapVO.ProfileSummary(42L, "林野", "8000", null, false),
                new UcenterBootstrapVO.SpaceSummary(10L, "林野的个人空间", 42L, 1L),
                UcenterRole.OWNER, List.of(), 0L, 1L);
        when(spaces.bootstrapForCurrentUser()).thenReturn(bootstrap);
        when(permissions.mayCurrentUserAccess("ucenter:order:list")).thenReturn(true);

        mvc.perform(get("/user/ucenter/bootstrap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.profile.userId").value(42));
        mvc.perform(post("/user/ucenter/page-access/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionCode\":\"ucenter:order:list\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(permissions).mayCurrentUserAccess("ucenter:order:list");
    }

    @Test
    void mutationRoutesAcceptOnlyTargetIdentifiersAndValidatedBodies() throws Exception {
        mvc.perform(put("/user/ucenter/space/members/30/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuCodes\":[\"ucenter:order:list\"]}"))
                .andExpect(status().isOk());
        mvc.perform(delete("/user/ucenter/space/members/30"))
                .andExpect(status().isOk());
        mvc.perform(post("/user/ucenter/space/ownership/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":30}"))
                .andExpect(status().isOk());
        mvc.perform(post("/user/ucenter/space/leave"))
                .andExpect(status().isOk());

        verify(permissions).replaceMemberMenus(30L, java.util.Set.of("ucenter:order:list"));
        verify(spaces).removeMember(30L);
        verify(spaces).transferOwnership(30L);
        verify(spaces).leaveCurrentSpace();
    }

    @Test
    void domainFailuresUseDistinctRepositoryCodes() throws Exception {
        when(spaces.bootstrapForCurrentUser()).thenThrow(
                new UcenterDomainException(UcenterDomainException.Kind.UNAUTHENTICATED, "登录状态已失效"));
        when(permissions.mayCurrentUserAccess("ucenter:permissions:manage")).thenThrow(
                new UcenterDomainException(UcenterDomainException.Kind.FORBIDDEN, "仅空间所有者可访问"));

        mvc.perform(get("/user/ucenter/bootstrap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(30802));
        mvc.perform(post("/user/ucenter/page-access/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionCode\":\"ucenter:permissions:manage\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(31005));

        UcenterController controller = new UcenterController(spaces, permissions, invitations, audits);
        long distinct = Arrays.stream(UcenterDomainException.Kind.values())
                .map(kind -> controller.handleDomainException(new UcenterDomainException(kind, "safe")))
                .map(response -> response.getCode())
                .distinct()
                .count();
        org.junit.jupiter.api.Assertions.assertEquals(UcenterDomainException.Kind.values().length, distinct);
    }
}
