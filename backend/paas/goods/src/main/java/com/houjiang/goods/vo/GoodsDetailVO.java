package com.houjiang.goods.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName GoodsDetailVO
 * @Description 商品信息
 * @Author Amos
 * @Date 2022/2/10 17:09
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsDetailVO {
    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品名
     */
    private String goodsName;
    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品对应的类目ID
     */
    private Integer goodsClassId;

    /**
     * 积分比例
     */
    private Double pointsRate;


    /**
     * 现金金额
     */
    private BigDecimal cashPrice;
}

