package com.houjiang.payment.dto.notify;

import lombok.Data;

/**
 * 分账结果通知DTO
 * 第三方支付平台的分账异步回调通知参数
 * 
 * 注意事项：
 * 1. 一般有终态时才会主动通知(分账成功或分账失败)
 * 2. yunst2isv渠道分账处理完成后会逐笔进行通知
 * 3. 业务系统需以异步通知中的分账结果为准
 * 4. 必须返回小写"success"确认收到通知
 * 5. 需要获取全量参数以兼容未来新增字段
 * 
 * @author Amos
 */
@Data
public class DivisionNotifyDTO {
    
    /**
     * 服务商号(可选)
     */
    private String orgId;
    
    /**
     * 商户号(必填)
     */
    private String mchNo;
    
    /**
     * 应用ID(必填)
     */
    private String appId;
    
    /**
     * 签名值(必填)
     */
    private String sign;
    
    /**
     * 签名类型(可选)
     */
    private String signType;
    
    /**
     * 分账批次号(必填)
     */
    private String batchNo;
    
    /**
     * 通企付分账订单号(必填)
     */
    private String assureOrderId;
    
    /**
     * 商户分账订单号(必填)
     */
    private String mchAssureOrderNo;
    
    /**
     * 原收款订单的通企付支付订单号(可选)
     */
    private String payOrderId;
    
    /**
     * 原收款订单的商户订单号(可选)
     */
    private String mchOrderNo;
    
    /**
     * 本次解冻的订单金额,单位:分(可选)
     */
    private Long orderAmount;
    
    /**
     * 累计解冻的订单金额,单位:分(可选)
     */
    private Long totalOrderAmount;
    
    /**
     * 分账结果(必填)
     * 0-订单生成
     * 1-进行中
     * 2-分账成功
     * 3-分账失败
     * 注:一般有终态时才会主动通知
     */
    private Integer state;
    
    /**
     * 订单分账成功时间,13位时间戳(必填)
     */
    private Long successTime;
    
    /**
     * 商户拓展参数(可选)
     */
    private String extParam;
    
    /**
     * 上游渠道返回的错误描述(可选)
     */
    private String errMsg;
}
