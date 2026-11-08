package com.houjiang.userms.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class UserAddressVO {

    /**
     *用户地址id
     */
    private Integer userAddressId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 所属联系地址
     */
    private String userAddressContact;

    /**
     *所属地址区id
     */
    private Integer userAddressProvinceId;

    /**
     *所属地址区名
     */
    private String userAddressProvinceName;

    /**
     *所属地址城市id
     */
    private Integer userAddressCityId;

    /**
     *所属地址城市名
     */
    private String userAddressCityName;

    /**
     *所属地址区id
     */
    private Integer userAddressAreaId;

    /**
     *所属地址区名2
     */
    private String userAddressAreaName;

    /**
     *所在地区-字符串组合
     */
    private String userAddressArea;

    /**
     *街道地址-不必重复填写地区
     */
    private String userAddressAddress;

    /**
     *联系电话
     */
    private String userAddressPhone;

    /**
     *默认地址0不是1是
     */
    private Integer userAddressDefault;

    /**
     *
     */
    private Date userAddressTime;
    /**
     * 国际地址 1 是 2 否
     */
    private Integer internationalType;
    /**
     * 国家id
     */
    private Integer countryId;
    /**
     * 国家名称
     */
    private String countryName;
    /**
     * 国际区号
     */
    private String internationalDialingCode;

}
