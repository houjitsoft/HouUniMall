package com.houjiang.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.permission.entity.PermissionMenu;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.RoleMenuMapper;
import com.houjiang.permission.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PagePermissionServiceImplTest {

    @Mock private PermissionMenuMapper permissionMenuMapper;
    @Mock private RoleMenuMapper roleMenuMapper;
    @Mock private UserRoleMapper userRoleMapper;
    private PagePermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PagePermissionServiceImpl(permissionMenuMapper, roleMenuMapper, userRoleMapper);
        lenient().when(userRoleMapper.getRoleIdsByUserId(7L)).thenReturn(List.of(2));
        lenient().when(roleMenuMapper.getMenuIdsByRoleIds(List.of(2))).thenReturn(List.of(10, 11));
    }

    @Test
    @SuppressWarnings("unchecked")
    void normalizesQueryHashLeadingAndTrailingSlashBeforeExactPathLookup() {
        when(permissionMenuMapper.selectOne(any())).thenReturn(
                page(10, 0, "C", "/setting/permission/menu"));

        assertTrue(service.checkPageAccess(7L, "setting/permission/menu/?tab=all#top"));

        ArgumentCaptor<QueryWrapper<PermissionMenu>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(permissionMenuMapper).selectOne(query.capture());
        assertEquals("/setting/permission/menu",
                PagePermissionServiceImpl.normalizePagePath(
                        "setting/permission/menu/?tab=all#top"));
        assertTrue(query.getValue().getSqlSegment().contains("paths"));
        assertFalse(query.getValue().getSqlSegment().contains("perms"));
    }

    @Test
    void similarPrefixDoesNotAuthorizeAnotherPage() {
        when(permissionMenuMapper.selectOne(any())).thenReturn(null);

        assertFalse(service.checkPageAccess(7L, "/setting/permission/menu-legacy"));
    }

    @Test
    void disabledPageIsNotAccessible() {
        when(permissionMenuMapper.selectOne(any())).thenReturn(null);

        assertFalse(service.checkPageAccess(7L, "/setting/permission/menu"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void pageButtonsAreRestrictedToEnabledChildrenOwnedByTheUsersRoles() {
        when(permissionMenuMapper.selectOne(any())).thenReturn(
                page(10, 0, "C", "/setting/permission/menu"));
        PermissionMenu edit = page(11, 10, "A", null);
        edit.setPerms("shell:setting:permission:menu:edit");
        when(permissionMenuMapper.selectList(any())).thenReturn(List.of(edit));

        assertEquals(List.of("shell:setting:permission:menu:edit"),
                service.getPageButtonPermissions(7L, "/setting/permission/menu"));

        ArgumentCaptor<QueryWrapper<PermissionMenu>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(permissionMenuMapper).selectList(query.capture());
        String sqlSegment = query.getValue().getSqlSegment().toLowerCase();
        assertTrue(sqlSegment.contains("pid"));
        assertTrue(sqlSegment.contains("is_disable"));
    }

    @Test
    void nullButtonCodeListReturnsEmptyResult() {
        assertEquals(Map.of(), service.batchCheckButtonPermissions(7L, null));
    }

    private static PermissionMenu page(int id, int pid, String type, String path) {
        PermissionMenu menu = new PermissionMenu();
        menu.setId(id);
        menu.setPid(pid);
        menu.setType(type);
        menu.setPaths(path);
        menu.setIsDisable(0);
        return menu;
    }
}
