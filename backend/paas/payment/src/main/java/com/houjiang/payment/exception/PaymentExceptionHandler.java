package com.houjiang.payment.exception;

import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.base.PaymentBaseResponse;
import com.houjiang.payment.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 支付异常统一处理
 * 
 * @author Amos
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.houjiang.payment.controller.api")
public class PaymentExceptionHandler {
    
    /**
     * 处理支付业务异常
     */
    @ExceptionHandler(PaymentException.class)
    public PaymentBaseResponse handlePaymentException(PaymentException e) {
        log.error("支付业务异常: code={}, message={}", e.getCode(), e.getMessage());
        
        PaymentBaseResponse response = new PaymentBaseResponse();
        response.setCode(e.getCode());
        response.setMsg(e.getMessage());
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        return response;
    }
    
    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public PaymentBaseResponse handleException(Exception e) {
        log.error("系统异常", e);
        
        PaymentBaseResponse response = new PaymentBaseResponse();
        response.setCode(PaymentConstants.ResponseCode.SYSTEM_ERROR);
        response.setMsg("系统异常，请稍后重试");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        return response;
    }
}
