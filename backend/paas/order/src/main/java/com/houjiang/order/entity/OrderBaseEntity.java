package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName
 * @Description  订单详细信息
 * @Author Amos
 * @Date 2023-05-31 11:57:00
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_base")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderBaseEntity  implements Serializable {
    /**
     * 订单号
     */
    @TableId(type = IdType.AUTO)
    private String orderId;
    /**
     * 卖家店铺id
     */
    private Integer shopId;
    /**
     * 卖家门店id
     */
    private Integer orderGateShopId;
    /**
     * 卖家店铺名称
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
     * 买家id
     */
    private String buyerUserId;
    /**
     * 买家姓名
     */
    private String buyerUserName;
    /**
     * 卖家id
     */
    private String sellerUserId;
    /**
     * 卖家姓名
     */
    private String sellerUserName;
    /**
     * 订单日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date orderDate;
    /**
     * 订单生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;
    /**
     * 收货人的姓名
     */
    private String orderReceiverName;
    /**
     * 收货人的详细地址
     */
    private String orderReceiverAddress;
    /**
     * 收货人的联系方式
     */
    private String orderReceiverContact;
    /**
     * 收货时间（最晚收货时间）
     */
    private Date orderReceiverDate;
    /**
     * 收货人收货地址区域ID
     */
    private Integer orderReceiverProvinceId;
    /**
     * 收货人市ID（便于后期扩充）
     */
    private Integer orderReceiverCityId;
    /**
     * 收货人区ID（便于后期统计扩充）
     */
    private Integer orderReceiverAreaId;
    /**
     * 支付方式id
     */
    private String paymentId;
    /**
     * 支付方式名称
     */
    private String paymentName;
    /**
     * 支付(付款)时间
     */
    private Date paymentTime;
    /**
     * 支付单号
     */
    private String paymentNumber;
    /**
     * 第三方支付平台交易号
     */
    private String paymentOtherNumber;
    /**
     * 支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付,6-购物卡支付，7-充值卡+支付宝，8-充值卡+微信,9-蜂币支付，10-蜂币+支付宝，11-蜂币+微信,12-嘉币/劲豆-积分支付'
     */
    private Integer payCode;
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
     * 卖家备注
     */
    private String orderShippingMessage;
    /**
     * 订单完成时间
     */
    private Date orderFinishedTime;
    /**
     * 商品总价格(不包含运费)
     */
    private BigDecimal orderGoodsAmount;
    /**
     * 应付金额（商品实际支付金额 + 运费）
     */
    private BigDecimal orderPaymentAmount;
    /**
     * 优惠价格（商品金额-支付金额）
     */
    private BigDecimal orderDiscountFee;
    /**
     * 手工调整金额
     */
    private BigDecimal orderAdjustFee;
    /**
     * 运费价格
     */
    private BigDecimal orderShippingFee;
    /**
     * 买家评价状态 0-未评价 1-已评价
     */
    private Integer orderBuyerEvaluationStatus;
    /**
     * 订单留言
     */
    private String orderMessage;
    /**
     * 订单状态 1.待付款  3.待发货 4.已发货  6.确认收货 7.取消 8.退款中 9.已退款 11.待自提
12 虚拟订单已过期
     */
    private Integer orderStatus;
    /**
     * 优惠方式：0.无；1.代金券；2.红包；3.店铺折扣；4.天九会员；5.红包代金券一起使用
     */
    private Integer orderDiscountType;
    /**
     * 代金券面额
     */
    private Double voucherPrice;
    /**
     * 代金券编码
     */
    private String voucherCode;
    /**
     * 退款状态:0是无退款,1是退款中,2是退款完成,3退款不通过
     */
    private String orderRefundStatus;
    /**
     * 退货状态:0是无退货,1是退货中,2是退货完成，3退货不通过
     */
    private String orderReturnStatus;
    /**
     * 换货状态：0：无换货 1：申请换货中 2：换货完成  3：商家拒绝换货
     */
    private Integer orderBarterStatus;
    /**
     * 退款金额
     */
    private Double orderRefundAmount;
    /**
     * 手机端1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
     */
    private Integer orderFrom;
    /**
     * 交易佣金
     */
    private Double orderCommissionFee;
    /**
     * 交易佣金退款
     */
    private Double orderCommissionReturnFee;
    /**
     * 卖家删除
     */
    private Integer orderShopHidden;
    /**
     * 订单取消者身份   1-买家 2-卖家 3-系统
     */
    private Integer orderCancelIdentity;
    /**
     * 订单取消原因
     */
    private String orderCancelReason;
    /**
     * 订单取消时间
     */
    private Date orderCancelDate;

    /**
     * 红包编码
     */
    private String redpacketCode;
    /**
     * 红包面额
     */
    private Double redpacketPrice;
    /**
     * 红包抵扣订单金额
     */
    private Double orderRptPrice;
    /**
     * 订单结算时间
     */
    private Date orderSettlementTime;
    /**
     * 订单是否结算 1-已结算 0-未结算
     */
    private String orderIsSettlement;
    /**
     * 所在地,从店铺中同步，冗余检索使用
     */
    private Integer districtId;
    /**
     * 1.线上支付  2.线下支付 3.货到付款
     */
    private Integer paymentCode;
    /**
     * 活动ID 0 普通点单 1 砍价（废弃） 2 秒杀 3 限时折扣 4拼团 5 满减增折
     */
    private Integer orderPromotionId;
    /**
     * 北京光声_拼团状态1-拼团中 2-已成功 3未成功 0非拼团
     */
    private Integer groupbuyStatus;
    /**
     * 北京光声_限时抢购id、拼团id、砍价id
     */
    private String orderBuyId;
    /**
     * 0，未启用 1，启用
     */
    private Integer orderDelayStatus;
    /**
     * 结算单id
     */
    private String orderSettlementId;
    /**
     * 删除 0：不删除，1：删除
     */
    private Integer orderDel;
    /**
     * 时间戳
     */
    private Date ts;
    /**
     * 虚拟订单 0 是 1 否
     */
    private Integer virtualGoods;
    /**
     * 虚拟订单有效期
     */
    private Date periodOfValidity;
    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;
    /**
     * 虚拟商品兑换码
     */
    private String redeemCode;
    /**
     * 虚拟商品兑换二维码url
     */
    private String redeemCodeUrl;
    /**
     * 是否核销0是1否 2已过期
     */
    private Integer isCancel;
    /**
     * 核销时间
     */
    private Date cancelDate;
    /**
     * 积分商品订单 0是 1否
     */
    private Integer integralGoods;
    /**
     * 所需的积分
     */
    private Integer totalIntegral;
    /**
     * 是否是门店自提订单 1是 2否
     */
    private Integer orderIsShopTake;
    /**
     * 微客申请分享人id
     */
    private String vkShareUserId;
    /**
     * 待发货订单 经销商佣金
     */
    private Double dealerCommission;
    /**
     * 经销商退还佣金
     */
    private Double dealerReturnCommission;
    /**
     * 待发货订单供应商佣金
     */
    private Double supplyCommission;
    /**
     * 供应商退还佣金
     */
    private Double supplyReturnCommission;
    /**
     * 1普通订单   2商家批发订单(商家购买批发商商品订单)  3爆品绑定订单   4直播订单
     */
    private Integer orderType;
    /**
     * 用户订单是否供应商代发货 1是  2否
     */
    private Integer dropShipping;
    /**
     * 订单最后售后时间
     */
    private Date orderLastReturnTime;
    /**
     * 商品订单供货价
     */
    private Double orderSupplyAmount;
    /**
     * 单位
     */
    private String orderCompany;
    /**
     * 拼团的 团id
     */
    private String pintuanMarkid;
    /**
     * 满减优惠金额
     */
    private BigDecimal manjianDiscount;
    /**
     * 满折优惠金额
     */
    private BigDecimal manzheDiscount;
    /**
     * 赠品信息
     */
    private String giveawayInfo;
    /**
     * 会员折扣
     */
    private Double baseMemberDiscount;
    /**
     * 是否 经销 结算
     */
    private Integer orderSupplyIsSettlement;
    /**
     * 经销结算id
     */
    private String orderSupplySettlementId;
    /**
     * 经销结算时间
     */
    private Date orderSupplySettlementTime;
    /**
     * 开票状态:0-未开票;1-审核中;2-已出票;3-未通过
     */
    private Integer invoiceStatus;
    /**
     * 发票ID
     */
    private Integer invoiceId;
    /**
     * 秒杀订单取消时间
     */
    private Date seckillCancelTime;
    /**
     * 用户申请退款时间
     */
    private Date refundApplicationTime;
    /**
     * 用户申请退货时间
     */
    private Date refundApplicationGoodsTime;
    /**
     * 1.退款,2.仅退款,3退货退款
     */
    private Integer returnMethodType;
    /**
     * plus会员折扣
     */
    private Double plusDiscount;
    /**
     * 是否为plus订单 0 否 1 是
     */
    private Integer plusOrder;

    /**
     * 经销折扣
     */
    private BigDecimal distributionDiscount;

    /**
     * 是否同城配送0-否 1-是
     */
    private Integer isDelivery;

    /**
     * 骑手-门店ID
     */
    private Integer deliveryChainId;

    /**
     * 骑手id
     */
    private String distributorId;

    /**
     * 是否评价骑手0-否 1-是
     */
    private Integer distributorEvaluation;

    /**
     * 是否骑手删除0-否 1-是
     */
    private Integer isDeliveryDel;

    /**
     * 电话
     */
    private String mobile;

    /**
     * 0 待取货 1待配送
     */
    private Integer pickup;

    /**
     * 服务订单支付次数
     */
    private Integer serviceNum;

    /**
     * 提货编号
     */
    private String pickupNum;

    /**
     * 骑手接单时间
     */
    private String deliveryTime;

    /**
     * 骑手完成时间
     */
    private String deliveryEndTime;
    /**
     * 最终 配送费 （消费者）
     */
    private BigDecimal endFree;
    /**
     * 加配送费
     */
    private String deliveryCharge;

    /**
     * 商家是否确认 0否 1是
     */
    private Integer shopAffirm;

    /**
     * 组合活动id
     */
    private Integer combinationId;

    /**
     * 组合活动类型 1固定组合 2搭配组合
     */
    private Integer combinationType;

    /**
     * 组合活动价格
     */
    private BigDecimal combinationPrice;

    /**
     * 是否是礼包订单 0否 1是
     */
    private Integer isGiftOrder;

    /**
     * 进货编号
     */
    private String purchaseNumber;

    /**
     * 一件代发商品拆分前的原始订单号
     */
    private String originalOrder;

    /**
     * 订单来源:0:自营订单,1:代发平台供应链订单,2:代发怡亚通供应链订单 3 京东
     */
    private Integer orderSource;
    /**
     * 国家id
     */
    private Integer countryId;
    /**
     * 国家名称
     */
    private String countryName;
    /**
     * 国际区号
     */
    private String internationalDialingCode;
    /**
     * 货币符号
     */
    private String currencySymbol;
    /**
     * 汇率
     */
    private BigDecimal exchangeRate;
    /**
     * 货币代码
     */
    private String currencyCode;
    /**
     * 京东订单号
     */
    private String jdOrderId;



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


    /**
     * 商品集合
     */
    @TableField(exist = false)
    private List<OrderGoodsEntity> goodsList;

    /**
     * 商品详情集合
     */
    @TableField(exist = false)
    private List<GoodsDetailsListVO> goodsDetailsList;
    /**
     * 发票类型: 0-普票；1-增值税专用票
     */
    @TableField(exist = false)
    private Integer invoiceType;
    /**
     * 发票开关审核状态:开票状态:0-未开票;1-审核中;2-已出票;3-未通过
     */
    @TableField(exist = false)
    private Integer invoiceAuditStatus;

    /**
     * 发票功能开关状态:0-关闭;1-开启
     */
    @TableField(exist = false)
    private Integer invoiceSwitchStatus;


    /**
     * 是否过期
     */
    @TableField(exist = false)
    private Integer isOutDate;

    /**
     * 总数量
     */
    @TableField(exist = false)
    private Integer goodsNum;

    /**
     * 商品Id
     */
    @TableField(exist = false)
    private Integer goodsId;

    /**
     * 拼团剩余人数
     */
    @TableField(exist = false)
    private Integer numberRemaining;

    /**
     * 店铺总价格
     */
    @TableField(exist = false)
    private BigDecimal shopOrderTotalPrice;

    /**
     * 退款实付价格
     */
    @TableField(exist = false)
    private BigDecimal refundRealPrice;

    /**
     * 总价格
     */
    @TableField(exist = false)
    private BigDecimal orderTotalPrice;


    /**
     * 配送公司名称
     */
    @TableField(exist = false)
    private String orderShippingExpress;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 收货人联系地址
     */
    @TableField(exist = false)
    private String orderReceiverDetails;
}
