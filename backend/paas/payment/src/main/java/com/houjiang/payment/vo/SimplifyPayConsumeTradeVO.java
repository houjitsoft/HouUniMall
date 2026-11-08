package com.houjiang.payment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName SimplifyPayConsumeTradeVO
 * @Description
 * @Author Amos
 * @Date 2023/6/14 9:14
 * @Version 1.0
 * @author Amos
 */
@Data
public class SimplifyPayConsumeTradeVO {

    /**
     * 金额
     */
    private String money;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 记录时间
     */
    private Date recordTime;

    /**
     * 记录状态
     */
    private Integer recordStatus;

    /**
     * 交易类型ID
     */
    private Integer tradeTypeId;

    /**
     * 加减标识
     */
    private Integer plusOrMinus;

    /**
     * 记录支付订单
     */
    private String recordPayorder;

    /**
     * 记录金额
     */
    private BigDecimal recordMoney;

    /**
     * 总积分
     */
    private Integer totalIntegral;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 过期标识
     */
    private Integer pastDue;

    /**
     * 取消标识
     */
    private Integer isCancel;

    /**
     * 退货状态
     */
    private Integer returnStatus;

    /**
     * 退款状态
     */
    private Integer refundStatus;

    /**
     * 收款人姓名
     */
    private String receiverName;

    /**
     * 收款人银行卡号
     */
    private String receiverBankNo;

    /**
     * 收款人银行名称
     */
    private String receiverBankName;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 审核评论
     */
    private String auditComment;

    /**
     * 退货标识
     */
    private Integer returnFlag;

    /**
     * 详情类型
     */
    private Integer detailType;
}
