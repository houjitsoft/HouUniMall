package com.houjiang.merchant.common.enums;

public enum CouponGetEnum {

    // 使用条件
    STATUS_WAIT(0, "未使用"),
    /** TODO 补充说明 */
    STATUS_USE(1, "已使用"),
    /** TODO 补充说明 */
    STATUS_EXPIRE(2, "已过期"),
    /** TODO 补充说明 */
    STATUS_INVALID(3, "已作废");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    CouponGetEnum(int code, String msg) {
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
        for(CouponGetEnum enumItem: CouponGetEnum.values()) {
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
        for(CouponGetEnum enumItem: CouponGetEnum.values()) {
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
        for(CouponGetEnum enumItem: CouponGetEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
    }
    /**
     * 获取变动类型
     *
     * @author Amos
     * @param code Integer
     * @return String
     */
    public static String getRecordDesc(Integer code) {
        for (CouponGetEnum value : CouponGetEnum.values()) {
            if (value.getCode() == code) {
                return value.getMsg();
            }
        }
        return "-";
    }
}
