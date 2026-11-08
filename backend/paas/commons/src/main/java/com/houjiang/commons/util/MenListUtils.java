package com.houjiang.commons.util;

import com.google.common.collect.Lists;
import com.houjiang.commons.vo.shop.ManageShopSelfCatMenuVO;
import com.houjiang.commons.vo.shop.ShopGoodsCatVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Amos
 */
public class MenListUtils {

    /**
     * 根据子类id查所有父级名称
     *
     * @param rootMenus
     * @param catId
     * @return
     */
    public static List<String> getMenuList2(List<ShopGoodsCatVO> rootMenus, Integer catId) {
        List<String> catNameList = Lists.newArrayList();
        for (ShopGoodsCatVO menu : rootMenus) {
            if (menu.getCatId().equals(catId)) {
                catNameList.add(menu.getCatName());
                catNameList = getChild2(menu.getCatParentId(), rootMenus, catNameList);
                break;
            }
        }
        return catNameList;
    }

    /**
     * 找出子菜单
     *
     * @param parentId
     * @param rootMenu
     * @return
     */
    private static List<String> getChild2(Integer parentId, List<ShopGoodsCatVO> rootMenu, List<String> catNameList) {
        // 子菜单
        for (ShopGoodsCatVO menu : rootMenu) {
            // 遍历所有节点，将父菜单id与传过来的id比较 1006081048
            if (!StringUtils.isEmpty(menu.getCatParentId().toString())) {
                if (menu.getCatId().equals(parentId)) {
                    catNameList.add(0, menu.getCatName());
                    if (!menu.getCatParentId().equals(0)) {
                        catNameList = getChild2(menu.getCatParentId(), rootMenu, catNameList);
                    }
                }
            }
        }
        return catNameList;
    }

    /**
     * 找出一级菜单和二级菜单
     *
     * @param rootMenus
     * @return
     */
    public static List<ManageShopSelfCatMenuVO> getMenuList(List<ManageShopSelfCatMenuVO> rootMenus) {
        List<ManageShopSelfCatMenuVO> list = new ArrayList<ManageShopSelfCatMenuVO>();
        // 先找到所有的一级菜单
        for (int i = 0; i < rootMenus.size(); i++) {
            ManageShopSelfCatMenuVO menu = rootMenus.get(i);
            // 一级菜单 没有父节点
            if (menu.getParentId() == 0) {
                list.add(menu);
            }
        }
        /**
         * 利用递归找出所有子菜单
         */
        for (ManageShopSelfCatMenuVO menu : list) {
            menu.setChildren(getChild(menu.getShopGoodsCatId(), rootMenus));
        }
        return list;

    }

    /**
     * 找出子菜单
     *
     * @param parentId
     * @param rootMenu
     * @return
     */
    private static List<ManageShopSelfCatMenuVO> getChild(Integer parentId, List<ManageShopSelfCatMenuVO> rootMenu) {
        // 子菜单
        List<ManageShopSelfCatMenuVO> childList = new ArrayList<ManageShopSelfCatMenuVO>();

        for (ManageShopSelfCatMenuVO menu : rootMenu) {
            // 遍历所有节点，将父菜单id与传过来的id比较
            if (!StringUtils.isEmpty(menu.getParentId().toString())) {
                if (menu.getParentId().equals(parentId)) {
                    childList.add(menu);
                }
            }
        }
        // 递归退出条件
        if (childList.size() == 0) {
            return null;
        }
        // 把子菜单的子菜单再循环一遍
        for (ManageShopSelfCatMenuVO menu : childList) {
            // 递归
            menu.setChildren(getChild(menu.getShopGoodsCatId(), rootMenu));
        }
        return childList;
    }

    /**
     * 根据子类id查所有父级名称
     *
     * @param rootMenus
     * @param catId
     * @return
     */
    public static List<String> getMenuList4(List<ShopGoodsCatVO> rootMenus, Integer catId) {
        //分类名称
        List<String> catNameList = Lists.newArrayList();
        for (ShopGoodsCatVO menu : rootMenus) {
            //是否是传过来的分类id
            if (menu.getCatId().equals(catId)) {
                catNameList.add(menu.getCatName());
                catNameList = getChild4(menu.getCatParentId(), rootMenus, catNameList);
                break;
            }
        }
        return catNameList;
    }

    /**
     * 找出子菜单
     *
     * @param parentId
     * @param rootMenu
     * @param catNameList
     * @return
     */
    public static List<String> getChild4(Integer parentId, List<ShopGoodsCatVO> rootMenu, List<String> catNameList) {
        // 子菜单
        for (ShopGoodsCatVO menu : rootMenu) {
            // 遍历所有节点，将父菜单id与传过来的id比较
            if (menu.getCatParentId() != null && (menu.getCatId().equals(parentId))) {
                catNameList.add(0, menu.getCatName());
                if (!menu.getCatParentId().equals(0)) {
                    catNameList = getChild4(menu.getCatParentId(), rootMenu, catNameList);
                }
            }
        }
        return catNameList;
    }

}
