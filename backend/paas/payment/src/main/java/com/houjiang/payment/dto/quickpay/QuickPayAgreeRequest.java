package com.houjiang.payment.dto.quickpay;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 快捷支付绑卡签约请求DTO
 * 接口：22.快捷支付绑卡签约
 * 请求地址：/quickpay/agree
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuickPayAgreeRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商id(可选)
     * 用于服务商代发起交易,传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 通企付商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     * 默认为企业appId,若为服务商模式,需传入服务商appId
     */
    private String appId;
    
    /**
     * 渠道(必填)
     * allinpay: 收银宝渠道
     */
    private String chanNo;
    
    /**
     * 操作类型(必填)
     * 1-签约申请
     * 2-签约确认
     */
    private String agreeType;
    
    /**
     * 商户自定义用户号(必填)
     */
    private String merUserId;
    
    /**
     * 卡类型(必填)
     * 00: 借记卡
     * 02: 准贷记卡/贷记卡
     */
    private String acctType;
    
    /**
     * 银行卡号(必填)
     */
    private String acctNo;
    
    /**
     * 户名(必填)
     */
    private String acctName;
    
    /**
     * 证件类型(必填)
     * 0: 身份证
     * 2: 护照
     * 5: 港澳通行证
     * 6: 台湾通行证
     */
    private String identityType;
    
    /**
     * 证件号(必填)
     * 如末位是X,必须大写
     */
    private String idNo;
    
    /**
     * 银行预留手机号码(必填)
     */
    private String phone;
    
    /**
     * 有效期(可选)
     * 格式: MMYY
     */
    private String validDate;
    
    /**
     * CVV2(可选)
     */
    private String cvv2;
    
    /**
     * 渠道参数(可选)
     * JSON字符串,特定渠道发起的额外参数
     * 如: 收银宝子商户号cusid,集团商户号orgid
     * 示例: {"orgid":"66045xxxxxxx","cusid":"66046xxxxxxxxx"}
     */
    private String channelExtra;
    
    /**
     * 短信验证码(可选)
     * 签约确认时必填
     */
    private String verifyCode;
    
    /**
     * 第三方信息(可选)
     * 签约申请时返回,如果不为空,则确认签约时原样带上
     */
    private String thpInfo;
}
