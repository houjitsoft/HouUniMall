package com.houjiang.payment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName ComsumeRecordPara
 * @Description 订单记录展示
 * @Author Amos
 * @Date 2023/4/13 16:32
 * @Version 1.0
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComsumeRecordVO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recordTime;

    /**
     * 交易类型(详情pdb_wordbook_pay_trade_type)
     */
    private Integer tradeTypeId;

    /**
     * 付款状态
     */
    private Integer recordStatus;

    /**
     * 0未删除 1删除
     */
    private Integer recordDelete;

    /**
     * 金额
     */
    private BigDecimal recordMoney;

    /**
     * 交易记录id
     */
    private Integer consumeRecordId;

    /**
     * 操作之后的金额
     */
    private BigDecimal recordAfterMoney;

    /**
     * 支付方式
     */
    private Integer payCode;

    /**
     * 实际支付单号
     */
    private String recordPayorder;

    /**
     * 用户资金-预付款充值、转账、结算后的款项等等
     */
    private BigDecimal userMoney;

    /**
     * 退货状态
     */
    private Integer orderReturnStatus;

    /**
     * 退款状态
     */
    private Integer orderRefundStatus;
}
