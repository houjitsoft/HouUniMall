package com.houjiang.merchant.common.config;

import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局配置
 * @author Amos
 */
public class ProjectConfig {
    /** 列表页 */
    public static Map<String, Integer> lists = new HashMap<String, Integer>(){{
        put("pageSizeMax", 25000);
        put("pageSize", 25);
    }};


    public static JSONObject website = new JSONObject(){{
        // 网站名称
        put("name", "多商户Java版");
        // 平台登录标题
        put("admin_login_name", "多商户Java版");
        // 后台logo
        put("admin_logo", "resource/image/adminapi/website/admin_logo.png");
        // 网站图标 favicon.ico
        put("web_favicon", "resource/image/adminapi/website/web_favicon.ico");
        // 平台登录封面
        put("admin_login_logo", "resource/image/adminapi/website/admin_login_logo.png");
        // 移动端logo
        put("mobile_logo", "resource/image/adminapi/website/mobile_logo.png");
        // 平台联系人
        put("contact", "");
        // 平台联系手机
        put("mobile", "");
        // 网站地址
        put("url", "www.likeadmin.cn/");
        // 商家登录端标题
        put("shop_login_title", "多商户Java版 商家端");
        // 商家后台logo
        put("shop_admin_logo", "resource/image/adminapi/website/shop_admin_logo.png");
        // 浏览器图标
        put("shop_browser_icon", "resource/image/adminapi/website/web_favicon.ico");
        // 商家登录封面
        put("shop_login_logo", "resource/image/adminapi/website/shop_login_logo.png");
        // 商家默认logo
        put("shop_logo", "resource/image/adminapi/website/shop_logo.png");
        // 商家默认背景
        put("shop_background", "resource/image/adminapi/website/shop_background.png");
    }};

    public static JSONObject defaultImage = new JSONObject(){{
        put("admin_avatar", "resource/image/adminapi/default/avatar.png");
        put("user_avatar", "resource/image/adminapi/default/default_avatar.png");
        put("shop_background", "resource/image/adminapi/default/default_shop_face.png");
        // qq群
        put("qq_group", "resource/image/adminapi/default/qq_group.png");
        put("customer_service", "resource/image/adminapi/default/customer_service.jpg");
    }};
}
