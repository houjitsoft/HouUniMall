package com.houjiang.goods.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class ManageGoodsBaseListVO {

    /**
     * 商品公共表id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;


    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 市场价
     */
    private BigDecimal goodsMarketPrice;

    /**
     * 门店库存总和最大值
     */
    private Integer shopGateStockMax;


    /**
     * 商品主图
     */
    private String goodsImage;

    /**
     * 门店当前库存
     */
    private Integer shopGateGoodsStock;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;

    /**
     * 商品点击数量
     */
    private Integer goodsClick;

    /**
     * 销售数量
     */
    private Integer goodsSalenum;

    /**
     * 收藏数量
     */
    private Integer goodsCollect;

    private String token;

    /**
     * plu码
     */
    private String plu;

    /**
     * 仓库商品库存
     */
    private Integer storageGoodsStock;
}
