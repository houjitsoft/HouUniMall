package com.houjiang.base.officialdownload;

import java.time.Instant;

public record VerifiedSession(Long leadId, String token, String tokenHash, String phoneHash,
                              Instant expiresAt, String ipHash) {
}
