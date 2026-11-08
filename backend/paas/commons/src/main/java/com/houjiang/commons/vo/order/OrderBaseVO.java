package com.houjiang.commons.vo.order;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName
 * @Description  订单详细信息
 * @Author Amos
 * @Date 2023-05-31 11:57:00
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderBaseVO {
    /**
     * 订单号
     */
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
    private Date orderDate;
    /**
     * 订单生成时间
     */
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
     * 支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付,6-购物卡支付，7-充值卡+支付宝，8-充值卡+微信,9-蜂币支付，10-蜂币+支付宝，11-蜂币+微信'
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderFinishedTime;
    /**
     * 发票信息
     */
    private String orderInvoice;
    /**
     * 发票id
     */
    private Integer orderInvoiceId;
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
     * 供应商实付金额 含运费
     */
    private BigDecimal goodsPaymentAmount;
    /**
     * 手工调整金额
     */
    private BigDecimal orderAdjustFee;
    /**
     * 买家使用积分
     */
    private Double orderPointFee;
    /**
     * 运费价格
     */
    private Double orderShippingFee;
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
     * 订单赠送积分
     */
    private Integer orderPointsAdd;
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
     * 虚拟订单
     */
    private String orderIsVirtual;
    /**
     * 虚拟商品兑换码
     */
    private String orderVirtualCode;
    /**
     * 虚拟商品是否使用 0-未使用 1-已使用
     */
    private Integer orderVirtualUse;
    /**
     * 卖家删除
     */
    private Integer orderShopHidden;
    /**
     * 买家删除
     */
    private Integer orderBuyerHidden;
    /**
     * 主管账号删除
     */
    private Integer orderSubuserHidden;
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
     * 门店id
     */
    private Integer chainId;
    /**
     * 卖家给卖家留言
     */
    private String orderSellerMessage;
    /**
     * 是否是分佣订单
     */
    private String directsellerFlag;
    /**
     * 推广员上级
     */
    private Integer directsellerPId;
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
     * 跨店满减券id(购物津贴)
     */
    private String platformCouponCode;
    /**
     * 跨店满减面额(购物津贴)
     */
    private BigDecimal platformCouponPrice;
    /**
     * 跨店满减抵扣金额(购物津贴)
     */
    private Double platformCouponRptPrice;
    /**
     * 订单结算时间
     */
    private Date orderSettlementTime;
    /**
     * 订单是否结算 1-已结算 0-未结算
     */
    private String orderIsSettlement;
    /**
     * 分销商
     */
    private String shopDistributorId;
    /**
     * SO订单分销类型 1:直销(E)  2:分销代销转发销售(P, SP)
     */
    private String orderDistributionSellerType;
    /**
     * PO订单类型 1:购买(E采购，SP:代销采购)  2:分销采购,代客下单 (P开头)
     */
    private String orderDistributionBuyerType;
    /**
     * 源订单Id（P开头）:SP开头订单对应的P开头订单
     */
    private String orderSourceId;
    /**
     * 推广员上级的上级
     */
    private Integer directsellerGpId;
    /**
     * 推广员
     */
    private Integer directsellerId;
    /**
     * 分销佣金是否结算 1-已经结算 0-未结算
     */
    private String directsellerIsSettlement;
    /**
     * 所在地,从店铺中同步，冗余检索使用
     */
    private Integer districtId;
    /**
     * 0：自己支付，1：主管账号支付
     */
    private Integer orderSubPay;
    /**
     * 付款主账号id
     */
    private Integer orderSubUser;
    /**
     * 分销员三级总佣金
     */
    private BigDecimal orderDirectsellerCommission;
    /**
     * 等级折扣金额
     */
    private BigDecimal directsellerDiscount;
    /**
     * 推广员用户ID
     */
    private Integer promoterUserId;
    /**
     * 上级店铺ID
     */
    private Integer parentShopId;
    /**
     * 推荐注册的城市分站ID(该笔订单由城市分站管理员发展的用户购买）
     */
    private String inviteSubsiteId;
    /**
     * 满减活动平台和商家共同承担折扣金额 此为平台承担的折扣金额
     */
    private Double orderShopBear;
    /**
     * 满减活动平台和商家共同承担折扣金额 此为平台承担的折扣金额
     */
    private Double orderPlatformBear;
    /**
     * 1.线上支付  2.线下支付 3.货到付款
     */
    private Integer paymentCode;
    /**
     * 赠品订单上级店铺id
     */
    private Integer treasureOrderShopId;
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
     * 退还新人随机立减金额
     */
    private Double orderRanderectReturn;
    /**
     * 订单来源 1 上海讯有 2 北京
     */
    private Integer orderOperator;
    /**
     * 0，未启用 1，启用
     */
    private Integer orderDelayStatus;
    /**
     * 结算单id
     */
    private String orderSettlementId;
    /**
     * 订单是否是小宝订单。1：是，0不是
     */
    private Integer isSmallorder;
    /**
     * 删除 0：不删除，1：删除
     */
    private Integer orderDel;
    /**
     * 手机端1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
     */
    private Integer payFrom;
    /**
     * 第三方退款金额
     */
    private Double returnThirdAmount;
    /**
     * 余额支付
     */
    private Double returnBalanceAmount;
    /**
     * 充值卡退款金额
     */
    private Double returnRechargeMoney;
    /**
     * 调整类型：1无调整，2多退款，3第三方支付退款到购物卡4,购物卡多支付
     */
    private Integer balanceType;
    /**
     * 调整金额
     */
    private Double balanceAmount;

    /**
     * 创建者
     */
    private String createUser;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改人
     */
    private String updateUser;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 是否删除0:否;1:是
     */
    private Integer isDelete;
    /**
     * 版本号
     */
    private Integer sysVersion;
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
     * 推广佣金
     */
    private Double tgCommission = 0.00;
    /**
     *推广佣金
     */
    private Double fxCommission = 0.00;
    /**
     *供货价
     */
    private Double goodsPriceSum = 0.00;

    /**
     * 供应商的商品价格
     */
    private BigDecimal supperGoodsPrice;

    /**
     * 供应商的商品id
     */
    private Integer supperCommonId;

    /**
     * 支付编号
     */
    private String paymentNo;

    //订单商品详情导出
    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品实际支付价格
     */
    private Double orderGoodsPaymentAmount;

    /**
     * 组合活动id
     */
    private Integer combinationId;
    /**
     * 订单来源:0:自营订单,1:代发平台供应链订单,2:代发怡亚通供应链订单 3 京东VOP
     */
    private Integer orderSource;
    /**
     * 京东Id
     */
    private String jdOrderId;

    /**
     * 外部
     */
    private Integer outShopId;


    /**
     * 1-用家，2-批发
     */
    private Integer customerType;




    /**
     * 嘉币支付的现金部分
     */
    private BigDecimal orderCashPayment;
}
