package com.houjiang.goods.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ManageGoodsBaseListResultDTO
 * @Author Amos
 * @Date 2023/6/26 14:06
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGoodsBaseListResultDTO {
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

    /**
     *
     */
    private String token;

    /**
     * 商品id
     */
    private List<Integer> goodsIdList;

    /**
     * 商品库存
     */
    private List<Integer> goodsStockList;

}
