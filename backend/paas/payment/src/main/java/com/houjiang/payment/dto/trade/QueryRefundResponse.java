package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询退款响应DTO
 * 接口：8.查询退款订单
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryRefundResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商户号（必填）
     */
    private String mchNo;
    
    /**
     * 应用ID（必填）
     */
    private String appId;
    
    /**
     * 退款订单号（必填）
     */
    private String refundOrderId;
    
    /**
     * 支付订单号（必填）
     */
    private String payOrderId;
    
    /**
     * 商户退款单号（必填）
     */
    private String mchRefundNo;
    
    /**
     * 支付金额，单位：分（必填）
     */
    private Integer payAmount;
    
    /**
     * 退款金额，单位：分（必填）
     */
    private Integer refundAmount;
    
    /**
     * 货币代码（必填）
     * 人民币:cny
     */
    private String currency;
    
    /**
     * 退款状态（必填）
     * 0-订单生成
     * 1-退款中
     * 2-退款成功
     * 3-退款失败
     * 4-退款关闭
     * 6-预消费退款
     */
    private Integer state;
    
    /**
     * 渠道订单号（可选）
     */
    private String channelOrderNo;
    
    /**
     * 渠道错误码（可选）
     */
    private String errCode;
    
    /**
     * 渠道错误描述（可选）
     */
    private String errMsg;
    
    /**
     * 扩展参数（可选）
     * 回调时会原样返回
     */
    private String extParam;
    
    /**
     * 订单创建时间，13位时间戳（必填）
     */
    private Long createdAt;
    
    /**
     * 订单支付成功时间，13位时间戳（可选）
     */
    private Long successTime;
    
    /**
     * 渠道返回的手续费，单位：分（可选）
     */
    private Long channelFeeAmount;
}
