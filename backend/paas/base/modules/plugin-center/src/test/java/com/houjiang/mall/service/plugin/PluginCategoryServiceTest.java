package com.houjiang.mall.service.plugin;

import com.houjiang.mall.mapper.plugin.AddonCateMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PluginCategoryServiceTest {
    private final AddonCateMapper mapper = mock(AddonCateMapper.class);
    private final PluginCenterService service = new PluginCenterService(null, null, null, null, null, null, null, mapper);

    @Test void dictionaryKeepsDatabaseOrderAndCustomerCategories() {
        var rows = List.<Map<String,Object>>of(Map.of("id", 42, "name", "客户分类", "sort", 1),
                Map.of("id", 3, "name", "商务", "sort", 2));
        when(mapper.selectAll()).thenReturn(rows);
        assertEquals(rows, service.listCategories());
    }

    @Test void missingCategoryIsRejectedWithoutCatalogMutation() {
        when(mapper.selectAll()).thenReturn(List.of(Map.of("id", 2, "name", "商务", "sort", 1)));
        assertThrows(IllegalArgumentException.class, () -> service.updateCategory("erp", 42));
        verify(mapper, never()).updateCatalogCategory(anyString(), anyInt(), anyString());
    }

    @Test void canonicalWriteUpdatesCatalogAndMarketDerivedName() {
        when(mapper.selectAll()).thenReturn(List.of(Map.of("id", 42, "name", "客户分类", "sort", 1)));
        when(mapper.updateCatalogCategory("erp", 42, "客户分类")).thenReturn(1);
        service.updateCategory("erp", 42);
        verify(mapper).updateCatalogCategory("erp", 42, "客户分类");
        verify(mapper).updateMarketCategory("erp", 42, "客户分类");
    }

    @Test void missingCatalogPluginIsRejectedBeforeMarketMutation() {
        assertThrows(IllegalArgumentException.class, () -> service.updateCategory("unknown", 0));
        verify(mapper, never()).updateMarketCategory(anyString(), anyInt(), anyString());
    }
}
