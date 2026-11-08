package com.houjiang.mall.pay.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.config.market.PluginMarketPayProperties;
import com.houjiang.mall.config.market.PluginPayConfigStore;
import com.houjiang.mall.entity.market.MarketPluginOrder;
import com.houjiang.plugin.market.adapter.outbound.feign.OrderPaasFeign;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * CASHIER 渠道：复用商城既有收银台（{@code /order/mall/payment/create}）收款。
 *
 * <p><b>为什么不直连微信/支付宝</b>：商城已有收银台承担了商户号、费率、对账与退款，
 * 插件市场再接一遍等于两套资金流水。这里只做「下单取支付参数 + 回调验签」。
 *
 * <p><b>回调验签</b>：收银台回调按 {@link PluginPaySigner} 的契约带
 * {@code orderNo / status / amount / timestamp / nonce / sign}，
 * 密钥取 {@code houcloud.plugin.market.pay.channels.CASHIER.apiKey}。
 * <b>未配置该密钥时一律拒收回调</b>（fail-closed）——不验签就结算等于任何人都能把订单刷成已支付。
 *
 * @author Amos
 */
@Slf4j
public class CashierPluginPayChannel implements PluginPayChannel {

    public static final String CODE = "CASHIER";

    private final OrderPaasFeign orderPaasFeign;
    private final PluginPayConfigStore store;
    private final PluginPaySigner signer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CashierPluginPayChannel(OrderPaasFeign orderPaasFeign,
                                   PluginPayConfigStore store,
                                   PluginPaySigner signer) {
        this.orderPaasFeign = orderPaasFeign;
        this.store = store;
        this.signer = signer;
    }

    /**
     * 当前生效配置快照：基线（yml/Nacos/env）叠加 {@code hou_market_pay_config} 覆盖后的最终值。
     * 每次调用都重新取，保证运营在后台改完价、点刷新后立刻生效，无需重启。
     */
    private PluginMarketPayProperties cfg() {
        return store.current();
    }

    @Override
    public String code() {
        return CODE;
    }

    /**
     * CASHIER 走商城内部服务，不需要本站配置商户号；
     * 但回调验签密钥必须有，否则回调只能拒收。
     */
    @Override
    public boolean available() {
        PluginMarketPayProperties.ChannelProperties channel = cfg().channel(CODE);
        return channel != null && !channel.getApiKey().isBlank();
    }

    @Override
    public PayIntent createPayment(MarketPluginOrder order, String payMethod, String terminal,
                                   String userId, String notifyUrl, String returnUrl) {
        MallCashierCreateRequest request = new MallCashierCreateRequest();
        request.setMallOrderNo(order.getOrderNo());
        request.setRequestNo(UUID.randomUUID().toString().replace("-", ""));
        request.setUserId(userId == null || userId.isBlank() ? String.valueOf(order.getBuyerId()) : userId);
        request.setAmount(order.getAmount());
        request.setCurrency(order.getCurrency());
        request.setPayMethod(payMethod);
        request.setTerminal(terminal == null || terminal.isBlank() ? "PC" : terminal);
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        request.setSubject("插件购买-" + order.getPluginCode());

        ResponseResult<MallCashierCreateResponse> result = orderPaasFeign.createMallCashier(request);
        if (result == null || result.getData() == null) {
            throw new IllegalStateException("创建支付单失败，收银台未返回有效数据（请检查 order 服务与收银台配置）");
        }
        MallCashierCreateResponse cashier = result.getData();
        if (cashier == null || cashier.getCashierNo() == null) {
            throw new IllegalStateException("创建支付单失败，收银台未返回流水号");
        }

        Map<String, Object> payData = toPayData(cashier.getPayData());
        if (notifyUrl != null && !notifyUrl.isBlank()) {
            payData.putIfAbsent("notifyUrl", notifyUrl);
        }

        return PayIntent.builder()
                .channel(CODE)
                .tradeNo(cashier.getCashierNo())
                .payOrderId(cashier.getPayOrderId())
                .payData(payData)
                .expireTime(cashier.getExpireTime())
                .redirect(false)
                .build();
    }

