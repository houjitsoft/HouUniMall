package com.houjiang.base.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName GoodsIdsQueryListResult
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:52
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsIdsQueryListVO {

    /**
     * 商品公共id
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
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 市场价
     */
    private BigDecimal goodsMarketPrice;

    /**
     * 商品库存
     */
    private Integer goodsStock;
}
