package com.houjiang.order.dto;

import lombok.Data;

/**
 * 商品
 * @author Amos
 */
@Data
public class ShipmentsGoodsDTO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 发货人名称
     */
    private String orderSellerName;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 发货人地址
     */
    private String orderSellerAddress;

    /**
     * 发货人手机号
     */
    private String orderSellerContact;

    /**
     * 发货地址id
     */
    private Integer shippingAddressId;

    /**
     * 商家备注
     */
    private String orderShippingMessage;

    /**
     * 配送公司id
     */
    private Integer orderShippingExpressId;
}
