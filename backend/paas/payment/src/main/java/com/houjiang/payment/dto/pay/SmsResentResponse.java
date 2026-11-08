package com.houjiang.payment.dto.pay;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 重新获取支付确认短信响应DTO
 * 接口：24.重新获取支付确认短信
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsResentResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 支付订单号
     */
    private String payOrderId;
    
    /**
     * 支付数据
     * 如果存在,下一步确认时需要原样带上
     */
    private String payData;
}
