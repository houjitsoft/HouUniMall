package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 查询发货订单详情
 *
 * @author Amos
 *
 */
@Data
public class ShipmentsByOrderBaseVO {

	/**
	 * 订单ID
	 */
	private String orderId;

	/**
	 * 支付码
	 */
	private Integer paymentCode;

	/**
	 * 订单状态
	 */
	private Integer orderStatus;

	/**
	 * 快递拼音简写
	 */
	private String expressPinyin;

	/**
	 * 买家用户名
	 */
	private String buyerUserName;

	/**
	 * 订单创建时间
	 */
	private String orderCreateTime;

	/**
	 * 订单物流单号
	 */
	private String orderShippingCode;

	/**
	 * 订单收货人姓名
	 */
	private String orderReceiverName;

	/**
	 * 订单退款状态
	 */
	private Integer orderRefundStatus;

	/**
	 * 订单退货状态
	 */
	private Integer orderReturnStatus;

	/**
	 * 换货状态：0：无换货 1：申请换货中 2：换货完成  3：商家拒绝换货
	 */
	private Integer orderBarterStatus;

	/**
	 * 订单收货人地址
	 */
	private String orderReceiverAddress;

	/**
	 * 订单收货人联系方式
	 */
	private String orderReceiverContact;

	/**
	 * 订单运费金额（BigDecimal类型用于存储浮点数）
	 */
	private BigDecimal orderShippingFee;

	private List<ShipmentsByOrderGoodsDetailsVO> shipmentsByOrderGoodsDetails;

	/**
	 * 用户订单是否供应商代发货 1是  2否
	 */
	private Integer dropShipping;

	/**
	 * 发货物流id
	 */
	private Integer orderShippingExpressId;

	/**
	 * 物流名
	 */
    private String expressName;

	/**
	 *订单运输信息
	 */
	private String orderShippingMessage;

	/**
	 * 满减赠赠品信息 json
	 */
	private String giveawayInfo;

	/**
	 * 满减赠赠品信息
	 */
	private List<ManJianZengPinVO> zengpinInfo;

	/**
	 * 发货店铺名称（可能为快递公司名称）
	 */
	private String deliverShopName;

	/**
	 * 店铺名称
	 */
	private String shopName;

	/**
	 * 应付金额（商品实际支付金额 + 运费）
	 */
	private BigDecimal orderPaymentAmount;

	/**
	 * 商品总数量
	 */
	private Integer orderGoodsNum;

	/**
	 * 订单折扣费用
	 */
	private BigDecimal orderDiscountFee;

	/**
	 * 一件代发商品拆分前的原始订单号
	 */
	private String originalOrder;

	/**
	 * 1自销 2经销
	 */
	private Integer goodsType;

	private String dealerShopName;

	private Integer orderSource;
}
