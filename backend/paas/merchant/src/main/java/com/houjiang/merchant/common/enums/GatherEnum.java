package com.houjiang.merchant.common.enums;

/**
 * 实物商品
 */
public enum GatherEnum {

    /** TODO 补充说明 */
    CHANNEL_NULL(0, "未知"),
    /** TODO 补充说明 */
    CHANNEL_TB(1, "淘宝"),
    /** TODO 补充说明 */
    CHANNEL_TM(2, "天猫"),
    /** TODO 补充说明 */
    CHANNEL_JD(3, "京东"),
    /** TODO 补充说明 */
    CHANNEL_1688(4, "1688");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    GatherEnum(int code, String msg) {
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
        for(GatherEnum enumItem: GatherEnum.values()) {
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
        for(GatherEnum enumItem: GatherEnum.values()) {
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
        for(GatherEnum enumItem: GatherEnum.values()) {
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
