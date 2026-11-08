package com.houjiang.base.config;

import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.mall.controller.plugin.PluginCenterController;
import com.houjiang.mall.service.plugin.PluginCenterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PluginCategoryAuthenticationTest {
    private final RedisTemplate<String,Object> redis = mock(RedisTemplate.class);
    private final ValueOperations<String,Object> values = mock(ValueOperations.class);
    private final PluginCenterService service = mock(PluginCenterService.class);
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void dictionaryRejectsMissingSessionBeforeAccessingData() throws Exception {
        MockMvcBuilders.standaloneSetup(new PluginCenterController(service, null, null, null))
                .addFilters(new PluginCenterSessionFilter(redis)).build()
                .perform(post("/base/plugin-center/categories")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test void authenticatedUserWithoutManagePermissionCanReadCategories() throws Exception {
        when(redis.opsForValue()).thenReturn(values);
        var user = new UserInfoVO(); user.setUserId("7");
        var session = new UserInfoDTO(); session.setUser(user); session.setPermissions(List.of());
        when(values.get(HouConfig.HOU_USER_TOKEN_USER + "session-token")).thenReturn(session);
        when(service.listCategories()).thenReturn(List.of(Map.of("id", 43, "name", "客户分类", "sort", 2)));
        MockMvcBuilders.standaloneSetup(new PluginCenterController(service, null, null, null))
                .addFilters(new PluginCenterSessionFilter(redis)).build()
                .perform(post("/base/plugin-center/categories").header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(43));
    }
}
