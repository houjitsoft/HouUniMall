package com.houjiang.mall.controller.plugin;

import com.houjiang.mall.service.plugin.PluginCenterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PluginCategoryControllerTest {
    private final PluginCenterService service = mock(PluginCenterService.class);
    private final PluginCenterController controller = new PluginCenterController(service, null, null, null);
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void ordinaryAuthenticatedUserCanReadDictionaryWithoutManagePermission() {
        authenticate("ROLE_CLOUD_USER");
        var rows = List.<Map<String, Object>>of(Map.of("id", 41, "name", "自定义分类", "sort", 7));
        when(service.listCategories()).thenReturn(rows);
        assertEquals(rows, controller.categories().getData());
    }

    @Test void unauthenticatedDictionaryReadIsRejected() {
        assertEquals(401, controller.categories().getCode());
        verifyNoInteractions(service);
    }

    @Test void pluginManagePermissionDoesNotGrantMenuCategoryWrite() {
        authenticate("shell:addons");
        assertEquals(403, controller.updateCategory(Map.of("pluginCode", "erp", "cateId", 41)).getCode());
        verifyNoInteractions(service);
    }

    @Test void exactMenuEditPermissionCanUpdateCanonicalCategory() {
        authenticate("shell:setting:permission:menu:edit");
        assertEquals(com.houjiang.commons.constant.CommonStatusEnum.SUCCESS.getCode(), controller.updateCategory(Map.of("pluginCode", "erp", "cateId", 41)).getCode());
        verify(service).updateCategory("erp", 41);
    }

    private void authenticate(String permission) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user-7", null, List.of(new SimpleGrantedAuthority(permission))));
    }
}
