package com.houjiang.payment.dto.cashier;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收银台下单请求
 *
 * @author Amos
 */
@Data
public class CashierCreateRequest implements Serializable {

    /** 商户订单号（可选，系统会自动生成收银台订单号） */
    private String mchOrderNo;

    /** 商城侧幂等请求号。 */
    private String requestNo;

    /** 商城订单号。 */
    private String mallOrderNo;

    /** 终端：WX_MINI/ALI_MINI/... */
    @NotBlank(message = "terminal 不能为空")
    private String terminal;

    /** 支付方式：WECHAT/ALIPAY/BALANCE/... */
    @NotBlank(message = "payMethod 不能为空")
    private String payMethod;

    /** 支付金额（元） */
    @NotNull(message = "amount 不能为空")
    @DecimalMin(value = "0.01", message = "amount 必须 >= 0.01")
    private BigDecimal amount;

    /** 币种，默认 CNY */
    private String currency;

    /** 订单标题 */
    private String subject;

    /** 订单描述 */
    private String body;

    /** 客户端IP */
    private String clientIp;

    /** 业务回调地址 */
    private String notifyUrl;

    /** 支付完成跳转地址 */
    private String returnUrl;

    /** 扩展参数(JSON 字符串或简单 Map 序列化) */
    private String extParam;

    /** 用户 openId/buyerId（微信/支付宝 JSAPI 支付需要） */
    private String openId;

    /**
     * 渠道编码（可选），例如 WECHAT_JSAPI / ALIPAY_WAP。
     * 传入后后端会优先从 cloud 后台 hou_pay_channel 表读取该渠道的 configJson 作为签名参数，
     * 缺失时回退使用 hou_pay_merchant_qual / hou_pay_config。
     */
    private String channelCode;

    private static final long serialVersionUID = 1L;
}
