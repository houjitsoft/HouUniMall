package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一订单查询请求DTO
 * 接口：4.统一订单查询
 * 请求URL: /pay/query
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryOrderRequest extends PaymentBaseRequest {
    
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
    
    /** 
     * 业务类型，不传默认为1
     * 1-支付订单查询
     * 4-提现订单查询
     * 5-转账订单查询
     */
    private String busiType = "1";
    
    /** 版本号，固定1.0 */
    private String version = "1.0";
}
