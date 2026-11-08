package com.houjiang.order.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 真实订单
 * @author Amos
 */
@Data
public class RealOrderSelfDTO extends PageHelper {

    /**
     * 订单id
     */
    private String orderId;
    /**
     * 私有成员变量paymentNumberk的注释说明
     * 此变量用于存储支付编号，支付编号是用户进行支付操作时生成的唯一标识。
     * 之所以选择私有成员变量的方式，是为了确保支付编号在类内部的封装性，
     * 通过这种方式，可以更好地控制支付编号的访问和修改，确保数据的安全性。
     */
    private String paymentNumberk;


    /**
     * 买家名称
     */
    private String buyerUserName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 开始时间
     */
    private String airTime;

    /**
     * 完成时间
     */
    private String finishTime;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 完成时间
     */
    private String endTime;

    /**
     * null 全部 1 待付款 3 代发货 4 已发货 6 已完成 7 已取消
     */
    private String orderStatus;

    /**
     * 订单开始时间
     */
    private String orderStartTime;

    /**
     * 订单完成时间
     */
    private String orderFinishTime;

    /**
     * 退货状态
     */
    private String orderReturnStatus;

    /**
     * 退款状态
     */
    private String orderRefundStatus;

    /**
     * not in 店舖
     */
    private String shopIdReal;

}
