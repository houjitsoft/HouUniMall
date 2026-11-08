package com.houjiang.order.dto;

import lombok.Data;

import java.util.List;

/**
 * 装运
 * @author Amos
 */
@Data
public class ShipmentsDTO {

    /**
     * token
     */
    private String token;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 发货信息id
     */
    private Integer shippingAddressId;

    /**
     * 商品详情集合
     */
    private List<ShipmentsGoodsDTO> goodsDetails;

}
