package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class OrderbaseDetailsparaVO {
	/**
	 * 支付单号
	 */
	private Integer paymentCode;
	/**
	 * 店铺名称
	 */
	private String shopName;
	/**
	 *订单状态
	 */
	private int orderStatus;
	/**
	 *订单号
	 */
	private String orderId;
	/**
	 *运费
	 */
	private double orderShippingFee;
	/**
	 *下单时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date orderCreateTime;
	/**
	 *实付金额
	 */
	private double orderPaymentAmount;
	/**
	 *代金券金额
	 */
	private double voucherPrice;
	/**
	 *红包金额
	 */
	private double redpacketPrice;
	/**
	 *
	 */
	private List<GoodsAllByidVO> GoodsAllByidParam;
	/**
	 *买家
	 */
	private String buyerUserName;
	/**
	 *收货人
	 */
	private String orderReceiverName;
	/**
	 *联系方式
	 */
	private String orderReceiverContact;
	/**
	 *收获地址
	 */
	private String orderReceiverAddress;
	/**
	 *发票信息
	 */
	private String orderInvoice;
	/**
	 *买家留言
	 */
	private String orderMessage;
	/**
	 *店主名称
	 */
	private String userName;
	/**
	 *联系方式
	 */
	private String shopTel;
	/**
	 *发货地址
	 */
	private String orderSellerAddress;
	/**
	 *发货时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date orderShippingTime;
	/**
	 *快递公司
	 */
	private String expressName;
	/**
	 *物流单号
	 */
	private String orderShippingCode;
	/**
	 *订单价格
	 */
	private double orderRptPrice;
	/**
	 *商品总金额
	 */
	private double orderGoodsAmount;
	/**
	 *平台优惠券折扣金额
	 */
	private double platformCouponRptPrice;
	/**
	 * 是否是积分商品0是1否
	 */
	private Integer integralGoods;
	/**
	 * 省id
	 */
	private String provinceId;

	/**
	 * 总积分
	 */
	private Integer totalIntegral;

	/**
	 * 经销商佣金
	 */
	private BigDecimal dealerCommission;

	/**
	 * 经销商返佣
	 */
	private BigDecimal dealerReturnCommission;

	/**
	 * 供应商佣金
	 */
	private BigDecimal supplyCommission;

	/**
	 * 供应商返佣
	 */
	private BigDecimal supplyReturnCommission;

	/**
	 * ODB支付凭证对象
	 */
	private OdbPaymentVocherVO odbPaymentVocherPO;

	/**
	 * 订单退货状态
	 */
	private Integer orderReturnStatus;

	/**
	 * 订单退款状态
	 */
	private Integer orderRefundStatus;

	/**
	 * 订单退款金额
	 */
	private BigDecimal orderRefundAmount;

	/**
	 * 订单是否店取
	 */
	private Integer orderIsShopTake;

	/**
	 * 虚拟商品数量
	 */
	private Integer virtualGoods;

	/**
	 * 逾期天数
	 */
	private Integer pastDue;

	/**
	 * 是否取消订单
	 */
	private Integer isCancel;

	/**
	 * 订单类型
	 */
	private Integer orderType;

	/**
	 * 退货标识
	 */
	private Integer returnFlag;
	/**
	 * 退货状态
	 */
	private Integer returnStatus;

	/**
	 * 退款状态：0-未退款；1-已退款；2-退款中；3-退款成功；4-拒绝退款
	 */
	private Integer refundStatus;

	/**
	 * 支付单号
	 */
	private String paymentNo;

	/**
	 * 支付时间，格式为：yyyy-MM-dd HH:mm:ss，时区为GMT+8
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date paymentTime;
	/**
	 * 商品总件数
	 */
	private Integer goodsNum;
	/**
	 * 平台总优惠
	 */
	private BigDecimal totalPlatformDiscount;
	/**
	 * 商家总优惠
	 */
	private BigDecimal totalMerchantDiscount;
	/**
	 * 预计获得的积分
	 */
	private Integer pointsObtained;

	/**
	 * 0 不是plus订单 1 是plus订单
	 */
	private Integer plusOrder;

	/**
	 * 组合活动类型 1固定组合 2搭配组合
	 */
	private Integer combinationType;

	/**
	 * 组合活动价格
	 */
	private BigDecimal combinationPrice;

	/**
	 * 折扣金额
	 */
	private BigDecimal orderDiscountFee;
	/**
	 *  支付方式1-余额支付，2-支付宝支付，3-微信支付，6-购物卡支付，12 线下支付 16-paypal支付 17-0元兑换券
	 */
	private Integer payCode;
	/**
	 * 发票 信息
	 */
	private SellerOrderInvoiceDetailVO sellerOrderInvoiceDetailVO;
}
