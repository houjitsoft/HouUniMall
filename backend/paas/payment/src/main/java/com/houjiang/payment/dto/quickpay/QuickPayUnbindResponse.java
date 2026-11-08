package com.houjiang.payment.dto.quickpay;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 快捷解绑响应DTO
 * 接口：25.快捷解绑
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuickPayUnbindResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商户用户号
     */
    private String merUserId;
}
