package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName ReturnBaseListVO
 * @Description
 * @Author Amos
 * @Date 2023/7/18 10:51
 * @Version 1.0
 * @author Amos
 */
@Data
public class ReturnBaseListVO {

    /**
     * 退款编号
     */
    private String returnCode;

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 退款金额
     */
    private BigDecimal returnCash;

    /**
     * 供货价
     */
    private BigDecimal orderSupplyAmount;

    /**
     * 退还佣金
     */
    private BigDecimal returnCommisionFee = new BigDecimal(0.00);

    /**
     * 退款状态：1-新发起等待卖家审核 2-卖家审核通过 3-卖家审核不通过 4-卖家收到货物 5-平台审核通过 6-买家已填物流 7-平台拒绝
     */
    private String returnState;

    /**
     * 退款申请日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date returnAddTime;

    /**
     * 买家
     */
    private String buyerUserAccount;

    /**
     * 退款原因
     */
    private String returnReason;

    /**
     * 退还红包
     */
    private String returnRptCash;

    /**
     * 退还会员折扣
     */
    private BigDecimal returnMemberDiscount = new BigDecimal(0.00);

    /**
     * 退还经销折扣
     */
    private BigDecimal returnSupperDiscountFee = new BigDecimal(0.00);

    /**
     * 退还经销订单价格
     */
    private BigDecimal supperReturnPrice = new BigDecimal(0.00);

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
    private BigDecimal orderGoodsPaymentAmount;

    /**
     * 自销退单金额
     */
    private BigDecimal selfSellingReturnCash = new BigDecimal(0.00);

    /**
     * 自销退还佣金
     */
    private BigDecimal selfSellingReturnCommission = new BigDecimal(0.00);

    /**
     * 经销退单金额
     */
    private BigDecimal sellOnReturnCash = new BigDecimal(0.00);

    /**
     * 经销退还佣金
     */
    private BigDecimal sellOnReturnCommission = new BigDecimal(0.00);

}
