package com.houjiang.payment.exception;

import lombok.Getter;

/**
 * 支付业务异常
 * 
 * @author Amos
 */
@Getter
public class PaymentException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String code;
    
    /**
     * 错误信息
     */
    private final String message;
    
    public PaymentException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public PaymentException(String message) {
        this("9999", message);
    }
    
    /**
     * 业务异常
     */
    public static PaymentException bizError(String message) {
        return new PaymentException("9999", message);
    }
    
    /**
     * 认证异常
     */
    public static PaymentException authError(String message) {
        return new PaymentException("4000", message);
    }
    
    /**
     * 系统异常
     */
    public static PaymentException systemError(String message) {
        return new PaymentException("10", message);
    }
}
