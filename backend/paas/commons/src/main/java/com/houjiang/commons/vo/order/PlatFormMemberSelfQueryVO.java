package com.houjiang.commons.vo.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.excel.ExcelCell;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PlatFormMemberSelfQueryVO implements Serializable {
    public static final long serialVersionUID = 1L;
    @ExcelCell(index = 1)
    private String orderId;
    @ExcelCell(index = 2)
    private String orderFrom;
    @ExcelCell(index = 3)
    private String orderReceiverName;
    @ExcelCell(index = 4)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;
    @ExcelCell(index = 5)
    private Double orderGoodsAmount;
    @ExcelCell(index = 6)
    private Double orderPaymentAmount;
    @ExcelCell(index = 7)
    /** 红包 */
    private Double redpacketPrice;
    @ExcelCell(index = 8)
    /** 代金券 */
    private Double voucherPrice;
    @ExcelCell(index = 9)
    /** 运费 */
    private Double orderShippingFee;
    @ExcelCell(index = 10)
    private String orderStatus;
    @ExcelCell(index = 11)
    /** 活动ID 1-正常下单 2-拼团订单 3-砍价订单 4限时抢购订单', */
    private Integer orderPromotionId;
    @ExcelCell(index = 12)
    private String orderRefundStatus;
    @ExcelCell(index = 13)
    private String orderReturnStatus;
    @ExcelCell(index = 14)
    private String isNotreFundReturn;
    @ExcelCell(index = 15)
    private String paymentNumber;

    @ExcelCell(index = 16)
    private String paymentName;
    @ExcelCell(index = 17)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paymentTime;
    @ExcelCell(index = 18)
    private String orderShippingCode;
    @ExcelCell(index = 19)
    /** 退款金额 */
    private Double orderRefundAmount;
    @ExcelCell(index = 20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /** 订单完成时间 */
    private Date orderFinishedTime;
    @ExcelCell(index = 21)
    /** 是否评价 */
    private String orderBuyerEvaluationStatus;
    @ExcelCell(index = 22)
    private Integer shopId;
    @ExcelCell(index = 23)
    private String shopName;
    @ExcelCell(index = 24)
    private String buyerUserId;
    @ExcelCell(index = 25)
    private String buyerUserName;
    /**  */
    public Double tradePaymentAmount;

    @ExcelCell(index = 26)
    /**  */
    public Double tradePaymentRechargeCard;
    @ExcelCell(index = 27)
    /**  */
    public Double tradeThirdPartyAmount;
    @ExcelCell(index = 28)
    public String payCode;

    /** 1-实物；2-虚拟；3-门店 */
    private Integer orderType;
    private Integer pastDue;
    private Integer isCancel;
    private Integer virtualGoods;
    private Integer orderIsShopTake;
    private Integer returnFlag;
    private Integer returnStatus;
    private Integer refundStatus;
    /** 余额支付 */
    private Double tradePaymentMoney;
    /** 订单金额不含运费 */
    private BigDecimal orderAmountNoShippingFee;
    /**
     * 1.线上支付  2.线下支付 3.货到付款
     */
    private Integer paymentCode;
    /**
     * 会员折扣
     */
    private BigDecimal baseMemberDiscount;




}
