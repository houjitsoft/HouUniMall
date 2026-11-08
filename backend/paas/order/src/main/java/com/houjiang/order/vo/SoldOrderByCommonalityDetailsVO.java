package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.order.dto.GateDetailDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 已售订单详情
 *
 * @author Amos
 */
@Data
public class SoldOrderByCommonalityDetailsVO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 买家评价状态 0-未评价 1-已评价
     */
    private Integer orderBuyerEvaluationStatus;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 自提订单1是2否
     */
    private Integer orderIsShopTake;

    /**
     * 卖家门店id
     */
    private Integer orderGateShopId;

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 支付方式
     */
    private Integer payCode;

    /**
     * 1.线上支付  2.线下支付 3.货到付款 4.账期支付
     */
    private Integer paymentCode;

    /**
     * 虚拟商品兑换码
     */
    private String redeemCode;

    /**
     * 虚拟商品兑换二维码url
     */
    private String redeemCodeUrl;

    /**
     * 付款时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paymentTime;

    /**
     * orderbase 订单状态
     */
    private Integer orderStatus;


    /**
     * 是否需要审批 0不需要 1需要
     */
    private Integer  isApproval;

    /**
     * 拼团id
     */
    private String orderBuyId;

    /**
     * 剩余拼团人数
     */
    private Integer numberRemaining;

    /**
     * 拼团状态
     */
    private Integer groupbuyStatus;

    /**
     * 买家留言
     */
    private String orderMessage;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 发票内容
     */
    private String invoiceContent;

    /**
     * 0不开发票1普通发票2电子发票3增值税发票
     */
    private Integer invoiceState;

    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;

    /**
     * 订单完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderFinishedTime;

    /**
     * 优惠券金额
     */
    private BigDecimal voucherPrice;

    /**
     * 优惠金额
     */
    private BigDecimal orderDiscountFee;

    /**
     * 优惠券编号
     */
    private String voucherCode;

    /**
     * 发货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderShippingTime;

    /**
     * 红包金额
     */
    private BigDecimal redpacketPrice;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 收货人名称
     */
    private String orderReceiverName;

    /**
     * 红包抵扣订单金额
     */
    private BigDecimal orderRptPrice;

    /**
     * 退款状态
     */
    private Integer orderRefundStatus;

    /**
     * 退货状态
     */
    private Integer orderReturnStatus;

    /**
     * 优惠券总金额
     */
    private BigDecimal voucherTotalPrice;

    /**
     * 平台优惠券抵扣金额
     */
    private BigDecimal platformCouponRptPrice;

    /**
     * 收货人联系地址
     */
    private String orderReceiverAddress;

    /**
     * 收货人联系方式
     */
    private String orderReceiverContact;

    /**
     * orderbase商品总价格(不包含运费)
     */
    private BigDecimal orderGoodsAmount;

    /**
     * orderbase运费价格
     */
    private BigDecimal orderShippingFee;

    /**
     * orderbase应付金额（商品实际支付金额 + 运费）
     */
    private BigDecimal orderPaymentAmount;

    /**
     * 配送公司名称
     */
    private String orderShippingExpressName;

    /**
     * 配送公司id
     */
    private Integer orderShippingExpressId;

    /**
     * 订单最后售后时间
     */
    private String orderLastReturnTime;

    /**
     * 查询已售订单商品详情集合
     */
    private List<SoldOrderByGoodsDetailsVO> soldOrderByGoodsDetailsList;

    /**
     * 门店详情对象
     */
    private GateDetailDTO gateDetailDTO;

    /**
     * 用户信息对象
     */
    private UserInfoVO userInfo;

    /**
     * 满减优惠金额
     */
    private BigDecimal manjianDiscount;

    /**
     * 满折优惠金额
     */
    private BigDecimal manzheDiscount;

    /**
     * 满赠赠品信息 json
     */
    private String giveawayInfo;

    /**
     * 满赠赠品信息
     */
    private List<FullReductionGiftsVO> zengpinInfo;

    /**
     * 退货物流单号
     */
    private String returnShippingCode;

    /**
     * 退货物流公司
     */
    private String returnPostName;

    /**
     * 退货物流公司ID
     */
    private Integer returnPostId;

    /**
     * 收货时间（最晚收货时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderReceiverDate;

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
     * 该订单是否含有会员价
     */
    private Integer shopMoPrice;

    /**
     * 会员等级名称集合
     */
    private List<String> gradeNameList;

    /**
     * 会员折扣集合
     */
    private List<Integer> hyZheKouList;

    /**
     * 询价单号
     */
    private String inquiryId;

    /**
     * 当前订单是否改过价
     */
    private Boolean updatePrice;

    /**
     * 平台处理结果 1 买家胜利  0卖家胜利 2 用户撤销
     */
    private Integer processingResults;

    /**
     * 1.退款,2.仅退款,3退货退款
     */
    private Integer returnMethodType;

    /**
     * 发票抬头，表示开具发票的公司或个人名称
     */
    private String invoiceHead;

    /**
     * 发票状态，用于表示发票的当前状态（如已开票、未开票等）
     */
    private String invoiceStatus;

    /**
     * 审核状态  0 待上传   1 待审核 2 已审核 3 审核驳回
     */
    /**  */
    private Integer reviewStatus;

    /**
     * 秒杀过期时间，Long类型用于表示较大的时间戳
     */
    private Long seckillExpireTime;

    /**
     * 虚拟商品数量，Integer类型用于表示整数数量
     */
    private Integer virtualGoods;
    /**
     * 会员折扣
     */
    private BigDecimal baseMemberDiscount;

    /**
     * 取消原因
     */
    private String orderCancelReason;

    /**
     * 是否供应商代发货 1是  2否
     */
    private Integer dropShipping;

    /**
     * 手动调整价格
     */
    private String orderAdjustFee;

    /**
     * 经销折扣
     */
    private BigDecimal distributionDiscount;
    /**
     * 0待取货 1已取货，待配送
     */
    private Integer pickup;
    /**
     * 骑手id
     */
    private String distributorId;
    /**
     * 骑手名称
     */
    private String riderName;
    /**
     * 骑手电话
     */
    private String riderTel;
    /**
     * 是否同城配送2-否 1-是
     */
    private Integer isDelivery;
    /**
     * 最终 配送费 （消费者）
     */
    private BigDecimal endFree;

    /**
     * 是否为礼包订单 1是 0否
     */
    private Integer isGiftOrder;
    /**
     * 国家名称
     */
    private String countryName;
    /**
     * 国际区号
     */
    private String internationalDialingCode;

    /**
     * 平台退款凭证
     */
    private List<String> returnPaymentVoucherList;




    /**
     * 嘉币支付的现金部分
     */
    private BigDecimal orderCashPayment;
}
