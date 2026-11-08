package com.houjiang.plugin.runtime;

import com.houjiang.plugin.api.extension.order.PromotionCalcExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 最终版 §3.2：活动插件化后，未安装任何活动插件不得阻断下单算价；
 * 装了 N 个活动插件时按优先级累加，Core 无需知道有哪几个活动。
 */
class PromotionCalcGatewayTest {

    private static ExtensionInvoker invoker(boolean enabled, PromotionCalcExtension... extensions) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setEnabled(enabled);
        ExtensionRegistry registry = new ExtensionRegistry();
        int priority = 0;
        for (PromotionCalcExtension ext : extensions) {
            registry.register(PromotionCalcExtension.class, ext, "activity-" + priority, priority, "official");
            priority += 10;
        }
        return new ExtensionInvoker(registry, properties);
    }

    private static PromotionCalcGateway.Result calc(ExtensionInvoker invoker, BigDecimal base) {
        return new PromotionCalcGateway(invoker)
                .calc("SECKILL", 1L, List.of(Map.of("skuId", 1, "quantity", 2)), base, Map.of());
    }

    @Test
    void 未安装任何活动插件时按原价成交且不阻断() {
        PromotionCalcGateway.Result result = calc(invoker(true), new BigDecimal("100.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(result.discountAmount()));
        assertFalse(result.aborted(), "§3.2：无活动插件不得阻断下单");
    }

    @Test
    void 引擎关闭时同样按原价成交() {
        PromotionCalcGateway.Result result = calc(invoker(false), new BigDecimal("100.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(result.discountAmount()));
        assertFalse(result.aborted());
    }

    @Test
    void 单个活动插件的优惠生效() {
        PromotionCalcExtension seckill = ctx -> ctx.discountAmount = new BigDecimal("20.00");

        PromotionCalcGateway.Result result = calc(invoker(true, seckill), new BigDecimal("100.00"));

        assertEquals(0, new BigDecimal("20.00").compareTo(result.discountAmount()));
    }

    @Test
    void 多个活动插件按优先级累加优惠() {
        PromotionCalcExtension seckill = ctx -> ctx.discountAmount =
                ctx.discountAmount.add(new BigDecimal("20.00"));
        PromotionCalcExtension coupon = ctx -> ctx.discountAmount =
                ctx.discountAmount.add(new BigDecimal("5.00"));

        PromotionCalcGateway.Result result = calc(invoker(true, seckill, coupon), new BigDecimal("100.00"));

        assertEquals(0, new BigDecimal("25.00").compareTo(result.discountAmount()),
                "秒杀与优惠券是两个独立插件，优惠应叠加");
    }

    @Test
    void 插件中断时回传原因供Core抛业务异常() {
        PromotionCalcExtension limitExceeded = ctx -> ctx.abort("该商品每人限购 1 件");

        PromotionCalcGateway.Result result = calc(invoker(true, limitExceeded), new BigDecimal("100.00"));

        assertTrue(result.aborted());
        assertEquals("该商品每人限购 1 件", result.abortReason());
    }

    @Test
    void 插件抛异常被隔离不影响算价() {
        PromotionCalcExtension broken = ctx -> {
            throw new IllegalStateException("活动插件内部错误");
        };
        PromotionCalcExtension healthy = ctx -> ctx.discountAmount = new BigDecimal("10.00");

        PromotionCalcGateway.Result result = calc(invoker(true, broken, healthy), new BigDecimal("100.00"));

        assertEquals(0, new BigDecimal("10.00").compareTo(result.discountAmount()),
                "单个活动插件异常不得打断整个算价");
        assertFalse(result.aborted());
    }

    @Test
    void 负优惠被夹为零避免用户多付() {
        PromotionCalcExtension malicious = ctx -> ctx.discountAmount = new BigDecimal("-30.00");

        PromotionCalcGateway.Result result = calc(invoker(true, malicious), new BigDecimal("100.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(result.discountAmount()));
    }

    @Test
    void 优惠超过原价被夹为原价() {
        PromotionCalcExtension excessive = ctx -> ctx.discountAmount = new BigDecimal("999.00");

        PromotionCalcGateway.Result result = calc(invoker(true, excessive), new BigDecimal("100.00"));

        assertEquals(0, new BigDecimal("100.00").compareTo(result.discountAmount()));
    }

    @Test
    void 促销明细随结果返回便于对账() {
        PromotionCalcExtension withDetail = ctx -> ctx.promotionDetail.put("activityId", "SEC-1");

        PromotionCalcGateway.Result result = calc(invoker(true, withDetail), new BigDecimal("100.00"));

        assertNotNull(result.promotionDetail());
        assertEquals("SEC-1", result.promotionDetail().get("activityId"));
    }
}
