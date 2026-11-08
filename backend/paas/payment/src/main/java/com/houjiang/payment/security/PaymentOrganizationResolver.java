package com.houjiang.payment.security;

import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.mapper.PaymentUserOrganizationMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 从认证用户的权威组织关系解析支付租户，禁止客户端伪造。 */
@Component
@RequiredArgsConstructor
public class PaymentOrganizationResolver {

    private final PaymentUserOrganizationMapper organizationMapper;

    public long requireCurrentTenantId() {
        UserInfoVO user = requireCurrentUser();
        List<Map<String, Object>> rows = organizationMapper.selectOrganizationsByUserId(user.getUserId());
        Set<Long> memberships = new LinkedHashSet<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object raw = row == null ? null : row.get("org_id");
                if (raw != null) {
                    try {
                        memberships.add(Long.valueOf(String.valueOf(raw).trim()));
                    } catch (NumberFormatException ignored) {
                        // 支付租户是数字类型；不可映射的成员关系不能用作授权。
                    }
                }
            }
        }
        if (memberships.isEmpty()) {
            throw new AccessDeniedException("无法确定当前支付组织");
        }
        String selected = selectedOrgId();
        if (selected != null) {
            long requested;
            try {
                requested = Long.parseLong(selected);
            } catch (NumberFormatException ex) {
                throw new AccessDeniedException("组织标识不合法");
            }
            if (!memberships.contains(requested)) {
                throw new AccessDeniedException("无权访问该支付组织");
            }
            return requested;
        }
        return memberships.iterator().next();
    }

    public String requireCurrentUserId() {
        return requireCurrentUser().getUserId();
    }

    private UserInfoVO requireCurrentUser() {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        if (user == null || user.getUserId() == null || user.getUserId().isBlank()) {
            throw new AccessDeniedException("请先登录");
        }
        return user;
    }

    private String selectedOrgId() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String value = request == null ? null : request.getHeader("X-Org-Id");
        return value == null || value.isBlank() ? null : value.trim();
    }
}
