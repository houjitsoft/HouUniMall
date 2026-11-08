package com.houjiang.auth.service.impl;

/**
 * 用户服务返回失败响应时抛出，避免被误判为用户不存在。
 */
class UserServiceLookupException extends RuntimeException {

    UserServiceLookupException(String message) {
        super(message);
    }
}
