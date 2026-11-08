package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * 店铺地址
 * @author Amos
 */
@Data
public class ShopShippingAddressVO {

    /**
     /**
     * 配送地址ID
     */
    private Integer shippingAddressId;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 配送地址联系人
     */
    private String shippingAddressContact;

    /**
     * 配送地址省份ID
     */
    private Integer shippingAddressProvinceId;

    /**
     * 配送地址城市ID
     */
    private Integer shippingAddressCityId;

    /**
     * 配送地址区域ID
     */
    private Integer shippingAddressAreaId;

    /**
     * 配送地址详细地址
     */
    private String shippingAddressAddress;

    /**
     * 配送地址电话
     */
    private String shippingAddressPhone;

    private String ShippingAddressProvince;

    private String ShippingAddressCity;

    private String ShippingAddressArea;


}
