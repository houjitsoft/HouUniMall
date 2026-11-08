package com.houjiang.merchant.common.enums;

/**
 * 相册枚举
 */
public enum YesNoEnum {

    /** TODO 补充说明 */
    YES(1, "是"),
    /** TODO 补充说明 */
    NO(0, "否");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    YesNoEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getStatusDesc(Integer status) {
        if (status.equals(YES.getCode())) {
            return "正常";
        }
        if (status.equals(NO.getCode())) {
            return "禁用";
        }
        return "";
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
