package com.houjiang.payment.dto.h5cash;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * H5收银台下单响应DTO
 * 接口：27.H5收银台下单
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class H5CashOrderResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 访问路径
     * 该字段值支持直接访问
     * 也支持转化为二维码后,由用户使用微信或支付宝扫码完成支付
     */
    private String H5Url;
}
