package com.houjiang.permission.service.impl;

import com.houjiang.permission.mapper.UserRoleMapper;
import com.houjiang.permission.mapper.RoleMenuMapper;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.service.PermissionService;
import com.houjiang.commons.config.RedisCache;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 权限服务实现类
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    
    @Autowired
    private PermissionMenuMapper permissionMenuMapper;
    
    @Autowired(required = false)
    private RedisCache redisCache;
    
    /** Jackson序列化器，避免FastJSON AutoType问题 */
    private static final ObjectMapper jacksonMapper = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<List<String>>() {};
    
    @Override
    public boolean hasPermission(Long userId, String permission) {
        try {
            // 超管（hou_user_info.is_admin=1）有意不绑 RBAC 角色，按角色查权限恒空。
            // 与 auth 服务兜底 permissions=["*"] 同口径：超管直接放行，否则清缓存等自服务接口恒 403。
            Integer isAdmin = permissionMenuMapper.selectIsAdmin(userId);
            if (isAdmin != null && isAdmin == 1) {
                log.debug("超管放行: userId={}, permission={}", userId, permission);
                return true;
            }

            // 获取用户角色
            List<Integer> roleIds = userRoleMapper.getRoleIdsByUserId(userId);
            if (roleIds == null || roleIds.isEmpty()) {
                log.debug("用户无角色: userId={}", userId);
                return false;
            }
            
            // 获取角色菜单权限
            List<Integer> menuIds = roleMenuMapper.getMenuIdsByRoleIds(roleIds);
            if (menuIds == null || menuIds.isEmpty()) {
                log.debug("角色无菜单权限: userId={}, roleIds={}", userId, roleIds);
                return false;
            }
            
            // 检查权限标识
            int count = permissionMenuMapper.checkPermission(userId, permission, menuIds);
            boolean hasPermission = count > 0;
            
            log.debug("权限检查: userId={}, permission={}, result={}", userId, permission, hasPermission);
            return hasPermission;
        } catch (Exception e) {
            log.error("权限检查失败: userId={}, permission={}", userId, permission, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "权限服务依赖暂时不可用", e);
        }
    }
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        try {
            String cacheKey = "user:permissions:" + userId;
            // 1. 从Redis读取（用Jackson反序列化，避免FastJSON问题）
            if (redisCache != null) {
                try {
                    Object cached = redisCache.getCacheObject(cacheKey);
                    if (cached != null) {
                        List<String> permissions = deserializePermissions(cached);
                        if (permissions != null) {
                            log.debug("用户权限命中缓存, userId={}, count={}", userId, permissions.size());
                            return permissions;
                        }
                    }
                } catch (Exception e) {
                    log.warn("权限缓存读取失败, userId={}", userId, e);
                }
            }
            // 2. 查库（STRAIGHT_JOIN已在Mapper层优化）
            List<String> permissions = permissionMenuMapper.selectUserPermissionPermsByUserId(userId);
            if (permissions == null) {
                permissions = new ArrayList<>();
            }
            // 3. 写入缓存（Jackson序列化为JSON字符串）
            if (redisCache != null && !permissions.isEmpty()) {
                try {
                    String jsonStr = jacksonMapper.writeValueAsString(permissions);
                    redisCache.setCacheObject(cacheKey, jsonStr, 60, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.warn("权限缓存写入失败, userId={}", userId, e);
                }
            }
            return permissions;
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}", userId, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "权限服务依赖暂时不可用", e);
        }
    }
    
    /**
     * 反序列化权限缓存，兼容JSON字符串和List对象
     */
    private List<String> deserializePermissions(Object cached) {
        try {
            if (cached instanceof String) {
                String json = (String) cached;
                return json.isEmpty() ? null : jacksonMapper.readValue(json, STRING_LIST_TYPE);
            } else if (cached instanceof List) {
                return jacksonMapper.convertValue(cached, STRING_LIST_TYPE);
            }
        } catch (Exception e) {
            log.warn("权限缓存反序列化失败: {}", e.getMessage());
        }
        return null;
    }
}
