package com.houjiang.payment.dto.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付请求基础类
 * 所有支付请求DTO的父类
 * 
 * @author Amos
 */
@Data
public class PaymentBaseRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商户号
     */
    private String merchantNo;
    
    /**
     * 终端号
     */
    private String terminalNo;
    
    /**
     * 请求时间 格式：yyyyMMddHHmmss
     */
    private String reqTime;
    
    /**
     * 请求流水号
     */
    private String reqSeqNo;
    
    /**
     * 签名
     */
    private String sign;
    
    /**
     * 签名方式 MD5/RSA
     */
    private String signType;
}
