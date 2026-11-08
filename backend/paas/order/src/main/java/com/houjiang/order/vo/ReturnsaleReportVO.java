package com.houjiang.order.vo;

import lombok.Data;

/**
 * 退货报告
 * @author Amos
 */
@Data
public class ReturnsaleReportVO {

    /**
     * 订单编号
     */
    public String orderId;

    /**
     * 店铺名称
     */
    public String shopName;

    /**
     * 退货编号
     */
    public String returnCode;

    /**
     * 退款金额
     */
    public Double returnCash;

    /**
     * 买家名称
     */
    public String buyerUserName;

    /**
     * 买家账号
     */
    public String buyerUserAccount;

    /**
     * 支付单号
     */
    public String paymentNumber;

    /**
     * 第三方支付平台交易号
     */
    public String paymentOtherNumber;

    /**
     * 应付金额（商品实际支付金额 + 运费）
     */
    public Double orderPaymentAmount;

    /**
     * 支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付
     */
    public String payCode;

    /**
     * 1-新发起等待卖家审核 2-卖家审核通过 3-卖家审核不通过 4-卖家收到货物 5-平台审核通过
     */
    public String returnState;

    /**
     * 1-退款申请 2-退货申请 3-虚拟退款
     */
    public String returnType;

    /**
     * 添加时间
     */
    public String returnAddTime;

    /**
     * 商家处理时间
     */
    public String returnShopTime;

    /**
     * 退款完成时间
     */
    public String returnFinishTime;

    /**
     * 商品名
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private String goodsPrice;

    /**
     * 订单商品数量
     */
    private Integer orderGoodsNum;

    /**
     * 退货数量
     */
    private Integer orderGoodsReturnnum;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 贸易付款金额
     */
    public Double tradePaymentAmount;

    /**
     * 卡支付金额
     */
    public Double tradePaymentRechargeCard;

    /**
     * 第三方支付金额
     */
    public Double tradeThirdPartyAmount;


}
