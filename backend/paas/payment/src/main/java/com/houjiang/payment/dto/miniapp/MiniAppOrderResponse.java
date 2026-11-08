package com.houjiang.payment.dto.miniapp;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 小程序下单响应
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MiniAppOrderResponse extends PaymentBaseResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 支付状态：success-支付成功，pending-支付中，cancel-取消支付，fail-支付失败 */
    private String code;
    
    /** 响应内容（当code为cancel、fail有值） */
    private String msg;
    
    /** 商户订单号 */
    private String mchOrderNo;
    
    /** 金额 */
    private String amount;
    
    /** 商户号 */
    private String mchNo;
    
    /** 商户名称 */
    private String mchName;
}
