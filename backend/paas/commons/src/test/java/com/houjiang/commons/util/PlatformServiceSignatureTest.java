package com.houjiang.commons.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformServiceSignatureTest {

    @Test
    void signsAndVerifiesCallerIdentityWithoutExposingTheSecret() {
        byte[] body = "{\"id\":42}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String signature = PlatformServiceSignature.sign(
                "test-service-secret", 1_800_000_000L, "nonce-1", "Bearer cloud-token",
                "POST", "/merchant/platform-finance/refund/retry", body);

        assertTrue(PlatformServiceSignature.verify(
                "test-service-secret", 1_800_000_030L, "1800000000", "nonce-1",
                "Bearer cloud-token", "POST", "/merchant/platform-finance/refund/retry", body, signature));
        assertFalse(PlatformServiceSignature.verify(
                "test-service-secret", 1_800_000_061L, "1800000000", "nonce-1",
                "Bearer cloud-token", "POST", "/merchant/platform-finance/refund/retry", body, signature));
        assertFalse(PlatformServiceSignature.verify(
                "test-service-secret", 1_800_000_030L, "1800000000", "nonce-1",
                "Bearer other-token", "POST", "/merchant/platform-finance/refund/retry", body, signature));
        assertFalse(PlatformServiceSignature.verify(
                "test-service-secret", 1_800_000_030L, "1800000000", "nonce-1",
                "Bearer cloud-token", "POST", "/merchant/platform-finance/refund/list", body, signature));
        assertFalse(PlatformServiceSignature.verify(
                "test-service-secret", 1_800_000_030L, "1800000000", "nonce-1",
                "Bearer cloud-token", "POST", "/merchant/platform-finance/refund/retry",
                "{\"id\":43}".getBytes(java.nio.charset.StandardCharsets.UTF_8), signature));
    }
}
