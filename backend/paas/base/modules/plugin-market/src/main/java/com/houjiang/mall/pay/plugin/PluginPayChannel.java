package com.houjiang.mall.pay.plugin;

import com.houjiang.mall.entity.market.MarketPluginOrder;

import java.util.Map;

/**
 * 收费插件的支付渠道抽象。
 *
 * <p>一条渠道只负责两件事：<b>把钱收出去</b>（{@link #createPayment}）和
 * <b>把结果验回来</b>（{@link #parseAndVerify}）。订单状态流转与授权签发由
 * {@code PluginPayNotifyService} 统一编排，渠道不碰业务状态，避免每接一个渠道就复制一遍结算逻辑。
 *
 * <p><b>新增渠道的步骤</b>：实现本接口 → 在 {@code PluginPayChannelRouter} 注册 →
 * 在 {@code houcloud.plugin.market.pay.channels.<CODE>} 配好商户参数 →
 * 把 {@code channel} 切到新编码。业务侧无需改动。
 *
 * @author Amos
 */
public interface PluginPayChannel {

    /** 渠道编码，与配置 {@code pay.channel} 及回调路径末段一致，如 WECHAT_V3。 */
    String code();

    /**
     * 是否可用（商户参数是否齐备）。不可用时不注册进路由，下单时报明确错误。
     */
    boolean available();

    /**
     * 不可用原因（供报错文案直出）。返回 null 时由路由按「缺哪些商户字段」兜底描述。
     *
     * <p>为什么需要它：{@code MOCK} 渠道不可用不是因为缺商户号，而是「生产环境不允许开沙箱」，
     * 若按缺字段描述会误导成一个永远填不满的清单。
     */
    default String unavailableReason() {
        return null;
    }

    /**
     * 发起支付：向渠道下单，返回前端支付所需参数。
     *
     * @param order     待支付订单（金额、币种、订单号均以此为准）
     * @param payMethod 支付方式（NATIVE / JSAPI / H5 ...），渠道不支持时抛异常
     * @param terminal  终端 PC / H5 / APP
     * @param userId    付款用户
     * @param notifyUrl 渠道回调地址（由配置拼出）
     * @param returnUrl 支付完成后前端跳转地址，可为 null
     */
    PayIntent createPayment(MarketPluginOrder order, String payMethod, String terminal,
                            String userId, String notifyUrl, String returnUrl);

    /**
     * 解析并验签回调。
     *
     * <p><b>必须做的三件事</b>：① 验签（不通过直接抛 fatal 异常）；
     * ② 校验金额与订单一致（防篡改）；③ 归一化结果为 SUCCESS / FAILED / REFUNDED。
     *
     * @param rawBody 原始报文体（微信 V3 为 JSON，支付宝为 form）
     * @param params  表单/查询参数（支付宝 form 回调、部分渠道签名在 query 上）
     * @param headers 请求头（微信 V3 的验签要素全在头里）
     * @throws PayNotifyException fatal=true 表示验签/金额问题，渠道会持续重发，需要人工介入
     */
    PayNotifyPayload parseAndVerify(String rawBody, Map<String, String> params,
                                    Map<String, String> headers);

    /** 返回给渠道的「处理成功」应答原文（微信 V3 要 JSON，支付宝要字符串 success）。 */
    String successResponse();

    /** 返回给渠道的「处理失败」应答原文，让渠道按自身策略重发。 */
    String failResponse(String message);
}
