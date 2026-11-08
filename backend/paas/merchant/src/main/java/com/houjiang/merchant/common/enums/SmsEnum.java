package com.houjiang.merchant.common.enums;

/**
 * 短信枚举
 */
public enum SmsEnum {

    /** TODO 补充说明 */
    SEND_ING(0, "发送中"),
    /** TODO 补充说明 */
    SEND_SUCCESS(1, "发送成功"),
    /** TODO 补充说明 */
    SEND_FAIL(2, "发送失败");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    SmsEnum(int code, String msg) {
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

}
