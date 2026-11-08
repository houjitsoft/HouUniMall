package com.houjiang.merchant.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MerchantPermissionCatalogInitializerTest {

    @Test
    void repairsBlankAnnouncementPermissionWithoutOverwritingConfiguredRows() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        MerchantPermissionCatalogInitializer initializer =
                new MerchantPermissionCatalogInitializer(jdbcTemplate);

        initializer.initialize();

        verify(jdbcTemplate).update(
                contains("access IS NULL OR TRIM(access) = ''"),
                eq(MerchantPermissionCatalogInitializer.ANNOUNCEMENT_MENU_ACCESS),
                eq(MerchantPermissionCatalogInitializer.ANNOUNCEMENT_MENU_ID));
    }

    @Test
    void repairsBlankCustomerServicePermissionWithoutOverwritingConfiguredRows() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        MerchantPermissionCatalogInitializer initializer =
                new MerchantPermissionCatalogInitializer(jdbcTemplate);

        initializer.initialize();

        verify(jdbcTemplate).update(
                contains("access IS NULL OR TRIM(access) = ''"),
                eq(MerchantPermissionCatalogInitializer.CUSTOMER_SERVICE_MENU_ACCESS),
                eq(MerchantPermissionCatalogInitializer.CUSTOMER_SERVICE_MENU_ID));
    }

    @Test
    void doesNotAbortStartupWhenLegacyPermissionTableCannotBeUpdated() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        doThrow(new IllegalStateException("update unavailable"))
                .when(jdbcTemplate).update(
                        contains("hou_merchant_mall_shop_menu"),
                        eq(MerchantPermissionCatalogInitializer.ANNOUNCEMENT_MENU_ACCESS),
                        eq(MerchantPermissionCatalogInitializer.ANNOUNCEMENT_MENU_ID));
        MerchantPermissionCatalogInitializer initializer =
                new MerchantPermissionCatalogInitializer(jdbcTemplate);

        assertDoesNotThrow(initializer::initialize);
    }
}
