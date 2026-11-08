package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName SaleRoomVO
 * @Description 商家后台首页订单金额
 * @Author Amos
 * @Date 2023/6/25 14:10
 * @Version 1.0
 * @author Amos
 */
@Data
public class SaleRoomVO {

    /**
     * 订单数
     */
    private Integer OrderCount;
    /**
     * 订单金额
     */
    private BigDecimal OrderTotalPrice;
    /**
     * 销售额
     */
    private BigDecimal salesVolumePrice;


    public BigDecimal getOrderTotalPrice() {
        if(null != OrderTotalPrice) {
            return OrderTotalPrice;
        }else{
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getSalesVolumePrice() {
        if(null != salesVolumePrice) {
            return salesVolumePrice;
        }else{
            return BigDecimal.ZERO;
        }
    }
}
