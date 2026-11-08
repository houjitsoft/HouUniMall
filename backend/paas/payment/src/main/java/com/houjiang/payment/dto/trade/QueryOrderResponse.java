package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一订单查询响应DTO
 * 接口：4.统一订单查询
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryOrderResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /** 商户号 */
    private String mchNo;
    
    /** 应用ID */
    private String appId;
    
    /** 支付订单号 */
    private String payOrderId;
    
    /** 商户订单号 */
    private String mchOrderNo;
    
    /** 支付接口 */
    private String ifCode;
    
    /** 
     * 支付方式
     * WX_LITE：微信小程序支付
     * SFT_HRJ：通联汇入金
     * ALI_JSAPI：支付宝JS支付
     * ALI_QR：支付宝主扫
     * GOV_B2BC_TRADE：转账汇款
     * B2B_ORDER：收银宝B2B订单
     * GATEWAY_PC：PC网关
     * ALI_CREDIT：花呗分期
     * WX_TRANS：微信预消费
     * WX_B2B：微信B2B门店助手
     * SYB_B2C_WECHAT：微信小程序收银台支付
     */
    private String wayCode;
    
    /** 支付金额，单位分 */
    private Integer amount;
    
    /** 货币代码，人民币:cny */
    private String currency;
    
    /** 
     * 订单状态
     * 0-订单生成
     * 1-支付中
     * 2-支付成功
     * 3-支付失败
     * 4-已撤销
     * 5-已退款
     * 6-订单关闭
     * 7-预消费支付成功
     */
    private Integer state;
    
    /** 客户端IP */
    private String clientIp;
    
    /** 商品描述 */
    private String body;
    
    /** 渠道订单号 */
    private String channelOrderNo;
    
    /** 渠道错误码 */
    private String errCode;
    
    /** 渠道错误描述 */
    private String errMsg;
    
    /** 扩展参数，回调时会原样返回 */
    private String extParam;
    
    /** 订单创建时间，13位时间戳 */
    private Long createdAt;
    
    /** 订单支付成功时间，13位时间戳 */
    private Long successTime;
    
    /** 订单退款成功时间，13位时间戳 */
    private Long rSuccessTime;
    
    /** 渠道返回的手续费，单位：分 */
    private Long channelFeeAmount;
    
    /** 付款人账号 */
    private String channelUser;
    
    /** 付款人户名 */
    private String channelUserName;
    
    /** 
     * 账户类型
     * 00-借记卡
     * 02-信用卡
     * 99-其他（花呗/余额等）
     */
    private String channelAcctType;
    
    /** 渠道商户号，格式：集团商户号-子商户号 */
    private String channelCusid;
    
    /** 发卡行 */
    private String channelBankCode;
}
