package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

/**
 * @ClassName UpdateOrderReceiveAddressDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 17:03
 * @Version 1.0
 * @author Amos
 */
@Data
public class UpdateOrderReceiveAddressDTO {

    /**
     * 发货手机号码
     * 用于存储订单的发货手机号码信息
     */
    private String shipMobile;

    /**
     * 订单编号
     * 用于唯一标识一个订单
     */
    private String orderSn;

    /**
     * 发货人姓名
     * 用于存储订单的发货人姓名信息
     */
    private String shipName;

    /**
     * 发货地址邮编
     * 用于存储订单的发货地址邮政编码信息
     */
    private String shipZipCode;

    /**
     * 发货详细地址
     * 用于存储订单的详细发货地址信息
     */
    private String shipAddress;

    /**
     * 发货地区编码
     * 用于存储订单的发货地区编码信息
     */
    private String shipAreaCode;




}
