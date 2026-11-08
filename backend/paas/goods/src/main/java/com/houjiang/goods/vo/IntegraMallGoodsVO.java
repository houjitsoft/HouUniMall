package com.houjiang.goods.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品
 * @author Amos
 */
@Data
public class IntegraMallGoodsVO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品所需积分
     */
    private Integer needIntegral;

    /**
     * 商品所需价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 已售数量
     */
    private Integer goodsSalenum;

    /**
     * 1-上架 2-下架 0店铺关闭手动下架
     */
    private Integer goodsIsShelves;

    /**
     * 是否推荐
     */
    private Integer goodsIsRecommend;

    /**
     * 是否是积分商品 0是 1否
     */
    private Integer integralGoods;
}

