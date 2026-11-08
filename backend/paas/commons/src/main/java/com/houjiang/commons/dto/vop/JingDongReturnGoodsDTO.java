package com.houjiang.commons.dto.vop;

import lombok.Data;

/**
 * @ClassName JingDongReturnGoodsDTO
 * @Author Amos
 * @Date 2025/6/17 15:07
 * @Version 1.0
 * @author Amos
 */
@Data
public class JingDongReturnGoodsDTO {
    /**
     * 商城商品id
     */
    public Integer goodsId;
    /**
     * 退单商品数量
     */
    private Integer goodsNum;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 京东订单号  如果京东子订单号不会空 就赋值京东子订单号
     */
    private String jdOrderId;
    /**
     * 京东skuId
     */
    private String skuId;
    /**
     * 商城订单号
     */
    public String orderId;
    /**
     * 京东售后单号
     */
    public String jdReturenOrderId;
}
