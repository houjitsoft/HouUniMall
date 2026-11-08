package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业会员实名开户请求DTO
 * 接口：2.企业会员实名开户
 * 请求地址：/payMember/regEnterprise
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegEnterpriseRequest extends PaymentBaseRequest {
    
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
     * 会员种类(必填)
     * 02-企业
     * 03-个体工商户
     */
    private String memCategory;
    
    /**
     * 会员角色(可选)
     * 引流方/供应商
     * chanNo为yunst2isv时必填
     */
    private String memRole;
    
    /**
     * 会员子类型(可选)
     * 2-分账方
     * chanNo为yunst2isv时必填
     */
    private String memSubType;
    
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
     * 企业会员开户结果通知地址(必填)
     */
    private String notifyUrl;
    
    /**
     * 会员简称(必填)
     * 可由客户自定义,校验唯一
     */
    private String memAlias;
    
    /**
     * 请求流水号(必填)
     */
    private String serialNo;
    
    /**
     * 企业信息(必填)
     * jsonObject格式
     */
    private String enterpriseInfo;
    
    /**
     * 银行卡信息(必填)
     * jsonObject格式
     */
    private String bankInfo;
}
