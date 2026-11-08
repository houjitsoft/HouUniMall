package com.houjiang.payment.service;

import com.houjiang.payment.channel.biz.impl.StripePayStrategy;
import com.houjiang.payment.constant.CashierOrderStatusEnum;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.mapper.PayCallbackLogMapper;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.plugin.PayCallbackGateway;
import com.houjiang.payment.plugin.PromotionPaySettleGateway;
import com.houjiang.payment.service.cashier.CashierBizNotifyService;
import com.houjiang.payment.service.cashier.WxJsapiPayHandler;
import com.houjiang.payment.service.cashier.impl.CashierCallbackServiceImpl;
import com.houjiang.payment.service.api.CompensateService;
import com.houjiang.payment.dto.channel.CompensateRegisterRequest;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.ArgumentCaptor;

class CashierCallbackServiceTest {

    @Test
    void concurrentSuccessCallbacksNotifyBusinessOnce() {
        PayCashierOrderMapper cashierMapper = mock(PayCashierOrderMapper.class);
        PayCallbackLogMapper logMapper = mock(PayCallbackLogMapper.class);
        WxJsapiPayHandler handler = mock(WxJsapiPayHandler.class);
        CashierBizNotifyService notifyService = mock(CashierBizNotifyService.class);
        CompensateService compensateService = mock(CompensateService.class);
        StripePayStrategy stripePayStrategy = mock(StripePayStrategy.class);
        when(handler.verifyNotify(any())).thenReturn(true);
        when(handler.buildNotifyResponse(any(Boolean.class), any())).thenReturn("SUCCESS");
        when(cashierMapper.selectByCashierNo("CS007"))
                .thenReturn(pendingOrder(), pendingOrder());
        when(cashierMapper.markPaymentSuccess(any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(1, 0);
        when(notifyService.notifyPaySuccess(any())).thenReturn(true);
        CashierCallbackServiceImpl service = new CashierCallbackServiceImpl(
                cashierMapper, logMapper, handler, notifyService, compensateService, stripePayStrategy,
                noopPayCallbackGateway(), noopPromotionPaySettleGateway());
        Map<String, String> callback = callback();

        service.handleWxJsapiNotify("<xml/>", callback);
        service.handleWxJsapiNotify("<xml/>", callback);

        verify(notifyService, times(1)).notifyPaySuccess(any());
    }

    @Test
    void failedBusinessNotificationRegistersCompensation() {
        PayCashierOrderMapper cashierMapper = mock(PayCashierOrderMapper.class);
        PayCallbackLogMapper logMapper = mock(PayCallbackLogMapper.class);
        WxJsapiPayHandler handler = mock(WxJsapiPayHandler.class);
        CashierBizNotifyService notifyService = mock(CashierBizNotifyService.class);
        CompensateService compensateService = mock(CompensateService.class);
        StripePayStrategy stripePayStrategy = mock(StripePayStrategy.class);
        when(handler.verifyNotify(any())).thenReturn(true);
        when(handler.buildNotifyResponse(any(Boolean.class), any())).thenReturn("SUCCESS");
        when(cashierMapper.selectByCashierNo("CS007")).thenReturn(pendingOrder());
        when(cashierMapper.markPaymentSuccess(any(), anyInt(), anyInt(), any(), any())).thenReturn(1);
        when(notifyService.notifyPaySuccess(any())).thenReturn(false);
        CashierCallbackServiceImpl service = new CashierCallbackServiceImpl(
                cashierMapper, logMapper, handler, notifyService, compensateService, stripePayStrategy,
                noopPayCallbackGateway(), noopPromotionPaySettleGateway());

        service.handleWxJsapiNotify("<xml/>", callback());

        ArgumentCaptor<CompensateRegisterRequest> request =
                ArgumentCaptor.forClass(CompensateRegisterRequest.class);
        verify(compensateService).register(request.capture());
        assertEquals("PAY_NOTIFY:CS007", request.getValue().getBizNo());
    }

    private static PayCallbackGateway noopPayCallbackGateway() {
        return new PayCallbackGateway(emptySupport());
    }

    private static PromotionPaySettleGateway noopPromotionPaySettleGateway() {
        return new PromotionPaySettleGateway(emptySupport());
    }

    private static ExtensionGatewaySupport emptySupport() {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(0);
        return new ExtensionGatewaySupport(new ExtensionInvoker(new ExtensionRegistry(), properties));
    }

    private PayCashierOrderEntity pendingOrder() {
        PayCashierOrderEntity order = new PayCashierOrderEntity();
        order.setCashierNo("CS007");
        order.setMchOrderNo("MO007");
        order.setAmount(new BigDecimal("10.00"));
        order.setStatus(CashierOrderStatusEnum.PAYING.getCode());
        return order;
    }

    private Map<String, String> callback() {
        Map<String, String> callback = new HashMap<>();
        callback.put("return_code", "SUCCESS");
        callback.put("result_code", "SUCCESS");
        callback.put("out_trade_no", "CS007");
        callback.put("transaction_id", "WX007");
        callback.put("total_fee", "1000");
        callback.put("sign", "signed");
        return callback;
    }
}
