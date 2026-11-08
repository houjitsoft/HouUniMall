package com.houjiang.merchant.service.mall;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.vo.mall.MallShopMenuVo;

import java.util.List;

/**
 * 系统菜单服务接口类
 */
public interface IMallShopMenuService {

    /**
     * 根据角色获取菜单
     *
     * @author Amos
     * @return JSONArray
     */
    JSONArray selectMenuByRoleId(List<Integer> roleId);

    /**
     * 菜单列表
     *
     * @author Amos
     * @return JSONArray
     */
    JSONArray list();

    /**
     * 菜单列表
     *
     * @author Amos
     * @return JSONArray
     */
    JSONArray systemMenuLists();

    /**
     * 菜单列表
     *
     * @author Amos
     * @return JSONArray
     */
    JSONArray all();

    /**
     * 菜单详情
     *
     * @author Amos
     * @param id 主键
     * @return SysMenu
     */
    MallShopMenuVo detail(Integer id);
}
