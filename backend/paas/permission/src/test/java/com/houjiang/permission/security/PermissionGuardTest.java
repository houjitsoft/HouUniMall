package com.houjiang.permission.security;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.permission.config.PermissionAuthorizationProperties;
import com.houjiang.permission.entity.PermissionMenu;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.RoleMapper;
import com.houjiang.permission.mapper.UserRoleMapper;
import com.houjiang.permission.service.PermissionService;
import com.houjiang.permission.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PermissionGuardTest {

    private static final Instant NOW = Instant.parse("2026-09-10T06:00:00Z");

    @Mock private PermissionIdentity identity;
    @Mock private PermissionService permissionService;
    @Mock private RoleMapper roleMapper;
    @Mock private PermissionMenuMapper permissionMenuMapper;

    private PermissionAuthorizationProperties properties;
    private PermissionGuard guard;

    @BeforeEach
    void setUp() {
        properties = new PermissionAuthorizationProperties();
        guard = new PermissionGuard(identity, permissionService, roleMapper, permissionMenuMapper,
                properties, Clock.fixed(NOW, ZoneOffset.UTC));
        lenient().when(identity.currentUserId()).thenReturn(88L);
    }

    @Test
    void permitsMissingReadPermissionOnlyDuringValidObservationWindow() {
        properties.setMode(PermissionAuthorizationProperties.Mode.OBSERVE);
        properties.setObserveUntil(NOW.plusSeconds(60));
        when(permissionService.hasPermission(88L, "permission:menu")).thenReturn(false);

        assertDoesNotThrow(() -> guard.require("permission:menu", PermissionGuard.Risk.READ));
    }

    @Test
    void expiredObservationWindowEnforcesReadPermission() {
        properties.setMode(PermissionAuthorizationProperties.Mode.OBSERVE);
        properties.setObserveUntil(NOW);
        when(permissionService.hasPermission(88L, "permission:menu")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> guard.require("permission:menu", PermissionGuard.Risk.READ));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void highRiskActionIsAlwaysEnforcedDuringObservation() {
        properties.setMode(PermissionAuthorizationProperties.Mode.OBSERVE);
        properties.setObserveUntil(NOW.plusSeconds(60));
        when(permissionService.hasPermission(88L, "permission:menu:delete")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> guard.require("permission:menu:delete", PermissionGuard.Risk.DELETE));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @ParameterizedTest
    @EnumSource(value = PermissionGuard.Risk.class, names = {
            "WRITE", "DELETE", "ASSIGN", "RESET_PASSWORD", "STATUS_CHANGE",
            "CACHE_CLEAR", "IDENTITY_MISMATCH"
    })
    void onlyReadMayBeObserved(PermissionGuard.Risk risk) {
        properties.setMode(PermissionAuthorizationProperties.Mode.OBSERVE);
        properties.setObserveUntil(NOW.plusSeconds(60));
        when(permissionService.hasPermission(88L, "permission:operation")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> guard.require("permission:operation", risk));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void observationWindowCannotExceedSeventyTwoHours() {
        properties.setMode(PermissionAuthorizationProperties.Mode.OBSERVE);
        properties.setObserveUntil(NOW.plusSeconds(72 * 60 * 60 + 1));
        when(permissionService.hasPermission(88L, "permission:menu")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> guard.require("permission:menu", PermissionGuard.Risk.READ));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void superAdminLookupFailureRemainsServiceUnavailable() {
        when(roleMapper.selectRoleNamesByUserId(88L)).thenThrow(new IllegalStateException("database offline"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, guard::requireSuperAdmin);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }

    @Test
    void dependencyFailureRemainsServiceUnavailable() {
        when(permissionService.hasPermission(88L, "permission:menu"))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> guard.require("permission:menu", PermissionGuard.Risk.READ));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }

    @Test
    void livePermissionLookupConvertsMapperFailureToServiceUnavailable() {
        PermissionServiceImpl service = new PermissionServiceImpl();
        UserRoleMapper failingMapper = mock(UserRoleMapper.class);
        ReflectionTestUtils.setField(service, "userRoleMapper", failingMapper);
        when(failingMapper.getRoleIdsByUserId(88L)).thenThrow(new IllegalStateException("database offline"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.hasPermission(88L, "permission:menu"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }

    @Test
    void permissionListLookupConvertsMapperFailureToServiceUnavailable() {
        PermissionServiceImpl service = new PermissionServiceImpl();
        PermissionMenuMapper failingMapper = mock(PermissionMenuMapper.class);
        ReflectionTestUtils.setField(service, "permissionMenuMapper", failingMapper);
        when(failingMapper.selectUserPermissionPermsByUserId(88L))
                .thenThrow(new IllegalStateException("database offline"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.getUserPermissions(88L));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void superAdminRequiresStandardRoleNameAndCompletePermissionCenterSet() {
        when(roleMapper.selectRoleNamesByUserId(88L)).thenReturn(List.of("super_admin"));
        PermissionMenu menu = menu(1, "shell:setting:permission:menu");
        PermissionMenu role = menu(2, "shell:setting:permission:role");
        when(permissionMenuMapper.selectList(any(Wrapper.class))).thenReturn(List.of(menu, role));
        when(permissionService.hasPermission(88L, "shell:setting:permission:menu")).thenReturn(true);
        when(permissionService.hasPermission(88L, "shell:setting:permission:role")).thenReturn(true);

        assertDoesNotThrow(guard::requireSuperAdmin);
    }

    private static PermissionMenu menu(int id, String permission) {
        PermissionMenu menu = new PermissionMenu();
        menu.setId(id);
        menu.setPerms(permission);
        menu.setIsDisable(0);
        return menu;
    }
}
