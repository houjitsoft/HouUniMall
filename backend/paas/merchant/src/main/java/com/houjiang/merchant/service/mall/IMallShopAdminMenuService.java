package com.houjiang.merchant.service.mall;

import com.alibaba.fastjson2.JSONArray;

import java.util.List;

/**
 *  商家账号接口类
 * @author Amos
 */
public interface IMallShopAdminMenuService {

    /**
     * 获取用户可用的菜单
     * @param adminId
     * @return
     */
    JSONArray getShopAdminMenuList(Integer adminId);

    /**
     * 返回权限key
     * @param adminId
     * @return
     */
    List<String> getAdminAccessList(Integer adminId);
}
