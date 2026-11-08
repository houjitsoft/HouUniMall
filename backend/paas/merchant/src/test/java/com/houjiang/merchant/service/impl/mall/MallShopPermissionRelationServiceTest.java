package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.entity.mall.MallShopAdminRole;
import com.houjiang.merchant.common.entity.mall.MallShopMenu;
import com.houjiang.merchant.common.entity.mall.MallShopRoleMenu;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminRoleMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMenuMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopRoleMenuMapper;
import com.houjiang.merchant.service.mall.IMallShopAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MallShopPermissionRelationServiceTest {

    @Mock
    private IMallShopAdminService adminService;
    @Mock
    private MallShopMenuMapper menuMapper;
    @Mock
    private MallShopAdminRoleMapper adminRoleMapper;
    @Mock
    private MallShopRoleMenuMapper roleMenuMapper;

    private MallShopAdminMenuServiceImpl adminMenuService;

    @BeforeEach
    void setUp() {
        adminMenuService = new MallShopAdminMenuServiceImpl();
        ReflectionTestUtils.setField(adminMenuService, "iMallShopAdminService", adminService);
        ReflectionTestUtils.setField(adminMenuService, "mallShopMenuMapper", menuMapper);
        ReflectionTestUtils.setField(adminMenuService, "mallShopAdminRoleMapper", adminRoleMapper);
        ReflectionTestUtils.setField(adminMenuService, "mallShopRoleMenuMapper", roleMenuMapper);
    }

    @Test
    void readsHistoricalPermissionRelationsWhoseDeleteTimeIsNull() {
        MallShopAdmin admin = new MallShopAdmin();
        admin.setId(9);
        admin.setIsSuper(0);
        MallShopAdminRole adminRole = new MallShopAdminRole();
        adminRole.setRoleId(3);
        MallShopRoleMenu roleMenu = new MallShopRoleMenu();
        roleMenu.setMenuId(5);
        MallShopMenu menu = new MallShopMenu();
        menu.setAccess("mall.Goods/lists");

        when(adminService.getById(9)).thenReturn(admin);
        when(adminRoleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(adminRole));
        when(roleMenuMapper.selectList(any(Wrapper.class))).thenReturn(List.of(roleMenu));
        when(menuMapper.selectList(any(Wrapper.class))).thenReturn(List.of(menu));

        assertEquals(List.of("mall.Goods/lists"), adminMenuService.getAdminAccessList(9));

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<Wrapper<MallShopAdminRole>> adminRoleQuery =
                ArgumentCaptor.forClass((Class) Wrapper.class);
        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<Wrapper<MallShopRoleMenu>> roleMenuQuery =
                ArgumentCaptor.forClass((Class) Wrapper.class);
        verify(adminRoleMapper).selectList(adminRoleQuery.capture());
        verify(roleMenuMapper).selectList(roleMenuQuery.capture());
        assertTrue(adminRoleQuery.getValue().getCustomSqlSegment().contains("delete_time IS NULL"));
        assertTrue(roleMenuQuery.getValue().getCustomSqlSegment().contains("delete_time IS NULL"));
    }

    @Test
    void newRoleMenuRelationsAreWrittenAsActive() {
        MallShopRoleMenuServiceImpl service = new MallShopRoleMenuServiceImpl();
        ReflectionTestUtils.setField(service, "mallShopRoleMenuMapper", roleMenuMapper);

        service.batchSaveByMenuIds(3, List.of(5), 7);

        ArgumentCaptor<MallShopRoleMenu> relation = ArgumentCaptor.forClass(MallShopRoleMenu.class);
        verify(roleMenuMapper).insert(relation.capture());
        assertEquals(0L, relation.getValue().getDeleteTime());
    }

    @Test
    void newAdminRoleRelationsAreWrittenAsActive() {
        MallShopAdminRoleServiceImpl service = new MallShopAdminRoleServiceImpl();
        ReflectionTestUtils.setField(service, "mallShopAdminRoleMapper", adminRoleMapper);

        service.batchInsert(9, List.of(3), 7);

        ArgumentCaptor<MallShopAdminRole> relation = ArgumentCaptor.forClass(MallShopAdminRole.class);
        verify(adminRoleMapper).insert(relation.capture());
        assertEquals(0L, relation.getValue().getDeleteTime());
    }
}
