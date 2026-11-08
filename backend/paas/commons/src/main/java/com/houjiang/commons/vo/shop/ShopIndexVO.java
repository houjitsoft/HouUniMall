package com.houjiang.commons.vo.shop;



import lombok.Data;

import java.util.List;

/**
 * 店铺
 * @author Amos
 */
@Data
public class ShopIndexVO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺粉丝数
     */
    private Integer shopCollect;

    /**
     * 轮播图
     */
    private String shopSlide;

    /**
     * 轮播图对应地址
     */
    private String shopSlideurl;

    /**
     * 移动端轮播图
     */
    private String mobileShopSlide;

    /**
     * 移动端轮播图对应地址
     */
    private String mobileShopSlideurl;

    /**
     * 店铺logo
     */
    private String shopLogo;

    /**
     * 店招
     */
    private String shopImage;

    /**
     * 店铺logo
     */
    private String wapShopLogo;

    /**
     * 查询返回对象
     */
    private List<ListQueryResultVO> gdsList;

    /**
     * 店铺数量
     */
    private Integer shopSumNum;

    /**
     * 店铺所在地址
     */
    private String shopCompanyAddress;

    /**
     * 详细地址
     */
    private String companyAddressDetail;

    /**
     * 是否已关注 0已关注1否
     */
    private Integer isEnshrine;

    /**
     * 移动店铺信息数组
     */
    private String[] mobileShopSlideArr;

    /**
     * 移动店铺信息返回集合
     */
    private List<ShopSlideUrlVO> mobileShopSlideurlList;

    /**
     * 卖家电话
     */
    private String shopContact;

    /**
     * 店铺信息数组
     */
    private String[] shopSlideArr;

    /**
     * 店铺信息返回集合
     */
    private List<ShopSlideUrlVO> shopSlideurlList;

    /**
     * 店铺所在地-省
     */
    private Integer shopProvince;
    /**
     * 店铺所在地-市
     */
    private Integer shopCity;
    /**
     * 店铺所在地-区
     */
    private Integer shopArea;
    /**
     * 店铺所在地-详细地址
     */
    private String shopAddress;

    /**
     * 店铺类型
     */
    private Integer shopType;
}
