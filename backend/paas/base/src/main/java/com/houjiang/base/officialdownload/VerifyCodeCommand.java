package com.houjiang.base.officialdownload;

public record VerifyCodeCommand(String phone, String code, String identityType, String companyName,
                                String useCase, String source, String privacyVersion,
                                boolean privacyAccepted, boolean marketingConsent,
                                String clientIp, String userAgent) {
}
