package com.houjiang.merchant.common.enums;

public enum ErrorEnum {

    /** TODO 补充说明 */
    SHOW_MSG(1, "显示信息"),
    /** TODO 补充说明 */
    HIDE_MSG(0, "隐藏信息"),
    /** TODO 补充说明 */
    SUCCESS(1, "成功"),
    /** TODO 补充说明 */
    FAILED(0, "失败"),
    /** TODO 补充说明 */
    PARAMS_VALID_ERROR(0, "参数校验错误"),
    /** TODO 补充说明 */
    PARAMS_TYPE_ERROR(0, "参数类型错误"),
    /** TODO 补充说明 */
    REQUEST_METHOD_ERROR(0, "请求方法错误"),
    /** TODO 补充说明 */
    ASSERT_ARGUMENT_ERROR(0, "断言参数错误"),
    /** TODO 补充说明 */
    ASSERT_MYBATIS_ERROR(0, "断言Mybatis错误"),

    /** TODO 补充说明 */
    LOGIN_ACCOUNT_ERROR(0, "登录账号或密码错误"),
    /** TODO 补充说明 */
    LOGIN_DISABLE_ERROR(0, "登录账号已被禁用了"),
    /** TODO 补充说明 */
    TOKEN_EMPTY(0, "登录超时，请重新登录"),
    /** TODO 补充说明 */
    TOKEN_INVALID(-1, "token参数无效"),
    /** TODO 补充说明 */
    CAPTCHA_ERROR(0, "验证码错误"),
    /** TODO 补充说明 */
    PAYMENT_ERROR(0, "发起支付失败"),

    /** TODO 补充说明 */
    NO_PERMISSION(0, "无相关权限"),
    /** TODO 补充说明 */
    REQUEST_404_ERROR(0, "请求接口不存在"),

    /** TODO 补充说明 */
    SYSTEM_ERROR(0, "系统错误");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    ErrorEnum(int code, String msg) {
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
