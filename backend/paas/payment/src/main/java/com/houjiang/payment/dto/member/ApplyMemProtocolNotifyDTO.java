package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 会员协议签约结果通知DTO
 * 接口：12.会员协议签约结果通知
 *
 * @author Amos
 */
@Data
public class ApplyMemProtocolNotifyDTO implements Serializable {

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
     * 通知类型(必填)
     * 1-签约结果 2-审核结果
     */
    private String noticeType;

    /**
     * 协议类型(必填)
     * 1-分账协议 2-抽佣协议
     */
    private String protocolType;

    /**
     * 分账协议签约状态(必填)
     * 1：签约成功 2：签约失败 3：签约中 0：未签约
     */
    private String divisionProtocolState;

    /**
     * 抽佣协议签约状态(可选)
     * 1：签约成功 2：签约失败 3：签约中 0：未签约
     */
    private String couponProtocolState;

    /**
     * 审批状态(可选)
     * 1-通过 2-拒绝
     * 分账协议且通知类型是审核结果时返回
     */
    private String auditResult;

    /**
     * 审核时间(可选)
     * 格式：yyyyMMddHHmmss
     * 分账协议且通知类型是审核结果时返回
     */
    private String auditTime;

    /**
     * 审批拒绝原因(可选)
     * 分账协议且通知类型是审核结果时返回
     */
    private String auditResultMsg;

    /**
     * 分账协议编号(必填)
     */
    private String divisionProtocolNo;

    /**
     * 分账协议信息(必填)
     * JSONObject格式
     */
    private String divisionMemInfo;

    /**
     * 抽佣协议编号(可选)
     */
    private String couponProtocolNo;

    /**
     * 抽佣协议信息(可选)
     * JSONObject格式
     */
    private String couponInfo;
}
