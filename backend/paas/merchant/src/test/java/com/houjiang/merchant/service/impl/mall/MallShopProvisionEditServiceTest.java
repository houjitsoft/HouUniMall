package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopCategoryMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.validate.mall.MallShopProvisionUpdateValidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.exception.OperateException;
import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class MallShopProvisionEditServiceTest {

    @Mock private MallShopMapper shopMapper;
    @Mock private MallShopAdminMapper adminMapper;
    @Mock private MallShopCategoryMapper categoryMapper;
    @Mock private JdbcTemplate jdbcTemplate;

    private MallShopProvisionService service;

    @BeforeEach
    void setUp() {
        service = new MallShopProvisionService(shopMapper, adminMapper, categoryMapper, jdbcTemplate);
    }

    @Test
    void renameLocksShopAndPersistsImmediatelyPreviousName() {
        MallShop current = currentShop();
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(current);
        service.update(request("新店名"));
        ArgumentCaptor<QueryWrapper<MallShop>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(shopMapper).selectOne(query.capture());
        assertTrue(query.getValue().getSqlSelect().contains("name"));
        assertTrue(query.getValue().getSqlSegment().endsWith("FOR UPDATE"));
        var order = inOrder(shopMapper, jdbcTemplate);
        order.verify(shopMapper).selectOne(any(Wrapper.class));
        order.verify(jdbcTemplate).queryForList(contains("FOR UPDATE"), eq(42), eq("store_profile"), eq("previous_name"));
        order.verify(jdbcTemplate).update(startsWith("INSERT INTO hou_merchant_mall_shop_config"),
                eq(42), eq("store_profile"), eq("previous_name"), eq("旧店名"), anyLong(), anyLong());
    }

    @Test
    void oldClientWithoutProfileFieldsDoesNotOverwriteMetadataOrPreviousName() {
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(currentShop());
        service.update(request("旧店名"));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void updatesExistingProfileWithoutInsertingDuplicateConfig() {
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(currentShop());
        when(jdbcTemplate.queryForList("SELECT id FROM hou_shop_level WHERE id = ?", Integer.class, 7))
                .thenReturn(List.of(7));
        when(jdbcTemplate.queryForList(contains("FOR UPDATE"), eq(42), eq("store_profile"), eq("entry_type")))
                .thenReturn(List.of(Map.of("id", 10)));
        when(jdbcTemplate.queryForList(contains("FOR UPDATE"), eq(42), eq("store_profile"), eq("shop_level_id")))
                .thenReturn(List.of(Map.of("id", 11)));
        var request = request("旧店名"); request.setEntryType(2); request.setShopLevelId(7);
        service.update(request);
        verify(jdbcTemplate).update(startsWith("UPDATE hou_merchant_mall_shop_config"), eq("2"), anyLong(), eq(10));
        verify(jdbcTemplate).update(startsWith("UPDATE hou_merchant_mall_shop_config"), eq("7"), anyLong(), eq(11));
        verify(jdbcTemplate, never()).update(startsWith("INSERT INTO hou_merchant_mall_shop_config"), any(Object[].class));
    }

    @Test
    void rejectsDuplicateMetadataInsteadOfCreatingAnotherRow() {
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(currentShop());
        when(jdbcTemplate.queryForList(contains("FOR UPDATE"), eq(42), eq("store_profile"), eq("previous_name")))
                .thenReturn(List.of(Map.of("id", 10), Map.of("id", 11)));
        assertThrows(OperateException.class, () -> service.update(request("新店名")));
        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void validatesEntryTypeAndRealLevelBeforeUpdatingShop() {
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(currentShop());
        var request = request("旧店名"); request.setEntryType(0);
        assertThrows(OperateException.class, () -> service.update(request));
        request.setEntryType(3);
        assertThrows(OperateException.class, () -> service.update(request));
        request.setEntryType(null); request.setShopLevelId(0);
        assertThrows(OperateException.class, () -> service.update(request));
        request.setShopLevelId(999);
        assertThrows(OperateException.class, () -> service.update(request));
        verify(jdbcTemplate).queryForList("SELECT id FROM hou_shop_level WHERE id = ?", Integer.class, 999);
        verify(shopMapper, never()).update(isNull(), any(Wrapper.class));
    }

    private MallShop currentShop() {
        MallShop current = new MallShop();
        current.setId(42); current.setName("旧店名"); current.setShopStatus(1);
        return current;
    }

    private MallShopProvisionUpdateValidate request(String name) {
        MallShopProvisionUpdateValidate request = new MallShopProvisionUpdateValidate();
        request.setId(42); request.setName(name); request.setType(1);
        request.setContact("联系人"); request.setMobile("13800138000");
        request.setShopStatus(1); request.setBusinessStatus(1);
        return request;
    }

    @Test
    void loadsTheSameMerchantShopUsedByTheList() {
        MallShop shop = new MallShop();
        shop.setId(42);
        shop.setName("厚匠测试店铺");
        shop.setMobile("[\"13800138000\"]");
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(shop);

        var detail = service.detail(42);

        assertEquals(42, detail.getId());
        assertEquals("厚匠测试店铺", detail.getName());
        assertEquals("13800138000", detail.getMobile());
    }

    @Test
    void updatesMerchantShopAndSynchronizesTheSuperAdminStatus() {
        MallShop current = new MallShop();
        current.setId(42);
        current.setShopStatus(1);
        MallShopAdmin admin = new MallShopAdmin();
        admin.setId(9);
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(current);
        when(shopMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(adminMapper.selectOne(any(Wrapper.class))).thenReturn(admin);

        MallShopProvisionUpdateValidate request = new MallShopProvisionUpdateValidate();
        request.setId(42);
        request.setName("厚匠测试店铺");
        request.setType(1);
        request.setContact("张三");
        request.setMobile("13800138000");
        request.setAddressInfo("上海市");
        request.setLat("31.2");
        request.setLng("121.4");
        request.setShopStatus(0);
        request.setBusinessStatus(1);

        service.update(request);

        verify(shopMapper).update(isNull(), any(Wrapper.class));
        verify(adminMapper).updateById(any(MallShopAdmin.class));
    }
}
