package com.houjiang.payment.dto.pay;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 重新获取支付确认短信请求DTO
 * 接口：24.重新获取支付确认短信
 * 请求地址：/pay/smsresent
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsResentRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商id(可选)
     * 用于服务商代发起交易,传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 通企付商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     * 默认为企业appId,若为服务商模式,需传入服务商appId
     */
    private String appId;
    
    /**
     * 商户订单号(可选)
     * 与payOrderId二者传一即可
     */
    private String mchOrderNo;
    
    /**
     * 支付订单号(可选)
     * 与mchOrderNo二者传一即可,建议使用支付订单号
     */
    private String payOrderId;
    
    /**
     * 协议编号(必填)
     * 快捷支付签约时返回的agreeId
     */
    private String agreeId;
    
    /**
     * 支付数据(可选)
     * 透传下单接口返回的payData
     */
    private String payData;
}
