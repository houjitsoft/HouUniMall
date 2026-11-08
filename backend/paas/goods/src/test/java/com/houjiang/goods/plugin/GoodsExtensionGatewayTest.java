package com.houjiang.goods.plugin;

import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import com.houjiang.plugin.api.extension.goods.StockChangeExtension;
import com.houjiang.plugin.api.extension.goods.StockLockExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * goods 域三个扩展点网关的真实链路测试（R1.5）。
 *
 * <p>不依赖 Spring 容器 / 数据库 / Redis：用真实的 {@link ExtensionRegistry} +
 * {@link ExtensionInvoker} + {@link ExtensionGatewaySupport}（引擎直调路径）驱动网关，
 * 并注册与二开实现行为等价的扩展实现，断言「二开实现确实被网关触发」，涵盖
 * after* 写回、before* 中断（abort → {@link PluginAbortException}）与领域夹取。
 *
 * @author Amos
 */
class GoodsExtensionGatewayTest {

    /** 二开取价：满 200 打 9 折（镜像 CustomPriceCalcExtension 行为）。 */
    private static final PriceCalcExtension DISCOUNT_OVER_200 = ctx -> {
        if (ctx.finalPrice != null && ctx.finalPrice.compareTo(new BigDecimal("200")) >= 0) {
            ctx.finalPrice = ctx.finalPrice.multiply(new BigDecimal("0.9"));
        }
    };

    /** 二开库存预占：单笔最大预占数量（镜像 CustomStockLockExtension 行为）。 */
    private static final StockLockExtension MAX_PER_ORDER = ctx -> {
        Object max = ctx.params == null ? null : ctx.params.get("maxPerOrder");
        if (max instanceof Number limit && limit.intValue() > 0 && ctx.quantity != null
                && ctx.quantity > limit.intValue()) {
            ctx.abort("单笔最多只能购买 " + limit.intValue() + " 件");
        }
    };

    @Test
    void 取价扩展点写回最终价生效() {
        PriceCalcGateway gateway = new PriceCalcGateway(supportOf(PriceCalcExtension.class, DISCOUNT_OVER_200));

        PriceCalcGateway.Result result = gateway.calc(1L, new BigDecimal("250.00"), Map.of());

        assertEquals(0, new BigDecimal("225.00").compareTo(result.finalPrice()),
                "满 200 打 9 折：二开写回的 finalPrice 必须生效");
        assertFalse(result.aborted());
    }

    @Test
    void 取价扩展点将越界值夹回原价区间() {
        // 负价 -> 夹为 0
        PriceCalcGateway negative = new PriceCalcGateway(
                supportOf(PriceCalcExtension.class, (PriceCalcExtension) ctx -> ctx.finalPrice = new BigDecimal("-5")));
        assertEquals(0, BigDecimal.ZERO.compareTo(negative.calc(1L, new BigDecimal("100"), Map.of()).finalPrice()));

        // 高于原价 -> 夹为原价
        PriceCalcGateway excessive = new PriceCalcGateway(
                supportOf(PriceCalcExtension.class, (PriceCalcExtension) ctx -> ctx.finalPrice = new BigDecimal("9999")));
        assertEquals(0, new BigDecimal("100").compareTo(excessive.calc(1L, new BigDecimal("100"), Map.of()).finalPrice()));
    }

    @Test
    void 取价扩展点中断时抛出业务中断异常() {
        PriceCalcGateway gateway = new PriceCalcGateway(
                supportOf(PriceCalcExtension.class, (PriceCalcExtension) ctx -> ctx.abort("黑名单用户不可见价")));

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> gateway.calc(1L, new BigDecimal("100"), Map.of()));
        assertEquals("PriceCalc", ex.getPoint());
        assertEquals("黑名单用户不可见价", ex.getReason());
        assertEquals(7001, ex.getMessageCode());
    }

    @Test
    void 无二开实现时取价按原价且零副作用() {
        PriceCalcGateway gateway = new PriceCalcGateway(supportOf(PriceCalcExtension.class));

        PriceCalcGateway.Result result = gateway.calc(1L, new BigDecimal("88.00"), Map.of());

        assertEquals(0, new BigDecimal("88.00").compareTo(result.finalPrice()));
        assertFalse(result.aborted());
    }

    @Test
    void 库存预占扩展点中断时预占被拒() {
        StockLockGateway gateway = new StockLockGateway(supportOf(StockLockExtension.class, MAX_PER_ORDER));
        Map<String, Object> params = new HashMap<>();
        params.put("maxPerOrder", 2);

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> gateway.lock(9L, 5, StockLockGateway.TYPE_LOCK, 100, "ORD-1", params));
        assertEquals("StockLock", ex.getPoint());
        assertEquals("单笔最多只能购买 2 件", ex.getReason());
    }

    @Test
    void 库存预占扩展点在限额内放行() {
        StockLockGateway gateway = new StockLockGateway(supportOf(StockLockExtension.class, MAX_PER_ORDER));
        Map<String, Object> params = new HashMap<>();
        params.put("maxPerOrder", 2);

        assertDoesNotThrow(() -> gateway.lock(9L, 2, StockLockGateway.TYPE_LOCK, 100, "ORD-1", params));
    }

    @Test
    void 库存变动扩展点在扣减后被触发() {
        AtomicInteger invoked = new AtomicInteger();
        String[] capturedType = new String[1];
        Integer[] capturedDelta = new Integer[1];
        StockChangeExtension recorder = ctx -> {
            invoked.incrementAndGet();
            capturedType[0] = ctx.type;
            capturedDelta[0] = ctx.delta;
        };
        StockChangeGateway gateway = new StockChangeGateway(supportOf(StockChangeExtension.class, recorder));

        gateway.onChange(7L, -3, StockChangeGateway.TYPE_OUT, new Object());

        assertEquals(1, invoked.get(), "库存扣减后必须通知二开 StockChangeExtension");
        assertEquals("OUT", capturedType[0]);
        assertEquals(-3, capturedDelta[0]);
    }

    /** 构造一个仅注册给定二开实现的网关骨架（直调路径，确定性无并发）。 */
    @SafeVarargs
    private static <E> ExtensionGatewaySupport supportOf(Class<E> type, E... extensions) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setEnabled(true);
        // <=0 走同线程直调：测试确定性，不引入隔离线程池
        properties.setExtensionTimeoutMs(0);
        ExtensionRegistry registry = new ExtensionRegistry();
        int priority = 100;
        for (E extension : extensions) {
            registry.register(type, extension, "custom-goods-" + priority, priority, "custom");
            priority += 10;
        }
        assertNotNull(registry);
        return new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));
    }
}
