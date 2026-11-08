package com.houjiang.permission.controller;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.util.JwtUtil;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.permission.config.SecurityConfig;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.RoleMapper;
import com.houjiang.permission.security.PermissionIdentity;
import com.houjiang.permission.security.PermissionGuard;
import com.houjiang.permission.security.PermissionSecurityExceptionHandler;
import com.houjiang.permission.service.MenuService;
import com.houjiang.permission.service.PagePermissionService;
import com.houjiang.permission.service.PermissionService;
import com.houjiang.permission.service.RoleMenuService;
import com.houjiang.permission.service.RoleService;
import com.houjiang.permission.service.UserRoleService;
import com.houjiang.permission.service.UserService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PermissionController.class, PagePermissionController.class})
@Import({SecurityConfig.class, PermissionIdentity.class, PermissionSecurityExceptionHandler.class})
class PermissionSelfServiceSecurityTest {

    private static final String CURRENT_USER_ID = "790708000000000001";
    private static final String OTHER_USER_ID = "640697115455213568";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedisCache redisCache;
    @MockBean
    private PermissionService permissionService;
    @MockBean
    private MenuService menuService;
    @MockBean
    private RoleService roleService;
    @MockBean
    private UserService userService;
    @MockBean
    private UserRoleService userRoleService;
    @MockBean
    private RoleMenuService roleMenuService;
    @MockBean
    private PagePermissionService pagePermissionService;
    @MockBean
    private PermissionMenuMapper permissionMenuMapper;
    @MockBean
    private RoleMapper roleMapper;
    @MockBean
    private PermissionGuard permissionGuard;

    static Stream<Arguments> selfServiceRequests() {
        return Stream.of(
                Arguments.of("/permission/user/menus", Map.of("userId", OTHER_USER_ID)),
                Arguments.of("/permission/user/permissions", Map.of("userId", OTHER_USER_ID)),
                Arguments.of("/permission/user/check", Map.of(
                        "userId", OTHER_USER_ID,
                        "permission", "shell:setting:permission")),
                Arguments.of("/permission/submenu/tree", Map.of(
                        "userId", OTHER_USER_ID,
                        "parentMenuId", 1)),
                Arguments.of("/permission/page/access/check", Map.of(
                        "userId", OTHER_USER_ID,
                        "pagePath", "/setting/permission/menu")),
                Arguments.of("/permission/page/buttons", Map.of(
                        "userId", OTHER_USER_ID,
                        "pagePath", "/setting/permission/menu")),
                Arguments.of("/permission/page/buttons/batch-check", Map.of(
                        "userId", OTHER_USER_ID,
                        "permissions", List.of("shell:setting:permission:menu:add")))
        );
    }

