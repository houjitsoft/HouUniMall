package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付回调流水（hou_market_plugin_pay_notify）。
 *
 * <p><b>存在意义：幂等 + 对账</b>。渠道「重复回调」是常态（微信在未收到 success 应答时会按
 * 15s/15s/30s/3m/10m… 重发多次），若以订单状态做唯一判据，「支付成功回调」与
 * 「退款回调」先后到达时会被互相覆盖。这里以 {@code (order_no, notify_id)} 唯一键
 * 做第一道去重，业务状态机做第二道，两道都过才结算。
 *
 * <p>被拒绝（验签失败 / 金额不符）的通知也会落一行，便于事后回答
 * 「渠道说它通知过，为什么订单没变」。
 *
 * @author Amos
 */
@Data
@TableName("hou_market_plugin_pay_notify")
public class MarketPluginPayNotify implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 已受理并完成业务处理 */
    public static final String RESULT_SUCCESS = "SUCCESS";

    /** 渠道通知支付失败 */
    public static final String RESULT_FAILED = "FAILED";

    /** 渠道通知退款成功 */
    public static final String RESULT_REFUNDED = "REFUNDED";

    /** 被拒（验签失败 / 金额不符 / 订单不存在），需人工介入 */
    public static final String RESULT_REJECTED = "REJECTED";

    /** 重复回调，未重复处理 */
    public static final String RESULT_DUPLICATED = "DUPLICATED";

    /** 超时关闭后才到账（钱收到了但订单已关），已补发授权并告警 */
    public static final String RESULT_LATE_PAID = "LATE_PAID";

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 我方订单号 */
    private String orderNo;

    /** 渠道编码 */
    private String channel;

    /** 渠道通知唯一 ID；渠道未提供时用 tradeNo 或报文摘要 */
    private String notifyId;

    /** 渠道支付单号 */
    private String tradeNo;

    /** 处理结果：见 RESULT_* 常量 */
    private String result;

    /** 渠道原始状态串 */
    private String rawStatus;

    /** 回调金额（元），用于核对是否被篡改 */
    private java.math.BigDecimal amount;

    /** 原始报文（截断到 4000 字符） */
    private String raw;

    /** 说明 / 拒绝原因 */
    private String message;

    private LocalDateTime createTime;
}
