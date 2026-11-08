package com.houjiang.bffweb.service;

public record FileImportAdapterResult(
        int code,
        String message,
        String businessRecordId,
        String detailUrl,
        String attachmentUrl
) {
    public static FileImportAdapterResult fail(int code, String message) {
        return new FileImportAdapterResult(code, message, null, null, null);
    }
}
