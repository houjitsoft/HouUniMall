package com.houjiang.payment.dto.applet;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关闭小程序链接请求DTO
 * 接口：21.关闭小程序链接
 * 请求地址：/applet/commonurl/colse
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CloseAppletUrlRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商id(必填)
     * 用于服务商代发起交易,传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     * 默认为企业appId,若为服务商模式,需传入服务商appId
     */
    private String appId;
    
    /**
     * 商户订单号(必填)
     */
    private String mchOrderNo;
}
