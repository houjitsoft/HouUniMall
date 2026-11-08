package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.feign.PermissionServiceFeign;
import com.houjiang.bffweb.service.FileImportAdapterRegistry;
import com.houjiang.bffweb.service.FileImportAdapterResult;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebCommonFileImportControllerTest {

    @TempDir
    Path uploadDir;

    @Test
    void catalogReturnsAllPermittedBusinessSystemsInFixedProductOrder() {
        BaseServiceFeign base = mock(BaseServiceFeign.class);
        PermissionServiceFeign permission = mock(PermissionServiceFeign.class);
        when(permission.getUserMenus(Map.of())).thenReturn(ResponseResult.success(List.of(
                menu(11, 0, "M", 1, "AICode", "/aicode"),
                menu(1, 0, "M", 1, "商城", "/mall"),
                menu(6, 0, "M", 1, "财务中心", "/finance"),
                menu(2, 0, "M", 1, "CRM", "/crm"),
                menu(10, 0, "M", 1, "GEO", "/geo"),
                menu(3, 0, "M", 1, "智能制造", "/mes"),
                menu(9, 0, "M", 1, "OA办公", "/oa"),
                menu(4, 0, "M", 1, "项目管理", "/pm"),
                menu(8, 0, "M", 1, "支付中心", "/payment"),
                menu(5, 0, "M", 1, "ERP", "/erp"),
                menu(7, 0, "M", 1, "供应商", "/supplier")
        )));
        when(base.getFileImportCatalog()).thenReturn(ResponseResult.success(List.of(
                object("mall", "商城", "goods"), object("crm", "CRM", "customer"),
                object("mes", "智能制造", "material"), object("pm", "项目管理", "project"),
                object("erp", "ERP", "goods"), object("finance", "财务中心", "voucher"),
                object("supplier", "供应商", "supplier"), object("payment", "支付中心", "payment"),
                object("oa", "OA办公", "document"), object("geo", "GEO", "article"),
                object("aicode", "AICode", "project")
        )));
        WebCommonController controller = controller(base, permission);

        ResponseResult result = controller.getFileImportCatalog();

        assertEquals(1, result.getCode());
        assertEquals(List.of("mall", "crm", "mes", "pm", "erp", "finance",
                "supplier", "payment", "oa", "geo", "aicode"), responseSystemKeys(result));
    }

    
    @Test
    void catalogRecognizesPluginNamespaceAndMallL1Menus() {
        BaseServiceFeign base = mock(BaseServiceFeign.class);
        PermissionServiceFeign permission = mock(PermissionServiceFeign.class);
        when(permission.getUserMenus(Map.of())).thenReturn(ResponseResult.success(List.of(
                menu(1, 0, "M", 1, "商品管理", "/goods"),
                menu(2, 0, "M", 1, "OA办公", "/plugin/oa"),
                menu(3, 0, "M", 1, "CRM", "/plugin/crm"),
                menu(4, 0, "M", 1, "支付中心", "/plugin/unipay")
        )));
        when(base.getFileImportCatalog()).thenReturn(ResponseResult.success(List.of(
                object("mall", "商城", "goods"), object("crm", "CRM", "customer"),
                object("payment", "支付中心", "payment"), object("oa", "OA办公", "document")
        )));
        WebCommonController controller = controller(base, permission);

        ResponseResult result = controller.getFileImportCatalog();

        assertEquals(1, result.getCode());
        assertEquals(List.of("mall", "crm", "payment", "oa"), responseSystemKeys(result));
    }

    @Test
    void catalogContainsOnlyTheCurrentUsersVisibleTopLevelSystems() {
        BaseServiceFeign base = mock(BaseServiceFeign.class);
        PermissionServiceFeign permission = mock(PermissionServiceFeign.class);
        when(permission.getUserMenus(Map.of())).thenReturn(ResponseResult.success(List.of(
                menu(1, 0, "M", 1, "CRM", "/crm"),
                menu(2, 0, "M", 1, "财务中心", "/finance"),
                menu(3, 1, "M", 1, "商城", "/mall"),
                menu(4, 0, "M", 0, "供应商", "/supplier")
        )));
        when(base.getFileImportCatalog()).thenReturn(ResponseResult.success(List.of(
                object("mall", "商城", "goods"), object("crm", "CRM", "customer"),
                object("finance", "财务中心", "voucher"), object("supplier", "供应商", "supplier")
        )));
        WebCommonController controller = controller(base, permission);

        ResponseResult result = controller.getFileImportCatalog();

        assertEquals(1, result.getCode());
        assertEquals(List.of("crm", "finance"), responseSystemKeys(result));
    }

    @Test
    void catalogDoesNotOpenAllSystemsWhenPermissionServiceFails() {
        BaseServiceFeign base = mock(BaseServiceFeign.class);
        PermissionServiceFeign permission = mock(PermissionServiceFeign.class);
        when(permission.getUserMenus(Map.of())).thenReturn(ResponseResult.fail(503, "权限服务暂时不可用"));
        WebCommonController controller = controller(base, permission);

        ResponseResult result = controller.getFileImportCatalog();

        assertEquals(503, result.getCode());
        verify(base, never()).getFileImportCatalog();
    }

    @Test
    void catalogDoesNotGrantSystemsForNamesOrPathsThatOnlyContainAKeySubstring() {
        BaseServiceFeign base = mock(BaseServiceFeign.class);
        PermissionServiceFeign permission = mock(PermissionServiceFeign.class);
        when(permission.getUserMenus(Map.of())).thenReturn(ResponseResult.success(List.of(
                menu(1, 0, "M", 1, "Superpower", "/superpower"),
                menu(2, 0, "M", 1, "Oasis", "/oasis"),
                menu(3, 0, "M", 1, "Geography", "/geography"),
                menu(4, 0, "M", 1, "Payment archive", "/payments-archive")
        )));
        when(base.getFileImportCatalog()).thenReturn(ResponseResult.success(List.of(
                object("erp", "ERP", "goods"), object("oa", "OA办公", "document"),
                object("geo", "GEO", "article"), object("payment", "支付中心", "payment")
        )));
        WebCommonController controller = controller(base, permission);

        ResponseResult result = controller.getFileImportCatalog();

        assertEquals(1, result.getCode());
        assertEquals(List.of(), responseSystemKeys(result));
    }

    @Test
    void recognizeRequiresSystemAndTrimmedDescriptionBeforeUpload() {
        BaseServiceFeign base = mock(BaseServiceFeign.class);
        WebCommonController controller = controller(base, mock(PermissionServiceFeign.class));
        MockMultipartFile file = new MockMultipartFile("file", "customer.pdf", "application/pdf", new byte[]{1});

        assertEquals(422, controller.recognizeFileImport(file, "", "说明", null).getCode());
        assertEquals(422, controller.recognizeFileImport(file, "crm", "   ", null).getCode());
        verify(base, never()).recognizeFileImport(any(), anyString(), anyString(), any(), anyString(), any());
    }

    @Test
    void recognizeSavesOriginalFileAndForwardsStableHashAndUrl() {
        BaseServiceFeign base = mock(BaseServiceFeign.class);
        PermissionServiceFeign permission = mock(PermissionServiceFeign.class);
        when(permission.getUserMenus(Map.of())).thenReturn(ResponseResult.success(List.of(
                menu(1, 0, "M", 1, "CRM", "/crm"))));
        WebCommonController controller = controller(base, permission);
        ReflectionTestUtils.setField(controller, "configuredUploadPath", uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "customer.pdf", "application/pdf", "pdf-data".getBytes());
        when(base.recognizeFileImport(eq(file), eq("crm"), eq("客户资料"), anyString(), anyString(), eq(null)))
                .thenReturn(ResponseResult.success(Map.of("draftNo", "draft-1")));

        ResponseResult result = controller.recognizeFileImport(file, "crm", " 客户资料 ", null);

        assertEquals(1, result.getCode());
        verify(base).recognizeFileImport(eq(file), eq("crm"), eq("客户资料"),
                org.mockito.ArgumentMatchers.startsWith("/uploads/"),
                org.mockito.ArgumentMatchers.matches("[0-9a-f]{64}"), eq(null));
        assertEquals(1, uploadDir.toFile().listFiles().length);
    }

    @Test
    void confirmProcessesEveryItemAndKeepsIndependentResults() {
        BaseServiceFeign base = mock(BaseServiceFeign.class);
        PermissionServiceFeign permission = mock(PermissionServiceFeign.class);
        FileImportAdapterRegistry adapters = mock(FileImportAdapterRegistry.class);
        when(permission.getUserMenus(Map.of())).thenReturn(ResponseResult.success(List.of(
                menu(1, 0, "M", 1, "CRM", "/crm"))));
        when(base.prepareFileImportItem(11L)).thenReturn(ResponseResult.success(prepared(11L, "crm")));
        when(base.prepareFileImportItem(12L)).thenReturn(ResponseResult.success(prepared(12L, "crm")));
        when(adapters.importItem(any(), any()))
                .thenReturn(new FileImportAdapterResult(1, "成功", "C-11", "/crm/customer/detail/C-11", "/uploads/a.pdf"))
                .thenReturn(FileImportAdapterResult.fail(422, "客户手机号不能为空"));
        when(base.completeFileImportItem(eq(11L), any())).thenReturn(ResponseResult.success());
        when(base.completeFileImportItem(eq(12L), any())).thenReturn(ResponseResult.success());
        WebCommonController controller = controller(base, permission, adapters);

        ResponseResult result = controller.confirmFileImport(Map.of("itemIds", List.of(11, 12)));

        assertEquals(1, result.getCode());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals(2, data.get("total"));
        assertEquals(1, data.get("successCount"));
        assertEquals(1, data.get("failedCount"));
        List<?> rows = (List<?>) data.get("results");
        assertEquals("success", ((Map<?, ?>) rows.get(0)).get("status"));
        assertEquals("failed", ((Map<?, ?>) rows.get(1)).get("status"));
        verify(base).completeFileImportItem(eq(11L), argThat(body -> "success".equals(body.get("status"))));
        verify(base).completeFileImportItem(eq(12L), argThat(body -> "failed".equals(body.get("status"))));
    }

    @Test
    void confirmRechecksSystemPermissionBeforeCallingBusinessAdapter() {
        BaseServiceFeign base = mock(BaseServiceFeign.class);
        PermissionServiceFeign permission = mock(PermissionServiceFeign.class);
        FileImportAdapterRegistry adapters = mock(FileImportAdapterRegistry.class);
        when(base.prepareFileImportItem(21L)).thenReturn(ResponseResult.success(prepared(21L, "erp")));
        when(permission.getUserMenus(Map.of())).thenReturn(ResponseResult.success(List.of(
                menu(1, 0, "M", 1, "CRM", "/crm"))));
        when(base.completeFileImportItem(eq(21L), any())).thenReturn(ResponseResult.success());
        WebCommonController controller = controller(base, permission, adapters);

        ResponseResult result = controller.confirmFileImport(Map.of("itemIds", List.of(21)));

        assertEquals(1, result.getCode());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals(0, data.get("successCount"));
        assertEquals(1, data.get("failedCount"));
        verify(adapters, never()).importItem(any(), any());
        verify(base).completeFileImportItem(eq(21L), argThat(body ->
                "failed".equals(body.get("status")) && String.valueOf(body.get("errorMessage")).contains("权限")));
    }

    private WebCommonController controller(BaseServiceFeign base, PermissionServiceFeign permission) {
        return controller(base, permission, mock(FileImportAdapterRegistry.class));
    }

    private WebCommonController controller(BaseServiceFeign base, PermissionServiceFeign permission,
                                           FileImportAdapterRegistry adapters) {
        WebCommonController controller = new WebCommonController();
        ReflectionTestUtils.setField(controller, "baseServiceFeign", base);
        ReflectionTestUtils.setField(controller, "permissionServiceFeign", permission);
        ReflectionTestUtils.setField(controller, "fileImportAdapterRegistry", adapters);
        ReflectionTestUtils.setField(controller, "configuredUploadPath", uploadDir.toString());
        return controller;
    }

    private static Map<String, Object> prepared(long itemId, String systemKey) {
        return Map.of(
                "itemId", itemId,
                "systemKey", systemKey,
                "adapterKey", systemKey + ".customer",
                "fields", Map.of("name", "客户"),
                "attachment", Map.of("fileName", "a.pdf", "fileUrl", "/uploads/a.pdf", "fileSize", 10));
    }

    private static Map<String, Object> menu(int id, int pid, String type, int shown, String name, String path) {
        return Map.of("id", id, "pid", pid, "type", type, "is_show", shown, "name", name, "paths", path);
    }

    private static Map<String, Object> object(String systemKey, String systemName, String objectKey) {
        return Map.of("systemKey", systemKey, "systemName", systemName, "objectKey", objectKey);
    }

    private static List<String> responseSystemKeys(ResponseResult result) {
        Map<?, ?> data = (Map<?, ?>) result.getData();
        List<?> systems = (List<?>) data.get("systems");
        return systems.stream().map(system -> String.valueOf(((Map<?, ?>) system).get("key"))).toList();
    }
}
