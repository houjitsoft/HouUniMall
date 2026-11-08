package com.houjiang.permission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.permission.dto.RoleCreateRequest;
import com.houjiang.permission.dto.RoleUpdateRequest;
import com.houjiang.permission.dto.UserCreateRequest;
import com.houjiang.permission.dto.UserSearchRequest;
import com.houjiang.permission.dto.UserUpdateRequest;
import com.houjiang.permission.entity.PermissionMenu;
import com.houjiang.permission.entity.Role;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.RoleMapper;
import com.houjiang.permission.security.PermissionGuard;
import com.houjiang.permission.service.MenuService;
import com.houjiang.permission.service.RoleMenuService;
import com.houjiang.permission.service.RoleService;
import com.houjiang.permission.service.UserRoleService;
import com.houjiang.permission.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PermissionManagementAuthorizationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PermissionController controller;
    private PermissionGuard guard;
    private MenuService menuService;
    private RoleService roleService;
    private UserService userService;
    private UserRoleService userRoleService;
    private RoleMenuService roleMenuService;
    private PermissionMenuMapper permissionMenuMapper;
    private RoleMapper roleMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new PermissionController();
        guard = mock(PermissionGuard.class);
        menuService = mock(MenuService.class);
        roleService = mock(RoleService.class);
        userService = mock(UserService.class);
        userRoleService = mock(UserRoleService.class);
        roleMenuService = mock(RoleMenuService.class);
        permissionMenuMapper = mock(PermissionMenuMapper.class);
        roleMapper = mock(RoleMapper.class);
        ReflectionTestUtils.setField(controller, "permissionGuard", guard);
        ReflectionTestUtils.setField(controller, "menuService", menuService);
        ReflectionTestUtils.setField(controller, "roleService", roleService);
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "userRoleService", userRoleService);
        ReflectionTestUtils.setField(controller, "roleMenuService", roleMenuService);
        ReflectionTestUtils.setField(controller, "permissionMenuMapper", permissionMenuMapper);
        ReflectionTestUtils.setField(controller, "roleMapper", roleMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("managementEndpointMatrix")
    void everyManagementEndpointRequiresItsBasePermission(
            String endpoint,
            Consumer<PermissionController> invocation,
            String permission,
            PermissionGuard.Risk risk) {
        invocation.accept(controller);

        verify(guard).require(permission, risk);
    }

    @Test
    void menuEditCannotSmuggleStatusChange() {
        PermissionController.MenuUpdateRequest request = new PermissionController.MenuUpdateRequest();
        request.setId(9);
        request.setIsDisable(1);
        var current = new com.houjiang.permission.vo.MenuVO();
        current.setIsDisable(0);
        when(menuService.getMenuDetail(9)).thenReturn(current);

        controller.editMenu(request);

        var order = inOrder(guard, menuService);
        order.verify(guard).require("shell:setting:permission:menu:edit", PermissionGuard.Risk.WRITE);
        order.verify(guard).require("shell:setting:permission:menu:disable", PermissionGuard.Risk.STATUS_CHANGE);
        order.verify(menuService).editMenu(request);
    }

    @Test
    void menuEditWithUnchangedStatusDoesNotRequireStatusPermission() {
        PermissionController.MenuUpdateRequest request = new PermissionController.MenuUpdateRequest();
        request.setId(9);
        request.setIsDisable(1);
        var current = new com.houjiang.permission.vo.MenuVO();
        current.setIsDisable(1);
        when(menuService.getMenuDetail(9)).thenReturn(current);

        controller.editMenu(request);

        verify(guard).require("shell:setting:permission:menu:edit", PermissionGuard.Risk.WRITE);
        verify(guard, never()).require("shell:setting:permission:menu:disable", PermissionGuard.Risk.STATUS_CHANGE);
    }

    @Test
    void roleCreateWithMenuIdsRequiresAssignPermission() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setMenuIds(List.of(1, 2));

        controller.createRole(request);

        verify(guard).require("shell:setting:permission:role:add", PermissionGuard.Risk.WRITE);
        verify(guard).require("shell:setting:permission:role:assign", PermissionGuard.Risk.ASSIGN);
    }

    @Test
    void userEditCannotSmuggleRoleOrStatusChanges() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId("9");
        request.setRoleIds(List.of(3));
        request.setStatus(0);
        var current = new com.houjiang.permission.vo.UserVO();
        current.setStatus(1);
        when(userService.getUserDetail("9")).thenReturn(current);

        controller.updateUser(request);

        verify(guard).require("shell:setting:permission:user:edit", PermissionGuard.Risk.WRITE);
        verify(guard).require("shell:setting:permission:user:assign", PermissionGuard.Risk.ASSIGN);
        verify(guard).require("shell:setting:permission:user:disable", PermissionGuard.Risk.STATUS_CHANGE);
    }

    @Test
    void userEditWithUnchangedStatusDoesNotRequireStatusPermission() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId("9");
        request.setStatus(1);
        var current = new com.houjiang.permission.vo.UserVO();
        current.setStatus(1);
        when(userService.getUserDetail("9")).thenReturn(current);

        controller.updateUser(request);

        verify(guard).require("shell:setting:permission:user:edit", PermissionGuard.Risk.WRITE);
        verify(guard, never()).require("shell:setting:permission:user:enable", PermissionGuard.Risk.STATUS_CHANGE);
    }

    @Test
    void fixAdminPermissionsRequiresAssignmentAndSuperAdmin() {
        Role admin = new Role();
        admin.setId(8);
        PermissionMenu required = new PermissionMenu();
        required.setId(21);
        required.setPerms("shell:setting:permission:user:disable");
        when(roleMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(admin));
        when(permissionMenuMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(required));

        controller.fixAdminPermissions(Map.of());

        var order = inOrder(guard);
        order.verify(guard).require("shell:setting:permission:role:assign", PermissionGuard.Risk.ASSIGN);
        order.verify(guard).requireSuperAdmin();
        verify(roleMenuService).addMissingMenuIds(8, List.of(21));
        verify(roleMenuService, never()).batchDeleteByRoleId(8);
        verify(roleMenuService, never()).batchSaveByMenuIds(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void clearingAllCacheRequiresExplicitConfirmation() throws Exception {
        mockMvc.perform(post("/permission/cache/clear")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("type", "all"))))
                .andExpect(status().isBadRequest());

        verify(guard).require("shell:setting:permission:cache:clear", PermissionGuard.Risk.CACHE_CLEAR);
    }

    @Test
    void removedRawQueryEndpointReturnsNotFound() throws Exception {
        mockMvc.perform(post("/permission/menu/raw-query")
                        .contentType("application/json")
                        .content("{\"pid\":1}"))
                .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> managementEndpointMatrix() {
        return Stream.of(
                endpoint("menu/list", PermissionController::getAllMenus, "shell:setting:permission:menu", PermissionGuard.Risk.READ),
                endpoint("menu/tree", PermissionController::getMenuTreeForRole, "shell:setting:permission:menu", PermissionGuard.Risk.READ),
                endpoint("menu/role/{id}", c -> c.getMenusByRole(1), "shell:setting:permission:menu", PermissionGuard.Risk.READ),
                endpoint("menu/detail/{id}", c -> c.getMenuDetail(1), "shell:setting:permission:menu", PermissionGuard.Risk.READ),
                endpoint("menu/add", c -> c.addMenu(new PermissionController.MenuCreateRequest()), "shell:setting:permission:menu:add", PermissionGuard.Risk.WRITE),
                endpoint("menu/edit", c -> c.editMenu(new PermissionController.MenuUpdateRequest()), "shell:setting:permission:menu:edit", PermissionGuard.Risk.WRITE),
                endpoint("menu/delete", c -> c.deleteMenu(new PermissionController.MenuDeleteRequest()), "shell:setting:permission:menu:delete", PermissionGuard.Risk.DELETE),
                endpoint("menu/clear-cache", PermissionController::clearMenuCache, "shell:setting:permission:menu:edit", PermissionGuard.Risk.CACHE_CLEAR),
                endpoint("role/all", PermissionController::getAllRoles, "shell:setting:permission:role", PermissionGuard.Risk.READ),
                endpoint("role/list", c -> c.getRoleList(Map.of()), "shell:setting:permission:role", PermissionGuard.Risk.READ),
                endpoint("role/detail/{id}", c -> c.getRoleDetail(1), "shell:setting:permission:role", PermissionGuard.Risk.READ),
                endpoint("role/add", c -> c.createRole(new RoleCreateRequest()), "shell:setting:permission:role:add", PermissionGuard.Risk.WRITE),
                endpoint("role/edit", c -> c.updateRole(new RoleUpdateRequest()), "shell:setting:permission:role:edit", PermissionGuard.Risk.WRITE),
                endpoint("role/delete", c -> c.deleteRole(new PermissionController.RoleDeleteRequest()), "shell:setting:permission:role:delete", PermissionGuard.Risk.DELETE),
                endpoint("role/users", c -> c.getRoleUsers(Map.of("roleId", 1)), "shell:setting:permission:role:assign", PermissionGuard.Risk.ASSIGN),
                endpoint("role/menu/batch-set", c -> c.batchSetRoleMenus(Map.of("roleId", 1)), "shell:setting:permission:role:assign", PermissionGuard.Risk.ASSIGN),
                endpoint("user/list", c -> c.getUserList(new UserSearchRequest()), "shell:setting:permission:user", PermissionGuard.Risk.READ),
                endpoint("user/detail/{id}", c -> c.getUserDetail("1"), "shell:setting:permission:user", PermissionGuard.Risk.READ),
                endpoint("role/user/{id}", c -> c.getRoleNamesByUserId("1"), "shell:setting:permission:user", PermissionGuard.Risk.READ),
                endpoint("user/add", c -> c.createUser(new UserCreateRequest()), "shell:setting:permission:user:add", PermissionGuard.Risk.WRITE),
                endpoint("user/edit", c -> c.updateUser(new UserUpdateRequest()), "shell:setting:permission:user:edit", PermissionGuard.Risk.WRITE),
                endpoint("user/delete", c -> c.deleteUser(new PermissionController.UserDeleteRequest()), "shell:setting:permission:user:delete", PermissionGuard.Risk.DELETE),
                endpoint("user/reset-password", c -> c.resetPassword(new PermissionController.UserResetPasswordRequest()), "shell:setting:permission:user:reset", PermissionGuard.Risk.RESET_PASSWORD),
                endpoint("user/roles", c -> c.getUserRoles(Map.of("userId", 1)), "shell:setting:permission:user:assign", PermissionGuard.Risk.ASSIGN),
                endpoint("user/roles/set", c -> c.setUserRoles(Map.of("userId", 1)), "shell:setting:permission:user:assign", PermissionGuard.Risk.ASSIGN),
                endpoint("user/update-status", c -> {
                    var request = new PermissionController.UserUpdateStatusRequest();
                    request.setStatus(0);
                    c.updateUserStatus(request);
                }, "shell:setting:permission:user:disable", PermissionGuard.Risk.STATUS_CHANGE),
                endpoint("cache/clear", c -> c.clearCache(Map.of()), "shell:setting:permission:cache:clear", PermissionGuard.Risk.CACHE_CLEAR));
    }

    private static Arguments endpoint(
            String name,
            Consumer<PermissionController> invocation,
            String permission,
            PermissionGuard.Risk risk) {
        return Arguments.of(name, invocation, permission, risk);
    }
}
