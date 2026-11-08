package com.houjiang.commons.config;

import com.houjiang.commons.util.JSEncrypt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RSAKeyConfigTest {
    @AfterEach void clearKey() { RSAKeyConfig.updatePrivateKeyForTest(null); }

    @Test void configuredKeyIsLoadedWithoutDatabaseAccess() {
        RSAKeyConfig config = new RSAKeyConfig(" configured-key ");
        config.loadPrivateKey();
        assertEquals("configured-key", RSAKeyConfig.getCachedPrivateKey());
    }

    @Test void decryptFailsClosedWhenNoDeploymentKeyIsConfigured() {
        RSAKeyConfig config = new RSAKeyConfig(" ");
        config.loadPrivateKey();
        assertThrows(IllegalStateException.class, () -> JSEncrypt.decrypt("not-used"));
    }
}
