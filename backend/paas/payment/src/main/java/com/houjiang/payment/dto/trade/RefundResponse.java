package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一退款响应DTO
 * 接口：6.统一退款
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /** 退款订单号 */
    private String refundOrderId;
    
    /** 商户退款单号 */
    private String mchRefundNo;
    
    /** 支付金额，单位分 */
    private Integer payAmount;
    
    /** 退款金额，单位分 */
    private Integer refundAmount;
    
    /** 
     * 订单状态
     * 0-订单生成
     * 1-退款中
     * 2-退款成功
     * 3-退款失败
     * 4-退款关闭
     * 6-预消费退款
     */
    private Integer state;
    
    /** 渠道退款单号 */
    private String channelOrderNo;
    
    /** 渠道错误码 */
    private String errCode;
    
    /** 渠道错误描述 */
    private String errMsg;
}