    @ParameterizedTest
    @MethodSource("selfServiceRequests")
    void shouldRejectAnonymousSelfServiceRequests(String path, Map<String, Object> body) throws Exception {
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @ParameterizedTest
    @MethodSource("selfServiceRequests")
    void shouldRejectRequestBodyUserIdThatDiffersFromToken(String path, Map<String, Object> body)
            throws Exception {
        String token = cacheAuthenticatedUser(CURRENT_USER_ID);

        mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @ParameterizedTest
    @MethodSource("malformedSelfServiceRequests")
    void shouldRejectMalformedRequestBodyUserId(
            String path,
            Map<String, Object> body) throws Exception {
        String token = cacheAuthenticatedUser(CURRENT_USER_ID);

        mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @SuppressWarnings("unchecked")
    static Stream<Arguments> malformedSelfServiceRequests() {
        Stream<Arguments> endpoints = Stream.of(
                Arguments.of("/permission/user/menus", Map.of()),
                Arguments.of("/permission/user/permissions", Map.of()),
                Arguments.of("/permission/user/check", Map.of(
                        "permission", "shell:setting:permission")),
                Arguments.of("/permission/submenu/tree", Map.of(
                        "parentMenuId", 1)),
                Arguments.of("/permission/page/access/check", Map.of(
                        "pagePath", "/setting/permission/menu")),
                Arguments.of("/permission/page/buttons", Map.of(
                        "pagePath", "/setting/permission/menu")),
                Arguments.of("/permission/page/buttons/batch-check", Map.of(
                        "permissions", List.of("shell:setting:permission:menu:add")))
        );

        return endpoints.flatMap(endpoint -> Stream.of("not-a-user-id", "   ")
                .map(userId -> Arguments.of(
                        endpoint.get()[0],
                        withUserId((Map<String, Object>) endpoint.get()[1], userId))));
    }

    private static Map<String, Object> withUserId(Map<String, Object> body, String userId) {
        Map<String, Object> request = new HashMap<>(body);
        request.put("userId", userId);
        return request;
    }

    @ParameterizedTest
    @MethodSource("matchingSelfServiceRequests")
    void shouldUseAuthenticatedUserForMatchingOrOmittedRequestUserId(
            String path,
            Map<String, Object> body) throws Exception {
        when(menuService.getUserMenus(anyLong())).thenReturn(List.of());
        when(menuService.getSubMenuTree(anyLong(), eq(1))).thenReturn(List.of());
        when(permissionService.getUserPermissions(anyLong())).thenReturn(List.of());
        when(pagePermissionService.getPageButtonPermissions(anyLong(), anyString())).thenReturn(List.of());
        when(pagePermissionService.batchCheckButtonPermissions(anyLong(), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(Map.of());
        String token = cacheAuthenticatedUser(CURRENT_USER_ID);

        mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verifyAuthenticatedUserPassedToService(path);
    }

    static Stream<Arguments> matchingSelfServiceRequests() {
        return Stream.of(
                Arguments.of("/permission/user/menus", Map.of()),
                Arguments.of("/permission/user/permissions", Map.of("userId", CURRENT_USER_ID)),
                Arguments.of("/permission/user/check", Map.of(
                        "permission", "shell:setting:permission")),
                Arguments.of("/permission/submenu/tree", Map.of("parentMenuId", 1)),
                Arguments.of("/permission/page/access/check", Map.of(
                        "pagePath", "/setting/permission/menu")),
                Arguments.of("/permission/page/buttons", Map.of(
                        "userId", CURRENT_USER_ID,
                        "pagePath", "/setting/permission/menu")),
                Arguments.of("/permission/page/buttons/batch-check", Map.of(
                        "permissions", List.of("shell:setting:permission:menu:add")))
        );
    }

    private void verifyAuthenticatedUserPassedToService(String path) {
        Long currentUserId = Long.valueOf(CURRENT_USER_ID);
        switch (path) {
            case "/permission/user/menus" -> verify(menuService).getUserMenus(currentUserId);
            case "/permission/user/permissions" -> verify(permissionService).getUserPermissions(currentUserId);
            case "/permission/user/check" -> verify(permissionService)
                    .hasPermission(currentUserId, "shell:setting:permission");
            case "/permission/submenu/tree" -> verify(menuService).getSubMenuTree(currentUserId, 1);
            case "/permission/page/access/check" -> verify(pagePermissionService)
                    .checkPageAccess(currentUserId, "/setting/permission/menu");
            case "/permission/page/buttons" -> verify(pagePermissionService)
                    .getPageButtonPermissions(currentUserId, "/setting/permission/menu");
            case "/permission/page/buttons/batch-check" -> verify(pagePermissionService)
                    .batchCheckButtonPermissions(
                            currentUserId,
                            List.of("shell:setting:permission:menu:add"));
            default -> throw new IllegalArgumentException("未覆盖的自助权限接口: " + path);
        }
    }

    private String cacheAuthenticatedUser(String userId) {
        String token = JwtUtil.createJWT(userId);
        UserInfoVO user = new UserInfoVO();
        user.setUserId(userId);
        user.setUserName("permission-security-test");
        UserInfoDTO login = new UserInfoDTO();
        login.setUser(user);
        login.setPermissions(List.of("shell:setting:permission"));
        when(redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + token))
                .thenReturn(JSON.toJSONString(login));
        when(redisCache.expire(anyString(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
        return token;
    }
}
