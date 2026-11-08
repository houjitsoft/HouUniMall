package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ApplyReturnGoodsDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 10:33
 * @Version 1.0
 * @author Amos
 */
@Data
public class ApplyReturnGoodsDTO {

    /**
     * 商品数量，用于记录商品的数量信息
     */
    private Integer goodsQuantity;

    /**
     * 商品规格ID，用于唯一标识商品的规格
     */
    private String goodsSpecId;

    /**
     * 退款金额,售后类型为"部分退款"时必填,且最多支持2位小数
     */
    private BigDecimal refundAmount;
}
