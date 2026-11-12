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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
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
    void levelOptionsRejectsMissingAuthenticationBeforeQuerying() {
        assertThrows(OperateException.class, () -> service.levelOptions(null));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void levelOptionsChecksPlatformRoleAndReturnsOnlyOptionFields() {
        try (var tokens = mockStatic(com.houjiang.commons.util.RedisUtil.class)) {
            tokens.when(() -> com.houjiang.commons.util.RedisUtil.getNoExpire(
                    com.houjiang.commons.config.HouConfig.HOU_USER_TOKEN_USER + "existing-session"))
                    .thenReturn("{\"user\":{\"userId\":\"123\"}}");
            when(jdbcTemplate.queryForObject(startsWith("SELECT COUNT(*) FROM hou_user_info"), eq(Long.class), eq("123")))
                    .thenReturn(0L, 1L);
            assertThrows(OperateException.class, () -> service.levelOptions("Bearer existing-session"));
            when(jdbcTemplate.queryForList("SELECT id,store_level AS storeLevel FROM hou_shop_level ORDER BY id"))
                    .thenReturn(List.of(Map.of("id", 1, "storeLevel", "普通店铺")));
            assertEquals(List.of(Map.of("id", 1, "storeLevel", "普通店铺")), service.levelOptions("Bearer existing-session"));
        }
    }

    @Test
    void createsShopAndSuperAdminTogether() {
        MallShopProvisionCreateValidate request = validRequest();
        request.setAccountMode("existing");
        request.setPlatformUserId("123");
        request.setEntryType(2);
        request.setShopLevelId(7);
        when(jdbcTemplate.queryForList("SELECT id FROM hou_shop_level WHERE id = ?", Integer.class, 7))
                .thenReturn(List.of(7));
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
        verify(jdbcTemplate).update(startsWith("INSERT INTO hou_merchant_mall_shop_config"),
                eq(42), eq("store_profile"), eq("entry_type"), eq("2"), anyLong(), anyLong());
        verify(jdbcTemplate).update(startsWith("INSERT INTO hou_merchant_mall_shop_config"),
                eq(42), eq("store_profile"), eq("shop_level_id"), eq("7"), anyLong(), anyLong());
        verify(jdbcTemplate).update(startsWith("INSERT INTO hou_merchant_mall_shop_config"),
                eq(42), eq("store_profile"), eq("audit_status"), eq("1"), anyLong(), anyLong());
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

    @Test
    void ownerAccountUsesOnlyDisplayedFirstActiveSuperAdminAndPreferredLogin() {
        when(jdbcTemplate.queryForList(anyString(), eq(Integer.class), any(Object[].class)))
                .thenReturn(List.of(42));
        when(shopMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>(1, 15));

        service.list(Map.of("ownerAccount", "x' OR 1=1 --"));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForList(sql.capture(), eq(Integer.class), eq("%x' OR 1=1 --%"));
        assertTrue(sql.getValue().contains("a.delete_time = 0 AND a.super = 1"));
        assertTrue(sql.getValue().contains("SELECT MIN(a2.id)"));
        assertTrue(sql.getValue().contains("a2.delete_time = 0 AND a2.super = 1"));
        assertTrue(sql.getValue().contains("COALESCE(NULLIF(TRIM(u.user_mobile), ''), TRIM(u.user_name)) LIKE ?"));
        assertFalse(sql.getValue().contains("OR 1=1"));
        ArgumentCaptor<QueryWrapper<MallShop>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(shopMapper).selectPage(any(Page.class), query.capture());
        assertTrue(query.getValue().getSqlSegment().contains("id IN"));
        assertTrue(query.getValue().getParamNameValuePairs().containsValue(42));
    }

    @Test
    void unmatchedOwnerReturnsEmptyPageWithoutPostPaginationFiltering() {
        var result = service.list(Map.of("ownerAccount", "不存在", "page", 3, "size", 20));
        assertEquals(0L, result.getCount());
        assertEquals(3, result.getPage_no());
        assertEquals(20, result.getPage_size());
        assertTrue(result.getLists().isEmpty());
        verifyNoInteractions(shopMapper);
    }

    @Test
    void entryTypeAndLevelFiltersUseProfileRatherThanLegacyShopType() {
        when(jdbcTemplate.queryForList(anyString(), eq(Integer.class), any(Object[].class)))
                .thenReturn(List.of(42, 43), List.of(42));
        when(shopMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>(1, 15));
        service.list(Map.of("entryType", 2, "shopLevelId", 7, "shopType", 1,
                "shopName", "测试", "status", 0));
        verify(jdbcTemplate).queryForList(contains("c.delete_time = 0 AND c.value = ?"),
                eq(Integer.class), eq("store_profile"), eq("entry_type"), eq("2"));
        verify(jdbcTemplate).queryForList(contains("SELECT MAX(c2.id)"),
                eq(Integer.class), eq("store_profile"), eq("shop_level_id"), eq("7"));
        verify(jdbcTemplate).queryForList(contains("CAST(l.id AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_general_ci"),
                eq(Integer.class), eq("store_profile"), eq("shop_level_id"), eq("7"));
        ArgumentCaptor<QueryWrapper<MallShop>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(shopMapper).selectPage(any(Page.class), query.capture());
        String sql = query.getValue().getSqlSegment();
        assertTrue(sql.contains("type ="));
        assertTrue(sql.contains("name LIKE"));
        assertTrue(sql.contains("shop_status ="));
        assertFalse(sql.contains("entry_type"));
        assertEquals(2, sql.split("id IN", -1).length - 1);
        assertTrue(query.getValue().getParamNameValuePairs().containsValue(0));
    }

    @Test
    void missingProfileRemainsUnknownAndCreateTimeUsesShopSeconds() {
        MallShop shop = new MallShop();
        shop.setId(42); shop.setType(0); shop.setCreateTime(1730000000L);
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(shop);
        var vo = service.detail(42);
        assertEquals(1730000000L, vo.getCreateTime());
        assertEquals(0, vo.getType());
        assertEquals("官方自营", vo.getTypeText());
        assertNull(vo.getEntryType());
        assertNull(vo.getShopLevelId());
        assertNull(vo.getLevelName());
        assertNull(vo.getPreviousName());
        assertNull(vo.getAuditStatus());
        assertEquals("-", vo.getAccount().get("loginAccount"));
    }

    @Test
    void profileReadsRealLevelNameAndNeverInventsMissingLevel() {
        MallShop shop = new MallShop(); shop.setId(42); shop.setType(1);
        when(shopMapper.selectOne(any(Wrapper.class))).thenReturn(shop);
        when(jdbcTemplate.queryForList(contains("SELECT name, value"), eq(42), eq("store_profile")))
                .thenReturn(List.of(Map.of("name", "entry_type", "value", "1"),
                        Map.of("name", "shop_level_id", "value", "7"),
                        Map.of("name", "previous_name", "value", "旧店名"),
                        Map.of("name", "audit_status", "value", "1")));
        when(jdbcTemplate.queryForList(contains("SELECT id, store_level"), eq(7)))
                .thenReturn(List.of(Map.of("id", 7, "store_level", "真实等级")), List.of());
        var vo = service.detail(42);
        assertEquals(1, vo.getEntryType()); assertEquals(1, vo.getType());
        assertEquals(7, vo.getShopLevelId()); assertEquals("真实等级", vo.getLevelName());
        assertEquals("旧店名", vo.getPreviousName()); assertEquals(1, vo.getAuditStatus());
        var orphan = service.detail(42);
        assertNull(orphan.getShopLevelId()); assertNull(orphan.getLevelName());
    }

    @Test
    void missingMetadataCannotMatchEntryTypeFilter() {
        var result = service.list(Map.of("entryType", 1));
        assertEquals(0L, result.getCount());
        verifyNoInteractions(shopMapper);
    }

    @Test
    void rejectsNonexistentLevelBeforeCreateWrites() {
        var request = validRequest(); request.setAccountMode("existing"); request.setPlatformUserId("123");
        request.setShopLevelId(999);
        assertThrows(OperateException.class, () -> service.create(request));
        verify(jdbcTemplate).queryForList("SELECT id FROM hou_shop_level WHERE id = ?", Integer.class, 999);
        verify(shopMapper, never()).insert(any(MallShop.class));
    }

    @Test
    void ownerSqlActuallyMatchesPreferredAccountAndIgnoresOtherAdmins() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:provision_owner;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE hou_merchant_mall_shop_admin (id INT, shop_id INT, user_id BIGINT, delete_time BIGINT, super INT)");
        jdbc.execute("CREATE TABLE hou_user_info (user_id BIGINT, user_mobile VARCHAR(50), user_name VARCHAR(100))");
        jdbc.update("INSERT INTO hou_user_info VALUES (1,'13800138000','hidden-name'),(2,'','owner@example.com'),(3,NULL,'other@example.com')");
        jdbc.update("INSERT INTO hou_merchant_mall_shop_admin VALUES (1,42,3,9,1),(2,42,2,0,0),(3,42,1,0,1),(4,42,3,0,1),(5,43,2,0,1)");
        var realSqlService = new MallShopProvisionService(shopMapper, adminMapper, categoryMapper, jdbc);
        assertEquals(0L, realSqlService.list(Map.of("ownerAccount", "hidden-name")).getCount());
        assertEquals(0L, realSqlService.list(Map.of("ownerAccount", "other@example.com")).getCount());
        when(shopMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>(1, 15));
        realSqlService.list(Map.of("ownerAccount", "owner@example.com"));
        ArgumentCaptor<QueryWrapper<MallShop>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(shopMapper).selectPage(any(Page.class), query.capture());
        query.getValue().getSqlSegment();
        assertTrue(query.getValue().getParamNameValuePairs().containsValue(43));
        assertFalse(query.getValue().getParamNameValuePairs().containsValue(42));
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
