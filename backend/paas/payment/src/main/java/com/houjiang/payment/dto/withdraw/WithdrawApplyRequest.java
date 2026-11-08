package com.houjiang.payment.dto.withdraw;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提现申请请求DTO
 * 接口：13.提现
 * 请求地址：/pay/withdrawApply
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WithdrawApplyRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务商ID（可选）
     * 用于服务商代发起交易，传了此参数需要用服务商密钥进行加签
     */
    private String orgId;
    
    /**
     * 商户号（必填）
     */
    private String mchNo;
    
    /**
     * 应用ID（必填）
     */
    private String appId;
    
    /**
     * 渠道（必填）
     * allinpay：收银宝渠道
     * yunst2isv：特色结算渠道
     * yunst：云商通渠道
     * yunst2yhzh：银行账户渠道
     */
    private String chanNo;
    
    /**
     * 渠道参数（可选）
     * JSON字符串，allinpay渠道需要指定提现商户号
     * 例如：{"orgid":"66045xxxxxxx","cusid":"66046xxxxxxxxx"}
     */
    private String channelExtra;
    
    /**
     * 商户提现订单号（必填）
     */
    private String mchOrderNo;
    
    /**
     * 提现金额，单位：分（必填）
     */
    private Long amount;
    
    /**
     * 平台抽佣金额，单位：分（可选）
     * 仅yunst2isv、yunst渠道可使用
     */
    private Long fee;
    
    /**
     * 商户会员编号（可选）
     * yunst2isv、yunst、yunst2yhzh渠道必传
     * 若是平台，上送固定值：#yunBizUserId_B2C#
     */
    private String bizUserId;
    
    /**
     * 提现银行卡号（可选）
     * yunst2isv、yunst渠道必传
     */
    private String bankCardNo;
    
    /**
     * 银行卡/账户属性（可选）
     * yunst渠道必传
     * 0：个人银行卡
     * 1：企业对公账户
     */
    private Integer bankCardPro;
    
    /**
     * 异步通知地址（可选）
     * allinpay渠道不可用
     */
    private String notifyUrl;
    
    /**
     * 摘要（可选）
     */
    private String summary;
    
    /**
     * 提现方式（可选）
     * allinpay渠道支持：D0-D+0到账；T1-T+1到账，默认为T1
     * yunst渠道支持：D0-D+0到账；D1-D+1到账；T1-T+1到账，默认为D0
     * yunst2isv渠道支持：D0-D+0到账
     * yunst2yhzh渠道支持：D0-D+0到账
     */
    private String withdrawType;
    
    /**
     * 备注（可选）
     */
    private String extParam;
}
