package com.houjiang.base.officialdownload;

public interface ArtifactCatalog {
    DownloadArtifact findPublished(String product, String version);
}
