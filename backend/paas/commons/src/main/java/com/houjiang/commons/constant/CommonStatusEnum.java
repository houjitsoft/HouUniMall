package com.houjiang.commons.constant;

import lombok.Getter;

/**
 * @author Amos
 */
public enum CommonStatusEnum {

    /**
     * 验证码错误提示：1000-1099
     */
    VERIFICATION_CODE_ERROR(1099,"验证码不正确"),

    /** 验证码已过期，请重新获取！ */
    VERIFICATION_CODE_OVERDUE(1098,"验证码已过期，请重新获取！"),

    /** 验证码为空，请输入验证码！ */
    VERIFICATION_CODE_NULL(1097,"验证码为空，请输入验证码！"),

    /**
     * Token类提示：1100-1199
     */
    TOKEN_ERROR(1199,"token错误"),

    /**
     * 用户提示：1200-1299
     */
    USER_NOT_EXISTS(1200,"当前用户不存在"),



    /**
     * 订单：1600-1699
     */
    ORDER_GOING_ON(1600,"有正在进行的订单"),

    /**
     * 下单异常
     */
    DEVICE_IS_BLACK(1601,"该设备超过下单次数"),


    /** 订单取消失败 */
    ORDER_CANCEL_ERROR(1603, "订单取消失败"),


    /**
     * 商家：2000-2099
     */
    SHOP_JOIN_ID_ERROR(2000, "joinId生成失败"),

    /** 店铺类型为空！ */
    SHOP_TYPE_NOT_EXISTS(2001, "店铺类型为空！"),

    /** 热销商品 */
    HOT_COMMODITIES(5, "热销商品"),

    /**
     * 操作：2401-2499
     */
    OPERATE_ERROR(2401,"请勿重复提交！"),

    /**
     * 成功
     */
    SUCCESS(1,"操作成功"),
    /**
     * 失败
     */
    FAIL(0,"操作失败")

    ;
    @Getter
    private int code;
    @Getter
    private String value;

    CommonStatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }
}
