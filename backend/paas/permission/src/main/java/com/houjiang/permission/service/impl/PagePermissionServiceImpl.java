package com.houjiang.permission.service.impl;

import com.houjiang.permission.service.PagePermissionService;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.RoleMenuMapper;
import com.houjiang.permission.mapper.UserRoleMapper;
import com.houjiang.permission.entity.PermissionMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 页面权限验证服务实现
 * 
 * @author Amos
 * @date 2024-09-26
 */
@Service
public class PagePermissionServiceImpl implements PagePermissionService {
    
    @Autowired
    private PermissionMenuMapper permissionMenuMapper;
    
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;

    public PagePermissionServiceImpl() {
    }

    public PagePermissionServiceImpl(
            PermissionMenuMapper permissionMenuMapper,
            RoleMenuMapper roleMenuMapper,
            UserRoleMapper userRoleMapper) {
        this.permissionMenuMapper = permissionMenuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
    }
    
    @Override
    public boolean checkPageAccess(Long userId, String pagePath) {
        if (userId == null || pagePath == null || pagePath.trim().isEmpty()) {
            return false;
        }
        
        String normalizedPath = normalizePagePath(pagePath);
        
        // 获取用户的角色ID列表
        List<Integer> roleIds = getUserRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }
        
        PermissionMenu pageMenu = findEnabledPage(normalizedPath);
        if (pageMenu == null) {
            return false;
        }
        
        // 检查用户角色是否有该菜单权限
        return hasMenuPermission(roleIds, pageMenu.getId());
    }
    
    @Override
    public List<String> getPageButtonPermissions(Long userId, String pagePath) {
        if (userId == null || pagePath == null || pagePath.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String normalizedPath = normalizePagePath(pagePath);
        
        // 获取用户的角色ID列表
        List<Integer> roleIds = getUserRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        
        List<Integer> userMenuIds = getUserMenuIds(roleIds);
        if (CollectionUtils.isEmpty(userMenuIds)) {
            return new ArrayList<>();
        }

        PermissionMenu pageMenu = findEnabledPage(normalizedPath);
        if (pageMenu == null || !userMenuIds.contains(pageMenu.getId())) {
            return new ArrayList<>();
        }

        QueryWrapper<PermissionMenu> wrapper = new QueryWrapper<>();
        wrapper.in("id", userMenuIds)
                .eq("pid", pageMenu.getId())
                .eq("type", "A")
                .eq("is_disable", 0)
                .isNotNull("perms");
        return permissionMenuMapper.selectList(wrapper).stream()
                .map(PermissionMenu::getPerms)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Boolean> batchCheckButtonPermissions(Long userId, List<String> buttonCodes) {
        Map<String, Boolean> result = new HashMap<>();
        
        if (buttonCodes == null) {
            return result;
        }
        if (userId == null || buttonCodes.isEmpty()) {
            buttonCodes.forEach(code -> result.put(code, false));
            return result;
        }
        
        // 获取用户所有按钮权限
        List<String> userButtonPermissions = getAllButtonPermissions(userId);
        Set<String> permissionSet = new HashSet<>(userButtonPermissions);
        
        // 批量检查权限
        for (String buttonCode : buttonCodes) {
            result.put(buttonCode, permissionSet.contains(buttonCode));
        }
        
        return result;
    }
    
    @Override
    public boolean checkButtonPermission(Long userId, String buttonCode) {
        if (userId == null || buttonCode == null || buttonCode.trim().isEmpty()) {
            return false;
        }
        
        // 获取用户的角色ID列表
        List<Integer> roleIds = getUserRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }
        
        // 查询按钮权限菜单（使用perms字段）
        QueryWrapper<PermissionMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("perms", buttonCode)
               // 只查A级按钮权限
               .eq("type", "A")
               .eq("is_disable", 0);
        
        PermissionMenu buttonMenu = permissionMenuMapper.selectOne(wrapper);
        if (buttonMenu == null) {
            return false;
        }
        
        // 检查用户角色是否有该按钮权限
        return hasMenuPermission(roleIds, buttonMenu.getId());
    }
    
    @Override
    public List<String> getAllPagePermissions(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        // 获取用户的角色ID列表
        List<Integer> roleIds = getUserRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        
        // 获取用户有权限的菜单ID列表
        List<Integer> menuIds = getUserMenuIds(roleIds);
        if (CollectionUtils.isEmpty(menuIds)) {
            return new ArrayList<>();
        }
        
        // 查询用户有权限的页面菜单（M级和C级）
        QueryWrapper<PermissionMenu> wrapper = new QueryWrapper<>();
        wrapper.in("id", menuIds)
               .in("type", Arrays.asList("M", "C"))
               .eq("is_disable", 0)
               .isNotNull("paths");
        
        List<PermissionMenu> pageMenus = permissionMenuMapper.selectList(wrapper);
        
        return pageMenus.stream()
                .map(PermissionMenu::getPaths)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getAllButtonPermissions(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        // 获取用户的角色ID列表
        List<Integer> roleIds = getUserRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        
        // 获取用户有权限的菜单ID列表
        List<Integer> menuIds = getUserMenuIds(roleIds);
        if (CollectionUtils.isEmpty(menuIds)) {
            return new ArrayList<>();
        }
        
        // 查询用户有权限的按钮菜单（A级）
        QueryWrapper<PermissionMenu> wrapper = new QueryWrapper<>();
        wrapper.in("id", menuIds)
               .eq("type", "A")
               .eq("is_disable", 0)
               .isNotNull("perms");
        
        List<PermissionMenu> buttonMenus = permissionMenuMapper.selectList(wrapper);
        
        return buttonMenus.stream()
                .map(PermissionMenu::getPerms)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的角色ID列表
     */
    private List<Integer> getUserRoleIds(Long userId) {
        return userRoleMapper.getRoleIdsByUserId(userId);
    }
    
    /**
     * 获取角色拥有的菜单ID列表
     */
    private List<Integer> getUserMenuIds(List<Integer> roleIds) {
        return roleMenuMapper.getMenuIdsByRoleIds(roleIds);
    }
    
    /**
     * 检查角色是否有指定菜单权限
     */
    private boolean hasMenuPermission(List<Integer> roleIds, Integer menuId) {
        if (CollectionUtils.isEmpty(roleIds) || menuId == null) {
            return false;
        }
        
        List<Integer> userMenuIds = getUserMenuIds(roleIds);
        return userMenuIds.contains(menuId);
    }
    
    private PermissionMenu findEnabledPage(String normalizedPath) {
        QueryWrapper<PermissionMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("paths", normalizedPath)
                .in("type", Arrays.asList("M", "C"))
                .eq("is_disable", 0);
        return permissionMenuMapper.selectOne(wrapper);
    }

    static String normalizePagePath(String pagePath) {
        String normalized = pagePath == null ? "" : pagePath.trim();
        int queryIndex = normalized.indexOf('?');
        int hashIndex = normalized.indexOf('#');
        int suffixIndex;
        if (queryIndex < 0) {
            suffixIndex = hashIndex;
        } else if (hashIndex < 0) {
            suffixIndex = queryIndex;
        } else {
            suffixIndex = Math.min(queryIndex, hashIndex);
        }
        if (suffixIndex >= 0) {
            normalized = normalized.substring(0, suffixIndex);
        }
        normalized = normalized.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
