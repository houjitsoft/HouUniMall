package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关闭支付订单响应DTO
 * 接口：5.关闭支付订单
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CloseOrderResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /** 渠道错误码 */
    private String errCode;
    
    /** 渠道错误描述 */
    private String errMsg;
}
