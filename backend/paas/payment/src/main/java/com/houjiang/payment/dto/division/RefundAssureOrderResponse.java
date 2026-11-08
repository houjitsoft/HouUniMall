package com.houjiang.payment.dto.division;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分账撤销响应DTO
 * 接口：18.分账撤销接口
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundAssureOrderResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 分账撤销订单号(必填)
     */
    private String refundAssureOrderId;
    
    /**
     * 订单状态(必填)
     * 0-订单生成
     * 1-撤销中
     * 2-撤销成功
     * 3-撤销失败
     */
    private Integer state;
    
    /**
     * 分账总金额,单位:分(必填)
     */
    private Long assureAmount;
    
    /**
     * 本次撤销成功金额,单位:分(必填)
     */
    private Long refundAmount;
    
    /**
     * 渠道错误码(可选)
     */
    private String errCode;
    
    /**
     * 渠道信息(可选)
     */
    private String errMsg;
}
