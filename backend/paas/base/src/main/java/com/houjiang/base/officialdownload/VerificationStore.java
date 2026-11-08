package com.houjiang.base.officialdownload;

import java.time.Instant;

public interface VerificationStore {
    VerificationRecord findLatest(String phoneHash);
    long countRecentByIp(String ipHash, Instant since);
    void save(VerificationRecord record);
    void update(VerificationRecord record);
}
