package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一下单响应DTO
 * 接口：2.统一下单 (API)
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnifiedOrderResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /** 通企付订单号 */
    private String payOrderId;
    
    /** 商户订单号 */
    private String mchOrderNo;
    
    /** 
     * 支付订单状态
     * 0-订单生成
     * 1-进行中
     * 2-支付成功
     * 3-支付失败
     * 4-已撤销
     * 5-已退款
     * 6-订单关闭
     * 7-预消费支付成功
     */
    private Integer orderState;
    
    /** 支付数据类型 */
    private String payDataType;
    
    /** 支付方式 */
    private String wayCode;
    
    /** 发起支付用到的支付参数 */
    private String payData;
    
    /** 上游渠道返回的错误码 */
    private String errCode;
    
    /** 上游渠道返回的错误描述 */
    private String errMsg;
}
