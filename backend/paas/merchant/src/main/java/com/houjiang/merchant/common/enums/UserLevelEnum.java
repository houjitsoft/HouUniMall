package com.houjiang.merchant.common.enums;

public enum UserLevelEnum {
    /** TODO 补充说明 */
    CONDITION_NONE(-1, "未设置"),
    /** TODO 补充说明 */
    CONDITION_ANY(0, "任意条件"),
    /** TODO 补充说明 */
    CONDITION_ALL(1, "全部条件");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    UserLevelEnum(int code, String msg) {
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
