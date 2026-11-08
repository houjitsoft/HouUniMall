package com.houjiang.payment.channel;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.houjiang.commons.util.PayCredentialCryptoUtil;
import com.houjiang.payment.channel.biz.impl.AlipayPayStrategy;
import com.houjiang.payment.channel.client.AlipayClientFactory;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.mapper.PayChannelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlipayPayStrategyTest {

    private AlipayClient client;
    private AlipayPayStrategy strategy;

    @BeforeEach
    void setUp() {
        PayCredentialCryptoUtil.setKeyIv("0123456789abcdef", "abcdef0123456789");
        client = mock(AlipayClient.class);
        AlipayClientFactory factory = mock(AlipayClientFactory.class);
        when(factory.create(any())).thenReturn(client);
        strategy = new AlipayPayStrategy(factory);

        PayChannelMapper mapper = mock(PayChannelMapper.class);
        PayChannelEntity channel = new PayChannelEntity();
        Map<String, Object> config = new HashMap<>();
        config.put("appId", "202607100001");
        config.put("privateKey", "merchant-private-key");
        config.put("alipayPublicKey", "alipay-public-key");
        channel.setConfigJson(PayCredentialCryptoUtil.encryptConfigJson(config));
        when(mapper.selectOne(any())).thenReturn(channel);
        ReflectionTestUtils.setField(strategy, "payChannelMapper", mapper);
    }

    @Test
    void createPayReturnsPcFormPayload() throws Exception {
        AlipayTradePagePayResponse response = new AlipayTradePagePayResponse();
        response.setBody("<form id='alipaysubmit'>pay</form>");
        when(client.pageExecute(any(AlipayTradePagePayRequest.class))).thenReturn(response);

        Map<String, Object> result = strategy.createPay(createRequest("PC"));

        assertEquals("ALIPAY_PAGE", result.get("channel"));
        assertTrue(result.get("formHtml").toString().contains("alipaysubmit"));
    }

    @Test
    void createPayRoutesH5ToWapApi() throws Exception {
        AlipayTradeWapPayResponse response = new AlipayTradeWapPayResponse();
        response.setBody("<form id='alipaysubmit'>wap</form>");
        when(client.pageExecute(any(AlipayTradeWapPayRequest.class))).thenReturn(response);

        Map<String, Object> result = strategy.createPay(createRequest("WAP"));

        assertEquals("ALIPAY_WAP", result.get("channel"));
        assertTrue(result.get("formHtml").toString().contains("wap"));
    }

    @Test
    void queryMapsSuccessfulTradeStatus() throws Exception {
        AlipayTradeQueryResponse response = new AlipayTradeQueryResponse();
        response.setCode("10000");
        response.setTradeNo("ALI-TRADE-1");
        response.setTradeStatus("TRADE_SUCCESS");
        when(client.execute(any(AlipayTradeQueryRequest.class))).thenReturn(response);

        Map<String, Object> result = strategy.queryStatus("CS001");

        assertEquals(1, result.get("status"));
        assertEquals("ALI-TRADE-1", result.get("channelOrderNo"));
    }

    @Test
    void refundUsesBusinessRefundNumber() throws Exception {
        AlipayTradeRefundResponse response = new AlipayTradeRefundResponse();
        response.setCode("10000");
        response.setTradeNo("ALI-TRADE-1");
        response.setFundChange("Y");
        when(client.execute(any(AlipayTradeRefundRequest.class))).thenReturn(response);

        Map<String, Object> result = strategy.refund(Map.of(
                "channelOrderNo", "CS001",
                "refundNo", "RF001",
                "refundAmount", new BigDecimal("2.00"),
                "refundReason", "customer request"));

        assertEquals("RF001", result.get("refundOrderNo"));
        assertEquals(1, result.get("status"));
    }

    private Map<String, Object> createRequest(String terminal) {
        return Map.of(
                "outTradeNo", "CS001",
                "amount", new BigDecimal("10.00"),
                "subject", "mall order",
                "terminal", terminal,
                "notifyUrl", "https://example.com/notify",
                "returnUrl", "https://example.com/return");
    }
}
