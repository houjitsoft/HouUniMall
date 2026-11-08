package com.houjiang.payment.dto.balance;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一余额查询响应DTO
 * 接口：12.统一余额查询
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceQueryResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总余额，单位：分（必填）
     * yunst2isv渠道情况下，该余额等于：可用+在途
     */
    private Long amount;
    
    /**
     * 子商户号（可选）
     * allinpay渠道必返
     */
    private String cusid;
    
    /**
     * 可用余额，单位：分（可选）
     * yunst2isv渠道必返
     */
    private Long availableAmt;
    
    /**
     * 在途余额，单位：分（可选）
     * yunst2isv渠道必返
     */
    private Long transitAmt;
}
