package com.houjiang.payment.dto.notify;

import lombok.Data;

/**
 * 支付通知DTO
 * 第三方支付平台的异步回调通知参数
 * 
 * 注意事项：
 * 1. 只有支付成功才会收到通知
 * 2. 通知失败会重试5次（间隔：30s、90s、180s、300s、450s）
 * 3. 必须返回小写"success"确认收到通知
 * 4. 需要获取全量参数以兼容未来新增字段
 * 
 * @author Amos
 */
@Data
public class PayNotifyDTO {
    
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
     * 支付订单号（必填）
     */
    private String payOrderId;
    
    /**
     * 商户订单号（必填）
     */
    private String mchOrderNo;
    
    /**
     * 支付接口（必填）
     */
    private String ifCode;
    
    /**
     * 支付方式（必填）
     * WX_LITE：微信小程序支付
     * SFT_HRJ：通联汇入金
     * XW_HRJ：新网汇入金
     * ALI_JSAPI：支付宝JS支付
     * ALI_QR：支付宝主扫
     * B2B_ORDER：收银宝B2B订单
     * GATEWAY_PC：PC网关
     * ALI_CREDIT：花呗分期
     * JDBT_PAY：白条分期
     * SYB_B2C_WECHAT：微信小程序收银台支付
     * WX_TRANS：微信预消费
     * WX_B2B：微信B2B门店助手
     * FME_CPAY：富民e贷
     * QUICK_PAY：快捷支付
     * YW_PAY：云微支付
     * YT_PAY：云梯支付
     * H5_CASHIER：H5收银台
     */
    private String wayCode;
    
    /**
     * 实际支付成功金额，单位：分（必填）
     */
    private Integer amount;
    
    /**
     * 货币代码，人民币:cny（必填）
     */
    private String currency;
    
    /**
     * 订单状态（必填）
     * 0-订单生成
     * 1-支付中
     * 2-支付成功
     * 3-支付失败
     * 4-已撤销
     * 5-已退款
     * 6-订单关闭
     * 7-预消费支付成功
     * 注：只有支付成功才会发起异步通知
     */
    private Integer state;
    
    /**
     * 客户端IP（可选）
     */
    private String clientIp;
    
    /**
     * 商品描述（必填）
     */
    private String body;
    
    /**
     * 商户名称（必填）
     */
    private String mchName;
    
    /**
     * 付款人渠道用户标识（可选）
     * 如微信openId, 支付宝账号，银行卡号
     */
    private String channelUser;
    
    /**
     * 付款人户名（可选）
     */
    private String channelUserName;
    
    /**
     * 账户类型（可选）
     * 00-借记卡
     * 02-信用卡
     * 99-其他（花呗/余额等）
     */
    private String channelAcctType;
    
    /**
     * 渠道订单号（可选）
     * 如：微信或支付宝的交易单号
     */
    private String channelOrderNo;
    
    /**
     * 渠道返回的手续费，单位：分（可选）
     */
    private Long channelFeeAmount;
    
    /**
     * 渠道商户号（可选）
     * 格式：集团商户号-子商户号
     */
    private String channelCusid;
    
    /**
     * 渠道错误码（可选）
     */
    private String errCode;
    
    /**
     * 渠道错误描述（可选）
     */
    private String errMsg;
    
    /**
     * 商户扩展参数，原样返回（可选）
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
    
    /**
     * 发卡行（可选）
     */
    private String channelBankCode;
}
