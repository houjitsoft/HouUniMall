package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.List;

public enum DistributionEnum {

    /** TODO 补充说明 */
    ORDER_STATUS_WAIT(0, "待结算"),
    /** TODO 补充说明 */
    ORDER_STATUS_SUCCESS(1, "已结算"),
    /** TODO 补充说明 */
    ORDER_STATUS_FAILED(2, "已失效"),

    /** TODO 补充说明 */
    ORDER_LEVEL_SELF(0, "自购佣金"),

    /** TODO 补充说明 */
    ORDER_LEVEL_ONE(1, "一级分佣"),

    /** TODO 补充说明 */
    ORDER_LEVEL_TWO(2, "二级分佣");


    public static Integer CONDITION_NONE = -1;

    public static Integer CONDITION_ANY = 0;

    public static Integer CONDITION_ALL = 1;

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    DistributionEnum(int code, String msg) {
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
        for(DistributionEnum enumItem: DistributionEnum.values()) {
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
        for(DistributionEnum enumItem: DistributionEnum.values()) {
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
        for(DistributionEnum enumItem: DistributionEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
    }

    public static String getLevelByCode(Integer code) {
        if (code != null) {
            if (code.intValue() == ORDER_LEVEL_SELF.getCode()) {
                return ORDER_LEVEL_SELF.getMsg();
            } else if (code.intValue() == ORDER_LEVEL_ONE.getCode()) {
                return ORDER_LEVEL_ONE.getMsg();
            } else if (code.intValue() == ORDER_LEVEL_TWO.getCode()) {
                return ORDER_LEVEL_TWO.getMsg();
            } else {
                return "";
            }
        }
        return "";
    }

    /**
     * 订单预估收益状态
     * @return
     */
    public static List<Integer> getOrderStatusCountArr() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(ORDER_STATUS_WAIT.getCode());
        list.add(ORDER_STATUS_SUCCESS.getCode());
        return list;
    }
}
