package com.houjiang.merchant.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Keeps permission tokens required by the merchant role editor compatible with legacy data.
 *
 * <p>Only blank legacy tokens are repaired. Existing non-blank administrator configuration
 * remains authoritative.</p>
 */
@Slf4j
@Component
public class MerchantPermissionCatalogInitializer {

    static final int ANNOUNCEMENT_MENU_ID = 221;
    static final String ANNOUNCEMENT_MENU_ACCESS = "mall.announcement/lists:detail";
    static final int CUSTOMER_SERVICE_MENU_ID = 230;
    static final String CUSTOMER_SERVICE_MENU_ACCESS = "config/get_service:set_service";

    private final JdbcTemplate jdbcTemplate;

    public MerchantPermissionCatalogInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initialize() {
        try {
            int updated = jdbcTemplate.update(
                    "UPDATE hou_merchant_mall_shop_menu "
                            + "SET access = ?, update_time = UNIX_TIMESTAMP() "
                            + "WHERE id = ? AND delete_time = 0 "
                            + "AND (access IS NULL OR TRIM(access) = '')",
                    ANNOUNCEMENT_MENU_ACCESS,
                    ANNOUNCEMENT_MENU_ID);
            if (updated > 0) {
                log.info("Repaired merchant announcement permission catalog entry");
            }
            updated = jdbcTemplate.update(
                    "UPDATE hou_merchant_mall_shop_menu "
                            + "SET access = ?, update_time = UNIX_TIMESTAMP() "
                            + "WHERE id = ? AND delete_time = 0 "
                            + "AND (access IS NULL OR TRIM(access) = '')",
                    CUSTOMER_SERVICE_MENU_ACCESS,
                    CUSTOMER_SERVICE_MENU_ID);
            if (updated > 0) {
                log.info("Repaired merchant customer service permission catalog entry");
            }
        } catch (Exception ex) {
            log.warn("Merchant permission catalog initialization failed; startup will continue: {}",
                    ex.getMessage());
        }
    }
}
