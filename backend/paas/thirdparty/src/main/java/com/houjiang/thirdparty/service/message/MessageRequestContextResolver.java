package com.houjiang.thirdparty.service.message;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.thirdparty.feign.UserServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Amos
 */
@Component
public class MessageRequestContextResolver {

    private final UserServiceClient userServiceClient;

    public MessageRequestContextResolver(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public MessageRequestContext resolveCurrent() {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        return resolve(user, currentOrgId());
    }

    public MessageRequestContext requireRecordAdminCurrent() {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        return requireRecordAdmin(user, currentOrgId());
    }

    public MessageRequestContext requireRecordAdmin(UserInfoVO user, String orgId) {
        MessageRequestContext context = resolve(user, orgId);
        String limits = user.getLimits();
        boolean allowed = limits != null && java.util.Arrays.stream(limits.split("[,;\\s]+"))
                .map(String::trim)
                .anyMatch("shell:setting:message:record"::equals);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无发送记录管理权限");
        }
        return context;
    }

    private String currentOrgId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("X-Org-Id");
        }
        return null;
    }

    public MessageRequestContext resolve(UserInfoVO user, String orgId) {
        if (user == null || user.getUserId() == null || user.getUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未登录或登录已过期");
        }
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "必须提供 X-Org-Id");
        }
        String normalizedOrgId = orgId.trim();
        if (normalizedOrgId.contains(",")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前用户不属于所选组织");
        }
        ResponseResult response = userServiceClient.checkUserOrgExists(
                user.getUserId().trim(), normalizedOrgId);
        if (response == null || !Boolean.TRUE.equals(response.getData())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前用户不属于所选组织");
        }
        return new MessageRequestContext(user.getUserId(), normalizedOrgId);
    }
}
