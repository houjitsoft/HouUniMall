package com.houjiang.order.plugin;

import com.houjiang.plugin.api.extension.order.AfterSaleCreateExtension;
import com.houjiang.plugin.api.extension.order.CartCalcExtension;
import com.houjiang.plugin.api.extension.order.CartValidateExtension;
import com.houjiang.plugin.api.extension.order.FreightCalcExtension;
import com.houjiang.plugin.api.extension.order.OrderCreateAfterExtension;
import com.houjiang.plugin.api.extension.order.OrderCreateBeforeExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * order 域六个扩展点网关的真实链路测试（R1.5）。
 *
 * <p>不依赖 Spring 容器 / 数据库 / Redis：用真实的 {@link ExtensionRegistry} +
 * {@link ExtensionInvoker} + {@link ExtensionGatewaySupport}（引擎直调路径）驱动网关，
 * 注册与二开实现行为等价的扩展实现，断言「二开实现确实被网关触发」，涵盖
 * after* 写回、before* 中断（abort → {@link PluginAbortException}）与领域夹取。
 *
 * @author Amos
 */
class OrderExtensionGatewayTest {

    /** 二开购物车满减：满 300 减 30（镜像 CustomCartCalcExtension）。 */
    private static final CartCalcExtension CART_FULL_REDUCE = ctx -> {
        if (ctx.goodsAmount == null) {
            return;
        }
        BigDecimal discount = ctx.discountAmount == null ? BigDecimal.ZERO : ctx.discountAmount;
        if (ctx.goodsAmount.compareTo(new BigDecimal("300")) >= 0) {
            discount = discount.add(new BigDecimal("30"));
        }
        if (discount.signum() < 0) {
            discount = BigDecimal.ZERO;
        }
        if (discount.compareTo(ctx.goodsAmount) > 0) {
            discount = ctx.goodsAmount;
        }
        ctx.discountAmount = discount;
        ctx.payAmount = ctx.goodsAmount.subtract(discount);
    };

    /** 二开购物车校验：限制商品种类数（镜像 CustomCartValidateExtension）。 */
    private static final CartValidateExtension CART_MAX_KINDS = ctx -> {
        Object max = ctx.params == null ? null : ctx.params.get("maxItemKinds");
        if (max instanceof Number limit && limit.intValue() > 0
                && ctx.items != null && ctx.items.size() > limit.intValue()) {
            ctx.abort("购物车最多只能放 " + limit.intValue() + " 种商品");
        }
    };

    /** 二开运费：偏远地区加收 20（镜像 CustomFreightCalcExtension）。 */
    private static final FreightCalcExtension REMOTE_SURCHARGE = ctx -> {
        BigDecimal base = ctx.baseFreight == null ? BigDecimal.ZERO : ctx.baseFreight;
        if (ctx.regionCode != null && List.of("XZ", "QH", "XJ", "NM").contains(ctx.regionCode.trim().toUpperCase())) {
            ctx.freightFee = base.add(new BigDecimal("20.00"));
        } else {
            ctx.freightFee = base;
        }
    };

    /** 二开下单前拦截：params.blockedSku=true 时拒绝（镜像 CustomOrderCreateBeforeExtension）。 */
    private static final OrderCreateBeforeExtension BLOCKED_SKU = ctx -> {
        Object blocked = ctx.params == null ? null : ctx.params.get("blockedSku");
        if (Boolean.TRUE.equals(blocked) || "true".equalsIgnoreCase(String.valueOf(blocked))) {
            ctx.abort("该商品已被二开规则限制下单");
        }
    };

    /** 二开下单后：补写外部系统单号（镜像 CustomOrderCreateAfterExtension）。 */
    private static final OrderCreateAfterExtension SYNC_EXTERNAL = ctx -> {
        if (ctx.params == null || Boolean.TRUE.equals(ctx.params.get("extSynced"))) {
            return;
        }
        ctx.params.put("extSynced", Boolean.TRUE);
        ctx.params.put("extExternalNo", "EXT-FIXED");
    };

