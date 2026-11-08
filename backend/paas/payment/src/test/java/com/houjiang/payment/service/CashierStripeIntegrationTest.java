package com.houjiang.payment.service;

import com.houjiang.payment.channel.biz.ChannelPayStrategy;
import com.houjiang.payment.channel.biz.PayChannelStrategyFactory;
import com.houjiang.payment.constant.PayMethodEnum;
import com.houjiang.payment.dto.cashier.CashierCreateRequest;
import com.houjiang.payment.dto.cashier.CashierCreateResponse;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import com.houjiang.payment.service.cashier.WxJsapiPayHandler;
import com.houjiang.payment.service.impl.CashierServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashierStripeIntegrationTest {

    @Test
    void stripeCashierUsesRegisteredRealChannelStrategy() {
        PayCashierOrderMapper cashierMapper = mock(PayCashierOrderMapper.class);
        PayUserResourceMapper resourceMapper = mock(PayUserResourceMapper.class);
        WxJsapiPayHandler wxHandler = mock(WxJsapiPayHandler.class);
        PayChannelStrategyFactory factory = mock(PayChannelStrategyFactory.class);
        ChannelPayStrategy strategy = mock(ChannelPayStrategy.class);
        when(factory.require(PayMethodEnum.STRIPE.getCode())).thenReturn(strategy);
        when(strategy.createPay(any())).thenReturn(Map.of(
                "channel", "STRIPE_CHECKOUT",
                "checkoutUrl", "https://checkout.stripe.com/c/pay/cs_test",
                "payUrl", "https://checkout.stripe.com/c/pay/cs_test",
                "channelOrderNo", "cs_test_001",
                "status", 0));
        CashierServiceImpl service = new CashierServiceImpl(
                cashierMapper, resourceMapper, wxHandler, factory);

        CashierCreateRequest request = new CashierCreateRequest();
        request.setMchOrderNo("MO-STRIPE-001");
        request.setTerminal("PC");
        request.setPayMethod("STRIPE");
        request.setAmount(new BigDecimal("19.90"));
        request.setCurrency("CNY");
        request.setSubject("stripe mall order");
        request.setReturnUrl("https://example.com/pay/success");

        CashierCreateResponse response = service.create(request, "U1");

        verify(strategy).createPay(any());
        assertEquals("STRIPE_CHECKOUT", response.getPayData().get("channel"));
        assertEquals("https://checkout.stripe.com/c/pay/cs_test", response.getPayData().get("checkoutUrl"));
        assertFalse(response.getPayData().containsKey("__placeholder__"));
    }
}
