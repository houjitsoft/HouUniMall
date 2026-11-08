package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员绑定手机号确认请求DTO
 * 接口：7.会员绑定手机号确认
 * 请求地址：/payMember/bindPhoneConfirm
 *
 * 会员绑定手机号上送短信验证码
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BindPhoneConfirmRequest extends PaymentBaseRequest {

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
     * 申请响应业务关联流水号(必填)
     */
    private String applyRespTraceNum;

    /**
     * 绑定手机(必填)
     */
    private String phone;

    /**
     * 短信验证码(必填)
     */
    private String verifyCode;
}
