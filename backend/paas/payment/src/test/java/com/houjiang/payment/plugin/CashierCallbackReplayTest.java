package com.houjiang.payment.plugin;

import com.houjiang.payment.channel.biz.impl.StripePayStrategy;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.mapper.PayCallbackLogMapper;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.service.api.CompensateService;
import com.houjiang.payment.service.cashier.CashierBizNotifyService;
import com.houjiang.payment.service.cashier.WxJsapiPayHandler;
import com.houjiang.payment.service.cashier.impl.CashierCallbackServiceImpl;
import com.houjiang.plugin.api.extension.payment.PayCallbackExtension;
import com.houjiang.plugin.api.extension.payment.PromotionPaySettleExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 支付回调<b>重放</b>验证：同一微信 JSAPI 回调重复投递，{@code PayCallbackExtension} /
 * {@code PromotionPaySettleExtension} 只应被触发 <b>一次</b>，重放不产生二次副作用。
 *
 * <p>用一个内存模拟的收银台订单贯穿两次回调：第一次状态由「待支付」迁移到「支付成功」并触发扩展点；
 * 第二次命中「已是 SUCCESS → 幂等跳过」分支，扩展点不再触发。
 */
class CashierCallbackReplayTest {

    @Test
    void 支付回调重放不产生二次副作用() {
        AtomicInteger payCallbackCount = new AtomicInteger();
        AtomicInteger promotionSettleCount = new AtomicInteger();

        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(0);
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(PayCallbackExtension.class,
                (PayCallbackExtension) ctx -> payCallbackCount.incrementAndGet(), "custom-pay", 0, "official");
        registry.register(PromotionPaySettleExtension.class,
                (PromotionPaySettleExtension) ctx -> promotionSettleCount.incrementAndGet(), "custom-settle", 0, "official");
        ExtensionGatewaySupport support = new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));

        PayCashierOrderMapper cashierMapper = mock(PayCashierOrderMapper.class);
        PayCallbackLogMapper callbackLogMapper = mock(PayCallbackLogMapper.class);
        WxJsapiPayHandler wxHandler = mock(WxJsapiPayHandler.class);
        CashierBizNotifyService bizNotifyService = mock(CashierBizNotifyService.class);
        CompensateService compensateService = mock(CompensateService.class);
        StripePayStrategy stripePayStrategy = mock(StripePayStrategy.class);

        PayCashierOrderEntity order = new PayCashierOrderEntity();
        order.setCashierNo("C1");
        order.setMchOrderNo("M1");
        order.setMallOrderNo("MALL1");
        order.setAmount(new BigDecimal("1.00"));
        order.setStatus(0);
        order.setPayMethod("WX_JSAPI");

        when(cashierMapper.selectByCashierNo("C1")).thenReturn(order);
        when(cashierMapper.markPaymentSuccess(anyString(), anyInt(), anyInt(), any(), any())).thenReturn(1);
        when(wxHandler.verifyNotify(any())).thenReturn(true);
        when(wxHandler.buildNotifyResponse(anyBoolean(), anyString())).thenReturn("<xml>OK</xml>");
        when(bizNotifyService.notifyPaySuccess(any())).thenReturn(true);

        CashierCallbackServiceImpl service = new CashierCallbackServiceImpl(
                cashierMapper, callbackLogMapper, wxHandler, bizNotifyService, compensateService, stripePayStrategy,
                new PayCallbackGateway(support), new PromotionPaySettleGateway(support));

        Map<String, String> notify = new HashMap<>();
        notify.put("return_code", "SUCCESS");
        notify.put("result_code", "SUCCESS");
        notify.put("out_trade_no", "C1");
        notify.put("transaction_id", "T1");
        notify.put("total_fee", "100");

        // 第一次回调：正常处理并触发扩展点
        service.handleWxJsapiNotify("<xml/>", new HashMap<>(notify));
        // 重放同一回调：命中幂等分支，扩展点不再触发
        service.handleWxJsapiNotify("<xml/>", new HashMap<>(notify));

        assertEquals(1, payCallbackCount.get(), "回调重放不得二次触发 PayCallbackExtension");
        assertEquals(1, promotionSettleCount.get(), "回调重放不得二次触发 PromotionPaySettleExtension");
        assertEquals(2, order.getStatus(), "订单应已迁移为支付成功");
    }
}
