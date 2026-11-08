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
public class MerchantsOrdersDetailsVO {

    /**
     * 订单id
     */
    private String orderId;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 支付码
     */
    private Integer payCode;

    /**
     * 商店名称
     */
    private String shopName;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paymentTime;

    /**
     * 支付方式名称
     */
    private String paymentName;

    /**
     * 订单发票号
     */
    private String orderInvoice;
    /**
     * 发票抬头
     */
    private String invoiceTitle;
    /**
     * 订单消息
     */
    private String orderMessage;

    /**
     * 订单状态，使用整数表示具体状态。具体状态的含义和取值范围根据实际业务语境确定。
     */
    private Integer orderStatus;
    /**
     * 0不开发票1普通发票2电子发票3增值税发票
     */
    private Integer invoiceState;
    /**
     * 订单创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;
    /**
     * 发票内容
     */
    private String invoiceContent;
    /**
     * 订单发货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderShippingTime;

    /**
     * 订单完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderFinishedTime;

    /**
     * 订单卖家名称
     */
    private String orderSellerName;

    /**
     * 订单卖家地址
     */
    private String orderSellerAddress;

    /**
     * 订单卖家联系方式
     */
    private String orderSellerContact;

    /**
     * 订单收货人名称
     */
    private String orderReceiverName;

    /**
     * 订单收货人地址
     */
    private String orderReceiverAddress;
    /**
     * 订单收货人联系方式
     */
    private String orderReceiverContact;

    /**
     * 配送公司ID
     */
    private Integer orderShippingExpressId;

    /**
     * 配送公司
     */
    private String orderShippingExpress;

    /**
     * 订单收货人地址
     */
    private String orderShippingCode;
    /**
     * 订单退款状态，使用整数表示具体的状态。具体状态的含义和取值范围根据实际业务语境确定。
     */
    private Integer orderRefundStatus;

    /**
     * 订单退货状态，使用整数表示具体的状态。具体状态的含义和取值范围根据实际业务语境确定。
     */
    private Integer orderReturnStatus;
    /**
     *  虚拟订单 0 是 1 否
     */
    private Integer virtualGoods;
    /**
     * 虚拟订单有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
     * 是否核销0是1否是否核销0是1否
     */
    private Integer isCancel;
    /**
     * 核销时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelDate;
    /**
     * 商品折扣信息
     */
    private List<AddOrderDetailsVO> addOrderDetails;
    /**
     * 退款退货信息
     */
    private List<ReturnOrderDetailsVO> returnOrderDetails;
    /**
     * 满减赠赠品信息 json
     */
    private String giveawayInfo;
    /**
     * 满减赠赠品信息
     */
    private List<ManJianZengPinVO> zengpinInfo;
    /**
     * 订单收货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderReceiverDate;

    /**
     * 订单支付金额
     */
    private BigDecimal orderPaymentAmount;

    /**
     * 订单折扣费用
     */
    private BigDecimal orderDiscountFee;
    /**
     * 1-退款申请 2-退货申请
     */
    private Integer returnType;
    /**
     * 支付方式    1.线上支付  2.线下支付 3.货到付款
     */
    private Integer paymentCode;

    /**
     * 发票Id
     */
    private Integer orderInvoiceId;

    /**
     * 取消原因
     */
    private String orderCancelReason;

    /**
     * 手工调整金额
     */
    private BigDecimal orderAdjustFee;

    /**
     * 满折活动折扣金额
     */
    private BigDecimal manzheDiscount;

    /**
     * 满减活动折扣金额
     */
    private BigDecimal manjianDiscount;

    /**
     * plus会员折扣金额
     */
    private BigDecimal plusDiscount;

    /**
     * 平台会员折扣
     */
    private BigDecimal baseMemberDiscount;

    /**
     * 运费价格
     */
    private BigDecimal orderShippingFee;

    /**
     * 订单总金额
     */
    private BigDecimal orderGoodsAmount;

    /**
     * 组合活动类型 1固定组合 2搭配组合
     */
    private Integer combinationType;

    /**
     * 组合活动价格
     */
    private BigDecimal combinationPrice;
    /**
     * 店铺印章
     */
    private String shopStamp;
    /**
     * 店铺印章 备注
     */
    private String shopStampMsg;
    /**
     * 订单佣金
     */
    private BigDecimal orderBaseCommissionFee;
    /**
     * 国家名称
     */
    private String countryName;
    /**
     * 国际区号
     */
    private String internationalDialingCode;




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
