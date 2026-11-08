package com.houjiang.payment.dto.notify;

import lombok.Data;

/**
 * 提现结果通知DTO
 * 第三方支付平台的提现异步回调通知参数
 * 
 * 注意事项：
 * 1. 提现成功(2)或提现失败(3)均会发起通知
 * 2. allinpay渠道不发异步通知,需通过统一订单查询接口查询提现结果
 * 3. 通知失败会重试5次(间隔:30s、90s、180s、300s、450s)
 * 4. 必须返回小写"success"确认收到通知
 * 5. 需要获取全量参数以兼容未来新增字段
 * 
 * @author Amos
 */
@Data
public class WithdrawNotifyDTO {
    
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
     * 商户提现订单号(必填)
     */
    private String mchOrderNo;
    
    /**
     * 通企付提现订单号(必填)
     */
    private String withdrawOrderId;
    
    /**
     * 提现金额,单位:分(必填)
     */
    private Integer amount;
    
    /**
     * 商户会员编号(必填)
     */
    private String bizUserId;
    
    /**
     * 提现完成时间,13位时间戳(可选)
     */
    private Long successTime;
    
    /**
     * 提现状态(必填)
     * 2-提现成功
     * 3-提现失败
     * 注:只有成功或失败状态会发起异步通知
     */
    private Integer state;
    
    /**
     * 渠道提现订单号(必填)
     */
    private String channelOrderNo;
    
    /**
     * 通知请求时间,13位时间戳(必填)
     */
    private Long reqTime;
    
    /**
     * 渠道错误码(可选)
     */
    private String errCode;
    
    /**
     * 渠道错误描述(可选)
     */
    private String errMsg;
    
    /**
     * 扩展参数,回调时会原样返回(可选)
     */
    private String extParam;
}
