package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.List;

/** 配送方式 */
public enum DeliveryEnum {

    /** TODO 补充说明 */
    DELIVERY_TYPE_EXPRESS(0, "快递发货"),
    /** TODO 补充说明 */
    DELIVERY_TYPE_SELF(1, "门店自提");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    DeliveryEnum(int code, String msg) {
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

    public final static List<Integer> DELIVERY_TYPE_ARR = new ArrayList() {{
        add(DELIVERY_TYPE_EXPRESS.getCode());
    }};
}
