package com.houjiang.commons.vo.shop;


import com.houjiang.commons.vo.goods.CommonAndGoodsVO;
import lombok.Data;

/**
 * 店铺
 * @author Amos
 */
@Data
public class ShopSlideUrlVO {

    /**
     * 轮播图对应地址
     */
    private String shopSlideUrl;

    /**
     * 1新品，2收藏量3销售量4价格
     * 2商品编号{"goodsId":5026,"commonId":5026}，
     * 3链接{"url":http://},4店铺{"shopId":11}
     * 6分类id {\"catId\":10000104}
     * 7不跳转8活动页
     */
    private Integer opType;

    /**
     * 商品查询条件对象
     */
    private CommonAndGoodsVO commonIdAndGoodsId;

    /**
     * 活动页
     */
    private String activityPage;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 地址
     */
    private String url;

}
