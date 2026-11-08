package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.List;

public enum DecorateEnum {

    /** TODO 补充说明 */
    TYPE_HOME(1, "商城首页"),
    /** TODO 补充说明 */
    TYPE_USER_CENTER(2, "个人中心"),
    /** TODO 补充说明 */
    TYPE_SERVICE(3, "客服设置"),

    /** TODO 补充说明 */
    TYPE_CATEGORY(4, "分类页面"),

    /** TODO 补充说明 */
    TYPE_GOODS_DETAIL(5, "商品详情"),

    /** TODO 补充说明 */
    TYPE_MALL_STYLE(6, "商城风格"),
    /** TODO 补充说明 */
    TYPE_MS(7, "秒杀装修"),
    /** TODO 补充说明 */
    TYPE_SUPER_TEAM(8, "超级拼团");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    DecorateEnum(int code, String msg) {
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
        for(DecorateEnum enumItem: DecorateEnum.values()) {
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
        for(DecorateEnum enumItem: DecorateEnum.values()) {
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
        for(DecorateEnum enumItem: DecorateEnum.values()) {
            if (enumItem.getCode() == code) {
                return enumItem.getMsg();
            }
        }
        return null;
    }
}
