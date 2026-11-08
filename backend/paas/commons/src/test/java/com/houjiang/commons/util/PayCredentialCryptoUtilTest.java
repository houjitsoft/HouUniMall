package com.houjiang.commons.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PayCredentialCryptoUtilTest {

    @Test
    void rejectsMissingEncryptionConfigurationInsteadOfUsingFallbackKey() {
        assertThrows(IllegalArgumentException.class,
                () -> PayCredentialCryptoUtil.setKeyIv("", ""));
    }
}
