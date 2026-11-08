package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺
 * @author Amos
 */
@Data
public class ShippingInfoDTO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;

    /**
     * 运费模板
     */
    private Integer transportTemplate;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 规格
     */
    private String goodsSpec;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品市场价格
     */
    private BigDecimal goodsMarketPrice;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 运费
     */
    private BigDecimal goodsShippingFee;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 1-上架 2-下架 0店铺关闭手动下架
     */
    private Integer goodsIsShelves;

    /**
     * 最大购买数量
     */
    private Integer goodsMaxSale;

    /**
     * 商品库存
     */
    private Integer goodsStock;


}
