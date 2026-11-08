package com.houjiang.merchant.common.enums;


public enum ShopAdminEum {

    /** TODO 补充说明 */
    SUPER_IS(1, "超级管理员 是"),
    /** TODO 补充说明 */
    SUPER_NO(0, "超级管理员 否"),
    /** TODO 补充说明 */
    STATUS_NORMAL(1, "正常"),
    /** TODO 补充说明 */
    STATUS_DISABLED(0, "停用");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    ShopAdminEum(int code, String msg) {
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
