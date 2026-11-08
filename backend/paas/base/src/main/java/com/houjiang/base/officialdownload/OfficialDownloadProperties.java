package com.houjiang.base.officialdownload;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "unimall.official-download")
public class OfficialDownloadProperties {
    private boolean enabled;
    private String encryptionKey;
    private String hmacKey;
    private int smsCooldownSeconds = 60;
    private int codeTtlSeconds = 300;
    private int maxCodeAttempts = 5;
    private int sessionTtlSeconds = 600;
    private int maxDownloads = 3;
    private String releaseApiToken;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getEncryptionKey() { return encryptionKey; }
    public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }
    public String getHmacKey() { return hmacKey; }
    public void setHmacKey(String hmacKey) { this.hmacKey = hmacKey; }
    public int getSmsCooldownSeconds() { return smsCooldownSeconds; }
    public void setSmsCooldownSeconds(int value) { this.smsCooldownSeconds = value; }
    public int getCodeTtlSeconds() { return codeTtlSeconds; }
    public void setCodeTtlSeconds(int value) { this.codeTtlSeconds = value; }
    public int getMaxCodeAttempts() { return maxCodeAttempts; }
    public void setMaxCodeAttempts(int value) { this.maxCodeAttempts = value; }
    public int getSessionTtlSeconds() { return sessionTtlSeconds; }
    public void setSessionTtlSeconds(int value) { this.sessionTtlSeconds = value; }
    public int getMaxDownloads() { return maxDownloads; }
    public void setMaxDownloads(int value) { this.maxDownloads = value; }
    public String getReleaseApiToken() { return releaseApiToken; }
    public void setReleaseApiToken(String value) { this.releaseApiToken = value; }
}
