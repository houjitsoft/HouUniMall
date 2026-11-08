package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum LogMoneyEnum {

    /**
     * 用户余额类型
     */
    USER_MONEY_RECHARGE(1000, "用户余额充值"),
    /** TODO 补充说明 */
    USER_MONEY_ORDER_PAY(1010, "下单扣减余额"),
    /** TODO 补充说明 */
    USER_MONEY_ORDER_CANCEL(1020, "取消订单退回余额"),
    /** TODO 补充说明 */
    USER_MONEY_ORDER_REFUND(1030, "售后退回余额"),
    /** TODO 补充说明 */
    USER_MONEY_SYS_INC(1040, "系统增加余额"),
    /** TODO 补充说明 */
    USER_MONEY_SYS_DEC(1050, "系统扣减余额"),
    /** TODO 补充说明 */
    USER_MONEY_DISTRIBUTION_WITHDRAW(1060, "佣金提现至余额"),

    /** TODO 补充说明 */
    DISTRIBUTION_MONEY_ORDER(2000, "订单佣金"),

    /** TODO 补充说明 */
    DISTRIBUTION_MONEY_WITHDRAW(2010, "佣金提现"),

    /** TODO 补充说明 */
    DISTRIBUTION_MONEY_WITHDRAW_FAILED(2020, "提现失败"),

    // 系统增加佣金
    DISTRIBUTION_MONEY_SYS_INC(2030, "系统增加佣金"),

    /** TODO 补充说明 */
    DISTRIBUTION_MONEY_SYS_DEC(2040, "系统扣减佣金"),
    /** TODO 补充说明 */
    ACCOUNT_STATUS_NONE(0, "无变化"),
    /** TODO 补充说明 */
    ACCOUNT_STATUS_UP(1, "增益"),
    /** TODO 补充说明 */
    ACCOUNT_STATUS_DOWN(2, "减益");
    // 系统扣减佣金


    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    LogMoneyEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static Integer getChangeObject(Integer changeType) {

        if (changeType != null) {
            if (changeType.equals(USER_MONEY_RECHARGE.getCode())
                    || changeType.equals(USER_MONEY_ORDER_PAY.getCode())
                    || changeType.equals(USER_MONEY_ORDER_CANCEL.getCode())
                    || changeType.equals(USER_MONEY_ORDER_REFUND.getCode())
                    || changeType.equals(USER_MONEY_SYS_INC.getCode())
                    || changeType.equals(USER_MONEY_SYS_DEC.getCode())) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }




    public static List<Integer> getUserMoneyChangeType() {
        List<Integer> ret = new ArrayList<>();
        ret.add(USER_MONEY_RECHARGE.getCode());
        ret.add(USER_MONEY_ORDER_PAY.getCode());
        ret.add(USER_MONEY_ORDER_CANCEL.getCode());
        ret.add(USER_MONEY_ORDER_REFUND.getCode());
        ret.add(USER_MONEY_SYS_INC.getCode());
        ret.add(USER_MONEY_SYS_DEC.getCode());
        ret.add(USER_MONEY_DISTRIBUTION_WITHDRAW.getCode());

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
     * 根据编码获取Msg
     *
     * @author Amos
     * @param code 类型
     * @return String
     */
    public static String getMsgByCode(Integer code){
        for(LogMoneyEnum enumItem : LogMoneyEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
    }

    /**
     * 获取所有类型
     *
     * @author Amos
     * @return String
     */
    public static Map<Integer, String> getTypeList(){
        Map<Integer, String> map = new LinkedHashMap<>();
        for(LogMoneyEnum enumItem : LogMoneyEnum.values()) {
            map.put(enumItem.getCode(), enumItem.getMsg());
        }
        return map;
    }


    public static String getAccountStatusArr(Integer accountStatus) {
        if (accountStatus.equals(ACCOUNT_STATUS_NONE.getCode())) {
            return ACCOUNT_STATUS_NONE.getMsg();
        } else if (accountStatus.equals(ACCOUNT_STATUS_UP.getCode())) {
            return ACCOUNT_STATUS_UP.getMsg();
        } else if (accountStatus.equals(ACCOUNT_STATUS_DOWN.getCode())) {
            return ACCOUNT_STATUS_DOWN.getMsg();
        }
        return "";
    }
}
