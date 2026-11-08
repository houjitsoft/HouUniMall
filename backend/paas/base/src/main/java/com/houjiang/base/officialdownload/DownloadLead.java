package com.houjiang.base.officialdownload;

import java.time.Instant;

public record DownloadLead(Long id, String phoneEncrypted, String phoneHash, String phoneMasked,
                           String identityType, String companyName, String useCase, String source,
                           String privacyVersion, boolean marketingConsent, Instant consentAt,
                           Instant firstSeenAt, Instant lastDownloadAt) {
    public DownloadLead withId(Long value) {
        return new DownloadLead(value, phoneEncrypted, phoneHash, phoneMasked, identityType, companyName,
                useCase, source, privacyVersion, marketingConsent, consentAt, firstSeenAt, lastDownloadAt);
    }
}
