package com.houjiang.payment.dto.withdraw;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提现申请响应DTO
 * 接口：13.提现
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WithdrawApplyResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 渠道提现订单号（必填）
     */
    private String channelOrderNo;
    
    /**
     * 提现金额，单位：分（必填）
     */
    private Long amount;
    
    /**
     * 通企付提现订单号（必填）
     */
    private String withdrawOrderId;
    
    /**
     * 商户提现订单号（必填）
     */
    private String mchOrderNo;
    
    /**
     * 订单状态（必填）
     * 0：订单生成
     * 1：提现中
     * 2：提现成功
     * 3：提现失败
     * 6：订单关闭
     */
    private Integer state;
    
    /**
     * 渠道错误码（可选）
     */
    private String errCode;
    
    /**
     * 渠道错误描述（可选）
     */
    private String errMsg;
}
