package com.houjiang.payment.dto.transfer;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 转账请求DTO
 * 接口：20.转账接口
 * 请求地址：/transferOrder
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransferOrderRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     */
    private String appId;
    
    /**
     * 商户转账订单号(必填)
     */
    private String mchOrderNo;
    
    /**
     * 支付接口代码(必填)
     * allinpay
     */
    private String ifCode;
    
    /**
     * 入账方式(必填)
     * 仅支持收银宝余额: SYB_CASH
     */
    private String entryType;
    
    /**
     * 转账金额,单位:分(必填)
     */
    private Long amount;
    
    /**
     * 货币代码(必填)
     * CNY
     */
    private String currency;
    
    /**
     * 转账账户,收银宝出金商户号(必填)
     */
    private String sendAccountNo;
    
    /**
     * 收款账户,收银宝入金商户号(必填)
     */
    private String accountNo;
    
    /**
     * 转账备注(必填)
     */
    private String transferDesc;
    
    /**
     * 收款人姓名(可选)
     */
    private String accountName;
    
    /**
     * 收款人开户行名称(可选)
     */
    private String bankName;
}
