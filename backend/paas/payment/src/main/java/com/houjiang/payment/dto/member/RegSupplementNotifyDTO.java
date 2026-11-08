package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 会员资料补录结果通知DTO
 * 接口：5.会员资料补录结果通知
 * 
 * @author Amos
 */
@Data
public class RegSupplementNotifyDTO implements Serializable {
    
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
     * OCR识别与企业工商认证信息是否一致(可选)
     * 1-是 2-否 3-识别失败
     */
    private String enterpriseCompareResult;
    
    /**
     * OCR识别与企业法人实名信息是否一致(可选)
     * 1-是 2-否 3-识别失败
     */
    private String legalPersonCompareResult;
    
    /**
     * 比对结果信息(可选)
     * 多个";"分隔
     */
    private String compareResults;
    
    /**
     * 企业影印件识别信息(可选)
     * JSONObject格式
     */
    private String enterpriseInfo;
    
    /**
     * 法人影印件肖像面识别信息(可选)
     * JSONObject格式
     */
    private String legalPersonCardFaceInfo;
    
    /**
     * 法人影印件国徽面识别信息(可选)
     * JSONObject格式
     */
    private String legalPersonCardNationalEmblemInfo;
}
