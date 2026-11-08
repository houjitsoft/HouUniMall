package com.houjiang.base.officialdownload;

import java.time.Instant;

public record DownloadTicket(String token, String tokenHash, Long leadId, Long artifactId,
                             String ipHash, Instant expiresAt, int remainingDownloads) {
}