    /** 二开售后：超时则拒绝，并把核定金额夹在 [0, 实付]（镜像 CustomAfterSaleCreateExtension）。 */
    private static final AfterSaleCreateExtension AFTER_SALE_RULE = ctx -> {
        if (ctx.params != null && Boolean.TRUE.equals(ctx.params.get("afterSaleExpired"))) {
            ctx.abort("已超过售后申请时限，请联系客服处理");
            return;
        }
        if (ctx.paidAmount != null && ctx.approveAmount != null) {
            if (ctx.approveAmount.signum() < 0) {
                ctx.approveAmount = BigDecimal.ZERO;
            }
            if (ctx.approveAmount.compareTo(ctx.paidAmount) > 0) {
                ctx.approveAmount = ctx.paidAmount;
            }
        }
    };

    // ---------- CartCalc ----------

    @Test
    void 购物车算价扩展点写回折扣与应付() {
        CartCalcGateway gateway = new CartCalcGateway(supportOf(CartCalcExtension.class, CART_FULL_REDUCE));

        CartCalcGateway.Result result = gateway.calc(null, new BigDecimal("350.00"), Map.of());

        assertEquals(0, new BigDecimal("30.00").compareTo(result.discountAmount()));
        assertEquals(0, new BigDecimal("320.00").compareTo(result.payAmount()));
    }

    @Test
    void 购物车算价扩展点越界折扣被夹取() {
        CartCalcGateway negative = new CartCalcGateway(
                supportOf(CartCalcExtension.class, (CartCalcExtension) ctx -> ctx.discountAmount = new BigDecimal("-5")));
        CartCalcGateway.Result negResult = negative.calc(null, new BigDecimal("100"), Map.of());
        assertEquals(0, BigDecimal.ZERO.compareTo(negResult.discountAmount()));
        assertEquals(0, new BigDecimal("100").compareTo(negResult.payAmount()));

        CartCalcGateway excessive = new CartCalcGateway(
                supportOf(CartCalcExtension.class, (CartCalcExtension) ctx -> ctx.discountAmount = new BigDecimal("9999")));
        CartCalcGateway.Result exResult = excessive.calc(null, new BigDecimal("100"), Map.of());
        assertEquals(0, new BigDecimal("100").compareTo(exResult.discountAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(exResult.payAmount()));
    }

    // ---------- CartValidate ----------

    @Test
    void 购物车校验扩展点超限时拦截() {
        CartValidateGateway gateway = new CartValidateGateway(supportOf(CartValidateExtension.class, CART_MAX_KINDS));
        Map<String, Object> params = new HashMap<>();
        params.put("maxItemKinds", 3);
        List<Object> items = new ArrayList<>(List.of(1, 2, 3, 4));

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> gateway.validate(null, items, CartValidateGateway.ACTION_CHECKOUT, params));
        assertEquals("CartValidate", ex.getPoint());
        assertEquals("购物车最多只能放 3 种商品", ex.getReason());
    }

    @Test
    void 购物车校验扩展点在限额内放行() {
        CartValidateGateway gateway = new CartValidateGateway(supportOf(CartValidateExtension.class, CART_MAX_KINDS));
        Map<String, Object> params = new HashMap<>();
        params.put("maxItemKinds", 3);
        List<Object> items = new ArrayList<>(List.of(1, 2));

        assertDoesNotThrow(() -> gateway.validate(null, items, CartValidateGateway.ACTION_CHECKOUT, params));
    }

    // ---------- FreightCalc ----------

    @Test
    void 运费扩展点写回偏远地区运费生效() {
        FreightCalcGateway gateway = new FreightCalcGateway(supportOf(FreightCalcExtension.class, REMOTE_SURCHARGE));

        assertEquals(0, new BigDecimal("30.00")
                .compareTo(gateway.calc(null, null, "XZ", new BigDecimal("10.00")).freightFee()),
                "偏远地区应在基础运费上加收 20");

        assertEquals(0, new BigDecimal("10.00")
                        .compareTo(gateway.calc(null, null, "BJ", new BigDecimal("10.00")).freightFee()),
                "非偏远地区维持基础运费");
    }

