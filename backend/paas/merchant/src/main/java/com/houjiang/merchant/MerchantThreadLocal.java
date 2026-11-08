package com.houjiang.merchant;

import com.houjiang.merchant.common.util.ListUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 商户模块本地线程上下文
 *
 * @author Amos
 */
public class MerchantThreadLocal {

    /**
     * 构造方法
     */
    public MerchantThreadLocal() {}

    /**
     * 取得本地线程对象
     */
    private static final java.lang.ThreadLocal<Map<String, Object>> MY_LOCAL = new java.lang.ThreadLocal<>();

    /**
     * 写入本地线程
     */
    public static void put(String key, Object val) {
        Map<String, Object> map = MY_LOCAL.get();
        if (map == null) {
            synchronized (MY_LOCAL) {
                map = new ConcurrentSkipListMap<>();
            }
        }
        map.put(key, val);
        MY_LOCAL.set(map);
    }

    /**
     * 获取本地线程
     */
    public static Object get(String key) {
        Map<String, Object> map = MY_LOCAL.get();
        if (map == null) {
            return "";
        }
        return map.getOrDefault(key, "");
    }

    /**
     * 获取管理员ID
     */
    public static Integer getAdminId() {
        String adminId = MerchantThreadLocal.get("adminId").toString();
        if (adminId.equals("")) {
            return 0;
        }
        return Integer.parseInt(adminId);
    }

    /**
     * 获取角色ID
     */
    public static List<Integer> getRoleIds() {
        String roleIds = MerchantThreadLocal.get("roleIds").toString();
        if (roleIds.equals("") || roleIds.equals("0")) {
            return Collections.emptyList();
        }
        return ListUtils.stringToListAsInt(roleIds, ",");
    }

    /**
     * 获取管理店铺ID
     */
    public static Integer getShopId() {
        String shopId = MerchantThreadLocal.get("shopId").toString();
        if (shopId.equals("")) {
            return 0;
        }
        return Integer.parseInt(shopId);
    }

    /**
     * 超级管理员 1是 0否
     */
    public static Integer getIsSuper() {
        String isSuper = MerchantThreadLocal.get("isSuper").toString();
        if (isSuper.equals("")) {
            return 0;
        }
        return Integer.parseInt(isSuper);
    }

    /**
     * 获取店铺名称
     */
    public static String getShopName() {
        String shopName = MerchantThreadLocal.get("shopName").toString();
        return shopName;
    }

    /**
     * 删除本地线程
     */
    public static void remove() {
        MY_LOCAL.remove();
    }

}
