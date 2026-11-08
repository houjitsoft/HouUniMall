package com.houjiang.payment.plugin;

import com.houjiang.plugin.api.extension.payment.RefundCreateExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link RefundCreateGateway} 契约：{@code before*} 可中断（abort → 业务异常，退款不创建），
 * 金额领域夹取 {@code [0, paidAmount]}、精度 2 位。
 */
class RefundCreateGatewayTest {

    private static final BigDecimal PAID = new BigDecimal("100.00");

    private static ExtensionGatewaySupport support(RefundCreateExtension... extensions) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(0);
        ExtensionRegistry registry = new ExtensionRegistry();
        int priority = 0;
        for (RefundCreateExtension extension : extensions) {
            registry.register(RefundCreateExtension.class, extension, "custom-" + priority, priority, "official");
            priority += 10;
        }
        return new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));
    }

    @Test
    void 无插件时金额保持不变() {
        RefundCreateGateway.Decision decision = new RefundCreateGateway(support())
                .beforeRefund("ORD1", PAID, new BigDecimal("30.00"), "ORIGINAL", "用户申请", null);

        assertEquals(0, new BigDecimal("30.00").compareTo(decision.refundAmount()));
    }

    @Test
    void 插件abort时抛业务异常() {
        RefundCreateExtension ext = ctx -> ctx.abort("风控拦截");

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> new RefundCreateGateway(support(ext))
                        .beforeRefund("ORD1", PAID, new BigDecimal("30.00"), "ORIGINAL", "用户申请", null));

        assertEquals("RefundCreate", ex.getPoint());
        assertEquals("风控拦截", ex.getReason());
        assertEquals(7001, ex.getMessageCode());
    }

    @Test
    void 插件下修退款金额被采纳() {
        RefundCreateExtension ext = ctx -> ctx.refundAmount = new BigDecimal("12.34");

        RefundCreateGateway.Decision decision = new RefundCreateGateway(support(ext))
                .beforeRefund("ORD1", PAID, new BigDecimal("30.00"), "ORIGINAL", "用户申请", null);

        assertEquals(0, new BigDecimal("12.34").compareTo(decision.refundAmount()));
    }

    @Test
    void 插件写入超过实付的金额被夹为实付() {
        RefundCreateExtension ext = ctx -> ctx.refundAmount = new BigDecimal("999.99");

        RefundCreateGateway.Decision decision = new RefundCreateGateway(support(ext))
                .beforeRefund("ORD1", PAID, new BigDecimal("30.00"), "ORIGINAL", "用户申请", null);

        assertEquals(0, PAID.compareTo(decision.refundAmount()));
    }

    @Test
    void 夹取边界_负数归零_空值归零_精度两位() {
        assertEquals(0, BigDecimal.ZERO.compareTo(RefundCreateGateway.clampRefund(new BigDecimal("-1"), PAID)));
        assertEquals(0, BigDecimal.ZERO.compareTo(RefundCreateGateway.clampRefund(null, PAID)));
        assertEquals(0, new BigDecimal("100.00").compareTo(RefundCreateGateway.clampRefund(new BigDecimal("100000"), PAID)));
        // 1.005 → 1.01（HALF_UP），保证精度不超过 2 位
        assertEquals(0, new BigDecimal("1.01").compareTo(RefundCreateGateway.clampRefund(new BigDecimal("1.005"), PAID)));
    }
}
