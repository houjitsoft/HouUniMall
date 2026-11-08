package com.houjiang.base.officialdownload;

public final class DownloadFlowException extends RuntimeException {
    private final String code;

    public DownloadFlowException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() { return code; }
}
