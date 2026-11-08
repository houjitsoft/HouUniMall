package com.houjiang.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 卖出订单展示所需参数
 *
 * @author Amos
 *
 */
@Data
public class SellingOrderVO implements Serializable {

	/**
	 * 订单ID
	 */
	private String orderId;

	/**
	 * 订单来源
	 */
	private String orderFrom;

	/**
	 * 订单接收者姓名
	 */
	private String orderReceiverName;

	/**
	 * 订单创建时间
	 */
	private String orderCreateTime;

	/**
	 * 订单支付金额
	 */
	private BigDecimal orderPaymentAmount;

	/**
	 * 订单状态
	 */
	private String orderStatus;

	/**
	 * 订单退款状态
	 */
	private String orderRefundStatus;

	/**
	 * 订单退货状态
	 */
	private String orderReturnStatus;

	/**
	 * 是否存在退货
	 */
	private String isExistReturn;

	/**
	 * 支付单号
	 */
	private String paymentNumber;

	/**
	 * 支付方式名称
	 */
	private String paymentName;

	/**
	 * 支付码
	 */
	private String payCode;

	/**
	 * 支付时间
	 */
	private String paymentTime;

	/**
	 * 订单物流信息编码
	 */
	private String orderShippingCode;

	/**
	 * 订单退款金额
	 */
	private BigDecimal orderRefundAmount;

	/**
	 * 订单完成时间
	 */
	private String orderFinishedTime;

	/**
	 * 订单买家评价状态
	 */
	private String orderBuyerEvaluationStatus;

	/**
	 * 店铺ID
	 */
	private Integer shopId;

	/**
	 * 店铺名称
	 */
	private String shopName;

	/**
	 * 买家用户ID
	 */
	private String buyerUserId;

	/**
	 * 买家用户名
	 */
	private String buyerUserName;

	/**
	 * 订单收货地址
	 */
	private String orderReceiverAddress;

	/**
	 * 订单收货人联系电话
	 */
	private String orderReceiverContact;

	/**
	 * 商品名称
	 */
	private String goodsName;

	/**
	 * 订单商品描述的中文翻译值
	 */
	private String orderSpecChineseValue;

	/**
	 * 订单商品数量
	 */
	private Integer orderGoodsNum;
	/**
	 * 交易支付金额，
	 */
	public double tradePaymentAmount;

	/**
	 * 取消日期，
	 */
	public String canceldate;

	/**
	 * 交易充值卡支付金额，是一个公共的BigDecimal类型的变量，可以在类或者类的实例之间共享和访问。
	 */
	public BigDecimal tradePaymentRechargeCard;

	/**
	 * 第三方支付金额，是一个公共的BigDecimal类型的变量，可以在类或者类的实例之间共享和访问。
	 */
	public BigDecimal tradeThirdPartyAmount;

	/**
	 * 团购状态，是一个公共的String类型的变量，可以在类或者类的实例之间共享和访问。
	 */
	public String groupbuyStatus;

	/**
	 * 订单总金额，是一个公共的BigDecimal类型的变量，可以在类或者类的实例之间共享和访问。
	 */
	public BigDecimal orderAmount;

	/**
	 * 其他支付编号，是一个公共的String类型的变量，可以在类或者类的实例之间共享和访问。
	 */
	public String paymentOtherNumber;

	/**
	 * 运送地址补充信息，是一个私有的String类型的变量，不可以直接从类或者类的实例之外访问。
	 */
	private String shippingAdd;

	/**
	 * 运送地址联系人，是一个私有的String类型的变量，不可以直接从类或者类的实例之外访问。
	 */
	private String shippingAddressContact;

	/**
	 * 运送地址电话，是一个私有的String类型的变量，不可以直接从类或者类的实例之外访问。
	 */
	private String shippingAddressPhone;

	/**
	 * 订单配送时间，是一个私有的String类型的变量，不可以直接从类或者类的实例之外访问。
	 */
	private String orderShippingTime;

	/**
	 * 评价状态，是一个私有的Integer类型的变量，不可以直接从类或者类的实例之外访问。
	 */
	private Integer reviewStatus;

	/**
	 * 支付来源，是一个私有的String类型的变量，不可以直接从类或者类的实例之外访问。
	 */
	private String payFrom;

	/**
	 * 订单类型
	 */
	private Integer orderType;

	/**
	 * 逾期天数
	 */
	private Integer pastDue;

	/**
	 * 是否取消订单
	 */
	private Integer isCancel;

	/**
	 * 虚拟商品数量
	 */
	private Integer virtualGoods;

	/**
	 * 是否需要商家自提
	 */
	private Integer orderIsShopTake;

	/**
	 * 退货标识
	 */
	private Integer returnFlag;
	/**
	 * 是否同城配送2-否 1-是
	 */
	private Integer isDelivery;
	/**
	 * 骑手id
	 */
	private String distributorId;
	/**
	 * 0待取货 1已取货，待配送
	 */
	private Integer pickup;
	/**
	 * 1.线上支付  2.线下支付 3.货到付款
	 */
	private Integer paymentCode;




	/**
	 * 嘉币支付的现金部分
	 */
	private BigDecimal orderCashPayment;


	/**
	 * 外部
	 */
	private Integer outShopId;


	/**
	 * 1-用家，2-批发
	 */
	private Integer customerType;
}
