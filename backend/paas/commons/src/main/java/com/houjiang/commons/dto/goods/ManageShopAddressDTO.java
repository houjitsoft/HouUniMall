package com.houjiang.commons.dto.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 发货地址
 *
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManageShopAddressDTO extends UserTokenBeanDTO {

    /**
     * 发货地址id
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
     * 默认地址1默认0不是默认
     */
    private Integer shippingAddressDefault;

    /**
     * 时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shippingAddressTime;

}
