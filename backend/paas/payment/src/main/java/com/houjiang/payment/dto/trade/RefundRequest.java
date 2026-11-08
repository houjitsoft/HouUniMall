package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一退款请求DTO
 * 接口：6.统一退款
 * 请求URL: /refund/refundOrder
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /** 服务商号，用于服务商代发起交易 */
    private String orgId;
    
    /** 商户号 */
    private String mchNo;
    
    /** 应用ID */
    private String appId;
    
    /** 商户退款订单号，商户平台唯一 */
    private String mchRefundNo;
    
    /** 商户订单号，与payOrderId二者传一即可 */
    private String mchOrderNo;
    
    /** 支付订单号，与mchOrderNo二者传一即可 */
    private String payOrderId;
    
    /** 渠道退款金额，单位分 */
    private Integer refundAmount;
    
    /** 货币代码，人民币:cny */
    private String currency = "cny";
    
    /** 退款原因 */
    private String refundReason;
    
    /** 客户端IP */
    private String clientIp;
    
    /** 异步通知地址 */
    private String notifyUrl;
    
    /** 渠道参数，特定渠道发起的额外参数，如分账退款信息 */
    private String channelExtra;
    
    /** 商户扩展参数，回调时会原样返回 */
    private String extParam;
    
    /** 版本号，固定1.0 */
    private String version = "1.0";
}
