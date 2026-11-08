package com.houjiang.payment.channel.biz.impl;

import com.houjiang.payment.channel.biz.AbstractChannelPayStrategy;
import com.houjiang.payment.channel.client.WechatClientFactory;
import com.houjiang.payment.constant.PayMethodEnum;
import com.houjiang.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Component
public class WechatV3PayStrategy extends AbstractChannelPayStrategy {

    private final WechatClientFactory clientFactory;

    @Override
    public String getChannel() {
        return PayMethodEnum.WECHAT_V3.getCode();
    }

    @Override
    protected String[] requiredFields() {
        return new String[]{"appId", "mchId", "apiV3Key", "certSerialNo", "privateKey"};
    }

    @Override
    public Map<String, Object> createPay(Map<String, Object> request) {
        requiredString(request, "outTradeNo");
        requiredAmount(request, "amount");
        String terminal = requiredString(request, "terminal").toUpperCase();
        Map<String, Object> credential = loadCredential(stringValue(request.get("mchNo")));
        WechatClientFactory.WechatClient client = clientFactory.create(credential);
        Map<String, Object> result = new HashMap<>();
        if ("PC".equals(terminal)) {
            String codeUrl = client.nativePrepay(request);
            if (codeUrl == null || codeUrl.isBlank()) throw PaymentException.bizError("WeChat returned empty codeUrl");
            result.put("channel", "WX_NATIVE");
            result.put("codeUrl", codeUrl);
        } else if ("WX_MINI".equals(terminal) || "WAP".equals(terminal)) {
            requiredString(request, "openId");
            result.put("channel", "WX_JSAPI");
            result.put("packageParams", client.jsapiPrepay(request));
        } else {
            throw PaymentException.bizError("Unsupported WeChat V3 terminal: " + terminal);
        }
        result.put("channelOrderNo", request.get("outTradeNo"));
        result.put("status", 0);
        return result;
    }

    @Override
    public Map<String, Object> queryStatus(String channelOrderNo) {
        Map<String, Object> response = clientFactory.create(loadCredential(null)).query(channelOrderNo);
        String tradeState = stringValue(response.get("tradeState"));
        Map<String, Object> result = new HashMap<>(response);
        result.put("status", mapTradeState(tradeState));
        result.put("channelOrderNo", response.get("transactionId"));
        result.put("paidTime", response.get("successTime"));
        return result;
    }

    @Override
    public Map<String, Object> refund(Map<String, Object> request) {
        String refundNo = requiredString(request, "refundNo");
        requiredString(request, "channelOrderNo");
        requiredAmount(request, "refundAmount");
        requiredAmount(request, "totalAmount");
        Map<String, Object> response = clientFactory
                .create(loadCredential(stringValue(request.get("mchNo"))))
                .refund(request);
        String status = stringValue(response.get("status"));
        Map<String, Object> result = new HashMap<>(response);
        result.put("refundOrderNo", refundNo);
        result.put("channelRefundNo", response.get("refundId"));
        result.put("status", "SUCCESS".equals(status) ? 1 : "CLOSED".equals(status) ? 2 : 0);
        return result;
    }

    public Map<String, Object> refreshPlatformCert(String mchNo) {
        Map<String, Object> credential = loadCredential(mchNo);
        clientFactory.evict(credential);
        clientFactory.create(credential);
        Map<String, Object> result = new HashMap<>();
        result.put("mchNo", mchNo);
        result.put("refreshAt", new Date());
        result.put("status", "refreshed");
        result.put("mchId", credential.get("mchId"));
        result.put("certSerialNo", credential.get("certSerialNo"));
        return result;
    }

    private int mapTradeState(String state) {
        if ("SUCCESS".equals(state)) return 1;
        if ("CLOSED".equals(state) || "REVOKED".equals(state) || "PAYERROR".equals(state)) return 2;
        if ("REFUND".equals(state)) return 3;
        return 0;
    }

    private String requiredString(Map<String, Object> data, String key) {
        String value = stringValue(data.get(key));
        if (value == null || value.isBlank()) throw PaymentException.bizError(key + " is required");
        return value;
    }

    private BigDecimal requiredAmount(Map<String, Object> data, String key) {
        Object value = data.get(key);
        BigDecimal amount = value == null ? null : new BigDecimal(String.valueOf(value));
        if (amount == null || amount.signum() <= 0) throw PaymentException.bizError(key + " must be positive");
        return amount;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
