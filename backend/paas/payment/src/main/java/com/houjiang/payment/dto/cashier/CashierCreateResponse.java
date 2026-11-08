package com.houjiang.payment.dto.cashier;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 收银台下单响应
 *
 * @author Amos
 */
@Data
public class CashierCreateResponse implements Serializable {

    /** 收银台订单号 */
    private String cashierNo;

    /** 商户订单号 */
    private String mchOrderNo;

    /** 支付平台订单号 */
    private String payOrderId;

    /** 支付方式 */
    private String payMethod;

    /** 终端 */
    private String terminal;

    /** 金额 */
    private BigDecimal amount;

    /** 订单状态：0-待支付；1-支付中；2-成功；3-失败；4-关闭；5-退款 */
    private Integer status;

    /**
     * 前端调起参数（按终端/支付方式下发）
     * 微信 JSAPI: {timeStamp, nonceStr, package, signType, paySign}
     * 支付宝 APP: {orderStr}
     * 余额支付: {payOrderId}
     */
    private Map<String, Object> payData;

    /** 过期时间(毫秒) */
    private Long expireTime;

    private static final long serialVersionUID = 1L;
}
