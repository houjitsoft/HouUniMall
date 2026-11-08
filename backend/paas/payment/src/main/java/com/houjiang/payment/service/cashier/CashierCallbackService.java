package com.houjiang.payment.service.cashier;

import java.util.Map;

/**
 * 收银台异步回调处理服务（Task 1）
 *
 * <p>接收支付渠道（微信/支付宝/银联/…）的异步通知，完成：
 * <ol>
 *   <li>验签</li>
 *   <li>写 callback_log 流水</li>
 *   <li>幂等判断：订单已是 SUCCESS 直接返回成功</li>
 *   <li>金额校验</li>
 *   <li>更新收银台订单状态为 SUCCESS</li>
 *   <li>触发业务 hook（CashierBizNotifyService）</li>
 * </ol>
 *
 * @author Amos
 */
public interface CashierCallbackService {

    /**
     * 处理微信 JSAPI 异步通知
     *
     * @param rawXml     原始 XML 报文（用于落库 callback_log）
     * @param notifyMap  解析后的 Map（含 sign）
     * @return 返回给微信的应答 XML（SUCCESS/FAIL）
     */
    String handleWxJsapiNotify(String rawXml, Map<String, String> notifyMap);

    /**
     * 处理 Stripe Webhook 异步通知
     *
     * @param payload   原始 JSON 报文
     * @param sigHeader Stripe-Signature 请求头
     * @return 处理结果文案（HTTP 200 时返回 "ok"）
     */
    String handleStripeNotify(String payload, String sigHeader);
}
