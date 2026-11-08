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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashierWechatV3IntegrationTest {

    @Test
    void newWechatCashierUsesV3StrategyInsteadOfLegacyV2Handler() {
        PayCashierOrderMapper cashierMapper = mock(PayCashierOrderMapper.class);
        PayUserResourceMapper resourceMapper = mock(PayUserResourceMapper.class);
        WxJsapiPayHandler legacyHandler = mock(WxJsapiPayHandler.class);
        PayChannelStrategyFactory factory = mock(PayChannelStrategyFactory.class);
        ChannelPayStrategy strategy = mock(ChannelPayStrategy.class);
        when(factory.require(PayMethodEnum.WECHAT_V3.getCode())).thenReturn(strategy);
        when(strategy.createPay(any())).thenReturn(Map.of(
                "channel", "WX_NATIVE", "codeUrl", "weixin://pay/real",
                "channelOrderNo", "CS002", "status", 0));
        CashierServiceImpl service = new CashierServiceImpl(
                cashierMapper, resourceMapper, legacyHandler, factory);

        CashierCreateRequest request = new CashierCreateRequest();
        request.setMchOrderNo("MO002");
        request.setTerminal("PC");
        request.setPayMethod("WECHAT");
        request.setChannelCode("WECHAT_V3");
        request.setAmount(new BigDecimal("20.00"));
        request.setSubject("mall order");
        request.setNotifyUrl("https://example.com/wechat/notify");

        CashierCreateResponse response = service.create(request, "U1");

        verify(strategy).createPay(any());
        verify(legacyHandler, never()).unifiedOrder(any(), any(), any(), any());
        assertEquals("WX_NATIVE", response.getPayData().get("channel"));
    }
}
