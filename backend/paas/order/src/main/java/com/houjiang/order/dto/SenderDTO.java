package com.houjiang.order.dto;

import lombok.Data;

/**
 * 发件人参数
 * @author Amos
 */
@Data
public class SenderDTO {

    /**
     * 收货姓名
     */
    private String Name;

    /**
     * 手机号
     */
    private String Mobile;

    /**
     * 省名
     */
    private String ProvinceName;

    /**
     * 市名
     */
    private String CityName;

    /**
     * 区县名
     */
    private String ExpAreaName;

    /**
     * 详细地址
     */
    private String Address;

}
