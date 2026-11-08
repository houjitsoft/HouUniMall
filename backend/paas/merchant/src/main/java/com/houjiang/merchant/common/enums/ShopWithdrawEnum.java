package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 商家提现枚举
 */
public enum ShopWithdrawEnum {

    /** TODO 补充说明 */
    STATUS_APPLY(0, "待提现"),
    /** TODO 补充说明 */
    STATUS_DEAL(1, "提现中"),
    /** TODO 补充说明 */
    STATUS_SUCCESS(2, "提现成功"),
    /** TODO 补充说明 */
    STATUS_FAILED(3, "提现失败");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    ShopWithdrawEnum(int code, String msg) {
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


    public static List<Integer> getInWithdraw() {
        List<Integer> ret = new ArrayList<>();
        ret.add(STATUS_APPLY.getCode());
        ret.add(STATUS_DEAL.getCode());
        return ret;
    }


    /**
     * 根据类型获取Code
     *
     * @author Amos
     * @param type 类型
     * @return Integer
     */
    public static Integer getCodeByType(String type){
        for(ShopWithdrawEnum enumItem: ShopWithdrawEnum.values()) {
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
        for(ShopWithdrawEnum enumItem: ShopWithdrawEnum.values()) {
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
        for(ShopWithdrawEnum enumItem: ShopWithdrawEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
    }

}
