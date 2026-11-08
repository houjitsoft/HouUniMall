package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 财务结算枚举
 */
public enum SettlementEnum {

    /** TODO 补充说明 */
    TYPE_ORDER(1, "订单收入"),
    /** TODO 补充说明 */
    TYPE_ORDER_AFTER_SALE(2, "订单售后退款");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    SettlementEnum(int code, String msg) {
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
        for(SettlementEnum enumItem: SettlementEnum.values()) {
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
        for(SettlementEnum enumItem: SettlementEnum.values()) {
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
        for(SettlementEnum enumItem: SettlementEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
    }
}
