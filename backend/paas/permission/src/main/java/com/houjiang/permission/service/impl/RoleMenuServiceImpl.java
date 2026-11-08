package com.houjiang.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.permission.entity.RoleMenu;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.RoleMenuMapper;
import com.houjiang.permission.service.MenuService;
import com.houjiang.permission.service.RoleMenuService;
import com.houjiang.permission.service.UserRoleService;
import com.houjiang.permission.support.MenuAncestorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色菜单服务实现类
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Service
public class RoleMenuServiceImpl implements RoleMenuService {

    private static final Logger log = LoggerFactory.getLogger(RoleMenuServiceImpl.class);
    private static final int INSERT_BATCH_SIZE = 400;
    
    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private PermissionMenuMapper permissionMenuMapper;

    @Lazy
    @Resource
    private MenuService menuService;

    @Lazy
    @Resource
    private UserRoleService userRoleService;

    @Override
    public List<Integer> getMenuIdsByRoleId(Integer roleId) {
        try {
            List<Integer> menuIds = new ArrayList<>();
            
            if (roleId == null) {
                return menuIds;
            }
            
            List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                    new QueryWrapper<RoleMenu>().eq("role_id", roleId));
            
            for (RoleMenu roleMenu : roleMenus) {
                menuIds.add(roleMenu.getMenuId());
            }
            
            return menuIds;
        } catch (Exception e) {
            System.err.println("Error in getMenuIdsByRoleId: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public List<Integer> getMenuIdsByRoleIds(List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new QueryWrapper<RoleMenu>().in("role_id", roleIds));
        return roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveByMenuIds(Integer roleId, List<Integer> menuIds) {
        if (roleId == null || menuIds == null || menuIds.isEmpty()) {
            return;
        }
        long t0 = System.nanoTime();
        Set<Integer> uniqueMenuIds = new LinkedHashSet<>();
        for (Integer menuId : menuIds) {
            if (menuId != null && menuId > 0) {
                uniqueMenuIds.add(menuId);
            }
        }
        if (uniqueMenuIds.isEmpty()) {
            return;
        }
        List<Integer> withAncestors = MenuAncestorSupport.expandMenuIdsWithAncestors(
                permissionMenuMapper, new ArrayList<>(uniqueMenuIds));
        long t1 = System.nanoTime();

        roleMenuMapper.deleteByRoleIdFast(roleId);
        long t2 = System.nanoTime();

        for (int i = 0; i < withAncestors.size(); i += INSERT_BATCH_SIZE) {
            int end = Math.min(i + INSERT_BATCH_SIZE, withAncestors.size());
            roleMenuMapper.insertBatchIds(roleId, new ArrayList<>(withAncestors.subList(i, end)));
        }
        long t3 = System.nanoTime();

        scheduleClearMenusCacheForRole(roleId);
        log.info("batchSaveByMenuIds roleId={} in={} saved={} expand={}ms delete={}ms insert={}ms total={}ms",
                roleId, menuIds.size(), withAncestors.size(),
                (t1 - t0) / 1_000_000L, (t2 - t1) / 1_000_000L, (t3 - t2) / 1_000_000L, (t3 - t0) / 1_000_000L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMissingMenuIds(Integer roleId, List<Integer> menuIds) {
        if (roleId == null || CollectionUtils.isEmpty(menuIds)) {
            return;
        }
        Set<Integer> requested = menuIds.stream()
                .filter(menuId -> menuId != null && menuId > 0)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (requested.isEmpty()) {
            return;
        }
        Set<Integer> existing = roleMenuMapper.selectList(
                        new QueryWrapper<RoleMenu>().eq("role_id", roleId)).stream()
                .map(RoleMenu::getMenuId)
                .collect(java.util.stream.Collectors.toSet());
        requested.removeAll(existing);
        for (Integer menuId : requested) {
            roleMenuMapper.insertIfMissing(roleId, menuId);
        }
        if (!requested.isEmpty()) {
            scheduleClearMenusCacheForRole(roleId);
        }
    }
    
    @Override
    @Transactional
    public void batchDeleteByRoleId(Integer roleId) {
        if (roleId == null) {
            return;
        }
        roleMenuMapper.deleteByRoleIdFast(roleId);
        scheduleClearMenusCacheForRole(roleId);
    }
    
    @Override
    @Transactional
    public void batchDeleteByMenuId(Integer menuId) {
        roleMenuMapper.delete(new QueryWrapper<RoleMenu>().eq("menu_id", menuId));
    }

    private void scheduleClearMenusCacheForRole(Integer roleId) {
        if (roleId == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    clearMenusCacheForRole(roleId);
                }
            });
        } else {
            clearMenusCacheForRole(roleId);
        }
    }

    /** 只清该角色下用户缓存，避免 Redis KEYS 全站扫描 */
    private void clearMenusCacheForRole(Integer roleId) {
        if (userRoleService == null || menuService == null) {
            return;
        }
        try {
            List<Long> userIds = userRoleService.getUserIdsByRoleId(roleId);
            if (userIds == null || userIds.isEmpty()) {
                return;
            }
            for (Long userId : userIds) {
                menuService.clearUserMenusCache(userId);
            }
        } catch (Exception e) {
            log.warn("clearMenusCacheForRole roleId={} failed: {}", roleId, e.getMessage());
        }
    }
}
