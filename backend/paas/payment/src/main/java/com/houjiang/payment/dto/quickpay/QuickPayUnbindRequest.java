package com.houjiang.payment.dto.quickpay;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 快捷解绑请求DTO
 * 接口：25.快捷解绑
 * 请求地址：/quickpay/unbind
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuickPayUnbindRequest extends PaymentBaseRequest {
    
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
     * 渠道(必填)
     * allinpay：收银宝渠道
     */
    private String chanNo;
    
    /**
     * 协议编号(必填)
     * 签约时返回的agreeId
     */
    private String agreeId;
}
