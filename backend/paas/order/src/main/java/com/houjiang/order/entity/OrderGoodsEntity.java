package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName
 * @Description 订单商品表
 * @Author Amos
 * @Date 2023-05-31 11:41:48
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_goods")
@Data
public class OrderGoodsEntity implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer orderGoodsId;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 店铺ID
     */
    private Integer shopId;
    /**
     * 店铺名
     */
    private String shopName;
    /**
     * 发货店铺id
     */
    private Integer deliverShopId;
    /**
     * 发货店铺
     */
    private String deliverShopName;
    /**
     * 用户订单是否供应商代发货 1是  2否
     */
    private Integer dropShipping;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品common_id
     */
    private Integer commonId;
    /**
     * 买家id
     */
    private String buyerUserId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品对应的类目ID
     */
    private Integer goodsClassId;

    /**
     * 中文规格值
     */
    @TableField("order_spec_chineseValue")
    private String orderSpecChineseValue;
    /**
     * 商品价格（商品原价，goods_base中的商品价格未参加任何活动的价格）
     */
    private Double goodsPrice;
    /**
     * 商品数量
     */
    private String orderGoodsNum;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 退货数量
     */
    private String orderGoodsReturnnum;
    /**
     * 商品金额 （实付金额）= order_goods_payment_amount* order_goods_num
     */
    private Double orderGoodsAmount;
    /**
     * 优惠活动中平台承担的金额
     */
    private Double orderGoodsPlatformBear;
    /**
     * 优惠金额 = （商品原价-实付金额）*商品数量(废弃)
     */
    private Double orderGoodsDiscountFee;
    /**
     * 实付金额
     */
    private BigDecimal orderGoodsPaymentAmount;
    /**
     * 手工调整金额
     */
    private Double orderGoodsAdjustFee;
    /**
     * 订单商品的佣金 (总)(废弃)
     */
    private Double orderGoodsCommission;
    /**
     * 订单状态
     */
    private Integer orderGoodsStatus;
    /**
     * 评价状态 0为评价，1已评价
     */
    private Integer orderGoodsEvaluationStatus;
    /**
     * 红包抵扣金额
     */
    private Double orderRptPrice;
    /**
     * 代金券抵扣金额
     */
    private Double voucherPrice;
    /**
     * 退货状态:0是无退货,1是退货中,2是退货完成 3商家拒绝退货
     */
    private Integer goodsReturnStatus;
    /**
     * 退款状态：0：无退款 1：退款中 2：退款完成  3：商家拒绝退款
     */
    private Integer goodsRefundStatus;
    /**
     * 换货状态：0：无换货 1：申请换货中 2：换货完成  3：商家拒绝换货
     */
    private Integer goodsBarterStatus;
    /**
     * 创建时间
     */
    private Date orderGoodsTime;
    /**
     * 订单商品完成时间
     */
    private Date orderGoodsFinishTime;
    /**
     * 商品订单是否已经发货。1-是，0-否，默认0
     */
    private Integer orderGoodsIdDeliver;
    /**
     * 发货人的姓名
     */
    private String orderSellerName;
    /**
     * 发货人的地址
     */
    private String orderSellerAddress;
    /**
     * 发货人的联系方式
     */
    private String orderSellerContact;
    /**
     * 配送时间
     */
    private Date orderShippingTime;
    /**
     * 配送公司ID
     */
    private Integer orderShippingExpressId;
    /**
     * 物流单号
     */
    private String orderShippingCode;
    /**
     * 商品运费
     */
    private Double shippingFee;
    /**
     * 卖家备注
     */
    private String orderShippingMessage;
    /**
     * 活动ID 0 普通点单 1 砍价（废弃） 2 秒杀 3 限时折扣 4拼团 5 满减增折
     */
    private Integer promotionId;
    /**
     * 活动ID
     */
    private Integer goodsPromotionId;
    /**
     * 删除 0：不删除，1：删除
     */
    private Integer orderDel;
    /**
     * 时间戳
     */
    private Date ts;
    /**
     * 商品购买所需积分
     */
    private Integer integral;
    /**
     * 代发货订单 经销商佣金
     */
    private Double dealerCommission;
    /**
     * 经销商退还佣金
     */
    private Double dealerReturnCommission;
    /**
     * 代发货订单供应商佣金
     */
    private Double supplyCommission;
    /**
     * 供应商退还佣金
     */
    private Double supplyReturnCommission;
    /**
     * 会员折扣
     */
    private Double goodsMemberDiscount;
    /**
     * 商家拒绝退款时间
     */
    private Date businessRefundTime;
    /**
     * 商品总邮费
     */
    private Double totalGoodsShippingFee;
    /**
     * plus会员折扣金额
     */
    private Double goodsPlusDiscount;
    /**
     * 是否为plus商品  0 不是  1是
     */
    private Integer goodsPlusDis;

    /**
     * 满减活动折扣金额
     */
    private BigDecimal fullReductionDiscount;

    /**
     * 满折活动折扣金额
     */
    private BigDecimal fullFoldingDiscount;

    /**
     * 改价最初金额
     */
    private BigDecimal orderGoodsAdjustBeforePrice;
    /**
     * 活动单价
     */
    private BigDecimal activityUnitPrice;

    /**
     * 优惠券id
     */
    private Integer voucherId;

    /**
     * 发货数量
     */
    private Integer shipmentQuantity;

    /**
     * 发货状态 0待发货 1部分发货 2发货完成
     */
    private Integer shipmentStatus;

    /**
     * 订单商品退款金额
     */
    private BigDecimal goodsReturnPrice;

    /**
     * 订单商品版本号
     */
    private Integer verId;

    /**
     * 换货商品ID
     */
    private Integer barterGoodsId;

    /**
     * 换货物流id
     */
    private Integer orderBarterId;
    /**
     * 京东订单id
     */
    private String jdOrderId;
    /**
     * 京东子订单id
     */
    private String jdSonOrderId;
    /**
     * 京东 售后申请单号
     */
    private String jdReturenOrderId;

    /**
     * 积分比例
     */
    private Double pointsRate;


    /**
     * 现金金额
     */
    private BigDecimal cashPrice;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
