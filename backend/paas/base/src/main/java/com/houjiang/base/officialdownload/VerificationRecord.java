package com.houjiang.base.officialdownload;

import java.time.Instant;

public record VerificationRecord(Long id, String phoneHash, String phoneMasked, String codeHash,
                                 String ipHash, Instant sentAt, Instant expiresAt, int attempts,
                                 boolean verified, boolean locked) {
    VerificationRecord failedAttempt(int maxAttempts) {
        int next = attempts + 1;
        return new VerificationRecord(id, phoneHash, phoneMasked, codeHash, ipHash, sentAt, expiresAt,
                next, false, next >= maxAttempts);
    }

    VerificationRecord markVerified() {
        return new VerificationRecord(id, phoneHash, phoneMasked, codeHash, ipHash, sentAt, expiresAt,
                attempts, true, locked);
    }
}
