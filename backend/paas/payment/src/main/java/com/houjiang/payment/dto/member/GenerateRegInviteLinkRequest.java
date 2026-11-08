package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员注册邀请码生成请求DTO
 * 接口：13.会员注册邀请码生成
 * 请求地址：/payMember/generateRegInviteLink
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GenerateRegInviteLinkRequest extends PaymentBaseRequest {

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
     * 渠道(必填)
     * 云商通特色结算：yunst2isv
     * 云商通银行账户：yunst2yhzh
     */
    private String chanNo;

    /**
     * 会员种类(必填)
     * 01-个人 02-企业对公 03-个体工商户
     * 注：yunst2yhzh渠道不支持个人
     */
    private String memCategory;

    /**
     * 会员类型(可选)
     * 2-分账方
     * chanNo为yunst2isv和yunst2yhzh时必填
     */
    private String memSubType;

    /**
     * 会员角色(可选)
     * 分账方会员角色: 引流方/供应商
     * chanNo为yunst2isv和yunst2yhzh时必填
     */
    private String memRole;

    /**
     * 商户会员编号(必填)
     */
    private String bizUserId;

    /**
     * 会员简称(必填)
     */
    private String memAlias;

    /**
     * 注册成功结果异步回调URL(可选)
     */
    private String notifyUrl;

    /**
     * 页面跳转地址(可选)
     * 返回时会拼接bizUserId和regState
     */
    private String jumpUrl;

    /**
     * 请求流水号(必填)
     */
    private String serialNo;

    /**
     * 手机号(可选,用于页面返显绑定手机号)
     */
    private String phoneNo;

    /**
     * 个人信息(可选)
     * memCategory为01-个人可上送,用于页面返显
     * jsonObject字符串
     */
    private String memInfo;

    /**
     * 企业信息(可选)
     * memCategory为02/03可上送,用于页面返显
     * jsonObject字符串
     */
    private String enterpriseInfo;

    /**
     * 绑卡信息(可选)
     * 绑定对私卡可上送,用于页面返显
     * jsonObject字符串
     */
    private String bankInfo;
}