    @Test
    void 运费扩展点负运费被夹为零() {
        FreightCalcGateway gateway = new FreightCalcGateway(
                supportOf(FreightCalcExtension.class, (FreightCalcExtension) ctx -> ctx.freightFee = new BigDecimal("-1")));

        assertEquals(0, BigDecimal.ZERO.compareTo(gateway.calc(null, null, "BJ", new BigDecimal("10.00")).freightFee()));
    }

    // ---------- OrderCreateBefore ----------

    @Test
    void 下单前扩展点拦截时下单被拒() {
        OrderCreateBeforeGateway gateway =
                new OrderCreateBeforeGateway(supportOf(OrderCreateBeforeExtension.class, BLOCKED_SKU));
        Map<String, Object> params = new HashMap<>();
        params.put("blockedSku", true);

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> gateway.validateBefore(new Object(), params));
        assertEquals("OrderCreateBefore", ex.getPoint());
        assertEquals("该商品已被二开规则限制下单", ex.getReason());
    }

    @Test
    void 下单前扩展点未命中时放行() {
        OrderCreateBeforeGateway gateway =
                new OrderCreateBeforeGateway(supportOf(OrderCreateBeforeExtension.class, BLOCKED_SKU));
        Map<String, Object> params = new HashMap<>();
        params.put("blockedSku", false);

        assertDoesNotThrow(() -> gateway.validateBefore(new Object(), params));
    }

    // ---------- OrderCreateAfter ----------

    @Test
    void 下单后扩展点被触发并回写结果字段() {
        OrderCreateAfterGateway gateway =
                new OrderCreateAfterGateway(supportOf(OrderCreateAfterExtension.class, SYNC_EXTERNAL));
        Map<String, Object> respData = new HashMap<>();

        gateway.afterCreate(new Object(), "result", respData);

        assertEquals(Boolean.TRUE, respData.get("extSynced"), "下单后扩展点应被触发并回写同步标记");
        assertEquals("EXT-FIXED", respData.get("extExternalNo"));
    }

    // ---------- AfterSaleCreate ----------

    @Test
    void 售后扩展点核定金额超实付被夹取() {
        AfterSaleCreateGateway gateway =
                new AfterSaleCreateGateway(supportOf(AfterSaleCreateExtension.class, AFTER_SALE_RULE));

        AfterSaleCreateGateway.Result result = gateway.beforeCreate(new Object(), null,
                AfterSaleCreateGateway.TYPE_REFUND, 1, new BigDecimal("80.00"), new BigDecimal("120.00"),
                "不想要了", Map.of());

        assertEquals(0, new BigDecimal("80.00").compareTo(result.approveAmount()));
    }

    @Test
    void 售后扩展点超时申请时拦截() {
        AfterSaleCreateGateway gateway =
                new AfterSaleCreateGateway(supportOf(AfterSaleCreateExtension.class, AFTER_SALE_RULE));
        Map<String, Object> params = new HashMap<>();
        params.put("afterSaleExpired", true);

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> gateway.beforeCreate(new Object(), null, AfterSaleCreateGateway.TYPE_REFUND, 1,
                        new BigDecimal("80.00"), new BigDecimal("50.00"), "不想要了", params));
        assertEquals("AfterSaleCreate", ex.getPoint());
        assertEquals("已超过售后申请时限，请联系客服处理", ex.getReason());
    }

    @Test
    void 无二开实现时运费按基础运费且不阻断() {
        FreightCalcGateway gateway = new FreightCalcGateway(supportOf(FreightCalcExtension.class));

        FreightCalcGateway.Result result = gateway.calc(null, null, "BJ", new BigDecimal("12.00"));

        assertEquals(0, new BigDecimal("12.00").compareTo(result.freightFee()));
        assertFalse(result.aborted());
        assertTrue(result.abortReason() == null);
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
            registry.register(type, extension, "custom-order-" + priority, priority, "custom");
            priority += 10;
        }
        return new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));
    }
}
