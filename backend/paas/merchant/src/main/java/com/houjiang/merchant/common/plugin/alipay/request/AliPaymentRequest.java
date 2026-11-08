package com.houjiang.merchant.common.plugin.alipay.request;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class AliPaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 支付终端 */
    private Integer terminal;

    /** 用户身份 */
    private String openId;

    /** 扩展字段 */
    private String attach;

    /** 订单编号 */
    private String outTradeNo;

    /** 订单金额 */
    private BigDecimal orderAmount;

    /** 订单描述 */
    private String description;

    /** 跳转链接*/
    private String url;
}
