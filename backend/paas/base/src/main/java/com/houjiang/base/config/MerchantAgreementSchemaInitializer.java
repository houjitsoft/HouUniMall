package com.houjiang.base.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensures that the merchant onboarding agreements required by tcenter exist.
 *
 * <p>The initialization is intentionally idempotent so a partially imported
 * database can recover without overwriting agreements maintained by admins.</p>
 */
@Slf4j
@Component
public class MerchantAgreementSchemaInitializer {

    static final String DEFAULT_AGREEMENT_URL = "/resource/files/supplierAgreement.pdf";

    private static final List<AgreementSeed> AGREEMENTS = List.of(
            new AgreementSeed("个人", "US_AGREEMENT"),
            new AgreementSeed("企业", "ME_AGREEMENT"),
            new AgreementSeed("入驻合同", "GEN_COR_AGREEMENT"),
            new AgreementSeed("通用", "BR_AGREEMENT"),
            new AgreementSeed("供应商商家", "GY_ME_AGREEMENT"),
            new AgreementSeed("供应商用户", "GY_US_AGREEMENT")
    );

    private final JdbcTemplate jdbcTemplate;

    public MerchantAgreementSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initialize() {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hou_base_merchant_agreement ("
                    + "id INT NOT NULL AUTO_INCREMENT COMMENT '协议id', "
                    + "name VARCHAR(50) NOT NULL COMMENT '商家协议/用户协议', "
                    + "url VARCHAR(250) NOT NULL COMMENT 'url', "
                    + "sign VARCHAR(50) NOT NULL COMMENT '标记', "
                    + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间', "
                    + "PRIMARY KEY (id), UNIQUE KEY uk_merchant_agreement_sign (sign)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户/商家协议表'");
        } catch (Exception ex) {
            log.warn("Merchant agreement table initialization failed; startup will continue: {}", ex.getMessage());
            return;
        }

        for (AgreementSeed agreement : AGREEMENTS) {
            try {
                jdbcTemplate.update(
                        "INSERT INTO hou_base_merchant_agreement (name, url, sign, update_time) "
                                + "SELECT ?, ?, ?, CURRENT_TIMESTAMP FROM DUAL "
                                + "WHERE NOT EXISTS (SELECT 1 FROM hou_base_merchant_agreement WHERE sign = ?)",
                        agreement.name(), DEFAULT_AGREEMENT_URL, agreement.sign(), agreement.sign());
            } catch (Exception ex) {
                log.warn("Merchant agreement seed failed for {}; remaining agreements will continue: {}",
                        agreement.sign(), ex.getMessage());
            }
        }
    }

    private record AgreementSeed(String name, String sign) {
    }
}
