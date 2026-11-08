package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.UcenterServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebUcenterControllerTest {
    private final UcenterServiceFeign feign = mock(UcenterServiceFeign.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new WebUcenterController(feign)).build();
    }

    @Test
    void forwardsBootstrapAndMenuGrantWithoutBrowserActorOrSpaceIdentifiers() throws Exception {
        when(feign.bootstrap()).thenReturn(ResponseResult.success(Map.of("role", "OWNER")));
        when(feign.replaceMemberMenus(eq(30L), any())).thenReturn(ResponseResult.success());

        mvc.perform(get("/web/ucenter/bootstrap").header("token", "consumer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("OWNER"));
        mvc.perform(put("/web/ucenter/space/members/30/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuCodes\":[\"ucenter:order:list\"],"
                                + "\"actorUserId\":999,\"ownerUserId\":999,\"spaceId\":999}"))
                .andExpect(status().isOk());

        verify(feign).bootstrap();
        verify(feign).replaceMemberMenus(eq(30L), eq(Map.of("menuCodes", java.util.List.of("ucenter:order:list"))));
    }

    @Test
    void exposesInvitationAndMembershipTransitionsAtExactBrowserRoutes() throws Exception {
        when(feign.acceptInvitation(8L)).thenReturn(ResponseResult.success());
        when(feign.rejectInvitation(8L)).thenReturn(ResponseResult.success());
        when(feign.cancelInvitation(8L)).thenReturn(ResponseResult.success());
        when(feign.removeMember(30L)).thenReturn(ResponseResult.success());
        when(feign.leaveSpace()).thenReturn(ResponseResult.success());

        mvc.perform(post("/web/ucenter/invitations/8/accept")).andExpect(status().isOk());
        mvc.perform(post("/web/ucenter/invitations/8/reject")).andExpect(status().isOk());
        mvc.perform(post("/web/ucenter/invitations/8/cancel")).andExpect(status().isOk());
        mvc.perform(delete("/web/ucenter/space/members/30")).andExpect(status().isOk());
        mvc.perform(post("/web/ucenter/space/leave")).andExpect(status().isOk());

        verify(feign).acceptInvitation(8L);
        verify(feign).rejectInvitation(8L);
        verify(feign).cancelInvitation(8L);
        verify(feign).removeMember(30L);
        verify(feign).leaveSpace();
    }
}
