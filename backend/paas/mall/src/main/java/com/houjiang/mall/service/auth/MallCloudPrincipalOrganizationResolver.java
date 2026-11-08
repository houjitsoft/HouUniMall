package com.houjiang.mall.service.auth;

import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Refreshes organization memberships missing from an older Cloud login cache.
 * Memberships are always read from the server-side association table; request
 * headers are deliberately not used as membership evidence.
 */
@Slf4j
@Component
public class MallCloudPrincipalOrganizationResolver {

    private final JdbcTemplate jdbcTemplate;
    private final String baseTablePrefix;

    public MallCloudPrincipalOrganizationResolver(
            JdbcTemplate jdbcTemplate,
            @Value("${database.prefix.base:}") String baseTablePrefix) {
        this.jdbcTemplate = jdbcTemplate;
        this.baseTablePrefix = normalizeTablePrefix(baseTablePrefix);
    }

    public UserInfoVO resolve(UserInfoVO user) {
        if (user == null || !StringUtils.hasText(user.getUserId()) || hasMembership(user)) {
            return user;
        }

        try {
            String sql = "SELECT uo.org_id, uo.is_primary FROM "
                    + baseTablePrefix
                    + "hou_user_organization uo "
                    + "WHERE uo.user_id = ? "
                    + "ORDER BY uo.is_primary DESC, uo.create_time DESC";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, user.getUserId());
            List<Map<String, Object>> memberships = normalizeMemberships(rows);
            if (!memberships.isEmpty()) {
                user.setOrgList(memberships);
                user.setOrgId(String.valueOf(memberships.get(0).get("org_id")));
            }
        } catch (RuntimeException e) {
            log.warn("Cloud principal organization refresh failed: userId={}, error={}",
                    user.getUserId(), e.getMessage());
        }
        return user;
    }

    private static boolean hasMembership(UserInfoVO user) {
        return StringUtils.hasText(user.getOrgId())
                || (user.getOrgList() != null && !user.getOrgList().isEmpty());
    }

    private static List<Map<String, Object>> normalizeMemberships(List<Map<String, Object>> rows) {
        List<Map<String, Object>> memberships = new ArrayList<>();
        if (rows == null) {
            return memberships;
        }
        for (Map<String, Object> row : rows) {
            if (row == null) {
                continue;
            }
            Object orgId = row.get("org_id");
            if (orgId == null) {
                orgId = row.get("orgId");
            }
            if (orgId == null || !StringUtils.hasText(orgId.toString())) {
                continue;
            }
            Map<String, Object> normalized = new LinkedHashMap<>(row);
            normalized.put("org_id", orgId.toString().trim());
            memberships.add(normalized);
        }
        return memberships;
    }

    private static String normalizeTablePrefix(String rawPrefix) {
        if (!StringUtils.hasText(rawPrefix)) {
            return "";
        }
        String normalized = rawPrefix.trim();
        if (!normalized.matches("[A-Za-z0-9_.]+")) {
            throw new IllegalArgumentException("Invalid database.prefix.base");
        }
        return normalized;
    }
}
