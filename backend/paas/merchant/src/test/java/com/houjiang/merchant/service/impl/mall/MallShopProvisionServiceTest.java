package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopCategoryMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.validate.mall.MallShopProvisionCreateValidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class MallShopProvisionServiceTest {

    @Mock
    private MallShopMapper shopMapper;
    @Mock
    private MallShopAdminMapper adminMapper;
    @Mock
    private MallShopCategoryMapper categoryMapper;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private MallShopProvisionService service;

    @BeforeEach
    void setUp() {
        service = new MallShopProvisionService(shopMapper, adminMapper, categoryMapper, jdbcTemplate);
    }

    @Test
    void createsShopAndSuperAdminTogether() {
        MallShopProvisionCreateValidate request = validRequest();
        request.setAccountMode("existing");
        request.setPlatformUserId("123");
        when(shopMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(jdbcTemplate.queryForList(any(String.class), any(Object[].class))).thenReturn(
                List.of(Map.of("userId", 123L, "userName", "13800138000", "userMobile", "13800138000",
                        "userNickname", "会员", "password", "encoded-password")), Collections.emptyList());
        when(jdbcTemplate.queryForObject(eq("SELECT LAST_INSERT_ID()"), eq(Long.class))).thenReturn(7L);
        doAnswer(invocation -> {
            MallShop shop = invocation.getArgument(0);
            shop.setId(42);
            return 1;
        }).when(shopMapper).insert(any(MallShop.class));

        var result = service.create(request);

        assertEquals(42, result.getShopId());
        assertEquals("13800138000", result.getLoginAccount());
        verify(shopMapper).insert(any(MallShop.class));
    }

    @Test
    void rejectsDuplicateLoginBeforeCreatingShop() {
        when(jdbcTemplate.queryForObject(any(String.class), any(Class.class), any(Object[].class)))
                .thenReturn(1L);

        assertThrows(OperateException.class, () -> service.create(validRequest()));

        verify(shopMapper, never()).insert(any(MallShop.class));
        verify(adminMapper, never()).insert(any(MallShopAdmin.class));
    }

    @Test
    void rejectsMismatchedPasswordsBeforeWriting() {
        MallShopProvisionCreateValidate request = validRequest();
        request.setAccountPasswordAgain("different");

        assertThrows(OperateException.class, () -> service.create(request));

        verify(shopMapper, never()).insert(any(MallShop.class));
        verify(adminMapper, never()).insert(any(MallShopAdmin.class));
    }

    @Test
    void updatesShopAndSuperAdminStatusTogether() {
        MallShop shop = new MallShop();
        shop.setId(42);
        MallShopAdmin admin = new MallShopAdmin();
        admin.setId(9);
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(shop);
        when(adminMapper.selectOne(any(Wrapper.class))).thenReturn(admin);

        service.updateStatus(42, 0);

        verify(shopMapper).updateById(any(MallShop.class));
        verify(adminMapper).updateById(any(MallShopAdmin.class));
    }

    private MallShopProvisionCreateValidate validRequest() {
        MallShopProvisionCreateValidate request = new MallShopProvisionCreateValidate();
        request.setName("厚匠测试商家");
        request.setType(1);
        request.setContact("张三");
        request.setMobile("13800138000");
        request.setShopStatus(1);
        request.setBusinessStatus(1);
        request.setAccountMode("new");
        request.setAccountIdentifier("13800138000");
        request.setAccountPassword("12345678");
        request.setAccountPasswordAgain("12345678");
        return request;
    }
}
