package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 月度报告销售额
 * @author Amos
 */
@Data
public class MonthReportSalesVO {

    public static final long serialVersionUID = 1L;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date paymentTime;

    /**
     * 订单号
     */
    public String orderId;

    /**
     * 卖家店铺名称
     */
    public String shopName;

    /**
     * 商品名称
     */
    public String goodsName;

    /**
     * 运费
     */
    public Double orderShippingFee;

    /**
     * 红包面额
     */
    public Double redpacketPrice;

    /**
     * 应付金额（商品实际支付金额 + 运费）
     */
    public Double orderPaymentAmount;

    /**
     * 订单状态
     */
    public String orderStatus;

    /**
     * 支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付
     */
    public String payCode;

    /**
     * 买家名称
     */
    public String buyerUserName;

    /**
     * 订单完后时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date orderFinishedTime;

    /**
     * 支付单号
     */
    public String paymentNumber;

    /**
     * 第三方支付平台交易号
     */
    public String paymentOtherNumber;

    /**
     * 订单物流
     */
    public String orderShippingCode;

    /**
     * 订单来源
     */
    public String orderFrom;

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

    /**
     * 商品详情参数集合
     */
    public List<GoodsDetailsVO> goods;

    /**
     * 优惠价格（商品金额-支付金额）
     */
    public Double orderDiscountFee;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 退货状态
     */
    public Integer orderReturnStatus;

    /**
     * 退款状态
     */
    public Integer orderRefundStatus;

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
     * 1-实物；2-虚拟；3-门店
     */
    private Integer orderType;

    /**
     *
     */
    private Integer pastDue;

    /**
     * 过期退款 0 是 1 否
     */
    private Integer isCancel;

    /**
     *退货状态
     */
    private String returnStatus;

    /**
     *退款状态
     */
    private String refundStatus;

    /**
     *退货标志
     */
    private Integer returnFlag;

    /**
     * 订单创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date orderCreateTime;
}
