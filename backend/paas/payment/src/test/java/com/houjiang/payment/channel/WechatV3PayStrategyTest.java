package com.houjiang.payment.channel;

import com.houjiang.commons.util.PayCredentialCryptoUtil;
import com.houjiang.payment.channel.biz.impl.WechatV3PayStrategy;
import com.houjiang.payment.channel.client.WechatClientFactory;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.mapper.PayChannelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WechatV3PayStrategyTest {

    private WechatClientFactory.WechatClient client;
    private WechatV3PayStrategy strategy;

    @BeforeEach
    void setUp() {
        PayCredentialCryptoUtil.setKeyIv("0123456789abcdef", "abcdef0123456789");
        client = mock(WechatClientFactory.WechatClient.class);
        WechatClientFactory factory = mock(WechatClientFactory.class);
        when(factory.create(any())).thenReturn(client);
        strategy = new WechatV3PayStrategy(factory);

        PayChannelMapper mapper = mock(PayChannelMapper.class);
        PayChannelEntity channel = new PayChannelEntity();
        Map<String, Object> config = new HashMap<>();
        config.put("appId", "wx-app-id");
        config.put("mchId", "1900000109");
        config.put("apiV3Key", "0123456789abcdef0123456789abcdef");
        config.put("certSerialNo", "CERT-SERIAL");
        config.put("privateKey", "-----BEGIN PRIVATE KEY-----\nmock\n-----END PRIVATE KEY-----");
        channel.setConfigJson(PayCredentialCryptoUtil.encryptConfigJson(config));
        when(mapper.selectOne(any())).thenReturn(channel);
        ReflectionTestUtils.setField(strategy, "payChannelMapper", mapper);
    }

    @Test
    void nativePayReturnsCodeUrl() {
        when(client.nativePrepay(any())).thenReturn("weixin://wxpay/bizpayurl?pr=abc");

        Map<String, Object> result = strategy.createPay(createRequest("PC", null));

        assertEquals("WX_NATIVE", result.get("channel"));
        assertEquals("weixin://wxpay/bizpayurl?pr=abc", result.get("codeUrl"));
    }

    @Test
    void miniProgramReturnsSignedJsapiPayload() {
        Map<String, String> payload = Map.of(
                "appId", "wx-app-id", "timeStamp", "1", "nonceStr", "nonce",
                "package", "prepay_id=wx123", "signType", "RSA", "paySign", "signature");
        when(client.jsapiPrepay(any())).thenReturn(payload);

        Map<String, Object> result = strategy.createPay(createRequest("WX_MINI", "openid-1"));

        assertEquals("WX_JSAPI", result.get("channel"));
        assertEquals(payload, result.get("packageParams"));
    }

    @Test
    void queryMapsSuccessfulTradeState() {
        when(client.query("CS002")).thenReturn(Map.of(
                "tradeState", "SUCCESS", "transactionId", "WX-TX-1",
                "successTime", "2026-07-10T16:00:00+08:00"));

        Map<String, Object> result = strategy.queryStatus("CS002");

        assertEquals(1, result.get("status"));
        assertEquals("WX-TX-1", result.get("channelOrderNo"));
    }

    @Test
    void refundUsesUniqueRefundNumber() {
        when(client.refund(any())).thenReturn(Map.of(
                "status", "SUCCESS", "refundId", "WX-RF-1",
                "successTime", "2026-07-10T16:01:00+08:00"));

        Map<String, Object> result = strategy.refund(Map.of(
                "channelOrderNo", "CS002", "refundNo", "RF002",
                "refundAmount", new BigDecimal("2.00"),
                "totalAmount", new BigDecimal("20.00")));

        assertEquals("RF002", result.get("refundOrderNo"));
        assertEquals(1, result.get("status"));
    }

    private Map<String, Object> createRequest(String terminal, String openId) {
        Map<String, Object> request = new HashMap<>();
        request.put("outTradeNo", "CS002");
        request.put("amount", new BigDecimal("20.00"));
        request.put("subject", "mall order");
        request.put("terminal", terminal);
        request.put("openId", openId);
        request.put("notifyUrl", "https://example.com/wechat/notify");
        return request;
    }
}
