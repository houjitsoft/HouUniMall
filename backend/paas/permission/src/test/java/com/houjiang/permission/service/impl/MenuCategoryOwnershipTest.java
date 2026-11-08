package com.houjiang.permission.service.impl;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.permission.entity.PermissionMenu;
import com.houjiang.permission.controller.PermissionController.MenuUpdateRequest;
import com.houjiang.permission.controller.PermissionController.MenuCreateRequest;
import com.houjiang.permission.feign.PluginCategoryClient;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MenuCategoryOwnershipTest {
    private final PermissionMenuMapper mapper = mock(PermissionMenuMapper.class);
    private final PluginCategoryClient catalog = mock(PluginCategoryClient.class);
    private final MenuServiceImpl service = new MenuServiceImpl();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "permissionMenuMapper", mapper);
        ReflectionTestUtils.setField(service, "pluginCategoryClient", catalog);
    }

    @Test void legacyReadDelegatesToBase() {
        var rows = List.<Map<String,Object>>of(Map.of("id", 77, "name", "客户分类", "sort", 3));
        when(catalog.categories()).thenReturn(ResponseResult.success(rows));
        assertEquals(rows, service.getAddonCateList());
    }

    @Test void allSixCoreRootsRejectMovingToPluginOrHidden() {
        for (int id : List.of(19, 1, 1009, 15, 4, 18)) {
            menu(id, "/core", "", "left");
            assertThrows(IllegalArgumentException.class, () -> service.updateMenuDisplay(id, "plugin", 77));
            assertThrows(IllegalArgumentException.class, () -> service.updateMenuDisplay(id, "hidden", 0));
        }
        verify(mapper, never()).updateById(any(PermissionMenu.class));
    }

    @Test void pluginRootCannotMoveLeft() {
        menu(88, "/plugin/erp", "erp", "plugin");
        assertThrows(IllegalArgumentException.class, () -> service.updateMenuDisplay(88, "left", 0));
        verify(mapper, never()).updateById(any(PermissionMenu.class));
    }

    @Test void lowcodeUsesPluginCodeForCanonicalCategoryWrite() {
        var menu = menu(88, "/lowcode", "lowcode", "plugin");
        when(catalog.updateCategory(Map.of("pluginCode", "lowcode", "cateId", 77))).thenReturn(ResponseResult.success());
        service.updateMenuDisplay(88, "plugin", 77);
        assertEquals(77, menu.getCateId());
        verify(catalog).updateCategory(Map.of("pluginCode", "lowcode", "cateId", 77));
        verify(mapper).updateById(menu);
    }

    @Test void upstreamRejectionPreventsLocalCategoryWrite() {
        menu(88, "/plugin/erp", "erp", "plugin");
        when(catalog.updateCategory(anyMap())).thenReturn(ResponseResult.fail(503, "base unavailable"));
        assertThrows(IllegalStateException.class, () -> service.updateMenuDisplay(88, "plugin", 77));
        verify(mapper, never()).updateById(any(PermissionMenu.class));
    }

    @Test void generalEditCannotReparentPluginRootUnderCore() {
        menu(88, "/plugin/erp", "erp", "plugin");
        var edit = new MenuUpdateRequest(); edit.setId(88); edit.setParentId(4); edit.setPaths("/plugin/erp");
        assertThrows(IllegalArgumentException.class, () -> service.editMenu(edit));
        verify(mapper, never()).updateById(any(PermissionMenu.class));
    }

    @Test void generalEditCannotChangeCoreOrderingOrParent() {
        menu(1, "/mall", "", "left");
        var edit = new MenuUpdateRequest(); edit.setId(1); edit.setParentId(4); edit.setSort(9000);
        assertThrows(IllegalArgumentException.class, () -> service.editMenu(edit));
        verify(mapper, never()).updateById(any(PermissionMenu.class));
    }

    @Test void fixedCoreCannotBeDeletedEvenWhenEmpty() {
        menu(1, "/mall", "", "left");
        when(mapper.selectList(any())).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> service.deleteMenu(1));
        verify(mapper, never()).deleteById(anyInt());
    }

    @Test void menuDetailIncludesCanonicalPluginCodeForCustomRoutes() {
        menu(88, "/custom-reports", "reporting", "plugin");
        assertEquals("reporting", service.getMenuDetail(88).getPluginCode());
    }

    @Test void childCreationInheritsRootOwnershipAndCategory() {
        menu(88, "/plugin/erp", "erp", "plugin").setCateId(999);
        var request = new MenuCreateRequest(); request.setParentId(88); request.setPaths("/plugin/erp/reports");
        service.addMenu(request);
        var added = org.mockito.ArgumentCaptor.forClass(PermissionMenu.class);
        verify(mapper).insert(added.capture());
        assertEquals("erp", added.getValue().getPluginCode());
        assertEquals(999, added.getValue().getCateId());
        assertEquals("left", added.getValue().getDisplayPos());
    }

    @Test void childCreationRejectsPathFromAnotherPlugin() {
        menu(88, "/plugin/erp", "erp", "plugin");
        var request = new MenuCreateRequest(); request.setParentId(88); request.setPaths("/plugin/finance/reports");
        assertThrows(IllegalArgumentException.class, () -> service.addMenu(request));
        verify(mapper, never()).insert(any(PermissionMenu.class));
    }

    @Test void childEditCannotMoveBetweenApplicationsOrBecomeRoot() {
        menu(88, "/plugin/erp", "erp", "plugin");
        menu(89, "/plugin/finance", "finance", "plugin");
        menu(90, "/plugin/erp/reports", "erp", "left").setPid(88);
        var request = new MenuUpdateRequest(); request.setId(90); request.setPaths("/plugin/erp/reports");
        for (int parentId : List.of(89, 0)) {
            request.setParentId(parentId);
            assertThrows(IllegalArgumentException.class, () -> service.editMenu(request));
        }
        verify(mapper, never()).updateById(any(PermissionMenu.class));
    }

    @Test void editingLegacyChildInheritsItsApplicationsCurrentCategory() {
        menu(88, "/plugin/erp", "erp", "plugin").setCateId(999);
        menu(90, "/reports", "", "left").setPid(88);
        var request = new MenuUpdateRequest(); request.setId(90); request.setParentId(88); request.setPaths("/reports");
        service.editMenu(request);
        var edited = org.mockito.ArgumentCaptor.forClass(PermissionMenu.class);
        verify(mapper).updateById(edited.capture());
        assertEquals("erp", edited.getValue().getPluginCode());
        assertEquals(999, edited.getValue().getCateId());
    }

    private PermissionMenu menu(int id, String path, String plugin, String position) {
        var menu = new PermissionMenu(); menu.setId(id); menu.setPid(0); menu.setPaths(path);
        menu.setPluginCode(plugin); menu.setDisplayPos(position); menu.setCateId(4);
        when(mapper.selectById(id)).thenReturn(menu); return menu;
    }
}
