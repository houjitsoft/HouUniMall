package com.houjiang.commons.dto.vop;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单参数
 *
 * @author Amos
 */
@Data
public class OrderGoodsJdVopDTO {
    /**
     * 京东父订单号
     */
    private String parentJdOrderId;
    /**
     * 京东skuId
     */
    private String skuId;
    /**
     * 商城goodsId
     */
    private String goodsId;
    /**
     * 京东子订单号
     */
    private String jdOrderId;
    /**
     * 京东订单总计运费
     */
    private BigDecimal shippingFee;
}
