package com.houjiang.base.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MerchantAgreementSchemaInitializerTest {

    @Test
    void createsTableAndSeedsAllRequiredAgreementTypes() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        MerchantAgreementSchemaInitializer initializer = new MerchantAgreementSchemaInitializer(jdbcTemplate);

        initializer.initialize();

        verify(jdbcTemplate).execute(contains("CREATE TABLE IF NOT EXISTS hou_base_merchant_agreement"));
        verify(jdbcTemplate, times(6)).update(
                contains("WHERE NOT EXISTS"),
                org.mockito.ArgumentMatchers.any(),
                eq(MerchantAgreementSchemaInitializer.DEFAULT_AGREEMENT_URL),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(jdbcTemplate).update(contains("WHERE NOT EXISTS"),
                eq("供应商商家"),
                eq(MerchantAgreementSchemaInitializer.DEFAULT_AGREEMENT_URL),
                eq("GY_ME_AGREEMENT"),
                eq("GY_ME_AGREEMENT"));
    }

    @Test
    void doesNotAbortApplicationStartupWhenDatabaseDdlIsUnavailable() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        doThrow(new IllegalStateException("no ddl permission"))
                .when(jdbcTemplate).execute(contains("hou_base_merchant_agreement"));
        MerchantAgreementSchemaInitializer initializer = new MerchantAgreementSchemaInitializer(jdbcTemplate);

        assertDoesNotThrow(initializer::initialize);
    }

    @Test
    void continuesSeedingWhenOneAgreementInsertFails() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        doThrow(new IllegalStateException("one seed failed"))
                .when(jdbcTemplate).update(
                        contains("WHERE NOT EXISTS"),
                        eq("个人"), any(), eq("US_AGREEMENT"), eq("US_AGREEMENT"));
        MerchantAgreementSchemaInitializer initializer = new MerchantAgreementSchemaInitializer(jdbcTemplate);

        assertDoesNotThrow(initializer::initialize);

        verify(jdbcTemplate).update(contains("WHERE NOT EXISTS"),
                eq("供应商用户"),
                eq(MerchantAgreementSchemaInitializer.DEFAULT_AGREEMENT_URL),
                eq("GY_US_AGREEMENT"),
                eq("GY_US_AGREEMENT"));
    }
}
