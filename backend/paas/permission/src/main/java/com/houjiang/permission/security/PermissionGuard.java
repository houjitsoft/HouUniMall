package com.houjiang.permission.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.permission.config.PermissionAuthorizationProperties;
import com.houjiang.permission.entity.PermissionMenu;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.RoleMapper;
import com.houjiang.permission.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Enforces the live RBAC decision for permission-center management APIs. */
@Slf4j
@Component
public class PermissionGuard {

    public enum Risk {
        READ,
        WRITE,
        DELETE,
        ASSIGN,
        RESET_PASSWORD,
        STATUS_CHANGE,
        CACHE_CLEAR,
        IDENTITY_MISMATCH
    }

    private static final Set<String> ADMIN_ROLE_NAMES = Set.of(
            "admin", "super_admin", "管理员", "超级管理员");
    private static final String PERMISSION_CENTER_ROOT = "shell:setting:permission";
    private static final String PERMISSION_CENTER_CHILD_PREFIX = PERMISSION_CENTER_ROOT + ":";

    private final PermissionIdentity identity;
    private final PermissionService permissionService;
    private final RoleMapper roleMapper;
    private final PermissionMenuMapper permissionMenuMapper;
    private final PermissionAuthorizationProperties properties;
    private final Clock clock;

    @Autowired
    public PermissionGuard(
            PermissionIdentity identity,
            PermissionService permissionService,
            RoleMapper roleMapper,
            PermissionMenuMapper permissionMenuMapper,
            PermissionAuthorizationProperties properties) {
        this(identity, permissionService, roleMapper, permissionMenuMapper, properties, Clock.systemUTC());
    }

    PermissionGuard(
            PermissionIdentity identity,
            PermissionService permissionService,
            RoleMapper roleMapper,
            PermissionMenuMapper permissionMenuMapper,
            PermissionAuthorizationProperties properties,
            Clock clock) {
        this.identity = identity;
        this.permissionService = permissionService;
        this.roleMapper = roleMapper;
        this.permissionMenuMapper = permissionMenuMapper;
        this.properties = properties;
        this.clock = clock;
    }

    public Long require(String permission, Risk risk) {
        Long userId = identity.currentUserId();
        if (permissionService.hasPermission(userId, permission)) {
            return userId;
        }

        if (isObservable(risk)) {
            log.warn("permission_authorization_observed userId={} permission={} risk={} mode={} observeUntil={}",
                    userId, permission, risk, properties.getMode(), properties.getObserveUntil());
            return userId;
        }

        log.warn("permission_authorization_denied userId={} permission={} risk={} mode={} observeUntil={}",
                userId, permission, risk, properties.getMode(), properties.getObserveUntil());
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权执行此权限操作");
    }

    public Long requireSuperAdmin() {
        Long userId = identity.currentUserId();
        try {
            List<String> roleNames = roleMapper.selectRoleNamesByUserId(userId);
            boolean standardAdmin = roleNames != null && roleNames.stream()
                    .filter(name -> name != null)
                    .map(name -> name.trim().toLowerCase(Locale.ROOT))
                    .anyMatch(ADMIN_ROLE_NAMES::contains);
            if (!standardAdmin) {
                throw forbiddenSuperAdmin();
            }

            QueryWrapper<PermissionMenu> query = new QueryWrapper<>();
            query.eq("is_disable", 0)
                    .isNotNull("perms")
                    .and(scope -> scope.eq("perms", PERMISSION_CENTER_ROOT)
                            .or()
                            .likeRight("perms", PERMISSION_CENTER_CHILD_PREFIX));
            Set<String> required = permissionMenuMapper.selectList(query).stream()
                    .map(PermissionMenu::getPerms)
                    .filter(permission -> permission != null && !permission.isBlank())
                    .collect(Collectors.toSet());
            boolean ownsAll = !required.isEmpty()
                    && required.stream().allMatch(permission -> permissionService.hasPermission(userId, permission));
            if (!ownsAll) {
                throw forbiddenSuperAdmin();
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "权限服务依赖暂时不可用", exception);
        }
        return userId;
    }

    private boolean isObservable(Risk risk) {
        return risk == Risk.READ
                && properties.isObservationActive(clock);
    }

    private ResponseStatusException forbiddenSuperAdmin() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "仅完整授权的超级管理员可执行此操作");
    }
}
