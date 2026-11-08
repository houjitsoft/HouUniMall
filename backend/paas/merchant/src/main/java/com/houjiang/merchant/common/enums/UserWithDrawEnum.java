package com.houjiang.merchant.common.enums;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserWithDrawEnum {


    /** 提现到钱包余额 */
    public static Integer TYPE_USER_MONEY       = 1;
    /** 提现到微信零钱 */
    public static Integer TYPE_WECHAT_CHANGE    = 2;
    /** 提现到银行卡 */
    public static Integer TYPE_BANK             = 3;
    /** 提现到微信收款码 */
    public static Integer TYPE_WECHAT_CODE      = 4;
    /** 提现到支付宝收款码 */
    public static Integer TYPE_ALI_CODE         = 5;



    public final static JSONArray TYPE_TEXT_SELECT_ARR = new JSONArray() {{

        add(new JSONObject(){{
            put("value", WithdrawType.TYPE_USER_MONEY.getValue());
            put("name", "钱包余额");
        }});

        add(new JSONObject(){{
            put("value", WithdrawType.TYPE_WECHAT_CHANGE.getValue());
            put("name", "微信零钱");
        }});

        add(new JSONObject(){{
            put("value", WithdrawType.TYPE_BANK.getValue());
            put("name", "银行卡");
        }});

        add(new JSONObject(){{
            put("value", WithdrawType.TYPE_WECHAT_CODE.getValue());
            put("name", "微信收款码");
        }});

        add(new JSONObject(){{
            put("value", WithdrawType.TYPE_ALI_CODE.getValue());
            put("name", "支付宝收款码");
        }});

    }};


    /** 提现类型 */
    public enum WithdrawType {
        /** TODO 补充说明 */
        TYPE_USER_MONEY(1, "钱包余额"),
        /** TODO 补充说明 */
        TYPE_WECHAT_CHANGE(2, "微信零钱"),
        /** TODO 补充说明 */
        TYPE_BANK(3, "银行卡"),
        /** TODO 补充说明 */
        TYPE_WECHAT_CODE(4, "微信收款码"),
        /** TODO 补充说明 */
        TYPE_ALI_CODE(5, "支付宝收款码");

        private final int value;
        private final String name;

        WithdrawType(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public static Map<Integer, String> getTypeTextArr() {
            Map<Integer, String> typeTextArr = new HashMap<>();
            for (WithdrawType type : values()) {
                typeTextArr.put(type.getValue(), type.getName());
            }
            return typeTextArr;
        }

        public static Map<String, Integer> getTypeSelectArr() {
            Map<String, Integer> typeSelectArr = new HashMap<>();
            for (WithdrawType type : values()) {
                typeSelectArr.put(type.getName(), type.getValue());
            }
            return typeSelectArr;
        }

        public static int[] getTransferArr() {
            return new int[]{TYPE_BANK.getValue(), TYPE_WECHAT_CODE.getValue(), TYPE_ALI_CODE.getValue()};
        }
    }

    /** 提现状态 */
    public enum WithdrawStatus {
        /** TODO 补充说明 */
        STATUS_WAIT(0, "待提现"),
        /** TODO 补充说明 */
        STATUS_ING(1, "待收款"),
        /** TODO 补充说明 */
        STATUS_SUCCESS(2, "提现成功"),
        /** TODO 补充说明 */
        STATUS_FAIL(3, "提现失败"),

        /** TODO 补充说明 */
        SUB_STATUS_NO_PROCESS(0, "无需处理"),
        /** TODO 补充说明 */
        SUB_STATUS_WECHAT_WAIT_USER(1, "微信待用户收款"),
        /** TODO 补充说明 */
        SUB_STATUS_WECHAT_USER_COMPLETED(2, "待到账"),
        /** TODO 补充说明 */
        SUB_STATUS_WECHAT_CALLBACK_RECEIVED(3, "已收微信回调"),
        /** TODO 补充说明 */
        SUB_STATUS_WECHAT_CALLBACK_FAIL(4, "微信转账失败");

        private final int value;
        private final String name;

        WithdrawStatus(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public static Map<Integer, String> getStatusTextArr() {
            Map<Integer, String> statusTextArr = new HashMap<>();
            for (WithdrawStatus status : values()) {
                statusTextArr.put(status.getValue(), status.getName());
            }
            return statusTextArr;
        }

        /**
         * 状态描述
         *
         * @author Amos
         * @param code Integer
         * @return String
         */
        public static String getStatusMsg(Integer code, Integer subStatus){
            switch (code) {
                case 0:
                    return STATUS_WAIT.STATUS_WAIT.getName();
                case 1:
                    if (subStatus.equals(STATUS_WAIT.SUB_STATUS_WECHAT_USER_COMPLETED.getValue())) {
                        return STATUS_WAIT.SUB_STATUS_WECHAT_USER_COMPLETED.getName();
                    }
                    return STATUS_WAIT.STATUS_ING.getName();
                case 2:
                    return STATUS_WAIT.STATUS_SUCCESS.getName();
                case 3:
                    return STATUS_WAIT.STATUS_FAIL.getName();
            }
            return "未知";
        }
    }
}
