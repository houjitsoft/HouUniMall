package com.houjiang.order.dto;

import lombok.Data;

/**
 * 收件人参数
 * @author Amos
 */
@Data
public class ReceiverDTO {

    /**
     * 名称
     */
    private String Name;

    /**
     * 电话
     */
    private String Mobile;

    /**
     * 省份
     */
    private String ProvinceName;

    /**
     * 城市
     */
    private String CityName;

    /**
     * 区县名
     */
    private String ExpAreaName;

    /**
     * 收件人地址
     */
    private String Address;

}
