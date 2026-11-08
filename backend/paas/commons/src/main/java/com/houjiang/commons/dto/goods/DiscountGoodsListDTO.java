package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName DiscountGoodsListDTO
 * @Description 商品信息
 * @Author Amos
 * @Date 2023/7/3 16:10
 * @Version 1.0
 * @author Amos
 */
@Data
public class DiscountGoodsListDTO {
    /**
     * 商品编号
     */
    private Integer goodsId;
    /**
     * 店铺编号
     */
    private Integer shopId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品原价
     */
    private BigDecimal goodsPrice;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 限时折扣价格
     */
    private BigDecimal discountPrice;
    /**
     * 折扣
     */
    private BigDecimal discountAmount;
}
