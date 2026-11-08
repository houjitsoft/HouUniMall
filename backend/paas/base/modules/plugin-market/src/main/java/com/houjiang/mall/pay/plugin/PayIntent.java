package com.houjiang.mall.pay.plugin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 发起支付的结果：把渠道返回的支付参数与流水号带回订单。
 *
 * @author Amos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayIntent {

    /** 渠道编码 */
    private String channel;

    /** 渠道侧支付单号（微信 transaction_id / 支付宝 trade_no / 收银台 payOrderId） */
    private String payOrderId;

    /** 我方中间流水号（收银台 cashierNo）；直连渠道可为 null */
    private String tradeNo;

    /**
     * 下发给前端的支付参数：二维码链接 / 表单 HTML / JSAPI 调起参数。
     * 直接落 {@code pay_data} 列，前端原样使用，后端不解释其内容。
     */
    private Map<String, Object> payData = new LinkedHashMap<>();

    /** 渠道声明的支付截止时间；为空则用订单自身 expire_time */
    private LocalDateTime expireTime;

    /** 是否需要前端跳转（form 提交类渠道为 true） */
    private boolean redirect;
}
