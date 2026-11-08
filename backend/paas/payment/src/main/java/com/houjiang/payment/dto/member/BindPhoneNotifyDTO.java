package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 会员绑定手机号结果通知DTO
 * 接口：8.会员绑定手机号结果通知
 *
 * @author Amos
 */
@Data
public class BindPhoneNotifyDTO implements Serializable {

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
     * 绑定结果(必填)
     * 1：成功 2：失败
     */
    private String bindResult;

    /**
     * 绑定失败原因(可选)
     */
    private String bindErrorMsg;

    /**
     * 绑定时间(可选)
     */
    private String bindTime;

    /**
     * 绑定手机号(可选)
     */
    private String phone;

    /**
     * 绑定手机类型(可选)
     * 1-法人手机号 2-企业授权手机号
     */
    private String phoneType;
}
