package com.houjiang.payment.plugin;

import com.houjiang.plugin.api.extension.payment.PromotionPaySettleExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link PromotionPaySettleGateway} 契约：订单号 / 实付金额 / 渠道流水号映射，
 * 无插件零副作用，abort → 业务异常。
 */
class PromotionPaySettleGatewayTest {

    private static ExtensionGatewaySupport support(PromotionPaySettleExtension... extensions) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(0);
        ExtensionRegistry registry = new ExtensionRegistry();
        int priority = 0;
        for (PromotionPaySettleExtension extension : extensions) {
            registry.register(PromotionPaySettleExtension.class, extension, "custom-" + priority, priority, "official");
            priority += 10;
        }
        return new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));
    }

    @Test
    void 无插件时零副作用且不抛异常() {
        new PromotionPaySettleGateway(support())
                .settle("ORD1", "PIN_TUAN", "WXPAY", "T1", new BigDecimal("9.90"), "2026-09-16 10:00:00", Map.of());
    }

    @Test
    void 支付事实原样透传给促销插件() {
        List<String> seen = new ArrayList<>();
        PromotionPaySettleExtension ext = ctx -> seen.add(
                ctx.orderNo + "|" + ctx.payMethod + "|" + ctx.channelTradeNo + "|" + ctx.paidAmount);
        new PromotionPaySettleGateway(support(ext))
                .settle("ORD1", "PIN_TUAN", "WXPAY", "T1", new BigDecimal("9.90"), "2026-09-16 10:00:00", Map.of());

        assertEquals(List.of("ORD1|WXPAY|T1|9.90"), seen);
    }

    @Test
    void 实付金额为空时按零处理不抛异常() {
        List<BigDecimal> seen = new ArrayList<>();
        PromotionPaySettleExtension ext = ctx -> seen.add(ctx.paidAmount);
        new PromotionPaySettleGateway(support(ext))
                .settle("ORD1", null, "WXPAY", "T1", null, null, null);

        assertEquals(List.of(BigDecimal.ZERO), seen);
    }

    @Test
    void 插件中断时抛可识别的业务异常() {
        PromotionPaySettleExtension ext = ctx -> ctx.abort("拼团已解散");

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> new PromotionPaySettleGateway(support(ext))
                        .settle("ORD1", "PIN_TUAN", "WXPAY", "T1", new BigDecimal("1.00"), null, null));

        assertEquals("PromotionPaySettle", ex.getPoint());
        assertEquals("拼团已解散", ex.getReason());
    }
}
