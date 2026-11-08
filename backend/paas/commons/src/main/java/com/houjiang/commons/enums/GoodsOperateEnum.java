package com.houjiang.commons.enums;

/**
 * @author Amos
 * @date 2021-05-1211:22
 * @Description: 商品操作类型
 **/
public enum GoodsOperateEnum {

    /** 一键上架 */
    PUTAWAY(1, "一键上架"),

    /** 立即购买 */
    BUY(2, "立即购买"),

    /** 申请经销商 */
    APPLY(3, "申请经销商"),

    /** 已经销 */
    HAVA_GOODS(4, "已经销"),
    ;
    /**+
     * 类型
     */
    private Integer type;

    /**
     * 类型名称
     */
    private String typeName;

    GoodsOperateEnum(Integer type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
