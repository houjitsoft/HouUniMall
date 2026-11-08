package com.houjiang.payment.dto.division;

import lombok.Data;

import java.io.Serializable;

/**
 * 分账撤销结果通知DTO
 * 接口：26.分账撤销结果通知
 * 
 * @author Amos
 */
@Data
public class RefundAssureNotifyDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商号(可选)
     */
    private String orgId;
    
    /**
     * 商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     * 默认为商户应用ID,若为服务商模式,需传入服务商appId
     */
    private String appId;
    
    /**
     * 签名值(必填)
     */
    private String sign;
    
    /**
     * 签名类型(可选)
     */
    private String signType;
    
    /**
     * 原收款订单的通企付支付订单号(可选)
     */
    private String payOrderId;
    
    /**
     * 原收款订单的商户订单号(可选)
     */
    private String mchOrderNo;
    
    /**
     * 通企付分账订单号(必填)
     */
    private String assureOrderId;
    
    /**
     * 商户分账撤销订单号(必填)
     */
    private String refundAssureMchOrderNo;
    
    /**
     * 分账撤销订单号(必填)
     */
    private String refundAssureOrderId;
    
    /**
     * 本次撤销订单金额(可选)
     */
    private String assureRefundAmount;
    
    /**
     * 分账撤销结果(必填)
     * 2-分账撤销成功
     * 3-分账撤销失败
     * 成功失败都通知
     */
    private String state;
    
    /**
     * 订单分账撤销成功时间(可选)
     * 13位时间戳
     */
    private Long successTime;
    
    /**
     * 上游渠道返回的错误描述(可选)
     */
    private String errMsg;
    
    /**
     * 所有参数的Map(用于验签)
     */
    private java.util.Map<String, String> allParams;
}
