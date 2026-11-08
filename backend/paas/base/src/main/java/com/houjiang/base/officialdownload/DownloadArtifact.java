package com.houjiang.base.officialdownload;

public record DownloadArtifact(Long id, String product, String version, String fileName,
                               String objectKey, String sha256, boolean published) {
}
