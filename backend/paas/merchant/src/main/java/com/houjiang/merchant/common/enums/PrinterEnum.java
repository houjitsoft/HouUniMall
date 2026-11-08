package com.houjiang.merchant.common.enums;

/**
 * 客户端枚举类
 */
public enum PrinterEnum {

    /** TODO 补充说明 */
    BRAND_MOTH(0, "飞蛾打印机"),
    /** TODO 补充说明 */
    TEMPLATE_TYPE_GOODS_ORDER(0, "商品订单");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    PrinterEnum(int code, String msg) {
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
        for(PrinterEnum enumItem: PrinterEnum.values()) {
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
        for(PrinterEnum enumItem: PrinterEnum.values()) {
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
        for(PrinterEnum enumItem: PrinterEnum.values()) {
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

    public static String getTemplateTypeTextArr(Integer code){
        switch (code) {
            case 0:
                return PrinterEnum.TEMPLATE_TYPE_GOODS_ORDER.getMsg();
        }
        return "未知";
    }

    public static String getBrandTextArr(Integer code){
        switch (code) {
            case 0:
                return PrinterEnum.BRAND_MOTH.getMsg();
        }
        return "未知";
    }
}
