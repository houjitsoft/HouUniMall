package com.houjiang.payment.channel.biz.impl;

import com.houjiang.payment.channel.biz.AbstractChannelPayStrategy;
import com.houjiang.payment.constant.PayMethodEnum;
import com.houjiang.payment.exception.PaymentException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Stripe 渠道策略：PC/H5 走 Checkout Session，APP 走 PaymentIntent。
 * 凭证优先从 hou_pay_channel 读取，缺失时回退 Nacos stripe.* 配置。
 * @author Amos
 */
@Slf4j
@Component
public class StripePayStrategy extends AbstractChannelPayStrategy {

    @Value("${stripe.key:}")
    private String nacosPublishableKey;

    @Value("${stripe.sectret:}")
    private String nacosSecretKey;

    @Value("${stripe.webhooksKey:}")
    private String nacosWebhookSecret;

    @Value("${stripe.merchantName:}")
    private String nacosMerchantName;

    @Value("${domainName:http://localhost:8207}")
    private String domainName;

    @Override
    public String getChannel() {
        return PayMethodEnum.STRIPE.getCode();
    }

    @Override
    protected String[] requiredFields() {
        return new String[]{"publishableKey", "secretKey"};
    }

    @Override
    public Map<String, Object> createPay(Map<String, Object> request) {
        String outTradeNo = requiredString(request, "outTradeNo");
        BigDecimal amount = requiredAmount(request, "amount");
        String subject = requiredString(request, "subject");
        String terminal = requiredString(request, "terminal").toUpperCase(Locale.ROOT);
        Map<String, Object> credential = resolveCredential(stringValue(request.get("mchNo")));
        applyApiKey(credential);

        String currency = resolveCurrency(request);
        long unitAmount = toMinorUnit(amount);
        String returnUrl = stringValue(request.get("returnUrl"));
        String body = stringValue(request.get("body"));

        try {
            return switch (terminal) {
                case "APP", "HARMONY" -> createPaymentIntent(
                        credential, outTradeNo, unitAmount, currency, subject);
                case "PC", "WAP", "H5", "WX_MINI", "ALI_MINI" -> createCheckoutSession(
                        outTradeNo, unitAmount, currency, subject, body, returnUrl, terminal);
                default -> throw PaymentException.bizError("Unsupported Stripe terminal: " + terminal);
            };
        } catch (StripeException e) {
            log.error("[Stripe] createPay failed, outTradeNo={}, err={}", outTradeNo, e.getMessage(), e);
            throw PaymentException.bizError("Stripe create payment failed: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> queryStatus(String channelOrderNo) {
        if (channelOrderNo == null || channelOrderNo.isBlank()) {
            throw PaymentException.bizError("channelOrderNo is required");
        }
        Map<String, Object> credential = resolveCredential(null);
        applyApiKey(credential);
        try {
            Map<String, Object> result = new HashMap<>();
            if (channelOrderNo.startsWith("cs_")) {
                Session session = Session.retrieve(channelOrderNo);
                result.put("status", mapSessionStatus(session.getPaymentStatus(), session.getStatus()));
                result.put("channelOrderNo", session.getPaymentIntent() != null
                        ? session.getPaymentIntent() : session.getId());
                result.put("paidTime", session.getCreated() == null ? null
                        : new java.util.Date(session.getCreated() * 1000L));
                result.put("paymentStatus", session.getPaymentStatus());
                return result;
            }
            PaymentIntent intent = PaymentIntent.retrieve(channelOrderNo);
            result.put("status", mapIntentStatus(intent.getStatus()));
            result.put("channelOrderNo", intent.getId());
            result.put("paidTime", intent.getCreated() == null ? null
                    : new java.util.Date(intent.getCreated() * 1000L));
            result.put("tradeStatus", intent.getStatus());
            return result;
        } catch (StripeException e) {
            throw PaymentException.bizError("Stripe query failed: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> refund(Map<String, Object> requestData) {
        String channelOrderNo = requiredString(requestData, "channelOrderNo");
        String refundNo = requiredString(requestData, "refundNo");
        BigDecimal refundAmount = requiredAmount(requestData, "refundAmount");
        Map<String, Object> credential = resolveCredential(stringValue(requestData.get("mchNo")));
        applyApiKey(credential);

        try {
            RefundCreateParams.Builder builder = RefundCreateParams.builder()
                    .setAmount(toMinorUnit(refundAmount))
                    .putMetadata("refundNo", refundNo);
            if (channelOrderNo.startsWith("pi_")) {
                builder.setPaymentIntent(channelOrderNo);
            } else if (channelOrderNo.startsWith("cs_")) {
                Session session = Session.retrieve(channelOrderNo);
                if (session.getPaymentIntent() == null || session.getPaymentIntent().isBlank()) {
                    throw PaymentException.bizError("Stripe session has no payment intent for refund");
                }
                builder.setPaymentIntent(session.getPaymentIntent());
            } else {
                builder.setPaymentIntent(channelOrderNo);
            }
            Refund refund = Refund.create(builder.build());
            Map<String, Object> result = new HashMap<>();
            result.put("refundOrderNo", refundNo);
            result.put("channelOrderNo", refund.getId());
            result.put("status", "succeeded".equalsIgnoreCase(refund.getStatus()) ? 1 : 0);
            result.put("successTime", refund.getCreated() == null ? null
                    : new java.util.Date(refund.getCreated() * 1000L));
            return result;
        } catch (StripeException e) {
            throw PaymentException.bizError("Stripe refund failed: " + e.getMessage());
        }
    }

    /**
     * 供回调验签读取 webhookSecret（优先渠道凭证，其次 Nacos）。
     */
    public String resolveWebhookSecret(String mchNo) {
        Map<String, Object> credential = resolveCredential(mchNo);
        String secret = stringValue(credential.get("webhookSecret"));
        if (secret == null || secret.isBlank()) {
            secret = nacosWebhookSecret;
        }
        return secret;
    }

    private Map<String, Object> createCheckoutSession(String outTradeNo, long unitAmount,
                                                      String currency, String subject, String body,
                                                      String returnUrl, String terminal) throws StripeException {
        String successUrl = buildReturnUrl(returnUrl, outTradeNo);
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setSuccessUrl(successUrl)
                .setCancelUrl(successUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount(unitAmount)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(subject)
                                        .setDescription(body == null || body.isBlank() ? subject : body)
                                        .build())
                                .build())
                        .build())
                .putMetadata("outTradeNo", outTradeNo)
                .putMetadata("currencyCode", currency)
                .putMetadata("terminal", terminal);

        Session session = Session.create(builder.build());
        if (session.getUrl() == null || session.getUrl().isBlank()) {
            throw PaymentException.bizError("Stripe returned empty checkout URL");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("channel", "STRIPE_CHECKOUT");
        result.put("checkoutUrl", session.getUrl());
        result.put("payUrl", session.getUrl());
        result.put("channelOrderNo", session.getId());
        result.put("status", 0);
        return result;
    }

    private Map<String, Object> createPaymentIntent(Map<String, Object> credential, String outTradeNo,
                                                    long unitAmount, String currency,
                                                    String subject) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(unitAmount)
                .setCurrency(currency)
                .setDescription(subject)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .putMetadata("outTradeNo", outTradeNo)
                .putMetadata("currencyCode", currency)
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        Map<String, Object> result = new HashMap<>();
        result.put("channel", "STRIPE_APP");
        result.put("clientSecret", intent.getClientSecret());
        result.put("publishableKey", stringValue(credential.get("publishableKey")));
        result.put("merchantName", firstNonBlank(
                stringValue(credential.get("merchantName")), nacosMerchantName, "Stripe"));
        result.put("channelOrderNo", intent.getId());
        result.put("status", 0);
        return result;
    }

    private Map<String, Object> resolveCredential(String mchNo) {
        try {
            return loadCredential(mchNo);
        } catch (PaymentException ex) {
            if (nacosSecretKey == null || nacosSecretKey.isBlank()
                    || nacosPublishableKey == null || nacosPublishableKey.isBlank()) {
                throw ex;
            }
            log.warn("[Stripe] channel credential missing, fallback to Nacos stripe.* config");
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("publishableKey", nacosPublishableKey);
            fallback.put("secretKey", nacosSecretKey);
            fallback.put("webhookSecret", nacosWebhookSecret);
            fallback.put("merchantName", nacosMerchantName);
            return fallback;
        }
    }

    private void applyApiKey(Map<String, Object> credential) {
        String secretKey = stringValue(credential.get("secretKey"));
        if (secretKey == null || secretKey.isBlank()) {
            throw PaymentException.bizError(ERR_CHANNEL_NOT_CONFIGURED + ": Stripe secretKey missing");
        }
        Stripe.apiKey = secretKey;
    }

    private String buildReturnUrl(String returnUrl, String outTradeNo) {
        String base = (returnUrl == null || returnUrl.isBlank())
                ? domainName + "/pay/success"
                : returnUrl;
        String joiner = base.contains("?") ? "&" : "?";
        return base + joiner + "cashierNo=" + outTradeNo + "&session_id={CHECKOUT_SESSION_ID}";
    }

    private String resolveCurrency(Map<String, Object> request) {
        String currency = stringValue(request.get("currency"));
        if (currency == null || currency.isBlank()) {
            currency = "CNY";
        }
        return currency.toLowerCase(Locale.ROOT);
    }

    private long toMinorUnit(BigDecimal amount) {
        return amount.multiply(new BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private int mapSessionStatus(String paymentStatus, String sessionStatus) {
        if ("paid".equalsIgnoreCase(paymentStatus)) return 1;
        if ("unpaid".equalsIgnoreCase(paymentStatus) && "expired".equalsIgnoreCase(sessionStatus)) return 2;
        return 0;
    }

    private int mapIntentStatus(String status) {
        if ("succeeded".equalsIgnoreCase(status)) return 1;
        if ("canceled".equalsIgnoreCase(status)) return 2;
        return 0;
    }

    private String requiredString(Map<String, Object> data, String key) {
        String value = stringValue(data.get(key));
        if (value == null || value.isBlank()) {
            throw PaymentException.bizError(key + " is required");
        }
        return value;
    }

    private BigDecimal requiredAmount(Map<String, Object> data, String key) {
        Object value = data.get(key);
        BigDecimal amount = value == null ? null : new BigDecimal(String.valueOf(value));
        if (amount == null || amount.signum() <= 0) {
            throw PaymentException.bizError(key + " must be positive");
        }
        return amount;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
