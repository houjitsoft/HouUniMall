package com.houjiang.payment.dto.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付响应基础类
 * 所有支付响应DTO的父类
 * 
 * @author Amos
 */
@Data
public class PaymentBaseResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 返回码 0-成功
     */
    private String code;
    
    /**
     * 返回信息
     */
    private String msg;
    
    /**
     * 响应时间 格式：yyyyMMddHHmmss
     */
    private String respTime;
    
    /**
     * 请求流水号
     */
    private String reqSeqNo;
    
    /**
     * 签名
     */
    private String sign;
    
    /**
     * 签名方式 MD5/RSA/SM2
     */
    private String signType;
    
    /**
     * 判断是否成功
     * 成功码是 "0"
     */
    public boolean isSuccess() {
        return "0".equals(this.code);
    }
}
