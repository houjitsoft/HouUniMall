package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName OrderRecieveAddrVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 9:27
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderRecieveAddrVO {

    /**
     * 城镇名称
     */
    private String townName;
    /**
     * 收货地址
     */
    private String receiveAddr;
    /**
     * 收货邮政编码
     */
    private String receiveZipCode;
    /**
     * 城市ID
     */
    private Integer cityId;
    /**
     * 城镇ID
     */
    private Integer townId;
    /**
     * 省份ID
     */
    private Integer provinceId;
    /**
     * 地区ID
     */
    private Integer areaId;
    /**
     * 收货人姓名
     */
    private String receiveName;
    /**
     * 城市名称
     */
    private String cityName;
    /**
     * 地区名称
     */
    private String areaName;
    /**
     * 详细收货地址
     */
    private String receiveAddrDetail;
    /**
     * 地址ID
     */
    private String id;
    /**
     * 省份名称
     */
    private String provinceName;
    /**
     * 收货人手机号码
     */
    private String receiveMobile;
}
