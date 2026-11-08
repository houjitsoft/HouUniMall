package com.houjiang.merchant.common.enums;
public enum OrderGoodsEnum {

    /** TODO 补充说明 */
    AFTER_SALE_NONE(0, "无售后"),
    /** TODO 补充说明 */
    AFTER_SALE_HAS(1, "有售后"),
    /** TODO 补充说明 */
    AFTER_SALE_FINAL(2, "已处理完成"),
    /** TODO 补充说明 */
    COMMENT_WAIT(0, "等待留言"),
    /** TODO 补充说明 */
    COMMENT_HAS(1, "已经留言"),
    /** TODO 补充说明 */
    COMMENT_DISABLED(2, "不可留言");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    OrderGoodsEnum(int code, String msg) {
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
        for(OrderGoodsEnum enumItem: OrderGoodsEnum.values()) {
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
        for(OrderGoodsEnum enumItem: OrderGoodsEnum.values()) {
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
        for(OrderGoodsEnum enumItem: OrderGoodsEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
    }
}
