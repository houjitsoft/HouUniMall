package com.houjiang.commons.util.excel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * @author Amos
 */
@Component
@RefreshScope
public class Constants {

    /**
     * update接口类型，1：新增， 2：更新
     */
    public static final int ADD_TYPE = 1;
    public static final int UPDATE_TYPE = 2;
    /**
     * 区域
     * 查询所有的区域
     */
    public static final String CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE = "DISTRICTS_ALL_BASE_QUERY";
    /** 查询所有的区域 */
    public static final String CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE2 = "DISTRICTS_ALL_BASE_QUERY2";

    /** 查询所有的区域,根据子分类划分好 */
    public static final String CACHE_DISTRICTS_PLATFORM_ALL_AND_CHILD = "DISTRICTS_CACHE_PLATFORM_ALL_AND_CHILD";
    /**
     * 分类
     * 商家后台和店铺个人入驻查询所有分类,根据子类分好
     */
    public static final String CACHE_GOODS_CAT_MANAGE_ALL = "GOODS_CAT_ALL_QUERY";
    /** 平台后台查询商品分类 */
    public static final String CACHE_PLATFORM_GOODS_ALL_CARS_QUERY = "GOODS_CAT_PLATFORM_ALL_QUERY";
    /** WEB商品列表页根据分类id查询子类 */
    public static final String CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX = "GOODS_CAT_WEB_GOODS_LIST_FIRST_CACHE_KEY";
    /** WAP商品列表页根据分类id查询子类 */
    public static final String CACHE_GOODS_WAP_GOODS_LIST_PREFIX = "GOODS_CAT_WAP_GOODS_LIST_INDEX_FIRST_CACHE_KEY";
    // app
    // app和wap端首页信息缓存
    public final static String MALLMS_APP_INDEX_MESSAGE = "mallms-app-index-message";
    public final static String DATA_ERROR = "数据操作异常";
    /**
     * 分销佣金设置
     */
    public static final String FX_GOODS_SET = "FX_GOODS_SET";
    /**
     * 首页轮播缓存
     */
    public static final String APP_TOP_SLISHOW = "app_top_slishow";
    /**
     * 首页导航
     */
    public static final String CAT_LEAD = "cat_lead";
    /**
     * 域名
     */
    public static String DOMAIN_NAME_URL;
    /**
     * 导出路径
     * 字体路径
     */
    public static String FONT_URL;
    /** 平台后台项目路径 */
    public static String PLATFORM_URL = null;
    /** 商家后台项目路径 */
    public static String MALLMS_URL = null;
    /** 商家后台项目路径 */
    public static String ORDER_URL = null;
    @Value("${domainName}")
    public String domainName;
    @Value("${fontUrlPath}")
    public String fontUrlPath;

    @PostConstruct
    public String getDomainNameUrl() {
        return DOMAIN_NAME_URL = this.domainName;
    }

    @PostConstruct
    public String getFontUrl() {
        return FONT_URL = this.fontUrlPath;
    }

    @PostConstruct
    public String getShoppingOrderUrl() {
        return ORDER_URL = this.domainName + "/ShoppingOrder/";
    }

    @PostConstruct
    public String getPlatformUrl() {
        return PLATFORM_URL = this.domainName + "/mallplatformmanager/";
    }

    @PostConstruct
    public String getMallmsUrl() {
        return MALLMS_URL = this.domainName + "/mallms/";
    }
}
