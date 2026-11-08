package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员协议签约申请请求DTO
 * 接口：11.会员协议签约申请
 * 请求地址：/payMember/applyMemProtocol
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplyMemProtocolRequest extends PaymentBaseRequest {

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
     * 商户请求流水号(必填)
     */
    private String serialNo;

    /**
     * 收款方商户会员编号(必填)
     */
    private String bizUserId;

    /**
     * 渠道(必填)
     * 云商通二代：yunst2isv
     */
    private String chanNo;

    /**
     * 协议类型(必填)
     * 1-分账协议
     * 2-抽佣协议
     */
    private String protocolType;

    /**
     * 分账协议信息(可选)
     * 分账协议必传
     * JSONObject字符串
     */
    private String divisionMemInfo;

    /**
     * 抽佣协议信息(可选)
     * 抽佣协议必传
     */
    private String couponInfo;

    /**
     * 签约结果和审核结果异步回调URL(必填)
     * 只有传了该值才会发起回调
     */
    private String notifyUrl;

    /**
     * 跳转页面类型(可选)
     * 1-H5页面 2-小程序页面，不送默认H5
     */
    private String jumpPageType;

    /**
     * 前端跳转地址(可选)
     */
    private String jumpUrl;
}
