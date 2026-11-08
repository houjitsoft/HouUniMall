package com.houjiang.order.dto;

import lombok.Data;

/**
 * 商户地址
 * @author Amos
 */
@Data
public class MerchantsAddressDTO {

    /**
     * 所在省份-字符串组合
     */
    private String shippingAddressProvince;

    /**
     * 所在城市-字符串组合
     */
    private String shippingAddressCity;

    /**
     * 所在地区-字符串组合
     */
    private String shippingAddressArea;

}
