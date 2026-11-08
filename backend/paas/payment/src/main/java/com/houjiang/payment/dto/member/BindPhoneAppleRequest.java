package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员绑定手机号申请请求DTO
 * 接口：6.会员绑定手机号申请
 * 请求地址：/payMember/bindPhoneApple
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BindPhoneAppleRequest extends PaymentBaseRequest {
    
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
     * 请求流水号(必填)
     */
    private String serialNo;
    
    /**
     * 绑定手机号(必填)
     */
    private String phone;
    
    /**
     * 绑定类型(必填)
     * 1-法人手机号
     * 2-企业授权手机号
     */
    private String phoneType;
    
    /**
     * 绑定手机号通知地址(必填)
     */
    private String notifyUrl;
    
    /**
     * 前端跳转地址(可选)
     * 仅绑定类型为2且授权类型为1上送,只支持H5
     */
    private String jumpUrl;
    
    /**
     * 被授权人信息(可选)
     * 绑定类型为2,必传
     * JSONObject格式
     */
    private String authPerInfo;
}
