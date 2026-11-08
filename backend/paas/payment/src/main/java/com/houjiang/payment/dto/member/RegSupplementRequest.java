package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员资料补录请求DTO
 * 接口：4.会员资料补录
 * 请求地址：/payMember/regSupplement
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegSupplementRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商id(可选)
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
     * 商户会员编号(必填)
     */
    private String bizUserId;
    
    /**
     * 渠道(必填)
     * 云商通二代isv：yunst2isv
     */
    private String chanNo;
    
    /**
     * 会员资料补录结果通知地址(必填)
     */
    private String notifyUrl;
    
    /**
     * 请求流水号(必填)
     */
    private String serialNo;
    
    /**
     * 统一信用证fileId(可选)
     */
    private String uniCreditFileId;
    
    /**
     * 身份证人像面fileId(可选)
     */
    private String legalIdZFileId;
    
    /**
     * 身份证国徽面fileId(可选)
     */
    private String legalIdFFileId;
}
