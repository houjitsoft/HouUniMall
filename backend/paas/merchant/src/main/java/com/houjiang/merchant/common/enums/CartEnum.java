package com.houjiang.merchant.common.enums;

/**
 * 客户端枚举类
 */
public enum CartEnum {

    /** TODO 补充说明 */
    NORMAL(0, "正常"),
    /** TODO 补充说明 */
    GOODS_STATUS_DOWN(1, "商品下架"),
    /** TODO 补充说明 */
    GOODS_NOT_EXISTS(2, "商品不存在 回收站或删除"),
    /** TODO 补充说明 */
    GOODS_SKU_DELETED(3, "规格删除 重新选择规格"),
    /** TODO 补充说明 */
    GOODS_SKU_STOCK_LACK(4, "SKU 库存不足"),
    /** TODO 补充说明 */
    SHOP_NOT_EXISTS(5, "店铺不存在 未营业或未已冻结");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    CartEnum(int code, String msg) {
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
        for(CartEnum enumItem: CartEnum.values()) {
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
        for(CartEnum enumItem: CartEnum.values()) {
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
        for(CartEnum enumItem: CartEnum.values()) {
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
