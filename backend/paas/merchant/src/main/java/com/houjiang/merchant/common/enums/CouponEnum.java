package com.houjiang.merchant.common.enums;

public enum CouponEnum {

    // 使用条件
    CONDITION_TYPE_NONE(0, "无门槛"),
    /** TODO 补充说明 */
    CONDITION_TYPE_ORDER_MONEY(1, "订单满"),

    // 发放数量限制
    STOCK_TYPE_NONE(0, "不限数量"),
    /** TODO 补充说明 */
    STOCK_TYPE_LIMIT(1, "限制数量"),

    // 用券时间
    USE_TIME_TYPE_DEFAULT(0, "固定日期"),
    /** TODO 补充说明 */
    USE_TIME_TYPE_CURRENT(1, "领券当日起"),
    /** TODO 补充说明 */
    USE_TIME_TYPE_NEXT(2, "领券次日起"),

    // 领取方式
    GET_TYPE_USER(0, "用户领取"),
    /** TODO 补充说明 */
    GET_TYPE_SYS(1, "系统发放"),

    // 领取数量限制
    GET_LIMIT_NONE(0, "不限领取"),
    /** TODO 补充说明 */
    GET_LIMIT_NUM(1, "限制领取"),
    /** TODO 补充说明 */
    GET_LIMIT_DAY_NUM(2, "每天限制领取"),

    // 允许使用商品
    USE_GOODS_TYPE_ALL(0, "全场通用"),
    /** TODO 补充说明 */
    USE_GOODS_TYPE_PART_YES(1, "部分商品可用"),
    /** TODO 补充说明 */
    USE_GOODS_TYPE_PART_NO(2, "部分商品不可用"),

    // 优惠券状态
//    COUPON_STATUS_WAIT(1, "待发布"),
    STATUS_WAIT(0, "未开始"),
    /** TODO 补充说明 */
    STATUS_START(1, "进行中"),
    /** TODO 补充说明 */
    STATUS_END(2, "已结束"),

    // 使用状态
    USE_STATUS_NOT(0, "未使用"),
    /** TODO 补充说明 */
    USE_STATUS_OK(1, "已使用"),
    /** TODO 补充说明 */
    USE_STATUS_EXPIRE(2, "已过期"),
    /** TODO 补充说明 */
    USE_STATUS_VOID(3, "已作废");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    CouponEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getUseTimeTypeText(Integer code) {
        switch (code) {
            case 0:
                return CouponEnum.USE_TIME_TYPE_DEFAULT.getMsg();
            case 1:
                return CouponEnum.USE_TIME_TYPE_CURRENT.getMsg();
            case 2:
                return CouponEnum.USE_TIME_TYPE_NEXT.getMsg();
        }
        return "未知";
    }

    public static String getConditionTypeTextArr(Integer code) {
        switch (code) {
            case 0:
                return CouponEnum.CONDITION_TYPE_NONE.getMsg();
            case 1:
                return CouponEnum.CONDITION_TYPE_ORDER_MONEY.getMsg();
        }
        return "未知";
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
     * 优惠券状态描述
     *
     * @author Amos
     * @param code 类型
     * @return String
     */
    public static String getCouponStatusMsg(Integer code){
        switch (code) {
            case 0:
                return CouponEnum.STATUS_WAIT.getMsg();
            case 1:
                return CouponEnum.STATUS_START.getMsg();
            case 2:
                return CouponEnum.STATUS_END.getMsg();
        }
        return "未知";
    }

    /**
     * 优惠券使用状态描述
     *
     * @author Amos
     * @param code 类型
     * @return String
     */
    public static String getUseCouponMsg(Integer code){
        switch (code) {
            case 0:
                return CouponEnum.USE_STATUS_NOT.getMsg();
            case 1:
                return CouponEnum.USE_STATUS_OK.getMsg();
            case 2:
                return CouponEnum.USE_STATUS_EXPIRE.getMsg();
            case 3:
                return CouponEnum.USE_STATUS_VOID.getMsg();
        }
        return "未知";
    }

    /**
     * 发放方式描述
     *
     * @author Amos
     * @param code 类型
     * @return String
     */
    public static String getGetTypeMsg(Integer code) {
        switch (code) {
            case 0:
                return CouponEnum.GET_TYPE_USER.getMsg();
            case 1:
                return CouponEnum.GET_TYPE_SYS.getMsg();
        }
        return "未知";
    }

    /**
     * 适用商品类型描述
     *
     * @author Amos
     * @param code 类型
     * @return String
     */
    public static String getUseGoodsTypeMsg(Integer code) {
        switch (code) {
            case 0:
                return CouponEnum.USE_GOODS_TYPE_ALL.getMsg();
            case 1:
                return CouponEnum.USE_GOODS_TYPE_PART_YES.getMsg();
            case 2:
                return CouponEnum.USE_GOODS_TYPE_PART_NO.getMsg();
        }
        return "未知";
    }

    public static String getLimitTextArr(Integer code) {
        switch (code) {
            case 0:
                return CouponEnum.GET_LIMIT_NONE.getMsg();
            case 1:
                return CouponEnum.GET_LIMIT_NUM.getMsg();
            case 2:
                return CouponEnum.GET_LIMIT_DAY_NUM.getMsg();
        }
        return "未知";
    }

}
