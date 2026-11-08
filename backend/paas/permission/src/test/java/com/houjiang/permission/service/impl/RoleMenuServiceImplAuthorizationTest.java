package com.houjiang.permission.service.impl;

import com.houjiang.permission.entity.RoleMenu;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.RoleMenuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleMenuServiceImplAuthorizationTest {

    private RoleMenuMapper roleMenuMapper;
    private RoleMenuServiceImpl service;

    @BeforeEach
    void setUp() {
        roleMenuMapper = mock(RoleMenuMapper.class);
        service = new RoleMenuServiceImpl();
        ReflectionTestUtils.setField(service, "roleMenuMapper", roleMenuMapper);
        ReflectionTestUtils.setField(service, "permissionMenuMapper", mock(PermissionMenuMapper.class));
    }

    @Test
    void addsOnlyMissingMenusWithoutReplacingExistingAssignments() {
        RoleMenu existing = new RoleMenu();
        existing.setRoleId(8);
        existing.setMenuId(20);
        when(roleMenuMapper.selectList(any())).thenReturn(List.of(existing));

        service.addMissingMenuIds(8, List.of(20, 21, 21));

        verify(roleMenuMapper).insertIfMissing(8, 21);
        verify(roleMenuMapper, never()).insertIfMissing(8, 20);
        verify(roleMenuMapper, never()).deleteByRoleIdFast(8);
    }
}
