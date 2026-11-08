package com.houjiang.base.officialdownload;

public record OfficialDownloadPolicy(int smsCooldownSeconds, int codeTtlSeconds, int maxCodeAttempts,
                                     int sessionTtlSeconds, int maxDownloads) {
}
