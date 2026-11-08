package com.houjiang.merchant.common.enums;

import java.util.LinkedHashMap;
import java.util.Map;

public enum LogEarningsEnum {

    /**
     * 佣金类型
     */
    UE_INC_DISTRIBUTION_SETTLE(1001, "分销订单结算"),
    /** TODO 补充说明 */
    UE_DEC_WITHDRAW(1002, "佣金提现"),
    /** TODO 补充说明 */
    UE_INC_REFUSE_WITHDRAWAL(1003, "拒绝提现回退金额"),
    /** TODO 补充说明 */
    UE_INC_TRANSFER_FAIL(1004, "转账失败回退金额"),
    /** TODO 补充说明 */
    UE_INC_PAYMENT_FAIL(1005, "提现到微信零钱失败回退金额"),
    /** TODO 补充说明 */
    UE_INC_ADMIN(1006, "平台增加金额"),
    /** TODO 补充说明 */
    UE_DEC_ADMIN(1007, "平台减少金额");


    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    LogEarningsEnum(int code, String msg) {
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
     * 根据编码获取Msg
     *
     * @author Amos
     * @param code 类型
     * @return String
     */
    public static String getMsgByCode(Integer code){
        for(LogEarningsEnum enumItem : LogEarningsEnum.values()) {
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
        for(LogEarningsEnum enumItem : LogEarningsEnum.values()) {
            map.put(enumItem.getCode(), enumItem.getMsg());
        }
        return map;
    }
}
