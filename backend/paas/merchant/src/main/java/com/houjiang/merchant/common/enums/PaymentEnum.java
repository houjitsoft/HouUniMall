package com.houjiang.merchant.common.enums;

/**
 * 支付枚举
 */
public enum PaymentEnum {

    /** TODO 补充说明 */
    UN_PAID(0, "未支付"),
    /** TODO 补充说明 */
    OK_PAID(1, "已支付"),

    /** TODO 补充说明 */
    TYPE_OTHER(0, "其他"),
    /** TODO 补充说明 */
    WALLET_PAY(1, "余额支付"),
    /** TODO 补充说明 */
    WX_PAY(2, "微信支付"),
    /** TODO 补充说明 */
    ALI_PAY(3, "支付宝支付"),

    //支付方式场景
    SCENE_MNP(1, "微信小程序"),
    /** TODO 补充说明 */
    SCENE_OA(2, "微信公众号"),
    /** TODO 补充说明 */
    SCENE_H5(3, "H5");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    PaymentEnum(int code, String msg) {
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
    public static String getPayWayMsg(Integer code){
        switch (code) {
            case 0:
                return "其他";
            case 1:
                return "余额支付";
            case 2:
                return "微信支付";
            case 3:
                return "支付宝支付";
        }
        return "未知";
    }

    /**
     * @author Amos
     * @param status 支付状态
     * @return String
     */
    public static String getPayStatusMsg(Integer status){
        switch (status) {
            case 0:
                return "未支付";
            case 1:
                return "已支付";
        }
        return "未知";
    }

}
