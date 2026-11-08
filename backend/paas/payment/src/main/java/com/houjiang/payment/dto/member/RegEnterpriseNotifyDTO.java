package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 企业会员开户结果通知DTO
 * 接口：3.企业会员开户结果通知
 * 
 * @author Amos
 */
@Data
public class RegEnterpriseNotifyDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商id(可选)
     * 有此参数需要用服务商密钥进行验签
     */
    private String orgId;
    
    /**
     * 商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     * 默认为企业appId,若是服务商模式,则为服务商appId
     */
    private String appId;
    
    /**
     * 签名(必填)
     */
    private String sign;
    
    /**
     * 签名类型(必填)
     * MD5/RSA/SM2
     */
    private String signType;
    
    /**
     * 商户会员编号(必填)
     */
    private String bizUserId;
    
    /**
     * 渠道(必填)
     * 云商通二代：yunst2isv
     */
    private String chanNo;
    
    /**
     * 审核结果(必填)
     * 2：通过
     * 3：失败
     */
    private String auditResult;
    
    /**
     * 审核时间(必填)
     * 格式：20240226170408
     */
    private String auditTime;
    
    /**
     * 人工审核备注(可选)
     */
    private String auditRemark;
    
    /**
     * 失败原因(可选)
     */
    private String auditResultMsg;
}
