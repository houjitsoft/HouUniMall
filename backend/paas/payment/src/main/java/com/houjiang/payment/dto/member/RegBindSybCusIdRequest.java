package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员绑定收银宝商户号开户请求DTO
 * 接口：15.会员绑定收银宝商户号开户
 * 请求地址：/payMember/regBindSybCusId
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegBindSybCusIdRequest extends PaymentBaseRequest {

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
     * 1-收款方
     * chanNo为yunst2isv时必填
     */
    private String memSubType;

    /**
     * 会员角色(可选)
     * 收款方: 经销商
     * chanNo为yunst2isv时必填
     */
    private String memRole;

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
     * 收银宝商户号(必填)
     */
    private String sybCusId;

    /**
     * 是否抽佣(可选)
     * 1-是 0-否
     * chanNo为yunst2isv时必填
     */
    private String isCoupon;
}
