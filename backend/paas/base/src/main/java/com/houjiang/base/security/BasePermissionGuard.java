package com.houjiang.base.security;

import com.houjiang.base.dto.OrganizationDTO;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base 服务入口权限和组织数据范围守卫。
 *
 * Base 历史安全链允许内部请求放行，因此组织 Controller 必须在业务入口自行拒绝。
 * 优先使用 JWT 过滤器写入的 SecurityContext；兼容没有安装过滤器时由 BFF 转发的令牌。
 */
@Slf4j
@Component
public class BasePermissionGuard {

    /**
     * 服务间内部调用令牌（job 链路同一份）。为空表示未配置，此时只允许「本人操作自己」。
     */
    @Value("${houcloud.plugin.internal-job-token:}")
    private String internalJobToken;

    public ResponseResult<Void> require(String permission) {
        UserInfoVO user = currentUser();
        if (user == null || user.getUserId() == null || user.getUserId().isBlank()) {
            return ResponseResult.fail(401, "请先登录");
        }
        if (!hasPermission(user, permission)) {
            return ResponseResult.fail(403, "无权执行组织管理操作");
        }
        if (accessibleOrganizationIds(user).isEmpty()) {
            return ResponseResult.fail(403, "当前用户没有可访问的组织");
        }
        return null;
    }

    public ResponseResult<Void> requireTarget(String permission, String orgId) {
        ResponseResult<Void> result = require(permission);
        if (result != null) return result;
        if (orgId == null || !accessibleOrganizationIds(currentUser()).contains(orgId.trim())) {
            return ResponseResult.fail(403, "无权访问该组织");
        }
        return null;
    }

    public ResponseResult<Void> requireAny(String... permissions) {
        UserInfoVO user = currentUser();
        if (user == null || user.getUserId() == null || user.getUserId().isBlank()) {
            return ResponseResult.fail(401, "请先登录");
        }
        if (accessibleOrganizationIds(user).isEmpty()) {
            return ResponseResult.fail(403, "当前用户没有可访问的组织");
        }
        for (String permission : permissions) {
            if (hasPermission(user, permission)) return null;
        }
        return ResponseResult.fail(403, "无权执行组织管理操作");
    }

    public void applyOrganizationScope(OrganizationDTO dto) {
        if (dto != null) dto.setAccessibleOrgIds(new ArrayList<>(accessibleOrganizationIds(currentUser())));
    }

    /**
     * 内部调用校验：只有持内部令牌的服务间调用，或「本人操作自己」才放行。
     *
     * <p>🔴 2026-10-27 修 P1：{@code /organization/ensurePersonalOrganization}
     * 与 {@code /organization/createUserOrg} 接收显式 {@code userId} 且不做任何校验，
     * base 的 Security 又是 {@code anyRequest().permitAll()}——
     * base 端口一旦在网络上直连可达，任何人都能按任意 userId 建组织、改归属。
     *
     * <p>内部令牌复用 job 链路既有的 {@code houcloud.plugin.internal-job-token}
     * （Nacos 里各服务同值下发），不新增凭据面。
     *
     * @param userId  目标用户 ID（请求里声明的）
     * @param request 当前请求
     * @return 拒绝时返回 401/403；放行返回 null
     */
    public ResponseResult<Void> requireInternalOrSelf(String userId, HttpServletRequest request) {
        if (request != null && StringUtils.hasText(internalJobToken)) {
            String supplied = request.getHeader("X-Internal-Job-Token");
            if (StringUtils.hasText(supplied) && constantTimeEquals(internalJobToken, supplied.trim())) {
                return null;
            }
        }
        UserInfoVO user = currentUser();
        if (user == null || !StringUtils.hasText(user.getUserId())) {
            return ResponseResult.fail(401, "请先登录");
        }
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(400, "缺少用户标识");
        }
        if (!user.getUserId().trim().equals(userId.trim())) {
            log.warn("拒绝跨用户组织操作: requester={} target={}", user.getUserId(), userId);
            return ResponseResult.fail(403, "无权操作其他用户的组织");
        }
        return null;
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null || expected.length() != actual.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < expected.length(); i++) {
            diff |= expected.charAt(i) ^ actual.charAt(i);
        }
        return diff == 0;
    }

    public UserInfoVO currentUser() {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        if (user != null) return user;

        HttpServletRequest request = currentRequest();
        if (request == null) return null;
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) token = request.getHeader("Token");
        if (token == null || token.isBlank()) token = request.getHeader("token");
        if (token == null || token.isBlank()) return null;
        if (token.startsWith("Bearer ")) token = token.substring(7).trim();
        try {
            return ParseTokenUtils.getUserInfoByToken(token);
        } catch (Exception e) {
            log.debug("解析 base 组织权限令牌失败: {}", e.getMessage());
            return null;
        }
    }

    public Set<String> accessibleOrganizationIds(UserInfoVO user) {
        Set<String> ids = new LinkedHashSet<>();
        if (user == null) return ids;
        if (user.getOrgList() != null) {
            for (Map<String, Object> row : user.getOrgList()) {
                if (row == null) continue;
                Object value = row.get("org_id") != null ? row.get("org_id") : row.get("orgId");
                if (value != null && !value.toString().isBlank()) ids.add(value.toString().trim());
            }
        }
        if (user.getOrgId() != null && !user.getOrgId().isBlank()) ids.add(user.getOrgId().trim());
        return ids;
    }

    private boolean hasPermission(UserInfoVO user, String permission) {
        return SecurityContextUtils.hasPermission(permission)
                || SecurityContextUtils.hasPermission(user, permission);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}
