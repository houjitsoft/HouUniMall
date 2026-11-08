package com.houjiang.commons.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Loads the RSA private key from deployment configuration. Business services
 * must not read base-owned configuration tables directly.
 */
@Slf4j
@Component
public class RSAKeyConfig {
    private static volatile String cachedPrivateKey;
    private final String configuredPrivateKey;

    public RSAKeyConfig(@Value("${unimall.security.rsa-private-key:${UNIMALL_RSA_PRIVATE_KEY:}}") String configuredPrivateKey) {
        this.configuredPrivateKey = configuredPrivateKey;
    }

    @PostConstruct
    public void loadPrivateKey() {
        cachedPrivateKey = normalize(configuredPrivateKey);
        if (cachedPrivateKey == null) {
            log.warn("RSA private key is not configured; RSA password decryption is disabled");
        } else {
            log.info("RSA private key loaded from deployment configuration");
        }
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().replace("\\n", "\n");
    }

    public static String getCachedPrivateKey() {
        return cachedPrivateKey;
    }

    public static void updatePrivateKey(String privateKey) {
        cachedPrivateKey = normalize(privateKey);
        log.info("RSA private key cache updated from trusted configuration");
    }

    static void updatePrivateKeyForTest(String privateKey) {
        cachedPrivateKey = normalize(privateKey);
    }
}
