package com.houjiang.order.vo;

import com.houjiang.commons.vo.user.ShopGateInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName OrderRiderBaseVO
 * @Description 订单骑手信息
 * @Author Amos
 * @Date 2024/4/29 16:17
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderRiderBaseVO {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 卖家店铺id
     */
    private Integer shopId;

    /**
     * 商品信息
     */
    private List<OrderDetailGoodsItemVO> goodsVoList;

    /**
     * 门店信息
     */
    private List<ShopGateInfo> shopGateInfoList;

    /**
     * 门店id
     */
    private Integer orderGateShopId;

    /**
     * 门店名称
     */
    private String shopGateName;

    /**
     * 卖家店铺名称
     */
    private String shopName;

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 买家姓名
     */
    private String buyerUserName;

    /**
     * 发件人邮编
     */
    private String buyerPostcode;

    /**
     * 卖家id
     */
    private String sellerUserId;

    /**
     * 卖家姓名
     */
    private String sellerUserName;

    /**
     * 收件人邮编
     */
    private String sellerPostcode;

    /**
     * 订单日期
     */
    private String orderDate;

    /**
     * 订单生成时间
     */
    private String orderCreateTime;

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
     * 国际手机号区号
     */
    private String areaCode;

    /**
     * 收货时间（最晚收货时间）
     */
    private String orderReceiverDate;

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
    private String paymentTime;

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 第三方支付平台交易号
     */
    private String paymentOtherNumber;

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
     * 国际手机号区号
     */
    private String orderSellerAreaCode;

    /**
     * 配送时间
     */
    private String orderShippingTime;

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
    private String orderFinishedTime;

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
     * 优惠价格
     */
    private BigDecimal orderDiscountFee;

    /**
     * 买家使用积分
     */
    private BigDecimal orderPointFee;

    /**
     * 运费价格
     */
    private BigDecimal orderShippingFee;

    /**
     * 买家评价状态 0-未评价 1-已评价
     */
    private Integer orderBuyerEvaluationStatus;

    /**
     * 评价时间
     */
    private String orderBuyerEvaluationTime;

    /**
     * 卖家评价状态 0为评价，1已评价
     */
    private Integer orderSellerEvaluationStatus;

    /**
     * 评价时间
     */
    private String orderSellerEvaluationTime;

    /**
     * 订单留言
     */
    private String orderMessage;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单赠送积分
     */
    private Integer orderPointsAdd;

    /**
     * 代金券id
     */
    private Integer voucherId;

    /**
     * 代金券面额
     */
    private BigDecimal voucherPrice;

    /**
     * 代金券编码
     */
    private String voucherCode;

    /**
     * 退款状态:0是无退款,1是退款中,2是退款完成
     */
    private Integer orderRefundStatus;

    /**
     * 退货状态:0是无退货,1是退货中,2是退货完成
     */
    private Integer orderReturnStatus;

    /**
     * 退款金额
     */
    private BigDecimal orderRefundAmount;

    /**
     * 退货数量
     */
    private Integer orderReturnNum;

    /**
     * 手机端 '1','2','3'
     */
    private String orderFrom;

    /**
     * 交易佣金
     */
    private BigDecimal orderCommissionFee;

    /**
     * 交易佣金退款
     */
    private BigDecimal orderCommissionReturnFee;

    /**
     * 虚拟订单
     */
    private Integer orderIsVirtual;

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
    private String orderCancelDate;

    /**
     * 店铺优惠
     */
    private String orderShopBenefit;

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
    private Integer directsellerFlag;

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
    private BigDecimal redpacketPrice;

    /**
     * 红包抵扣订单金额
     */
    private BigDecimal orderRptPrice;

    /**
     * 红包退款金额
     */
    private BigDecimal orderRptReturn;

    /**
     * 订单结算时间
     */
    private String orderSettlementTime;

    /**
     * 订单是否结算 1-已结算 0-未结算
     */
    private Integer orderIsSettlement;

    /**
     * 分销商
     */
    private Integer shopDistributorId;

    /**
     * SO订单分销类型 1:直销(E)  2:分销代销转发销售(P, SP)
     */
    private Integer orderDistributionSellerType;

    /**
     * PO订单类型 1:购买(E采购，SP:代销采购)  2:分销采购,代客下单 (P开头)
     */
    private Integer orderDistributionBuyerType;

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
    private Integer directsellerIsSettlement;

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
     * 会员折扣
     */
    private BigDecimal orderUserDiscount;

    /**
     * 砍价订单 0- 不是砍价订单 1-砍价订单
     */
    private Integer orderIsBargain;

    /**
     * 配送方式
     */
    private Integer orderSendType;

    /**
     * 送货员联系姓名
     */
    private String contactName;

    /**
     * 送货员联系电话
     */
    private String contactMobile;

    /**
     * 送货备注
     */
    private String contactRemarks;

    /**
     * 是否是秒杀订单 1-是
     */
    private Integer isSeckill;

    /**
     * 是否已经收获完成,0未发货或部分发货 ,1全部发完,2全部收货完成
     */
    private String isReceivng;

    /**
     * 订单物流的单号以逗号分割
     */
    private String shipingCodes;

    /**
     * 是否是预售订单 0不是 1是
     */
    private Integer isPresale;

    /**
     * 预售定金
     */
    private BigDecimal presaleDeposit;

    /**
     * 预售尾款
     */
    private BigDecimal finalPrice;

    /**
     * 预售尾款单号
     */
    private String presaleOrderId;

    /**
     * 预收尾款开始时间
     */
    private String presaleFinalTime;

    /**
     * 尾款手机号
     */
    private String finalMobile;

    /**
     * 尾款通知 0未通知 1通知
     */
    private Integer finalMessage;

    /**
     * 是否同城配送0-否 1-是
     */
    private Integer isDelivery;

    /**
     * 同城配送链ID
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
     * 商家是否确认 0否 1是
     */
    private Integer shopAffirm;

    /**
     * 骑手完成时间
     */
    private String deliveryEndTime;

    /**
     * 加配送费
     */
    private String deliveryCharge;
}
