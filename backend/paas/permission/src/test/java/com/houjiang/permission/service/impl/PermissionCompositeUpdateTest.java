package com.houjiang.permission.service.impl;

import com.houjiang.permission.dto.RoleUpdateRequest;
import com.houjiang.permission.dto.UserUpdateRequest;
import com.houjiang.permission.entity.Role;
import com.houjiang.permission.entity.User;
import com.houjiang.permission.entity.UserRoleRelation;
import com.houjiang.permission.mapper.RoleMapper;
import com.houjiang.permission.mapper.UserMapper;
import com.houjiang.permission.mapper.UserRoleRelationMapper;
import com.houjiang.permission.service.RoleMenuService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionCompositeUpdateTest {

    @Test
    void omittedRoleMenuIdsPreserveExistingAssignments() {
        RoleMapper roleMapper = mock(RoleMapper.class);
        RoleMenuService roleMenuService = mock(RoleMenuService.class);
        RoleServiceImpl service = new RoleServiceImpl();
        ReflectionTestUtils.setField(service, "roleMapper", roleMapper);
        ReflectionTestUtils.setField(service, "roleMenuService", roleMenuService);

        Role existing = new Role();
        existing.setId(8);
        existing.setName("operator");
        when(roleMapper.selectOne(any())).thenReturn(existing).thenReturn((Role) null);
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setId(8);
        request.setName("operator");
        request.setSort(1);
        request.setMenuIds(null);

        service.updateRole(request);

        verify(roleMenuService, never()).batchDeleteByRoleId(8);
        verify(roleMenuService, never()).batchSaveByMenuIds(any(), any());
    }

    @Test
    void explicitEmptyRoleMenuIdsClearAssignments() {
        RoleMapper roleMapper = mock(RoleMapper.class);
        RoleMenuService roleMenuService = mock(RoleMenuService.class);
        RoleServiceImpl service = new RoleServiceImpl();
        ReflectionTestUtils.setField(service, "roleMapper", roleMapper);
        ReflectionTestUtils.setField(service, "roleMenuService", roleMenuService);

        Role existing = new Role();
        existing.setId(8);
        existing.setName("operator");
        when(roleMapper.selectOne(any())).thenReturn(existing).thenReturn((Role) null);
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setId(8);
        request.setName("operator");
        request.setSort(1);
        request.setMenuIds(List.of());

        service.updateRole(request);

        verify(roleMenuService).batchDeleteByRoleId(8);
    }

    @Test
    void omittedUserRoleIdsPreserveExistingAssignments() {
        UserMapper userMapper = mock(UserMapper.class);
        UserRoleRelationMapper relationMapper = mock(UserRoleRelationMapper.class);
        UserServiceImpl service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", userMapper);
        ReflectionTestUtils.setField(service, "userRoleRelationMapper", relationMapper);

        User existing = new User();
        existing.setUserId(9L);
        when(userMapper.selectOne(any())).thenReturn(existing);
        when(userMapper.selectByUsername("operator")).thenReturn(existing);
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId("9");
        request.setUsername("operator");
        request.setRealName("操作员");
        request.setRoleIds(null);

        service.updateUser(request);

        verify(relationMapper, never()).delete(any());
        verify(relationMapper, never()).insert(any(UserRoleRelation.class));
    }

    @Test
    void explicitEmptyUserRoleIdsClearAssignments() {
        UserMapper userMapper = mock(UserMapper.class);
        UserRoleRelationMapper relationMapper = mock(UserRoleRelationMapper.class);
        UserServiceImpl service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", userMapper);
        ReflectionTestUtils.setField(service, "userRoleRelationMapper", relationMapper);

        User existing = new User();
        existing.setUserId(9L);
        when(userMapper.selectOne(any())).thenReturn(existing);
        when(userMapper.selectByUsername("operator")).thenReturn(existing);
        UserUpdateRequest request = new UserUpdateRequest();
        request.setId("9");
        request.setUsername("operator");
        request.setRealName("操作员");
        request.setRoleIds(List.of());

        service.updateUser(request);

        verify(relationMapper).delete(any());
        verify(relationMapper, never()).insert(any(UserRoleRelation.class));
    }
}
