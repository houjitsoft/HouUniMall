package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.List;

public enum RefundEnum {

    // 退款类型
    TYPE_ADMIN(1, "后台退款"),

    // 退款状态
    REFUND_ING(0, "退款中"),
    /** TODO 补充说明 */
    REFUND_SUCCESS(1, "退款成功"),
    /** TODO 补充说明 */
    REFUND_ERROR(2, "退款失败"),

    // 退款订单类型
    ORDER_TYPE_ORDER(1, "普通订单"),
    /** TODO 补充说明 */
    ORDER_TYPE_RECHARGE(2, "充值订单"),
    /** TODO 补充说明 */
    TYPE_ORDER_SYS_CANCEL(0, "系统取消订单"),
    /** TODO 补充说明 */
    TYPE_ORDER_ADMIN_CANCEL(1, "管理员取消订单"),
    /** TODO 补充说明 */
    TYPE_ORDER_SHOP_CANCEL(2, "商家取消订单"),
    /** TODO 补充说明 */
    TYPE_ORDER_USER_CANCEL(3, "用户取消订单"),
    /** TODO 补充说明 */
    TYPE_REFUND(4, "售后退款"),
    /** TODO 补充说明 */
    STATUS_WAIT_RETURN(0, "退款中"),
    /** TODO 补充说明 */
    STATUS_SUCCESS(1, "退款成功"),
    /** TODO 补充说明 */
    STATUS_FAILED(2, "退款失败");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    RefundEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static List<Integer> getStatusWaitArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(STATUS_WAIT_RETURN.getCode());
        ret.add(STATUS_FAILED.getCode());
        return ret;
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
     * 订单类型标识
     *
     * @author Amos
     * @param code 编码
     * @return String
     */
    public static String getOrderType(Integer code){
        switch (code) {
            case 1:
                return "order";
            case 2:
                return "recharge";
        }
        return "未知";
    }

    /**
     * 退款类型描述
     *
     * @author Amos
     * @param code 编码
     * @return String
     */
    public static String getRefundTypeMsg(Integer code)
    {
        if (code == 1) {
            return RefundEnum.TYPE_ADMIN.getMsg();
        }
        return "未知";
    }

    /**
     * 退款状态描述
     *
     * @author Amos
     * @param code 编码
     * @return String
     */
    public static String getRefundStatusMsg(Integer code){
        switch (code) {
            case 0:
                return RefundEnum.REFUND_ING.getMsg();
            case 1:
                return RefundEnum.REFUND_SUCCESS.getMsg();
            case 2:
                return RefundEnum.REFUND_ERROR.getMsg();
        }
        return "未知";
    }

    /**
     * @param code
     * @return
     */
    public static String getTypeTextArr(Integer code) {
        switch (code) {
            case 0:
                return RefundEnum.TYPE_ORDER_SYS_CANCEL.getMsg();
            case 1:
                return RefundEnum.TYPE_ORDER_ADMIN_CANCEL.getMsg();
            case 2:
                return RefundEnum.TYPE_ORDER_SHOP_CANCEL.getMsg();
            case 3:
                return RefundEnum.TYPE_ORDER_USER_CANCEL.getMsg();
            case 4:
                return RefundEnum.TYPE_REFUND.getMsg();
        }
        return "未知";
    }
}
