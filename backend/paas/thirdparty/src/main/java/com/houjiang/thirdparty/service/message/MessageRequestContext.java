package com.houjiang.thirdparty.service.message;

public record MessageRequestContext(String userId, String orgId) {

    public MessageRequestContext {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (orgId == null || orgId.isBlank()) {
            throw new IllegalArgumentException("orgId 不能为空");
        }
        userId = userId.trim();
        orgId = orgId.trim();
    }
}
