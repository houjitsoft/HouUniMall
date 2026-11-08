package com.houjiang.payment.channel;

import com.houjiang.commons.util.PayCredentialCryptoUtil;
import com.houjiang.payment.channel.biz.impl.StripePayStrategy;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayChannelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StripePayStrategyTest {

    private StripePayStrategy strategy;

    @BeforeEach
    void setUp() {
        PayCredentialCryptoUtil.setKeyIv("0123456789abcdef", "abcdef0123456789");
        strategy = new StripePayStrategy();
        ReflectionTestUtils.setField(strategy, "domainName", "https://example.com");
        ReflectionTestUtils.setField(strategy, "nacosPublishableKey", "pk_nacos");
        ReflectionTestUtils.setField(strategy, "nacosSecretKey", "sk_nacos");
        ReflectionTestUtils.setField(strategy, "nacosWebhookSecret", "whsec_nacos");
        ReflectionTestUtils.setField(strategy, "nacosMerchantName", "Test Merchant");

        PayChannelMapper mapper = mock(PayChannelMapper.class);
        PayChannelEntity channel = new PayChannelEntity();
        Map<String, Object> config = new HashMap<>();
        config.put("publishableKey", "pk_test_xxx");
        config.put("secretKey", "sk_test_xxx");
        config.put("webhookSecret", "whsec_xxx");
        config.put("merchantName", "Demo Shop");
        channel.setConfigJson(PayCredentialCryptoUtil.encryptConfigJson(config));
        when(mapper.selectOne(any())).thenReturn(channel);
        ReflectionTestUtils.setField(strategy, "payChannelMapper", mapper);
    }

    @Test
    void resolveWebhookSecretFromCredential() {
        assertEquals("whsec_xxx", strategy.resolveWebhookSecret(null));
    }

    @Test
    void createPayRequiresTerminal() {
        Map<String, Object> request = new HashMap<>();
        request.put("outTradeNo", "CS001");
        request.put("amount", new BigDecimal("1.00"));
        request.put("subject", "test");

        PaymentException ex = assertThrows(PaymentException.class, () -> strategy.createPay(request));
        assertTrue(ex.getMessage().contains("terminal"));
    }

    @Test
    void channelCodeIsStripe() {
        assertEquals("STRIPE", strategy.getChannel());
    }

    @Test
    void fallbackToNacosWhenChannelMissing() {
        PayChannelMapper mapper = mock(PayChannelMapper.class);
        when(mapper.selectOne(any())).thenReturn(null);
        ReflectionTestUtils.setField(strategy, "payChannelMapper", mapper);

        assertEquals("whsec_nacos", strategy.resolveWebhookSecret(null));
    }
}