    @Override
    public PayNotifyPayload parseAndVerify(String rawBody, Map<String, String> params,
                                           Map<String, String> headers) {
        Map<String, String> data = merge(params, parseBody(rawBody));
        PluginMarketPayProperties.ChannelProperties channel = cfg().channel(CODE);
        if (channel == null || channel.getApiKey().isBlank()) {
            // fail-closed：没有密钥就无法确认回调来自收银台
            throw new PayNotifyException(
                    "CASHIER 渠道未配置回调验签密钥，拒收回调：请配置 "
                            + "houcloud.plugin.market.pay.channels.CASHIER.api-key"
                            + "（环境变量 HOU_PLUGIN_PAY_CHANNELS_CASHIER_API_KEY），"
                            + "值需与 order 服务下发回调时的签名密钥一致");
        }
        signer.verify(data, channel.getApiKey(),
                Math.max(60L, cfg().getNotifyToleranceMinutes() * 60L));

        String orderNo = firstNonBlank(data, "orderNo", "order_no", "mallOrderNo", "mall_order_no",
                "outTradeNo", "out_trade_no");
        if (orderNo == null) {
            throw new PayNotifyException("回调未携带订单号（orderNo），无法定位订单");
        }
        String status = firstNonBlank(data, "status", "tradeStatus", "trade_status", "result");
        PayNotifyPayload.Result result = normalize(status);
        BigDecimal amount = parseAmount(firstNonBlank(data, "amount", "totalAmount", "total_amount", "total_fee"));

        return PayNotifyPayload.builder()
                .orderNo(orderNo.trim())
                .tradeNo(firstNonBlank(data, "payOrderId", "pay_order_id", "tradeNo", "trade_no", "transactionId"))
                .notifyId(firstNonBlank(data, "notifyId", "notify_id"))
                .amount(amount)
                .rawStatus(status)
                .raw(toRaw(rawBody, data))
                .result(result)
                .build();
    }

    @Override
    public String successResponse() {
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }

    @Override
    public String failResponse(String message) {
        return "{\"code\":\"FAIL\",\"message\":\"" + escape(message) + "\"}";
    }

    /** 收银台状态串归一化；认不出的状态按 FAILED 处理（不猜成成功）。 */
    private PayNotifyPayload.Result normalize(String status) {
        if (status == null || status.isBlank()) {
            return PayNotifyPayload.Result.FAILED;
        }
        String value = status.trim().toUpperCase();
        if ("SUCCESS".equals(value) || "TRADE_SUCCESS".equals(value) || "PAID".equals(value)
                || "1".equals(value) || "PAY_SUCCESS".equals(value)) {
            return PayNotifyPayload.Result.SUCCESS;
        }
        if ("REFUND".equals(value) || "REFUNDED".equals(value) || "TRADE_CLOSED_REFUND".equals(value)) {
            return PayNotifyPayload.Result.REFUNDED;
        }
        return PayNotifyPayload.Result.FAILED;
    }

    private BigDecimal parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            log.warn("回调金额格式非法，跳过金额比对: {}", text);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toPayData(Object value) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            map.forEach((k, v) -> data.put(String.valueOf(k), v));
        } else if (value != null) {
            try {
                Map<String, Object> converted = objectMapper.readValue(
                        objectMapper.writeValueAsString(value), Map.class);
                if (converted != null) {
                    data.putAll(converted);
                }
            } catch (Exception e) {
                log.warn("支付参数转换失败，原样透传: {}", e.getMessage());
                data.put("raw", value);
            }
        }
        return data;
    }

    private Map<String, String> parseBody(String rawBody) {
        Map<String, String> result = new LinkedHashMap<>();
        if (rawBody == null || rawBody.isBlank()) {
            return result;
        }
        try {
            Map<?, ?> parsed = objectMapper.readValue(rawBody, Map.class);
            parsed.forEach((k, v) -> {
                if (k != null && v != null) {
                    result.put(String.valueOf(k), String.valueOf(v));
                }
            });
        } catch (Exception e) {
            log.warn("回调报文不是 JSON，按表单参数处理: {}", e.getMessage());
        }
        return result;
    }

    private Map<String, String> merge(Map<String, String> params, Map<String, String> body) {
        Map<String, String> merged = new LinkedHashMap<>();
        if (params != null) {
            merged.putAll(params);
        }
        body.forEach(merged::putIfAbsent);
        return merged;
    }

    private String firstNonBlank(Map<String, String> data, String... keys) {
        for (String key : keys) {
            String value = data.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String toRaw(String rawBody, Map<String, String> data) {
        if (rawBody != null && !rawBody.isBlank()) {
            return rawBody.length() > 4000 ? rawBody.substring(0, 4000) : rawBody;
        }
        try {
            String json = objectMapper.writeValueAsString(data);
            return json.length() > 4000 ? json.substring(0, 4000) : json;
        } catch (Exception e) {
            return data.toString();
        }
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\'");
    }

    /** 供沙箱/自测构造一个合法的回调签名（与 verify 契约一致）。 */
    public String signForTest(Map<String, String> params, long timestamp, String nonce) {
        PluginMarketPayProperties.ChannelProperties channel = cfg().channel(CODE);
        String secret = channel == null ? "" : channel.getApiKey();
        return PluginPaySigner.hmacSha256(
                PluginPaySigner.canonical(params, timestamp, nonce), secret);
    }

    /** 当前秒级时间戳（自测签名用）。 */
    public long nowSeconds() {
        return Instant.now().getEpochSecond();
    }
}
