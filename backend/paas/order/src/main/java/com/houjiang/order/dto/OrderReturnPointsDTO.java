package com.houjiang.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @ClassName OrderReturnPointsDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/26 9:49
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderReturnPointsDTO {

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 订单商品退货数量
     */
    private Integer orderGoodsReturnnum;

}
