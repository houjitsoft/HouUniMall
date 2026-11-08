package com.houjiang.payment.dto.trade;

import com.houjiang.payment.dto.base.PaymentBaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一下单请求DTO
 * 接口：2.统一下单 (API)
 * 请求URL: /pay/unifiedOrder
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnifiedOrderRequest extends PaymentBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /** 商户号 */
    private String mchNo;
    
    /** 应用ID */
    private String appId;
    
    /** 服务商号 */
    private String orgId;
    
    /** 商户订单号，商户系统唯一 */
    private String mchOrderNo;
    
    /** 支付方式代码，参考附录1.支付方式说明 */
    private String wayCode;
    
    /** 支付金额，单位分 */
    private String amount;
    
    /** 三位货币代码，人民币:cny */
    private String currency = "cny";
    
    /** 客户端IPV4地址，wayCode为JDBT_PAY、YT_PAY必传 */
    private String clientIp;
    
    /** 商品描述 */
    private String body;
    
    /** 异步通知地址，支付结果异步回调URL */
    private String notifyUrl;
    
    /** 支付结果同步跳转通知URL，不允许带参数 */
    private String returnUrl;
    
    /** 订单失效时间，单位秒，默认1小时 */
    private Integer expiredTime;
    
    /** 渠道参数，特定渠道发起的额外参数(json字符串)，如openid、subAppid等 */
    private String channelExtra;
    
    /** 备注，商户扩展参数，回调时会原样返回 */
    private String extParam;
    
    /** 版本号，固定1.0 */
    private String version = "1.0";
}
