package com.houjiang.merchant.common.enums;


public enum OperatorEnum {

    /** TODO 补充说明 */
    SYS(0, "系统"),
    /** TODO 补充说明 */
    ADMIN(1, "管理员"),
    /** TODO 补充说明 */
    SHOP(2, "商家"),
    /** TODO 补充说明 */
    USER(3, "用户");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    OperatorEnum(int code, String msg) {
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

}
