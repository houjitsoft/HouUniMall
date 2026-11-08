package com.houjiang.payment.dto.pay;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信支付确认响应DTO
 * 接口：23.短信支付确认
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsConfirmResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 支付订单号
     */
    private String payOrderId;
    
    /**
     * 商户订单号
     */
    private String mchOrderNo;
    
    /**
     * 订单状态
     * 0-订单生成
     * 1-支付中
     * 2-支付成功
     * 3-支付失败
     * 4-已撤销
     * 5-已退款
     * 6-订单关闭
     * 7-预消费支付成功
     */
    private Integer state;
    
    /**
     * 渠道返回错误代码
     */
    private String errCode;
    
    /**
     * 渠道返回错误信息
     */
    private String errMsg;
}
