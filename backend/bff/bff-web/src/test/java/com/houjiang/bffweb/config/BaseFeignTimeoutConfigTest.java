package com.houjiang.bffweb.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseFeignTimeoutConfigTest {

    @Test
    void baseReadTimeoutCoversSynchronousAiFileRecognition() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("bootstrap.yml"));
        Properties properties = yaml.getObject();

        assertNotNull(properties);
        String configuredTimeout = properties.getProperty(
                "spring.cloud.openfeign.client.config.base.readTimeout");
        assertNotNull(configuredTimeout, "base read timeout must be configured explicitly");
        assertTrue(Integer.parseInt(configuredTimeout) >= 210_000,
                "base read timeout must exceed the AI client maximum wait time");
    }
}
