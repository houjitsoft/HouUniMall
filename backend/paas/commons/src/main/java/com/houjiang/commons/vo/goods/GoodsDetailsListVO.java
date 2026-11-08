package com.houjiang.commons.vo.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 商品详情信息
 *
 * @author Amos
 *
 */
@Data
public class GoodsDetailsListVO {
	/**
	 * 通用ID
	 */
	private Integer commonId;

	/**
	 * 商品ID
	 */
	private Integer goodsId;

	/**
	 * 商品图片
	 */
	private String goodsImage;

	/**
	 * 商品名称
	 */
	private String goodsName;

	/**
	 * 商品库存
	 */
	private Integer goodsStock;

	/**
	 * 商品状态 0下架，1正常，10违规（禁售）
	 */
	private Integer commonState;

	/**
	 * 商品价格
	 */
	private BigDecimal goodsPrice;

	/**
	 * 商品邮费
	 */
	private BigDecimal shippingFee;

	/**
	 * 订单邮费
	 */
	private BigDecimal orderShippingFee;

	/**
	 * 商品实付金额
	 */
	private BigDecimal orderGoodsPaymentAmount;

	/**
	 * 订单实付金额
	 */
	private BigDecimal orderPaymentAmount;

	/**
	 * 商品数量
	 */
	private Integer orderGoodsNum;

	/**
	 * 退货数量
	 */
	private Integer orderGoodsReturnnum;

	/**
	 * 商品金额（实付金额）= order_goods_payment_amount * order_goods_num
	 */
	private BigDecimal orderGoodsAmount;

	/**
	 * 规格值
	 */
	private String orderSpecChineseValue;

	/**
	 * 评价状态
	 */
	private Integer orderGoodsEvaluationStatus;

	/**
	 * 所需积分
	 */
	private Integer needIntegral;

	/**
	 * 商品积分
	 */
	private Integer goodsIntegral;

	/**
	 * 订单物流单号
	 */
	private String orderShippingCode;

	/**
	 * 商品类型
	 */
	private Integer goodsType;

	/**
	 * 活动类型 0普通订单 1砍价（废弃）2秒杀 3限时折扣 4拼团 5满减赠 6满减折 7固定组合 8搭配组合
	 */
	private Integer promotionId;

	/**
	 * 店铺ID
	 */
	private String shopId;

	/**
	 * 订单ID
	 */
	private String orderId;

	/**
	 * 店铺名称
	 */
	private String shopName;

	/**
	 * 订单创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date orderCreateTime;

	/**
	 * 是否上架 1:上架
	 */
	private Integer goodsIsShelves;

	/**
	 * 发货时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date orderShippingTime;

	/**
	 * 订单状态
	 */
	private Integer orderStatus;

	/**
	 * 配送时间
	 */
	private Integer deliveryTime;

	/**
	 * 是否发货 0未发货 1发货
	 */
	private Integer orderGoodsIdDeliver;

	/**
	 * 售后状态
	 */
	private Integer returnState;

	/**
	 * 订单退款状态
	 */
	private Integer orderRefundStatus;

	/**
	 * 订单退货状态
	 */
	private Integer orderReturnStatus;

	/**
	 * 订单换货状态
	 */
	private Integer orderBarterStatus;

	/**
	 * 1：会员商品，2：阶梯价商品
	 */
	private Integer hyOrStep;

	/**
	 * 含税单价
	 */
	private BigDecimal goodsTaxPrice;

	/**
	 * 未税单价
	 */
	private BigDecimal goodsWeiPrice;

	/**
	 * 规格
	 */
	private String goodsSpec;

	/**
	 * 单位
	 */
	private String dw;

	/**
	 * 税率
	 */
	private Double taxRate;

	/**
	 * 订货编码
	 */
	private String goodsCode;

	/**
	 * 是否询价
	 */
	private Integer isEnquiry;

	/**
	 * 发货数量
	 */
	private Integer shipmentQuantity;

	/**
	 * 发货状态 0待发货 1部分发货 2发货完成
	 */
	private Integer shipmentStatus;

	/**
	 * 订单商品版本号
	 */
	private Integer verId;

	/**
	 * 换货商品ID
	 */
	private Integer barterGoodsId;

	/**
	 * 换货商品信息
	 */
	private OrderGoodsBarterVO barterGoods;

	/**
	 * 换货商品数量
	 */
	private Integer barterGoodsNum;

	/**
	 * 物流信息（保留最小状态值对应的信息）
	 */
	private Map<String, String> delivery;


	/**
	 * 积分比例
	 */
	private Double pointsRate;



	/**
	 * 现金金额
	 */
	private BigDecimal cashPrice;
}
