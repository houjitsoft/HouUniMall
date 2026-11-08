package com.houjiang.payment.dto.cashier;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 收银台订单状态响应
 *
 * @author Amos
 */
@Data
public class CashierStatusResponse implements Serializable {

    /** 收银台订单号 */
    private String cashierNo;

    /** 支付平台订单号 */
    private String payOrderId;

    /** 商户订单号 */
    private String mchOrderNo;

    /** 支付方式 */
    private String payMethod;

    /** 终端 */
    private String terminal;

    /** 金额 */
    private BigDecimal amount;

    /** 订单状态 */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

    /** 支付成功时间 */
    private Date successTime;

    /** 错误码 */
    private String errCode;

    /** 错误信息 */
    private String errMsg;

    private static final long serialVersionUID = 1L;
}
