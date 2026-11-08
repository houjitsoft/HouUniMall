package com.houjiang.goods.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class PromotionGoodsListVO {
    /**
     * 自增
     */
    private Integer commonId;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 第一个商品的规格及规格名称
     */
    private Integer goodsId;
    /**
     * 商品价格
     */
    private BigDecimal commonPrice;
    /**
     * 商品主图
     */
    private String commonImage;
    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣'
     */
    private Integer commonIsXian;
    /**
     * 商品库存
     */
    private Integer commonStock;
    /**
     * 店铺id
     */
    private Integer shopId;
}
