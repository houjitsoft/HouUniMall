package com.houjiang.permission.security;

import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.permission.util.PermissionUserIdParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * Resolves the authenticated identity used by permission self-service APIs.
 */
@Component
public class PermissionIdentity {

    public Long currentUserId() {
        String userId = SecurityContextUtils.getCurrentUserId();
        if (!StringUtils.hasText(userId)) {
            throw unauthorized();
        }

        try {
            return Long.valueOf(userId.trim());
        } catch (NumberFormatException exception) {
            throw unauthorized();
        }
    }

    public Long requireSelf(Object requestedUserId) {
        Long currentUserId = currentUserId();
        if (requestedUserId == null) {
            return currentUserId;
        }

        Long parsedRequestedUserId;
        try {
            parsedRequestedUserId = PermissionUserIdParser.parse(requestedUserId);
        } catch (NumberFormatException exception) {
            throw badRequest();
        }

        if (parsedRequestedUserId == null) {
            throw badRequest();
        }
        if (!currentUserId.equals(parsedRequestedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问其他用户的权限信息");
        }
        return currentUserId;
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未登录或登录已过期");
    }

    private ResponseStatusException badRequest() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户ID格式错误");
    }
}
