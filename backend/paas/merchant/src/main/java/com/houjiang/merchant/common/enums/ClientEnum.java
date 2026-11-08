package com.houjiang.merchant.common.enums;

/**
 * 客户端枚举类
 */
public enum ClientEnum {

    /** TODO 补充说明 */
    MNP(1, "微信小程序"),
    /** TODO 补充说明 */
    OA(2, "微信公众号"),
    /** TODO 补充说明 */
    H5(3, "手机H5"),
    /** TODO 补充说明 */
    PC(4, "电脑PC"),
    /** TODO 补充说明 */
    IOS(5, "苹果APP"),
    /** TODO 补充说明 */
    APK(6, "安卓APP");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    ClientEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 根据类型获取Code
     *
     * @author Amos
     * @param type 类型
     * @return Integer
     */
    public static Integer getCodeByType(String type){
        for(ClientEnum enumItem: ClientEnum.values()) {
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
        for(ClientEnum enumItem: ClientEnum.values()) {
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
        for(ClientEnum enumItem: ClientEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
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
