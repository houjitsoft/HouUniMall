package com.houjiang.mall.pay.plugin;

import com.houjiang.mall.config.market.PluginMarketPayProperties;
import com.houjiang.mall.config.market.PluginPayConfigStore;
import com.houjiang.mall.entity.market.MarketPluginOrder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MOCK 沙箱渠道：不碰真实资金，用于联调与演示整条闭环。
 *
 * <p>能做什么：下单返回「模拟支付」参数 → 前端点「模拟支付成功」→ 走
 * {@code /base/plugin-center/market/pay/sandbox/confirm} → 真实回调链路结算发证。
 * 除「钱是真的」之外，其余代码路径（验签、幂等、状态机、发证、安装门控）与生产完全一致。
 *
 * <p>⚠️ <b>生产必须关闭</b>：{@code pay.sandbox=false} 且 {@code pay.channel != MOCK}。
 * 沙箱开启时，任何人都能把任意订单刷成已支付并领走授权。
 *
 * @author Amos
 */
@Slf4j
public class MockPluginPayChannel implements PluginPayChannel {

    public static final String CODE = "MOCK";

    /** 未单独配置 MOCK.api-key 时的演示密钥。仅沙箱可用，禁止用于生产。 */
    public static final String DEMO_SECRET = "HOUCLOUD-SANDBOX-DEMO-KEY";

    private final PluginPayConfigStore store;
    private final PluginPaySigner signer;

    public MockPluginPayChannel(PluginPayConfigStore store, PluginPaySigner signer) {
        this.store = store;
        this.signer = signer;
    }

    /** 当前生效配置快照（DB 覆盖后的最终值），每次重新取以支持热更新。 */
    private PluginMarketPayProperties cfg() {
        return store.current();
    }

    @Override
    public String code() {
        return CODE;
    }

    /**
     * 仅沙箱开关打开时可用。
     *
     * <p><b>为什么不再认 {@code channel=MOCK}</b>：原先 {@code sandbox=false 且 channel=MOCK} 也算可用，
     * 于是「生产环境把 channel 配成 MOCK」就能用
     * {@code /base/plugin-center/market/pay/sandbox/confirm} 之外的方式拿到模拟支付能力——
     * 只要有人知道订单号就能把订单刷成已支付并领走授权。沙箱能力必须由 {@code sandbox} 这一个开关管，
     * 且默认关闭。
     */
    @Override
    public boolean available() {
        return cfg().isSandbox();
    }

    @Override
    public String unavailableReason() {
        return "沙箱未开启（houcloud.plugin.market.pay.sandbox=false）。MOCK 渠道只在沙箱联调时可用，"
                + "生产环境请配置 sandbox=false 并使用 CASHIER 等真实渠道";
    }

    @Override
    public PayIntent createPayment(MarketPluginOrder order, String payMethod, String terminal,
                                   String userId, String notifyUrl, String returnUrl) {
        if (!available()) {
            throw new IllegalStateException(
                    "沙箱渠道不可用：请设置 houcloud.plugin.market.pay.sandbox=true（生产环境请保持 false）");
        }
        Map<String, Object> payData = new LinkedHashMap<>();
        payData.put("mode", "MOCK");
        payData.put("orderNo", order.getOrderNo());
        payData.put("amount", order.getAmount());
        payData.put("currency", order.getCurrency());
        payData.put("pluginCode", order.getPluginCode());
        payData.put("expireSeconds", secondsBetween(LocalDateTime.now(), order.getExpireTime()));
        // 前端据此渲染「模拟支付成功 / 模拟支付失败」按钮，走真实回调链路而非直接改状态
        payData.put("sandboxConfirmPath", "/web/plugin-center/market/pay/sandbox/confirm");
        payData.put("notifyUrl", notifyUrl);

        return PayIntent.builder()
                .channel(CODE)
                .tradeNo("MOCK" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
                .payOrderId("MOCKPAY" + System.currentTimeMillis())
                .payData(payData)
                .expireTime(order.getExpireTime())
                .redirect(false)
                .build();
    }

    @Override
    public PayNotifyPayload parseAndVerify(String rawBody, Map<String, String> params,
                                           Map<String, String> headers) {
        if (!available()) {
            throw new PayNotifyException("沙箱渠道未开启，拒收 MOCK 回调");
        }
        Map<String, String> data = params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params);
        PluginMarketPayProperties.ChannelProperties channel = cfg().channel(CODE);
        String secret = channel != null && !channel.getApiKey().isBlank()
                ? channel.getApiKey()
                : DEMO_SECRET;
        if (secret.equals(DEMO_SECRET)) {
            log.warn("MOCK 渠道使用内置演示密钥，仅可用于沙箱联调");
        }
        signer.verify(data, secret, Math.max(60L, cfg().getNotifyToleranceMinutes() * 60L));

        String orderNo = data.get("orderNo");
        if (orderNo == null || orderNo.isBlank()) {
            throw new PayNotifyException("回调缺少 orderNo");
        }
        String status = data.get("result");
        if (status == null || status.isBlank()) {
            status = data.get("status");
        }
        PayNotifyPayload.Result result = normalize(status);
        BigDecimal amount = null;
        String amountText = data.get("amount");
        if (amountText != null && !amountText.isBlank()) {
            try {
                amount = new BigDecimal(amountText.trim());
            } catch (NumberFormatException ignored) {
                log.warn("沙箱回调金额格式非法，跳过比对: {}", amountText);
            }
        }
        return PayNotifyPayload.builder()
                .orderNo(orderNo.trim())
                .tradeNo(data.get("tradeNo"))
                .notifyId(data.get("notifyId"))
                .amount(amount)
                .rawStatus(status)
                .raw(data.toString())
                .result(result)
                .build();
    }

    @Override
    public String successResponse() {
        return "success";
    }

    @Override
    public String failResponse(String message) {
        return "fail:" + (message == null ? "" : message);
    }

    private PayNotifyPayload.Result normalize(String status) {
        if (status == null || status.isBlank()) {
            return PayNotifyPayload.Result.FAILED;
        }
        return switch (status.trim().toUpperCase()) {
            case "SUCCESS", "PAID" -> PayNotifyPayload.Result.SUCCESS;
            case "REFUND", "REFUNDED" -> PayNotifyPayload.Result.REFUNDED;
            default -> PayNotifyPayload.Result.FAILED;
        };
    }

    private long secondsBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return 0;
        }
        long seconds = java.time.Duration.between(from, to).getSeconds();
        return Math.max(seconds, 0);
    }

    /** 构造沙箱自测签名（与 parseAndVerify 契约一致）。 */
    public Map<String, String> buildSignedParams(String orderNo, String result, String amount) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("orderNo", orderNo);
        params.put("result", result);
        if (amount != null) {
            params.put("amount", amount);
        }
        long ts = Instant.now().getEpochSecond();
        String nonce = UUID.randomUUID().toString().substring(0, 8);
        params.put("timestamp", String.valueOf(ts));
        params.put("nonce", nonce);
        PluginMarketPayProperties.ChannelProperties channel = cfg().channel(CODE);
        String secret = channel != null && !channel.getApiKey().isBlank()
                ? channel.getApiKey()
                : DEMO_SECRET;
        params.put("sign", PluginPaySigner.hmacSha256(
                PluginPaySigner.canonical(params, ts, nonce), secret));
        return params;
    }
}
