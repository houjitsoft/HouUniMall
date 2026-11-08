package com.houjiang.payment.dto.quickpay;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 快捷支付绑卡签约响应DTO
 * 接口：22.快捷支付绑卡签约
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuickPayAgreeResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商户自定义用户号
     */
    private String merUserId;
    
    /**
     * 第三方信息
     * 签约申请时返回,如果不为空,则确认签约时原样带上
     */
    private String thpInfo;
    
    /**
     * 协议编号
     * 签约确认返回参数,交易时需要传入
     */
    private String agreeId;
    
    /**
     * 银行代码
     * 签约确认返回参数
     */
    private String bankCode;
    
    /**
     * 银行名称
     * 签约确认返回参数
     */
    private String bankName;
}
