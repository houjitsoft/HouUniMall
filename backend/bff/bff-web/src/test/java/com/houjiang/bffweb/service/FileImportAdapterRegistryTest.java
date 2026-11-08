package com.houjiang.bffweb.service;

import com.houjiang.bffweb.feign.CrmServiceFeign;
import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.feign.MesServiceFeign;
import com.houjiang.bffweb.feign.PmServiceFeign;
import com.houjiang.bffweb.service.fileimport.FileImportBusinessAdapter;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileImportAdapterRegistryTest {

    @Test
    void registersBusinessAdaptersByKeyAndRejectsDuplicateKeys() throws ReflectiveOperationException {
        FileImportBusinessAdapter finance = adapter(Set.of("finance.income", "finance.spending", "finance.invoice"));
        FileImportAdapterRegistry registry = registry(List.of(finance));

        assertTrue(registry.supportedKeys().containsAll(finance.keys()));
        assertEquals("finance.income", registry.importItem(
                item("finance.income", Map.of("amount", 1)), attachment()).businessRecordId());

        InvocationTargetException duplicate = assertThrows(InvocationTargetException.class,
                () -> registryConstructor().newInstance(mock(MallServiceFeign.class), mock(CrmServiceFeign.class),
                        mock(MesServiceFeign.class), mock(PmServiceFeign.class), mock(PluginApiClient.class),
                        List.of(finance, adapter(Set.of("finance.income")))));
        assertTrue(duplicate.getCause() instanceof IllegalStateException);
    }

    @Test
    void exposesExactlyTheTenControlledAdapters() {
        FileImportAdapterRegistry registry = registry(mock(MallServiceFeign.class), mock(CrmServiceFeign.class),
                mock(MesServiceFeign.class), mock(PmServiceFeign.class), mock(PluginApiClient.class));

        assertEquals(Set.of("mall.goods", "mall.customer", "crm.customer", "crm.business",
                "mes.material", "mes.production", "pm.project", "pm.task",
                "erp.goods", "erp.purchase"), registry.supportedKeys());
        assertEquals(422, registry.importItem(item("unknown.object", Map.of()), attachment()).code());
    }

    @Test
    void mapsOnlyAllowedFieldsAndAddsProjectAttachment() {
        MallServiceFeign mall = mock(MallServiceFeign.class);
        CrmServiceFeign crm = mock(CrmServiceFeign.class);
        MesServiceFeign mes = mock(MesServiceFeign.class);
        PmServiceFeign pm = mock(PmServiceFeign.class);
        PluginApiClient erp = mock(PluginApiClient.class);
        when(pm.createProject(any())).thenReturn(ResponseResult.success(Map.of("id", 88)));
        FileImportAdapterRegistry registry = registry(mall, crm, mes, pm, erp);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("number", "P-001");
        fields.put("name", "厂房改造");
        fields.put("dropTable", "never");

        FileImportAdapterResult result = registry.importItem(item("pm.project", fields), attachment());

        assertEquals(1, result.code());
        assertEquals("88", result.businessRecordId());
        assertEquals("/pm/project/detail/88", result.detailUrl());
        assertEquals("https://files/source.pdf", result.attachmentUrl());
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(pm).createProject(payload.capture());
        assertFalse(payload.getValue().containsKey("dropTable"));
        assertTrue(payload.getValue().containsKey("attachments"));
    }

    @Test
    void routesEveryDomainObjectToItsExistingCreateApi() {
        MallServiceFeign mall = mock(MallServiceFeign.class);
        CrmServiceFeign crm = mock(CrmServiceFeign.class);
        MesServiceFeign mes = mock(MesServiceFeign.class);
        PmServiceFeign pm = mock(PmServiceFeign.class);
        PluginApiClient erp = mock(PluginApiClient.class);
        ResponseResult ok = ResponseResult.success(Map.of("id", 1));
        when(mall.addGoodsForImport(any())).thenReturn(ok);
        when(mall.saveCustomer(any())).thenReturn(ok);
        when(crm.addCustomer(any())).thenReturn(ok);
        when(crm.addBusiness(any())).thenReturn(ok);
        when(mes.createMaterial(any())).thenReturn(ok);
        when(mes.createProductionPlan(any())).thenReturn(ok);
        when(pm.createProject(any())).thenReturn(ok);
        when(pm.createTask(any())).thenReturn(ok);
        when(erp.post(eq("erp"), eq("/goods"), any())).thenReturn(ok);
        when(erp.post(eq("erp"), eq("/purchase"), any())).thenReturn(ok);
        FileImportAdapterRegistry registry = registry(mall, crm, mes, pm, erp);

        for (String key : registry.supportedKeys()) {
            assertEquals(1, registry.importItem(item(key, validFields(key)), attachment()).code(), key);
        }

        verify(mall).addGoodsForImport(any());
        verify(mall).saveCustomer(any());
        verify(crm).addCustomer(any());
        verify(crm).addBusiness(any());
        verify(mes).createMaterial(any());
        verify(mes).createProductionPlan(any());
        verify(pm).createProject(any());
        verify(pm).createTask(any());
        verify(erp).post(eq("erp"), eq("/goods"), any());
        verify(erp).post(eq("erp"), eq("/purchase"), any());
    }

    private FileImportAdapterRegistry registry(MallServiceFeign mall, CrmServiceFeign crm,
                                               MesServiceFeign mes, PmServiceFeign pm, PluginApiClient erp) {
        return new FileImportAdapterRegistry(mall, crm, mes, pm, erp);
    }

    private FileImportAdapterRegistry registry(List<FileImportBusinessAdapter> adapters) throws ReflectiveOperationException {
        return registryConstructor().newInstance(mock(MallServiceFeign.class), mock(CrmServiceFeign.class),
                mock(MesServiceFeign.class), mock(PmServiceFeign.class), mock(PluginApiClient.class), adapters);
    }

    @SuppressWarnings("unchecked")
    private Constructor<FileImportAdapterRegistry> registryConstructor() throws NoSuchMethodException {
        return FileImportAdapterRegistry.class.getConstructor(MallServiceFeign.class, CrmServiceFeign.class,
                MesServiceFeign.class, PmServiceFeign.class, PluginApiClient.class, List.class);
    }

    private FileImportBusinessAdapter adapter(Set<String> keys) {
        return new FileImportBusinessAdapter() {
            @Override
            public Set<String> keys() {
                return keys;
            }

            @Override
            public FileImportAdapterResult importItem(String key, Map<String, Object> fields,
                                                      Map<String, Object> attachment) {
                return new FileImportAdapterResult(1, "ok", key, "/" + key, null);
            }
        };
    }

    private Map<String, Object> item(String adapterKey, Map<String, Object> fields) {
        return Map.of("adapterKey", adapterKey, "fields", fields, "detailPath", detailPath(adapterKey));
    }

    private Map<String, Object> attachment() {
        return Map.of("fileName", "source.pdf", "fileUrl", "https://files/source.pdf", "fileSize", 123L);
    }

    private String detailPath(String key) {
        return switch (key) {
            case "mall.goods" -> "/mall/goods/detail/";
            case "mall.customer" -> "/mall/customer/detail/";
            case "crm.customer" -> "/crm/customer/detail/";
            case "crm.business" -> "/crm/business/detail/";
            case "mes.material" -> "/mes/material/detail/";
            case "mes.production" -> "/mes/production/detail/";
            case "pm.project" -> "/pm/project/detail/";
            case "pm.task" -> "/pm/task/detail/";
            case "erp.goods" -> "/erp/goods/detail/";
            case "erp.purchase" -> "/erp/purchase/detail/";
            default -> "";
        };
    }

    private Map<String, Object> validFields(String key) {
        return switch (key) {
            case "mall.goods" -> Map.of("goodsName", "商品");
            case "mall.customer", "crm.customer" -> Map.of("name", "客户");
            case "crm.business" -> Map.of("name", "商机");
            case "mes.material" -> Map.of("materialCode", "M1", "materialName", "物料");
            case "mes.production" -> Map.of("planCode", "PL1", "goodsName", "产品");
            case "pm.project" -> Map.of("number", "P1", "name", "项目");
            case "pm.task" -> Map.of("taskNo", "T1", "name", "任务");
            case "erp.goods" -> Map.of("goodsCode", "G1", "goodsName", "ERP商品");
            case "erp.purchase" -> Map.of("purchaseCode", "PO1", "purchaseTime", "2026-09-09");
            default -> Map.of();
        };
    }
}
