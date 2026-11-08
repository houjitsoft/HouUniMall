package com.houjiang.base.security;

import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基础服务组织上下文解析器。
 * 组织只能来自登录用户的组织成员关系，客户端传入的 X-Org-Id 仅用于在成员关系内切换。
 *
 * <p>会话里的 orgList 可能因历史登录未补全而为空；此时回落到
 * {@code hou_user_organization} 权威表，避免已登记组织的账号被误判为「未加入组织」。</p>
 */
@Slf4j
@Component
public class BaseOrganizationResolver {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public String resolveCurrentOrgId() {
        UserInfoVO user = currentUser();
        if (user == null || blank(user.getUserId())) {
            return null;
        }

        Set<String> memberships = memberships(user);
        if (memberships.isEmpty()) {
            log.warn("基础服务组织未确定 userId={}：账号未加入任何组织", user.getUserId());
            return null;
        }

        String requested = requestedOrgId();
        if (requested != null && memberships.contains(requested)) {
            return requested;
        }
        if (requested != null) {
            // 浏览器里残留的 currentOrgId 可能属于上一账号；拒绝跨组织，但不因此整单失败。
            log.warn("忽略无效 X-Org-Id，回落到账号所属组织 userId={}, requestedOrgId={}, memberships={}",
                    user.getUserId(), requested, memberships);
        }
        return memberships.iterator().next();
    }

    /**
     * 当前登录用户是否归属任何组织。
     * 用于把「未加入组织」和「选了不属于自己的组织」区分开，给出可操作的提示。
     */
    public boolean hasAnyMembership() {
        UserInfoVO user = currentUser();
        if (user == null || blank(user.getUserId())) {
            return false;
        }
        return !memberships(user).isEmpty();
    }

    private UserInfoVO currentUser() {
        UserInfoVO securityUser = SecurityContextUtils.getCurrentUserOrNull();
        if (securityUser != null && !blank(securityUser.getUserId())) return securityUser;
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || attributes.getRequest() == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            return ParseTokenUtils.getUserInfoByToken(authorization.substring(7).trim());
        } catch (Exception e) {
            log.debug("基础服务解析登录令牌失败: {}", e.getMessage());
            return null;
        }
    }

    private Set<String> memberships(UserInfoVO user) {
        Set<String> result = new LinkedHashSet<>();
        if (user.getOrgList() != null) {
            for (Map<String, Object> row : user.getOrgList()) {
                if (row == null) continue;
                Object value = row.get("org_id") != null ? row.get("org_id") : row.get("orgId");
                if (value != null && !blank(value.toString())) result.add(value.toString().trim());
            }
        }
        if (!blank(user.getOrgId())) result.add(user.getOrgId().trim());
        if (result.isEmpty()) {
            result.addAll(loadMembershipsFromDb(user.getUserId()));
        }
        return result;
    }

    private Set<String> loadMembershipsFromDb(String userId) {
        Set<String> result = new LinkedHashSet<>();
        if (jdbcTemplate == null || blank(userId)) {
            return result;
        }
        try {
            List<String> rows = jdbcTemplate.query(
                    "SELECT org_id FROM hou_user_organization WHERE user_id = ? ORDER BY is_primary DESC, create_time DESC",
                    (rs, rowNum) -> rs.getString("org_id"),
                    userId);
            if (rows != null) {
                for (String orgId : rows) {
                    if (!blank(orgId)) result.add(orgId.trim());
                }
            }
            if (!result.isEmpty()) {
                log.info("会话无组织，已从 hou_user_organization 回落 userId={}, orgCount={}",
                        userId, result.size());
            }
        } catch (Exception e) {
            log.warn("查询 hou_user_organization 失败 userId={}: {}", userId, e.getMessage());
        }
        return result;
    }

    private String requestedOrgId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || attributes.getRequest() == null) return null;
        String raw = attributes.getRequest().getHeader("X-Org-Id");
        if (raw == null || raw.isBlank()) return null;
        String normalized = raw.trim();
        return normalized.matches("[A-Za-z0-9_-]+") ? normalized : "__invalid__";
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
