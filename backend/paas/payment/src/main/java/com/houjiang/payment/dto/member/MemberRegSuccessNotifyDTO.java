package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 会员注册成功通知DTO
 * 接口：14.会员注册成功通知
 * @author Amos
 */
@Data
public class MemberRegSuccessNotifyDTO implements Serializable {

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
     * 通过邀请码注册时为接口上送的签名类型
     */
    private String signType;

    /**
     * 会员简称(必填)
     */
    private String memAlias;

    /**
     * 会员种类(必填)
     * 01-个人 02-企业对公 03-个体工商户
     */
    private String memCategory;

    /**
     * 商户会员编号(必填)
     */
    private String bizUserId;

    /**
     * 会员类型(必填)
     * 1-收款方 2-分账方
     */
    private String memSubType;

    /**
     * 通企付会员编号(必填)
     */
    private String memNo;

    /**
     * 绑定手机号(可选)
     */
    private String phoneNo;

    /**
     * 个人信息(可选)
     * 会员种类为01-个人时返回,jsonObject字符串
     */
    private String memInfo;

    /**
     * 企业信息(可选)
     * 会员种类为02/03时返回,jsonObject字符串
     */
    private String enterpriseInfo;

    /**
     * 绑卡信息列表(可选)
     * jsonArray字符串
     */
    private String bankList;

    /**
     * 渠道注册信息列表(必填)
     * jsonArray字符串
     */
    private String chanList;
}
