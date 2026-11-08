package com.houjiang.merchant.common.enums;


import com.alibaba.fastjson2.JSONObject;

import java.util.*;

public enum OrderAfterSaleEnum {
    //退款类型
    TYPE_ONLY_MONEY(0, "仅退款"),
    /** TODO 补充说明 */
    TYPE_GOODS_AND_MONEY(1, "退货退款"),
    //状态
    STATUS_APPLY(0, "申请中"),
    /** TODO 补充说明 */
    STATUS_AGREE(10, "等待退款"),
    /** TODO 补充说明 */
    STATUS_SUCCESS(20, "售后成功"),
    /** TODO 补充说明 */
    STATUS_FAILED(30, "售后失败"),
    // 子状态
    SUB_STATUS_WAIT_SELLER_AGREE(1, "买家发起售后，等待商家同意"),
    /** TODO 补充说明 */
    SUB_STATUS_WAIT_SELLER_REFUND(11, "商家同意售后，等待商家退款"),
    /** TODO 补充说明 */
    SUB_STATUS_WAIT_BUYER_RETURN(12, "商家同意售后，等待买家寄回商品"),
    /** TODO 补充说明 */
    SUB_STATUS_WAIT_SELLER_RECEIVE(13, "买家寄回商品，等待商家收货"),
    /** TODO 补充说明 */
    SUB_STATUS_WAIT_SELLER_DEAL(14, "商家已收货，等待商家处理"),
    /** TODO 补充说明 */
    SUB_STATUS_DEAL_WAIT_SELLER_REFUND(15, "商家已处理，等待商家退款"),
    /** TODO 补充说明 */
    SUB_STATUS_SUCCESS(21, "售后退款成功"),
    /** TODO 补充说明 */
    SUB_STATUS_SELLER_REFUSE(31, "商家拒绝售后"),
    /** TODO 补充说明 */
    SUB_STATUS_BUYER_CANCEL(32, "买家取消售后"),
    /** TODO 补充说明 */
    SUB_STATUS_REFUSE_RECEIVE(33, "商家拒绝收货"),
    //退款原因
    REASON_OTHER(0, "其他"),
    /** TODO 补充说明 */
    REASON_7_DAY(10, "7天无理由退"),
    /** TODO 补充说明 */
    REASON_DISLIKE(20, "不喜欢不想要"),
    /** TODO 补充说明 */
    REASON_NO_EXPRESS(30, "快递/物流未发货"),
    /** TODO 补充说明 */
    REASON_EMPTY_GOODS(40, "空包裹"),
    /** TODO 补充说明 */
    REASON_BAD_GOODS(50, "货物破损已拒签");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    OrderAfterSaleEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /** 可取消子状态 */
    public static List<Integer> getSubStatusCancelArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(SUB_STATUS_WAIT_SELLER_AGREE.getCode());
        ret.add(SUB_STATUS_SELLER_REFUSE.getCode());
        ret.add(SUB_STATUS_WAIT_BUYER_RETURN.getCode());
        return ret;
    }

    /** 可退款状态 */
    public static List<Integer> getSubStatusCanRefundArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(SUB_STATUS_WAIT_SELLER_REFUND.getCode());
        ret.add(SUB_STATUS_DEAL_WAIT_SELLER_REFUND.getCode());
        return ret;
    }

    /** 可重新申请子状态 */
    public static List<Integer> getSubStatusReApplyArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(SUB_STATUS_SELLER_REFUSE.getCode());
        ret.add(SUB_STATUS_BUYER_CANCEL.getCode());
        ret.add(SUB_STATUS_REFUSE_RECEIVE.getCode());
        return ret;
    }

    /** 处理中状态 */
    public static List<Integer> getSubStatusInProgressArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(SUB_STATUS_WAIT_SELLER_AGREE.getCode());
        ret.add(SUB_STATUS_WAIT_SELLER_REFUND.getCode());
        ret.add(SUB_STATUS_SELLER_REFUSE.getCode());
        ret.add(SUB_STATUS_BUYER_CANCEL.getCode());
        ret.add(SUB_STATUS_REFUSE_RECEIVE.getCode());
        ret.add(SUB_STATUS_WAIT_BUYER_RETURN.getCode());
        ret.add(SUB_STATUS_WAIT_SELLER_RECEIVE.getCode());
        ret.add(SUB_STATUS_WAIT_SELLER_DEAL.getCode());
        ret.add(SUB_STATUS_DEAL_WAIT_SELLER_REFUND.getCode());
        return ret;
    }

    /** 已处理状态 */
    public static List<Integer> getSubStatusHasProgressArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(SUB_STATUS_SUCCESS.getCode());
        return ret;
    }

    public static String getStatusTextArr(Integer code) {
        if (code.equals(STATUS_APPLY.getCode())) {
            return STATUS_APPLY.getMsg();
        }
        if (code.equals(STATUS_AGREE.getCode())) {
            return STATUS_AGREE.getMsg();
        }
        if (code.equals(STATUS_SUCCESS.getCode())) {
            return STATUS_SUCCESS.getMsg();
        }
        if (code.equals(STATUS_FAILED.getCode())) {
            return STATUS_FAILED.getMsg();
        }
        return "";
    }

    public static String getSubStatusTextArr(Integer code) {
        if (code.equals(SUB_STATUS_WAIT_SELLER_AGREE.getCode())) {
            return SUB_STATUS_WAIT_SELLER_AGREE.getMsg();
        }
        if (code.equals(SUB_STATUS_WAIT_SELLER_REFUND.getCode())) {
            return SUB_STATUS_WAIT_SELLER_REFUND.getMsg();
        }
        if (code.equals(SUB_STATUS_WAIT_BUYER_RETURN.getCode())) {
            return SUB_STATUS_WAIT_BUYER_RETURN.getMsg();
        }
        if (code.equals(SUB_STATUS_WAIT_SELLER_DEAL.getCode())) {
            return SUB_STATUS_WAIT_SELLER_DEAL.getMsg();
        }
        if (code.equals(SUB_STATUS_DEAL_WAIT_SELLER_REFUND.getCode())) {
            return SUB_STATUS_DEAL_WAIT_SELLER_REFUND.getMsg();
        }
        if (code.equals(SUB_STATUS_SUCCESS.getCode())) {
            return SUB_STATUS_SUCCESS.getMsg();
        }
        if (code.equals(SUB_STATUS_SELLER_REFUSE.getCode())) {
            return SUB_STATUS_SELLER_REFUSE.getMsg();
        }
        if (code.equals(SUB_STATUS_BUYER_CANCEL.getCode())) {
            return SUB_STATUS_BUYER_CANCEL.getMsg();
        }
        if (code.equals(SUB_STATUS_REFUSE_RECEIVE.getCode())) {
            return SUB_STATUS_REFUSE_RECEIVE.getMsg();
        }
        return "";
    }

    public static List<Integer> getStatusSuccessArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(STATUS_SUCCESS.getCode());
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


    public static List<Integer> getStatusApplyArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(STATUS_APPLY.getCode());
        ret.add(STATUS_AGREE.getCode());
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
        for(OrderAfterSaleEnum enumItem: OrderAfterSaleEnum.values()) {
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
        for(OrderAfterSaleEnum enumItem: OrderAfterSaleEnum.values()) {
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
        for(OrderAfterSaleEnum enumItem: OrderAfterSaleEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
    }

    public static String getReasonTextArr(Integer code) {
        if (code.equals(REASON_OTHER.getCode())) {
            return REASON_OTHER.getMsg();
        }
        if (code.equals(REASON_7_DAY.getCode())) {
            return REASON_7_DAY.getMsg();
        }
        if (code.equals(REASON_DISLIKE.getCode())) {
            return REASON_DISLIKE.getMsg();
        }
        if (code.equals(REASON_NO_EXPRESS.getCode())) {
            return REASON_NO_EXPRESS.getMsg();
        }
        if (code.equals(REASON_EMPTY_GOODS.getCode())) {
            return REASON_EMPTY_GOODS.getMsg();
        }
        if (code.equals(REASON_BAD_GOODS.getCode())) {
            return REASON_BAD_GOODS.getMsg();
        }
        return "";
    }

    public static Map<Integer, Object> getReasonTextArr() {
        Map<Integer, Object> ret = new HashMap<Integer, Object>();
        ret.put(REASON_OTHER.getCode(), REASON_OTHER.getMsg());
        ret.put(REASON_7_DAY.getCode(), REASON_7_DAY.getMsg());
        ret.put(REASON_DISLIKE.getCode(), REASON_DISLIKE.getMsg());
        ret.put(REASON_NO_EXPRESS.getCode(), REASON_NO_EXPRESS.getMsg());
        ret.put(REASON_EMPTY_GOODS.getCode(), REASON_EMPTY_GOODS.getMsg());
        ret.put(REASON_BAD_GOODS.getCode(), REASON_BAD_GOODS.getMsg());
        return ret;
    }

    public static Map<Integer, Object> getTypeTextArr() {
        Map<Integer, Object> ret = new HashMap<Integer, Object>();
        ret.put(TYPE_ONLY_MONEY.getCode(), TYPE_ONLY_MONEY.getMsg());
        ret.put(TYPE_GOODS_AND_MONEY.getCode(), TYPE_GOODS_AND_MONEY.getMsg());
        return ret;
    }

    public static String getTypeText(Integer code) {
        if (code.equals(TYPE_ONLY_MONEY.getCode())) {
            return TYPE_ONLY_MONEY.getMsg();
        }
        if (code.equals(TYPE_GOODS_AND_MONEY.getCode())) {
            return TYPE_GOODS_AND_MONEY.getMsg();
        }
        return "";
    }
}
