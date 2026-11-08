package com.houjiang.commons.constant;

/**
 * 订单模块中涉及到的常量
 * @author Amos
 */
public class OrderConstant {
    public static final String ORDER_TOKEN_PREFIX = "order:token";

    public static final String ROCKETMQ_ORDER_TOPIC = "ORDER-TOPIC";

    public static final String ROCKETMQ_SECKILL_ORDER_TOPIC = "SECKILL-ORDER-TOPIC";

    /** 用于选品中心创建订单时的redis锁的key */
    public static final String ORDER_SUPPLIER_CREATE = "ORDER_SUPPLIER_CREATE";

    /** 订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】 */
    public enum OrderStateEnum{
        /** 待付款 */
        FOR_THE_PAYMENT(0,"待付款"),
        /** 待发货 */
        TO_SEND_GOODS(1,"待发货"),
        /** 已发货 */
        HAS_BEEN_SHIPPED(2,"已发货"),
        /** 已完成 */
        HAS_BEEN_COMPLETED(3,"已完成"),
        /** 已关闭 */
        CLOSE(4,"已关闭"),
        /** 无效订单 */
        INVAILD_ORDERS(5,"无效订单");

        private int code;
        private String msg;
        OrderStateEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }
        public int getCode(){
            return  code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
