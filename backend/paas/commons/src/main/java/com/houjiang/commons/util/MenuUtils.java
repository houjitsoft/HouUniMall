package com.houjiang.commons.util;

import com.google.common.collect.Lists;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.commons.vo.shop.ManageShopSelfCatMenuVO;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Amos
 */
public class MenuUtils {


    /**
     * 获取商品分类所有的子分类
     *
     * @param allMenu
     * @param catId
     * @return
     */
    public static List<Integer> getMenuList(List<WebCatMenuVO> allMenu, Integer catId) {
        List<Integer> allChildCatIds = Lists.newArrayList();

        if (!CollectionUtils.isEmpty(allMenu)) {
            for (WebCatMenuVO catMenu : allMenu) {
                if (catMenu.getCatId().equals(catId)) {
                    allChildCatIds.add(catId);
                    getChild(allMenu, catMenu.getCatId(), allChildCatIds);
                }

            }
        }
        return allChildCatIds;

    }


    /**
     * 获取商品分类所有的子分类
     *
     * @param allMenu
     * @param catId
     * @return
     */
    public static Integer getMenuList3(List<WebCatMenuVO> allMenu, Integer catId) {
        Integer rootCatId = null;

        if (!CollectionUtils.isEmpty(allMenu)) {
            for (WebCatMenuVO catMenu : allMenu) {
                if (catMenu.getCatId().equals(catId)) {
                    if (catMenu.getCatParentId().equals(0)) {
                        rootCatId = catId;
                    } else {
                        rootCatId = getChild3(allMenu, catMenu);
                    }

                }

            }
        }
        return rootCatId;

    }

    /**
     * 找出子菜单
     *
     * @param
     * @param
     * @return
     */
    private static Integer getChild3(List<WebCatMenuVO> allMenu, WebCatMenuVO catParentMenu) {
        Boolean flag = false;
        Integer rootCatId = null;
        for (WebCatMenuVO catMenu : allMenu) {
            if (catMenu.getCatId().equals(catParentMenu.getCatParentId())) {
                if (catMenu.getCatParentId().equals(0)) {
                    rootCatId = catMenu.getCatId();
                    break;
                } else {
                    catParentMenu = catMenu;
                    flag = true;
                    break;
                }
            }
        }

        if (flag) {
            rootCatId = getChild3(allMenu, catParentMenu);
        }

        return rootCatId;
    }


    /**
     * 找出子菜单
     *
     * @param
     * @param
     * @return
     */
    private static List<Integer> getChild(List<WebCatMenuVO> allMenu, Integer parentId, List<Integer> allChildCatIds) {

        List<Integer> childList = Lists.newArrayList();
        for (WebCatMenuVO catMenu : allMenu) {
            if (catMenu.getCatParentId().equals(parentId)) {
                allChildCatIds.add(catMenu.getCatId());
                childList.add(catMenu.getCatId());
                getChild(allMenu, catMenu.getCatId(), allChildCatIds);
            }
        }

        if (CollectionUtils.isEmpty(childList)) {
            return null;
        }

        return allChildCatIds;
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

        // 查找二级菜单
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
            if (!StringUtils.isEmpty(menu.getParentId())) {
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
     * 获取商品分类所有的子分类
     *
     * @param allMenu
     * @param catId
     * @return
     */
    public static List<WebCatMenuVO> getMenuList4(List<WebCatMenuVO> allMenu, Integer catId) {
        List<WebCatMenuVO> goodsCatsList = Lists.newArrayList();

        if (!CollectionUtils.isEmpty(allMenu)) {
            for (WebCatMenuVO catMenu : allMenu) {
                if (catMenu.getCatId().equals(catId)) {
                    goodsCatsList.add(catMenu);
                    if (!catMenu.getCatParentId().equals(0)) {
                        goodsCatsList = getChild4(allMenu, catMenu.getCatParentId(), goodsCatsList);
                    }
                    break;
                }

            }
        }
        return goodsCatsList;

    }

    /**
     * 找出子菜单
     *
     * @param
     * @param
     * @return
     */
    private static List<WebCatMenuVO> getChild4(List<WebCatMenuVO> allMenu, Integer parentId, List<WebCatMenuVO> goodsCatsList) {

        for (WebCatMenuVO catMenu : allMenu) {
            if (catMenu.getCatId().equals(parentId)) {
                goodsCatsList.add(0, catMenu);
                parentId = catMenu.getCatParentId();
                if (!parentId.equals(0)) {
                    goodsCatsList = getChild4(allMenu, parentId, goodsCatsList);
                }
            }
        }

        return goodsCatsList;
    }
}
