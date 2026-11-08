package com.houjiang.merchant.common.enums;


public enum ShopEum {

    /** TODO 补充说明 */
    TYPE_SELF(0, "自营商家"),
    /** TODO 补充说明 */
    TYPE_APPLY(1, "入驻商家"),
    /** TODO 补充说明 */
    SHOP_STATUS_NORMAL(1, "正常"),
    /** TODO 补充说明 */
    SHOP_STATUS_DISABLED(0, "冻结"),
    /** TODO 补充说明 */
    BUSINESS_STATUS_OPEN(1, "营业中"),
    /** TODO 补充说明 */
    BUSINESS_STATUS_PAUSE(0, "暂停营业"),
    /** TODO 补充说明 */
    HOME_RECOMMEND_IS(1, "推荐"),
    /** TODO 补充说明 */
    HOME_RECOMMEND_NO(0, "不推荐"),
    /** TODO 补充说明 */
    GOODS_AUDIT_NEED(1, "需要审核"),
    /** TODO 补充说明 */
    GOODS_AUDIT_NO_NEED(0, "无需审核");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    ShopEum(int code, String msg) {
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
     * 商家类型标识
     *
     * @author Amos
     * @param code 编码
     * @return String
     */
    public static String getTypeDesc(Integer code){
        switch (code) {
            case 0:
                return "自营商家";
            case 1:
                return "入驻商家";
        }
        return "";
    }
}
