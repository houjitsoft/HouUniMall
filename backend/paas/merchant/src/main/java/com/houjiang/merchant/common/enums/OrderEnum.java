package com.houjiang.merchant.common.enums;


import java.util.ArrayList;
import java.util.List;

public enum OrderEnum {

    /** TODO 补充说明 */
    STATUS_WAIT_PAY(0, "待付款"),
    /** TODO 补充说明 */
    STATUS_WAIT_SEND(1, "待发货"),
    /** TODO 补充说明 */
    STATUS_WAIT_RECEIVE(2, "待收货"),
    /** TODO 补充说明 */
    STATUS_FINISH(3, "已完成"),
    /** TODO 补充说明 */
    STATUS_CANCEL(4, "已取消"),
    /** TODO 补充说明 */
    ADD_TYPE_NORMAL(0, "普通购物车提交"),
    /** TODO 补充说明 */
    ADD_TYPE_BUY_NOW(1, "立即购买"),
    /** TODO 补充说明 */
    ADD_TYPE_MS(2, "秒杀"),
    /** TODO 补充说明 */
    ADD_TYPE_PT(3, "拼团"),
    /** TODO 补充说明 */
    TYPE_MS(10, "秒杀"),
    /** TODO 补充说明 */
    TYPE_PT(20, "拼团"),
    /** TODO 补充说明 */
    TYPE_NORMAL(0, "普通订单"),

    /** TODO 补充说明 */
    GOODS_PRICE_TYPE_NORMAL(0, "普通"),
    /** TODO 补充说明 */
    GOODS_PRICE_TYPE_MS(1, "秒杀"),
    /** TODO 补充说明 */
    GOODS_PRICE_TYPE_PT(2, "拼团");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    OrderEnum(int code, String msg) {
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

    public static String getStatusDesc(Integer code){
        switch (code) {
            case 0:
                return "待付款";
            case 1:
                return "待发货";
            case 2:
                return "待收货";
            case 3:
                return "已完成";
            case 4:
                return "已关闭";
        }
        return "";
    }

    public static List<Integer> getStatusCanCancelArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(STATUS_WAIT_PAY.getCode());
        ret.add(STATUS_WAIT_SEND.getCode());
        return ret;
    }

    public static String getTypeTextArr(Integer code){
        switch (code) {
            case 0:
                return "普通订单";
            case 10:
                return "秒杀订单";
            case 20:
                return "拼团订单";
        }
        return "";
    }

    public static String getGoodsPriceTypeTextArr(Integer code){
        switch (code) {
            case 0:
                return "普通";
            case 1:
                return "秒杀";
            case 2:
                return "拼团";
        }
        return "";
    }
}
