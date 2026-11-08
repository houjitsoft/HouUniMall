package com.houjiang.merchant.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchantDatasourceConfigTest {

    @Test
    void staleDatabaseConnectionsAreDetectedBeforeLoginTimesOut() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("bootstrap.yml"));
        Properties properties = yaml.getObject();

        assertNotNull(properties);
        long connectionTimeout = requiredLong(properties,
                "spring.datasource.hikari.connection-timeout");
        long validationTimeout = requiredLong(properties,
                "spring.datasource.hikari.validation-timeout");
        long maxLifetime = requiredLong(properties,
                "spring.datasource.hikari.max-lifetime");
        long keepaliveTime = requiredLong(properties,
                "spring.datasource.hikari.keepalive-time");

        assertTrue(connectionTimeout <= 10_000,
                "a login request must not wait for Hikari's default 30 second connection timeout");
        assertTrue(validationTimeout <= 2_000,
                "each closed connection validation must fail faster than Hikari's default five seconds");
        assertTrue(maxLifetime <= 600_000,
                "connections must be retired before the production network closes idle sessions");
        assertTrue(keepaliveTime > 0 && keepaliveTime <= 30_000,
                "idle connections must be kept alive at least every 30 seconds");
    }

    private long requiredLong(Properties properties, String key) {
        String value = properties.getProperty(key);
        assertNotNull(value, key + " must be configured explicitly");
        return Long.parseLong(value);
    }
}
