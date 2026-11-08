package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 企业信息DTO
 * 
 * @author Amos
 */
@Data
public class EnterpriseInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 企业名称(必填)
     * 营业执照名称
     */
    private String companyName;
    
    /**
     * 企业地址(必填)
     */
    private String companyAddress;
    
    /**
     * 地区码(必填)
     * 见附录2.省市地区码
     */
    private String companyAddrCode;
    
    /**
     * 认证类型(必填)
     * 2:一证
     */
    private String authType;
    
    /**
     * 统一社会信用代码(必填)
     */
    private String uniCredit;
    
    /**
     * 统一社会信用生效日期(可选)
     * yyyy-mm-dd
     */
    private String effLicense;
    
    /**
     * 统一社会信用到期时间(可选)
     * yyyy-MM-dd
     */
    private String expLicense;
    
    /**
     * 法人姓名(必填)
     */
    private String legalName;
    
    /**
     * 证件类型(必填)
     * 1-身份证
     */
    private String identityType;
    
    /**
     * 法人身份证(必填)
     * aes加密
     */
    private String legalIds;
    
    /**
     * 法人手机号(必填)
     */
    private String legalPhone;
    
    /**
     * 法人证件人像面影印件fileId(可选)
     */
    private String legalIdZFileId;
    
    /**
     * 法人证件国徽面影印件fileId(可选)
     */
    private String legalIdFFileId;
    
    /**
     * 统一社会信用代码影印件fileId(可选)
     */
    private String uniCreditFileId;
}
