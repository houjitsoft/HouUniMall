package com.houjiang.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.permission.dto.PageRequest;
import com.houjiang.permission.dto.RoleCreateRequest;
import com.houjiang.permission.dto.RoleUpdateRequest;
import com.houjiang.permission.entity.Role;
import com.houjiang.permission.entity.RoleMenu;
import com.houjiang.permission.mapper.RoleMapper;
import com.houjiang.permission.mapper.RoleMenuMapper;
import com.houjiang.permission.service.RoleMenuService;
import com.houjiang.permission.service.RoleService;
import com.houjiang.permission.service.UserRoleService;
import com.houjiang.permission.vo.PageResult;
import com.houjiang.permission.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 角色服务实现类
 *
 * @author Amos
 * @since 2025-01-10
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {
    
    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private RoleMenuService roleMenuService;
    
    @Resource
    private UserRoleService userRoleService;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 格式化时间戳为字符串
     */
    private String formatTimestamp(Integer timestamp) {
        if (timestamp == null || timestamp == 0) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.ofHours(8));
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
    @Override
    public List<RoleVO> getAllRoles() {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "name", "sort", "create_time", "update_time");
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));
        queryWrapper.isNull("delete_time");
        List<Role> roles = roleMapper.selectList(queryWrapper);
        
        List<RoleVO> list = new ArrayList<>();
        for (Role role : roles) {
            RoleVO vo = new RoleVO();
            vo.setId(role.getId());
            vo.setName(role.getName());
            vo.setSort(role.getSort());
            vo.setCreateTime(formatTimestamp(role.getCreateTime()));
            vo.setUpdateTime(formatTimestamp(role.getUpdateTime()));
            vo.setNum(0);
            vo.setDesc("");
            vo.setMenusId(new ArrayList<>());
            list.add(vo);
        }
        
        return list;
    }
    
    @Override
    public PageResult<RoleVO> getRoleList(PageRequest pageRequest) {
        Integer page = pageRequest.getPageNo();
        Integer limit = pageRequest.getPageSize();
        
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));
        queryWrapper.isNull("delete_time");
        
        IPage<Role> iPage = roleMapper.selectPage(new Page<>(page, limit), queryWrapper);
        
        List<RoleVO> list = new ArrayList<>();
        for (Role role : iPage.getRecords()) {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            // 获取角色的菜单权限
            List<Integer> menuIds = roleMenuService.getMenuIdsByRoleId(role.getId());
            vo.setMenusId(menuIds);
            vo.setNum(userRoleService.getUserCountByRoleId(role.getId()));
            vo.setCreateTime(formatTimestamp(role.getCreateTime()));
            vo.setUpdateTime(formatTimestamp(role.getUpdateTime()));
            list.add(vo);
        }
        
        return PageResult.of(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }
    
    @Override
    public RoleVO getRoleDetail(Integer id) {
        try {
            log.info("获取角色详情: id={}", id);

            Role role = roleMapper.selectOne(new QueryWrapper<Role>()
                    .eq("id", id)
                    .isNull("delete_time")
                    .last("limit 1"));

            log.info("查询到的角色: {}", role);

            if (role == null) {
                log.error("角色不存在: id={}", id);
                throw new RuntimeException("角色已不存在!");
            }

            log.info("角色查询成功，开始获取菜单权限");

            List<Integer> roleIds = new ArrayList<>();
            roleIds.add(role.getId());

            log.info("直接查询数据库获取菜单权限");

            List<Integer> menuIds = new ArrayList<>();
            try {
                log.info("roleMenuMapper: {}", roleMenuMapper);
                log.info("roleId: {}", role.getId());

                // 直接查询数据库，避免调用RoleMenuService
                List<RoleMenu> roleMenus =
                    roleMenuMapper.selectList(new QueryWrapper<RoleMenu>()
                        .eq("role_id", role.getId()));

                log.info("查询结果 roleMenus: {}", roleMenus);

                for (RoleMenu roleMenu : roleMenus) {
                    menuIds.add(roleMenu.getMenuId());
                }

                log.info("直接查询获取到的菜单IDs: {}", menuIds);
            } catch (Exception e) {
                log.error("查询角色菜单关联表失败: {}", e.getMessage(), e);
                // 如果查询失败，使用空菜单列表
                log.info("返回空菜单列表继续处理");
            }

            RoleVO vo = new RoleVO();
            vo.setId(role.getId());
            vo.setName(role.getName());
            vo.setDesc(role.getDesc());
            vo.setSort(role.getSort());
            vo.setNum(0);
            // 恢复菜单权限查询
            vo.setMenusId(menuIds);
            vo.setCreateTime(formatTimestamp(role.getCreateTime()));
            vo.setUpdateTime(formatTimestamp(role.getUpdateTime()));

            log.info("角色详情组装完成: {}", vo);

            return vo;
        } catch (Exception e) {
            log.error("获取角色详情异常: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void createRole(RoleCreateRequest request) {
        // 检查角色名称是否已存在
        Role existingRole = roleMapper.selectOne(new QueryWrapper<Role>()
                .select("id", "name")
                .eq("name", request.getName().trim())
                .isNull("delete_time")
                .last("limit 1"));
        Assert.isNull(existingRole, "角色名称已存在!");
        
        Role role = new Role();
        role.setName(request.getName().trim());
        role.setDesc(request.getDesc());
        role.setSort(request.getSort());
        role.setCreateTime((int) (System.currentTimeMillis() / 1000));
        role.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        roleMapper.insert(role);
        
        // 保存角色菜单关系
        if (request.getMenuIds() != null && !request.getMenuIds().isEmpty()) {
            roleMenuService.batchSaveByMenuIds(role.getId(), request.getMenuIds());
        }
    }
    
    @Override
    @Transactional
    public void updateRole(RoleUpdateRequest request) {
        // 检查角色是否存在
        Role existingRole = roleMapper.selectOne(new QueryWrapper<Role>()
                .select("id", "name")
                .eq("id", request.getId())
                .isNull("delete_time")
                .last("limit 1"));
        Assert.notNull(existingRole, "角色已不存在!");
        
        // 检查角色名称是否已存在（排除当前角色）
        Role duplicateRole = roleMapper.selectOne(new QueryWrapper<Role>()
                .select("id", "name")
                .ne("id", request.getId())
                .eq("name", request.getName().trim())
                .isNull("delete_time")
                .last("limit 1"));
        Assert.isNull(duplicateRole, "角色名称已存在!");
        
        Role role = new Role();
        role.setId(request.getId());
        role.setName(request.getName().trim());
        role.setDesc(request.getDesc());
        role.setSort(request.getSort());
        role.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        roleMapper.updateById(role);
        
        // 省略 menuIds 表示只改角色资料；显式空数组才表示清空授权。
        if (request.getMenuIds() != null) {
            if (request.getMenuIds().isEmpty()) {
                roleMenuService.batchDeleteByRoleId(request.getId());
            } else {
                roleMenuService.batchSaveByMenuIds(request.getId(), request.getMenuIds());
            }
        }
    }
    
    @Override
    @Transactional
    public void deleteRole(Integer id) {
        // 检查角色是否存在
        Role role = roleMapper.selectOne(new QueryWrapper<Role>()
                .select("id", "name")
                .eq("id", id)
                .isNull("delete_time")
                .last("limit 1"));
        Assert.notNull(role, "角色已不存在!");
        
        // 检查角色是否被用户使用
        List<Long> userIds = userRoleService.getUserIdsByRoleId(id);
        Assert.isTrue(userIds == null || userIds.isEmpty(), "角色已被用户使用,请先移除");
        
        // 删除角色
        role.setDeleteTime((int) (System.currentTimeMillis() / 1000));
        roleMapper.updateById(role);
        
        // 删除角色菜单关系
        roleMenuService.batchDeleteByRoleId(id);
        
        // 删除用户角色关联
        userRoleService.deleteByRoleId(id);
    }
    
    @Override
    public List<String> getRoleNamesByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Long uid = Long.parseLong(userId.trim());
        List<String> names = roleMapper.selectRoleNamesByUserId(uid);
        return names != null ? names : new ArrayList<>();
    }
}
