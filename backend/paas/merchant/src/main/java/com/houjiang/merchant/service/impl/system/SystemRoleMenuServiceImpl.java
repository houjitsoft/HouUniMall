package com.houjiang.merchant.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.service.system.ISystemRoleMenuService;
import com.houjiang.merchant.common.entity.system.SystemRole;
import com.houjiang.merchant.common.entity.system.SystemRoleMenu;
import com.houjiang.merchant.common.mapper.system.SystemRoleMapper;
import com.houjiang.merchant.common.mapper.system.SystemRoleMenuMapper;
import com.houjiang.merchant.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 角色菜单关系实现类
 */
@Service
public class SystemRoleMenuServiceImpl implements ISystemRoleMenuService {


    @Resource
    SystemRoleMenuMapper systemRoleMenuMapper;

    @Resource
    SystemRoleMapper systemAuthRoleMapper;

    /**
     * 根据角色ID获取菜单ID
     *
     * @param roleIds 角色ID
     * @return List<Integer>
     */
    @Override
    public List<Integer> selectMenuIdsByRoleId(List<Integer> roleIds) {
        List<Integer> menus = new LinkedList<>();

        if (roleIds.isEmpty()) {
            return menus;
        }

        SystemRole systemAuthRole = systemAuthRoleMapper.selectOne(new QueryWrapper<SystemRole>()
                .in("id", roleIds)
//                .eq("is_disable", 0)
                .last("limit 1"));

        if (StringUtils.isNull(systemAuthRole)) {
            return menus;
        }

        List<SystemRoleMenu> systemRoleMenus = systemRoleMenuMapper.selectList(
                new QueryWrapper<SystemRoleMenu>()
                        .in("role_id", roleIds));

        for (SystemRoleMenu systemRoleMenu : systemRoleMenus) {
            menus.add(systemRoleMenu.getMenuId());
        }

        return menus;
    }

    /**
     * 批量写入角色菜单
     *
     * @author Amos
     * @param roleId 角色ID
     * @param menuIds 菜单ID组
     */
    @Override
    @Transactional
    public void batchSaveByMenuIds(Integer roleId, List<Integer> menuIds) {
        if (menuIds != null && !menuIds.isEmpty()) {
            menuIds.forEach(item-> {
                SystemRoleMenu model = new SystemRoleMenu();
                model.setRoleId(roleId);
                model.setMenuId(item);
                systemRoleMenuMapper.insert(model);
            });
        }
    }

    /**
     * 批量删除角色菜单(根据角色ID)
     *
     * @author Amos
     * @param roleId 角色ID
     */
    @Override
    public void batchDeleteByRoleId(Integer roleId) {
        systemRoleMenuMapper.delete(new QueryWrapper<SystemRoleMenu>().eq("role_id", roleId));
    }

    /**
     * 批量删除角色菜单(根据菜单ID)
     *
     * @author Amos
     * @param menuId 菜单ID
     */
    @Override
    public void batchDeleteByMenuId(Integer menuId) {
        systemRoleMenuMapper.delete(new QueryWrapper<SystemRoleMenu>().eq("menu_id", menuId));
    }
}
