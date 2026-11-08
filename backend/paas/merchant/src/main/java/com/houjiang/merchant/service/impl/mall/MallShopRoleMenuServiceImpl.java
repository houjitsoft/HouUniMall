package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallShopRole;
import com.houjiang.merchant.common.entity.mall.MallShopRoleMenu;
import com.houjiang.merchant.common.mapper.mall.MallShopRoleMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopRoleMenuMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.service.mall.IMallShopRoleMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 商家角色权限实现类
 * @author Amos
 */
@Service
public class MallShopRoleMenuServiceImpl implements IMallShopRoleMenuService {
        
    @Resource
    MallShopRoleMenuMapper mallShopRoleMenuMapper;

    @Resource
    MallShopRoleMapper mallShopRoleMapper;


    @Override
    public List<Integer> selectMenuIdsByRoleId(List<Integer> roleIds) {
        List<Integer> menus = new LinkedList<>();

        if (roleIds.isEmpty()) {
            return menus;
        }

        MallShopRole mallShopRole = mallShopRoleMapper.selectOne(new QueryWrapper<MallShopRole>()
                .in("id", roleIds)
                .last("limit 1"));

        if (StringUtils.isNull(mallShopRole)) {
            return menus;
        }

        List<MallShopRoleMenu> mallShopRoleMenus = mallShopRoleMenuMapper.selectList(
                new QueryWrapper<MallShopRoleMenu>()
                        .in("role_id", roleIds));

        for (MallShopRoleMenu mallShopRoleMenu : mallShopRoleMenus) {
            menus.add(mallShopRoleMenu.getMenuId());
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
    public void batchSaveByMenuIds(Integer roleId, List<Integer> menuIds, Integer shopId) {
        if (menuIds != null && !menuIds.isEmpty()) {
            menuIds.forEach(item-> {
                MallShopRoleMenu model = new MallShopRoleMenu();
                model.setRoleId(roleId);
                model.setMenuId(item);
                model.setShopId(shopId);
                model.setCreateTime(System.currentTimeMillis() / 1000);
                model.setUpdateTime(System.currentTimeMillis() / 1000);
                model.setDeleteTime(0L);
                mallShopRoleMenuMapper.insert(model);
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
        mallShopRoleMenuMapper.delete(new QueryWrapper<MallShopRoleMenu>().eq("role_id", roleId));
    }

    /**
     * 批量删除角色菜单(根据菜单ID)
     *
     * @author Amos
     * @param menuId 菜单ID
     */
    @Override
    public void batchDeleteByMenuId(Integer menuId) {
        mallShopRoleMenuMapper.delete(new QueryWrapper<MallShopRoleMenu>().eq("menu_id", menuId));
    }
}
