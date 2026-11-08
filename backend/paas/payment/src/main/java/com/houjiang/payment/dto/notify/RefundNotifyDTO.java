package com.houjiang.payment.dto.notify;

import lombok.Data;

/**
 * 退款通知DTO
 * 第三方支付平台的退款异步回调通知参数
 * 
 * 注意事项：
 * 1. 只有退款成功(2)、退款失败(3)、预消费退款(6)三种状态会发起异步通知
 * 2. 通知失败会重新发起通知
 * 3. 必须返回小写"success"确认收到通知
 * 4. 需要获取全量参数以兼容未来新增字段
 * 
 * @author Amos
 */
@Data
public class RefundNotifyDTO {
    
    /**
     * 服务商号（可选）
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
     * 签名值（必填）
     */
    private String sign;
    
    /**
     * 签名类型（可选）
     */
    private String signType;
    
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
     * 货币代码，人民币:cny（必填）
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
     * 注：只有2/3/6三种状态会发起退款异步通知
     */
    private Integer state;
    
    /**
     * 渠道订单号（可选）
     */
    private String channelOrderNo;
    
    /**
     * 渠道返回的手续费，单位：分（可选）
     */
    private Long channelFeeAmount;
    
    /**
     * 渠道错误码（可选）
     */
    private String errCode;
    
    /**
     * 渠道错误描述（可选）
     */
    private String errMsg;
    
    /**
     * 扩展参数，回调时会原样返回（可选）
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
     * 通知请求时间，13位时间戳（必填）
     */
    private Long reqTime;
}
