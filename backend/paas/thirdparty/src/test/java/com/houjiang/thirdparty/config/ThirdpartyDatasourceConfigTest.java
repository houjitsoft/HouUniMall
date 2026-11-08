package com.houjiang.thirdparty.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThirdpartyDatasourceConfigTest {

    @Test
    void staleDatabaseConnectionsAreDetectedBeforeApiRequestsTimeOut() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("bootstrap.yml"));
        Properties properties = yaml.getObject();

        assertNotNull(properties);
        long validationTimeout = requiredLong(properties,
                "spring.datasource.hikari.validation-timeout");
        long maxLifetime = requiredLong(properties,
                "spring.datasource.hikari.max-lifetime");
        long keepaliveTime = requiredLong(properties,
                "spring.datasource.hikari.keepalive-time");

        assertTrue(validationTimeout <= 2_000,
                "closed connections must not block each validation for Hikari's default five seconds");
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
