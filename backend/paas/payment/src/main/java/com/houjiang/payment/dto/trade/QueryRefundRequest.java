package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询退款请求DTO
 * 接口：8.查询退款订单
 * 请求地址：/refund/query
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryRefundRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商号（可选）
     * 用于服务商查询，传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 商户号（必填）
     */
    private String mchNo;
    
    /**
     * 应用ID（必填）
     * 默认为商户appId，若为服务商模式，需传入服务商appId
     */
    private String appId;
    
    /**
     * 退款订单号（必填）
     * 与mchRefundNo二者传一即可
     */
    private String refundOrderId;
    
    /**
     * 商户退款单号（必填）
     * 与refundOrderId二者传一即可
     */
    private String mchRefundNo;
}
