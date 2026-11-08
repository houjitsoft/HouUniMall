package com.houjiang.base.controller;

import com.houjiang.base.service.FileRecognizeCatalog;
import com.houjiang.base.service.FileRecognizeDraftService;
import com.houjiang.base.service.FileRecognizeService;
import com.houjiang.base.security.BaseOrganizationResolver;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class FileRecognizeControllerAuthTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resumeDraftUsesForwardedAuthorizationWhenBaseHasNoJwtFilter() throws Exception {
        FileRecognizeController controller = new FileRecognizeController();
        FileRecognizeDraftService draftService = mock(FileRecognizeDraftService.class);
        ReflectionTestUtils.setField(controller, "fileRecognizeDraftService", draftService);
        ReflectionTestUtils.setField(controller, "fileRecognizeService", mock(FileRecognizeService.class));
        ReflectionTestUtils.setField(controller, "fileRecognizeCatalog", mock(FileRecognizeCatalog.class));
        when(draftService.resume("42", "7")).thenReturn(ResponseResult.success());

        UserInfoVO user = new UserInfoVO();
        user.setUserId("42");
        user.setOrgId("7");

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        try (MockedStatic<ParseTokenUtils> tokens = mockStatic(ParseTokenUtils.class)) {
            tokens.when(() -> ParseTokenUtils.getUserInfoByToken("valid-token")).thenReturn(user);

            mvc.perform(get("/base/file/import/draft/resume")
                            .header("Authorization", "Bearer valid-token"))
                    .andExpect(jsonPath("$.code").value(1));
        }

        verify(draftService).resume(eq("42"), eq("7"));
    }

    @Test
    void rejectsDraftAccessWhenSelectedOrganizationIsNotAMember() throws Exception {
        FileRecognizeController controller = new FileRecognizeController();
        FileRecognizeDraftService draftService = mock(FileRecognizeDraftService.class);
        BaseOrganizationResolver organizationResolver = mock(BaseOrganizationResolver.class);
        ReflectionTestUtils.setField(controller, "fileRecognizeDraftService", draftService);
        ReflectionTestUtils.setField(controller, "fileRecognizeService", mock(FileRecognizeService.class));
        ReflectionTestUtils.setField(controller, "fileRecognizeCatalog", mock(FileRecognizeCatalog.class));
        ReflectionTestUtils.setField(controller, "organizationResolver", organizationResolver);
        when(organizationResolver.resolveCurrentOrgId()).thenReturn(null);

        UserInfoVO user = new UserInfoVO();
        user.setUserId("42");
        user.setOrgId("7");

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        try (MockedStatic<ParseTokenUtils> tokens = mockStatic(ParseTokenUtils.class)) {
            tokens.when(() -> ParseTokenUtils.getUserInfoByToken("valid-token")).thenReturn(user);

            mvc.perform(get("/base/file/import/draft/resume")
                            .header("Authorization", "Bearer valid-token")
                            .header("X-Org-Id", "999"))
                    .andExpect(jsonPath("$.code").value(422));
        }

        verifyNoInteractions(draftService);
    }

    @Test
    void tellsUserToJoinOrganizationWhenAccountHasNoMembership() throws Exception {
        FileRecognizeController controller = new FileRecognizeController();
        BaseOrganizationResolver organizationResolver = mock(BaseOrganizationResolver.class);
        ReflectionTestUtils.setField(controller, "fileRecognizeDraftService", mock(FileRecognizeDraftService.class));
        ReflectionTestUtils.setField(controller, "fileRecognizeService", mock(FileRecognizeService.class));
        ReflectionTestUtils.setField(controller, "fileRecognizeCatalog", mock(FileRecognizeCatalog.class));
        ReflectionTestUtils.setField(controller, "organizationResolver", organizationResolver);
        when(organizationResolver.resolveCurrentOrgId()).thenReturn(null);
        when(organizationResolver.hasAnyMembership()).thenReturn(false);

        UserInfoVO user = new UserInfoVO();
        user.setUserId("42");
        user.setOrgId("7");

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        try (MockedStatic<ParseTokenUtils> tokens = mockStatic(ParseTokenUtils.class)) {
            tokens.when(() -> ParseTokenUtils.getUserInfoByToken("valid-token")).thenReturn(user);

            mvc.perform(get("/base/file/import/draft/resume")
                            .header("Authorization", "Bearer valid-token"))
                    .andExpect(jsonPath("$.code").value(422))
                    .andExpect(jsonPath("$.message").value("当前账号未加入任何组织，不能使用智能入库"));
        }
    }

    @Test
    void tellsUserToReselectOrganizationWhenSelectedOrgIsNotAMember() throws Exception {
        FileRecognizeController controller = new FileRecognizeController();
        BaseOrganizationResolver organizationResolver = mock(BaseOrganizationResolver.class);
        ReflectionTestUtils.setField(controller, "fileRecognizeDraftService", mock(FileRecognizeDraftService.class));
        ReflectionTestUtils.setField(controller, "fileRecognizeService", mock(FileRecognizeService.class));
        ReflectionTestUtils.setField(controller, "fileRecognizeCatalog", mock(FileRecognizeCatalog.class));
        ReflectionTestUtils.setField(controller, "organizationResolver", organizationResolver);
        when(organizationResolver.resolveCurrentOrgId()).thenReturn(null);
        when(organizationResolver.hasAnyMembership()).thenReturn(true);

        UserInfoVO user = new UserInfoVO();
        user.setUserId("42");
        user.setOrgId("7");

        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        try (MockedStatic<ParseTokenUtils> tokens = mockStatic(ParseTokenUtils.class)) {
            tokens.when(() -> ParseTokenUtils.getUserInfoByToken("valid-token")).thenReturn(user);

            mvc.perform(get("/base/file/import/draft/resume")
                            .header("Authorization", "Bearer valid-token")
                            .header("X-Org-Id", "999"))
                    .andExpect(jsonPath("$.code").value(422))
                    .andExpect(jsonPath("$.message").value("当前选择的组织无效，请重新选择组织"));
        }
    }
}
