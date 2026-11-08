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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
