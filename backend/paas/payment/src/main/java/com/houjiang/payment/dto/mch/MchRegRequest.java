package com.houjiang.payment.dto.mch;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户进件请求DTO
 * 接口：商户进件
 * 请求地址：/mch/reg
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MchRegRequest extends PaymentBaseRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 服务商号(可选)
     */
    private String orgId;

    /**
     * 服务商appId(必填)
     */
    private String appId;

    /**
     * 企业名称(必填)
     */
    private String companyName;

    /**
     * 企业简称(必填)
     */
    private String mchShortName;

    /**
     * 统一社会信用代码(必填)
     */
    private String uniCredit;

    /**
     * 法人/负责人手机号(必填)
     */
    private String phone;

    /**
     * 登录账号(必填)
     * 字母开头6-18位
     */
    private String loginUsername;

    /**
     * 收银宝商户号(必填)
     */
    private String cusid;

    /**
     * 拓展方编号(可选)
     */
    private String agencyNo;
}
