package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ManageShopAddressQueryVO {

    /**
     * 地址id
     */
    private Integer shippingAddressId;

    /**
     * 联系人
     */
    private String shippingAddressContact;

    /**
     * 省份ID
     */
    private Integer shippingAddressProvinceId;

    /**
     * 城市ID
     */
    private Integer shippingAddressCityId;

    /**
     * 区县ID
     */
    private Integer shippingAddressAreaId;

    /**
     * 所在地区-字符串组合
     */
    private String shippingAddressArea;

    /**
     * 街道地址-不必重复填写地区
     */
    private String shippingAddressAddress;

    /**
     * 联系电话
     */
    private String shippingAddressPhone;

    /**
     * 默认地址
     */
    private Integer shippingAddressDefault;
}
