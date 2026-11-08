package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 个人会员实名及绑卡请求DTO
 * 接口：9.个人会员实名及绑卡
 * 请求地址：/payMember/regPerson
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegPersonRequest extends PaymentBaseRequest {

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
     * 会员角色(可选)
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
     * 会员简称(必填)
     */
    private String memAlias;

    /**
     * 请求流水号(必填)
     */
    private String serialNo;

    /**
     * 个人信息(必填)
     * jsonObject字符串
     */
    private String memInfo;

    /**
     * 银行卡信息(必填)
     * jsonObject字符串
     */
    private String bankInfo;
}
