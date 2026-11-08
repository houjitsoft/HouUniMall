package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.ShopMenu;
import com.houjiang.mall.mapper.shop.ShopMenuMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商家菜单权限服务层
 *
 * @author Amos
 */
@Service
public class ShopMenuService extends ServiceImpl<ShopMenuMapper, ShopMenu> {

    /**
     * 获取菜单树形列表（全量，含禁用）
     *
     * @return 树形菜单列表
     */
    public List<ShopMenu> treeList() {
        List<ShopMenu> all = this.list(new LambdaQueryWrapper<ShopMenu>()
                .orderByAsc(ShopMenu::getSort));
        return buildTree(all, 0L);
    }

    /**
     * 获取启用的菜单树形列表（用于下拉选择父级）
     *
     * @return 启用的树形菜单列表
     */
    public List<ShopMenu> treeListEnabled() {
        List<ShopMenu> all = this.list(new LambdaQueryWrapper<ShopMenu>()
                .eq(ShopMenu::getStatus, 1)
                .orderByAsc(ShopMenu::getSort));
        return buildTree(all, 0L);
    }

    /**
     * 递归构建树形结构
     *
     * @param all 全量菜单列表
     * @param pid 父级ID
     * @return 树形菜单列表
     */
    private List<ShopMenu> buildTree(List<ShopMenu> all, Long pid) {
        return all.stream()
                .filter(m -> pid.equals(m.getPid()))
                .peek(m -> m.setChildren(buildTree(all, m.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 更新菜单状态
     *
     * @param id     菜单ID
     * @param status 新状态: 0-禁用 1-启用
     * @return 是否成功
     */
    public boolean updateStatus(Long id, Integer status) {
        ShopMenu menu = this.getById(id);
        if (menu == null) return false;
        menu.setStatus(status);
        return this.updateById(menu);
    }
}
