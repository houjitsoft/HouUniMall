package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.im.ImConfigVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单参数
 *
 * @author Amos
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderListVO {
	/**
	 * 用户id
	 */
	private String userId;
	/**
	 * 购买人id
	 */
	private String buyerUserId;

	/**
	 * 订单id
	 */
	private String orderId;

	/**
	 * 店铺id
	 */
	private Integer shopId;

	/**
	 * 店铺名
	 */
	private String shopName;

	/**
	 * 卖家门店id
	 */
	private Integer orderGateShopId;

	/**
	 * 卖家门店名
	 */
	private String orderGateName;

	/**
	 * 支付方式
	 */
	private Integer payCode;

	/**
	 * 总数量
	 */
	private Integer goodsNum;

	/**
	 * 拼团Markid
	 */
	private String orderBuyId;

	/**
	 * 拼团剩余人数
	 */
	private Integer numberRemaining;

	/**
	 * 用户订单是否供应商代发货 1是  2否
	 */
	private Integer dropShipping;

	/**
	 * 拼团状态
	 */
	private Integer groupBuyStatus;

	/**
	 * 订单状态
	 */
	private Integer orderStatus;

	/**
	 * 退款金额
	 */
	private BigDecimal returnCash;

	/**
	 * 支付单号
	 */
	private String paymentNumber;

	/**
	 * 下单时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date orderCreateTime;

	/**
	 * 发货时间
	 */
	private String orderShippingTime;

	/**
	 * 收货人名称
	 */
	private String orderReceiverName;

	/**
	 * 退款实付价格
	 */
	private BigDecimal refundRealPrice;

	/**
	 * 总价格
	 */
	private BigDecimal orderTotalPrice;

	/**
	 * 物流单号
	 */
	private String orderShippingCode;

	/**
	 * 订单延迟状态
	 */
	private Integer orderDelayStatus;

	/**
	 * 收货人联系方式
	 */
	private String orderReceiverContact;

	/**
	 * 收货人联系地址
	 */
	private String orderReceiverAddress;

	/**
	 * 运费价格
	 */
	private BigDecimal orderShippingFee;

	/**
	 * 退款状态
	 */
	private Integer orderRefundStatus;

	/**
	 * 退货状态
	 */
	private Integer orderReturnStatus;

	/**
	 * 商品实付
	 */
	private BigDecimal orderGoodsAmount;

	/**
	 * 店铺总价格
	 */
	private BigDecimal shopOrderTotalPrice;

	/**
	 * 退款金额
	 */
	private BigDecimal orderRefundAmount;

	/**
	 * 实付总价
	 */
	private BigDecimal orderPaymentAmount;

	/**
	 * 评价状态0未评价，1已评价
	 */
	private Integer orderBuyerEvaluationStatus;

	/**
	 * 订单最后售后时间
	 */
	private String orderLastReturnTime;

	/**
	 * 是否是虚拟商品
	 */
	private Integer virtualGoods;

	/**
	 * 虚拟订单有效期
	 */
	private String periodOfValidity;

	/**
	 * 虚拟订单是否过期1:过期,0:未过期
	 */
	private Integer expire=0;

	/**
	 * 过期退货0是1否
	 */
	private Integer pastDue;

	/**
	 * 虚拟商品兑换码
	 */
	private String redeemCode;

	/**
	 * 虚拟商品兑换二维码
	 */
	private String redeemCodeUrl;

	/**
	 * 是否核销0是1否2已过期
	 */
	private Integer isCancel;

	/**
	 * 积分商品订单0是1否
	 */
	private Integer integralGoods;

	/**
	 * 所需的积分
	 */
	private Integer totalIntegral;

	/**
	 * 1.线上支付2.线下支付3.货到付款4.账期支付
	 */
	private Integer paymentCode;

	/**
	 * 商品列表
	 */
	private List<GoodsDetailsListVO> goodsDetailsList;

	/**
	 * 商品数量
	 */
	private Integer goodsDetailsCount;

	/**
	 * 订单店铺总税费
	 */
	private BigDecimal shopTaxPrice;

	/**
	 * 订单店铺总未税价格
	 */
	private BigDecimal shopWeiShuiPrice;

	/**
	 * 订单店铺总会员折扣价格
	 */
	private BigDecimal shopZheKouPrice;

	/**
	 * 订单总含税价格
	 */
	private BigDecimal shopTotalPrice;

	/**
	 * 订单未折扣含税价钱
	 */
	private Integer shopMoPrice;

	/**
	 * 买家
	 */
	private String buyerUserName;

	/**
	 * 银行账号
	 */
	private String bankAccount;

	/**
	 * 开户银行
	 */
	private String openingBank;

	/**
	 * 企业名称
	 */
	private String businessName;

	/**
	 * 订单最晚支付时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date orderEndTime;

	/**
	 * 是否审批
	 */
	private Integer isApproval;

	/**
	 * 平台处理结果 1 买家胜利  0卖家胜利 2 用户撤销
	 */
	private Integer processingResults;

	/**
	 * 1.退款,2.仅退款,3退货退款
	 */
	private Integer returnMethodType;

	/**
	 * 平台审核标志
	 */
	public Integer platformAudit;

	/**
	 * 平台介入ID
	 */
	private Integer platformInterventionId;

	/**
	 * 介入状态
	 */
	private Integer interventionState;

	/**
	 * 审核状态  0 待上传   1 待审核 2 已审核 3 审核驳回
	 */
	private Integer reviewStatus;

	private Long orderCancelTime;

	/**
	 * 是否再来一单
	 */
	private Boolean isAnotherOrder;
	/**
	 * 投诉id
	 */
	private Integer id;
	/**
	 * 投诉单状态
	 */
	private Integer complaintsStatus;
	/**
	 * 满减赠赠品信息json
	 */
	private String giveawayInfo;

	/**
	 * 满减赠赠品信息
	 */
	private List<FullReductionGiftsVO> zengpinInfo;

	/**
	 * 满减优惠金额
	 */
	private BigDecimal manjianDiscount;

	/**
	 * 满折优惠金额
	 */
	private BigDecimal manzheDiscount;

	/**
	 * 优惠金额
	 */
	private BigDecimal orderDiscountFee;

	/**
	 * 秒杀取消时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date seckillCancelTime;

	/**
	 * 0待取货 1已取货，待配送
	 */
	private Integer pickup;
	/**
	 * 骑手id
	 */
	private String distributorId;
	/**
	 * 是否评价骑手0-否 1-是
	 */
	private Integer distributorEvaluation;
	/**
	 * 经度
	 */
	private String longitude;
	/**
	 * 纬度
	 */
	private String latitude;

	/**
	 * 是否为礼包订单 1是 0否
	 */
	private Integer isGiftOrder;
	/**
	 * im配置
	 */
	private ImConfigVO imConfigVO;

	private String orderSource;

	/**
	 * 平台退款凭证
	 */
	private List<String> returnPaymentVoucherList;

	/**
	 * 换货状态：0：无换货 1：申请换货中 2：换货完成  3：商家拒绝换货
	 */
	private Integer orderBarterStatus;




	/**
	 * 嘉币支付的现金部分
	 */
	private BigDecimal orderCashPayment;
}
