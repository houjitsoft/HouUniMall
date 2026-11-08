package com.houjiang.merchant.common.enums;

/**
 * 通知枚举类
 */
public enum AccountLogEnum {

    /**
     * 变动对象
     * UM 用户余额(user_money)
     */
//    UM(1, "变动对象"),
    /**
     * 动作
     * INC 增加
     * DEC 减少
     */
//    INC(1, "增加"),
//    DEC(2, "减少"),
//    /**
//     * 用户余额减少类型
//     */
//    UM_DEC_ADMIN(100, "平台减少余额"),
//    UM_DEC_RECHARGE_REFUND(101, "充值订单退款减少余额"),
//
//    UM_INC_ADMIN(200, "平台增加余额"),
//    UM_INC_RECHARGE(201, "充值增加余额");

    /**
     * 构造方法
     */
//    private final int code;
//    private final String msg;
//    AccountLogEnum(int code, String msg) {
//        this.code = code;
//        this.msg = msg;
//    }
//
//    /**
//     * @notes 变动类型描述
//     * @param $changeType
//     * @param false $flag
//     * @return string|string[]
//     * @author Amos
//     * @date 2023/2/23 10:07
//     */
//    public static String getChangeTypeDesc(Integer changeType) {
//        JSONObject ret = new JSONObject();
//        ret.put(String.valueOf(UM_DEC_ADMIN.getCode()), UM_DEC_ADMIN.getMsg());
//        ret.put(String.valueOf(UM_INC_ADMIN.getCode()), UM_INC_ADMIN.getMsg());
//        ret.put(String.valueOf(UM_INC_RECHARGE.getCode()), UM_INC_RECHARGE.getMsg());
//        ret.put(String.valueOf(UM_DEC_RECHARGE_REFUND.getCode()), UM_DEC_RECHARGE_REFUND.getMsg());
//
//        return ret.getString(String.valueOf(changeType));
//    }
//
//    /**
//     * @notes 获取变动对象
//     * @param changeType
//     * @return false
//     * @author Amos
//     */
//    public static Integer getChangeObject(Integer changeType) {
//        List<Integer> typeList = getUserMoneyChangeType();
//        if (typeList.contains(changeType)) {
//            return UM.getCode();
//        } else {
//            return 0;
//        }
//    }
//
//    /**
//     * 获取状态码
//     *
//     * @author Amos
//     * @return Long
//     */
//    public int getCode() {
//        return this.code;
//    }
//
//    /**
//     * 获取提示
//     *
//     * @author Amos
//     * @return String
//     */
//    public String getMsg() {
//        return this.msg;
//    }
//
//    /**
//     * 验证码场景
//     */
//    public static List<Integer> getUserMoneyChangeType() {
//        List<Integer> ret = new ArrayList<>();
//        ret.add(UM_DEC_ADMIN.getCode());
//        ret.add(UM_DEC_RECHARGE_REFUND.getCode());
//        ret.add(UM_INC_ADMIN.getCode());
//        ret.add(UM_INC_RECHARGE.getCode());
//        return ret;
//    }

}
