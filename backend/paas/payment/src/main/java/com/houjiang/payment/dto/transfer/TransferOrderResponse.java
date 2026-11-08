package com.houjiang.payment.dto.transfer;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 转账响应DTO
 * 接口：20.转账接口
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransferOrderResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 通企付转账订单号(必填)
     */
    private String transferId;
    
    /**
     * 渠道转账订单号(必填)
     */
    private String channelOrderNo;
    
    /**
     * 收款账户,收银宝入金商户号(必填)
     */
    private String accountNo;
    
    /**
     * 转账金额,单位:分(必填)
     */
    private Long amount;
    
    /**
     * 商户转账订单号(必填)
     */
    private String mchOrderNo;
    
    /**
     * 状态(必填)
     * 0-订单生成
     * 1-转账中
     * 2-转账成功
     * 3-转账失败
     */
    private Integer state;
    
    /**
     * 收款人姓名,原样返回(可选)
     */
    private String accountName;
    
    /**
     * 收款人开户行名称,原样返回(可选)
     */
    private String bankName;
    
    /**
     * 渠道错误码(可选)
     */
    private String errCode;
    
    /**
     * 渠道错误描述(可选)
     */
    private String errMsg;
}
