package com.houjiang.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.permission.entity.UserRole;
import com.houjiang.permission.mapper.UserRoleMapper;
import com.houjiang.permission.service.MenuService;
import com.houjiang.permission.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户角色关联服务实现类
 * 
 * @author Amos
 * @since 2025-01-09
 */
@Slf4j
@Service
public class UserRoleServiceImpl implements UserRoleService {
    
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private MenuService menuService;
    
    @Override
    public List<Integer> getRoleIdsByUserId(Long userId) {
        log.info("根据用户ID获取角色ID列表: userId={}", userId);
        return userRoleMapper.getRoleIdsByUserId(userId);
    }
    
    @Override
    public List<Long> getUserIdsByRoleId(Integer roleId) {
        log.info("根据角色ID获取用户ID列表: roleId={}", roleId);
        return userRoleMapper.getUserIdsByRoleId(roleId);
    }
    
    @Override
    public Integer getUserCountByRoleId(Integer roleId) {
        log.info("根据角色ID获取用户数量: roleId={}", roleId);
        return userRoleMapper.getUserCountByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public void batchSetUserRoles(Long userId, List<Integer> roleIds) {
        log.info("批量设置用户角色: userId={}, roleIds={}", userId, roleIds);
        
        // 先删除用户的所有角色关联
        deleteByUserId(userId);
        
        // 批量插入新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Integer roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
            log.info("成功设置用户角色关联: userId={}, roleIds={}", userId, roleIds);
        }
        menuService.clearUserMenusCache(userId);
    }
    
    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        log.info("删除用户的所有角色关联: userId={}", userId);
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        userRoleMapper.delete(wrapper);
    }
    
    @Override
    @Transactional
    public void deleteByRoleId(Integer roleId) {
        log.info("删除角色的所有用户关联: roleId={}", roleId);
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        userRoleMapper.delete(wrapper);
    }
}
