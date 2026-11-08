package com.houjiang.merchant.common.enums;

/**
 * 客户端枚举类
 */
public enum ShopAccountEnum {
    /** TODO 补充说明 */
    CHANGE_OBJECT_SHOP_MONEY(0, "余额"),
    /** TODO 补充说明 */
    CHANGE_OBJECT_SHOP_GATHER_MONEY(1, "采集余额"),
    /** TODO 补充说明 */
    SHOP_MONEY_WITHDRAW(1000, "商家提现"),
    /** TODO 补充说明 */
    SHOP_MONEY_WITHDRAW_FAIL(1010, "商家提现失败"),
    /** TODO 补充说明 */
    SHOP_MONEY_REBATE(1020, "结算入账"),
    /** TODO 补充说明 */
    SHOP_MONEY_REBATE_RETURN_SHOP_SERVICE(1021, "交易服务费"),
    /** TODO 补充说明 */
    SHOP_MONEY_SYS_INC(1030, "系统增加"),
    /** TODO 补充说明 */
    SHOP_MONEY_SYS_DEC(1040, "系统扣减"),
    /** TODO 补充说明 */
    SHOP_GATHER_MONEY_SYS_INC(2000, "平台增加"),
    /** TODO 补充说明 */
    SHOP_GATHER_MONEY_SYS_DEC(2010, "平台扣减"),
    /** TODO 补充说明 */
    SHOP_GATHER_MONEY_GATHER_GOODS(2020, "采集商品");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    ShopAccountEnum(int code, String msg) {
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
        for(ShopAccountEnum enumItem: ShopAccountEnum.values()) {
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
        for(ShopAccountEnum enumItem: ShopAccountEnum.values()) {
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
        for(ShopAccountEnum enumItem: ShopAccountEnum.values()) {
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
