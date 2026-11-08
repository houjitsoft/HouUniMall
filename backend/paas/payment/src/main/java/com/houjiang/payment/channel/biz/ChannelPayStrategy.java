package com.houjiang.payment.channel.biz;

import java.util.Map;

/**
 * 自研支付平台·渠道策略统一接口（W4 P0 任务 T8）
 * <p>
 * 与历史 {@link com.houjiang.payment.service.PayStrategyHandleService} 物理隔离：
 * <ul>
 *   <li>历史接口面向通企付适配，调用 Seata + Feign + 通企付历史 Mapper</li>
 *   <li>本接口面向自研支付平台，仅约束渠道厂商交互（下单/查单/退款），不涉及订单/账户事务</li>
 *   <li>各实现直连厂商 SDK（云闪付 SDK/支付宝 SDK 等），不通过通企付转发</li>
 * </ul>
 *
 * @author Amos
 */
public interface ChannelPayStrategy {

    /**
     * 渠道编码，必须返回 {@link com.houjiang.payment.constant.PayMethodEnum} 中的 code。
     */
    String getChannel();

    /**
     * 创建渠道支付订单。
     * @param req 入参约定字段：mchNo / outTradeNo / amount(BigDecimal) / subject / notifyUrl / extra
     * @return 出参约定字段：channelOrderNo / payUrl|prepayInfo / raw
     */
    Map<String, Object> createPay(Map<String, Object> req);

    /**
     * 查询渠道订单状态。
     * @param channelOrderNo 渠道订单号
     * @return 出参约定字段：status(0待支付 1成功 2失败 3已退款) / paidTime / raw
     */
    Map<String, Object> queryStatus(String channelOrderNo);

    /**
     * 渠道退款。
     * @param req 入参约定字段：channelOrderNo / refundAmount(BigDecimal) / refundReason
     * @return 出参约定字段：refundOrderNo / status / raw
     */
    Map<String, Object> refund(Map<String, Object> req);
}
