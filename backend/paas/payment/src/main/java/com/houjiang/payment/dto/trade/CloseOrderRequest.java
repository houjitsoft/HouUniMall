package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关闭支付订单请求DTO
 * 接口：5.关闭支付订单
 * 请求URL: /pay/close
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CloseOrderRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /** 服务商id，用于服务商代发起交易 */
    private String orgId;
    
    /** 商户号 */
    private String mchNo;
    
    /** 应用ID */
    private String appId;
    
    /** 商户订单号，与payOrderId二者传一即可 */
    private String mchOrderNo;
    
    /** 支付订单号，与mchOrderNo二者传一即可 */
    private String payOrderId;
    
    /** 版本号，固定1.0 */
    private String version = "1.0";
}
