package com.houjiang.merchant.common.enums;


import java.util.ArrayList;
import java.util.List;

public enum OrderLogEnum {

    /** TODO 补充说明 */
    SYSTEM_CANCEL_ORDER(101, "系统取消订单"),
    /** TODO 补充说明 */
    SYSTEM_CONFIRM_ORDER(102, "系统确认订单"),
    /** TODO 补充说明 */
    ADMIN_CANCEL_ORDER(201, "管理员取消订单"),
    /** TODO 补充说明 */
    ADMIN_CONFIRM_ORDER(202, "管理员确认订单"),
    /** TODO 补充说明 */
    SHOP_CANCEL_ORDER(301, "商家取消订单"),
    /** TODO 补充说明 */
    SHOP_DELIVERY_ORDER(302, "商家发货"),
    /** TODO 补充说明 */
    SHOP_CONFIRM_ORDER(303, "商家确认收货"),
    /** TODO 补充说明 */
    SHOP_ADDRESS_EDIT(304, "商家修改地址"),
    /** TODO 补充说明 */
    SHOP_ORDER_REMARKS(305, "商家备注"),

    /** TODO 补充说明 */
    USER_ADD_ORDER(401, "提交订单"),
    /** TODO 补充说明 */
    USER_CANCEL_ORDER(402, "取消订单"),
    /** TODO 补充说明 */
    USER_CONFIRM_ORDER(403, "确认收货"),
    /** TODO 补充说明 */
    USER_PAID_ORDER(404, "支付订单");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    OrderLogEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取状态码
     *
     * @author Amos
     * @return Long
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 获取提示
     *
     * @author Amos
     * @return String
     */
    public String getMsg() {
        return this.msg;
    }


    /**
     * 根据类型获取Code
     *
     * @author Amos
     * @param type 类型
     * @return Integer
     */
    public static Integer getCodeByType(String type){
        for(OrderLogEnum enumItem: OrderLogEnum.values()) {
            if (enumItem.toString().equals(type.toUpperCase())) {
                return enumItem.getCode();
            }
        }
        return null;
    }

    /**
     * 根据类型获取Msg
     *
     * @author Amos
     * @param type 类型
     * @return String
     */
    public static String getMsgByType(String type){
        for(OrderLogEnum enumItem: OrderLogEnum.values()) {
            if (enumItem.toString().equals(type)) {
                return enumItem.getMsg();
            }
        }
        return null;
    }

    /**
     * 根据编码获取Msg
     *
     * @author Amos
     * @param code 类型
     * @return String
     */
    public static String getMsgByCode(Integer code){
        for(OrderLogEnum enumItem: OrderLogEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
    }
    /**
     * 获取变动类型
     *
     * @author Amos
     * @param code Integer
     * @return String
     */
    public static String getRecordDesc(Integer code) {
        for (OrderLogEnum value : OrderLogEnum.values()) {
            if (value.getCode() == code) {
                return value.getMsg();
            }
        }
        return "-";
    }
}
