package com.houjiang.mall.pay.plugin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 解析并验签通过的回调载荷。
 *
 * @author Amos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayNotifyPayload {

    /** 我方订单号（hou_market_plugin_order.order_no） */
    private String orderNo;

    /** 渠道支付单号 */
    private String tradeNo;

    /** 渠道侧通知唯一 ID，用于幂等去重；渠道未提供时用 tradeNo */
    private String notifyId;

    /** 回调金额（元）；为 null 表示渠道未下发金额，跳过金额比对 */
    private BigDecimal amount;

    /** 回调原始报文（JSON），落回调流水便于对账 */
    private String raw;

    /** 渠道原始状态串，如 SUCCESS / TRADE_SUCCESS / PAYERROR */
    private String rawStatus;

    /**
     * 归一化结果。
     */
    public enum Result {
        /** 支付成功：结算并发证 */
        SUCCESS,
        /** 支付失败 / 关闭：订单置为支付失败，可重新发起 */
        FAILED,
        /** 退款成功：吊销授权并把订单置为已退款 */
        REFUNDED
    }

    private Result result;
}
