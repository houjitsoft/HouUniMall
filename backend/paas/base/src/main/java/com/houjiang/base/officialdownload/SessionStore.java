package com.houjiang.base.officialdownload;

import java.time.Instant;

public interface SessionStore {
    void save(VerifiedSession session);
    VerifiedSession findActive(String tokenHash, Instant now);
}
