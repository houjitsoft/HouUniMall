package com.houjiang.goods.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class PcConfigGoodsVO {
    /**
     * 商品名
     */
    private String commonName;
    /**
     * 商品图片
     */
    private String commonImage;
    /**
     * 店铺名
     */
    private String shopName;
    /**
     * 评论数
     */
    private Integer commonEvaluate;
    /**
     * 商品价格
     */
    private BigDecimal commonPrice;
    /**
     * 市场价
     */
    private BigDecimal commonMarketPrice;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 货品id
     */
    private Integer goodsId;
    /**
     * 收藏量
     */
    private Integer commonCollect;
    /**
     * 销售量
     */
    private Integer commonSalenum;
    /**
     * 是否自营店铺
     */
    private String shopSelfSupport;
    /**
     * 是否plus商品
     */
    private Integer plusGoods;


}
