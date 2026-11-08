package com.houjiang.goods.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName JingDongGoodsVO
 * @Author Amos
 * @Date 2025/5/28 16:31
 * @Version 1.0
 * @author Amos
 */
@Data
public class JingDongGoodsVO {
    /**
     * skuId
     */
    private String supplierGoodsId;

    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品主图
     */
    private String goodsImage;
    /**
     * 协议价
     */
    private BigDecimal agreementPrice;
    /**
     * 市场价
     */
    private BigDecimal marketPrice;
    /**
     * 利润率
     */
    private String profitMargin;

    /**
     * 是否已代发
     */
    private Boolean dropShippingStart = false;
    /**
     * 建议零售价
     */
    private BigDecimal msrp;
    /**
     * 商品数量
     */
    private Integer goodsNum;
}
