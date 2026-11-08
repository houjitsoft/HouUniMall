package com.houjiang.payment.dto.instalment;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分期利息计算请求DTO
 * 接口：19.分期利息计算
 * 请求地址：/instalment/calFee
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CalFeeRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商号(可选)
     * 用于服务商代发起交易,传了此参数需要用服务商密钥进行加签
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
     * 支付金额,单位:分(必填)
     */
    private Long amount;
    
    /**
     * 支付方式(必填)
     * 白条分期: JDBT_PAY
     */
    private String wayCode;
}
