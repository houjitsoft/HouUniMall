package com.houjiang.ext.goods;

import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HelloWorldTest {
    @Test void writesGreetingWithoutChangingPrice() {
        var ctx = new PriceCalcExtension.Context();
        ctx.basePrice = new BigDecimal("120.00");
        ctx.finalPrice = new BigDecimal("105.00");
        var ext = new HelloWorldExtension();
        assertTrue(ext.supports(ctx));
        assertFalse(ext.supports(null));
        ext.execute(ctx);
        assertEquals("Hello World", ctx.extra.get("hello-world.custom"));
        assertEquals(new BigDecimal("120.00"), ctx.basePrice);
        assertEquals(new BigDecimal("105.00"), ctx.finalPrice);
    }
    @Test void beanIsOptInAndHasAStableCode() {
        for (boolean enabled : new boolean[]{false, true}) {
            try (var context = new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
                context.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource(
                    "test", java.util.Map.of("houcloud.custom.goods.hello-world.enabled", Boolean.toString(enabled))));
                context.register(CustomGoodsAutoConfiguration.class);
                context.refresh();
                assertEquals(enabled ? 1 : 0, context.getBeansOfType(HelloWorldExtension.class).size());
            }
        }
        var ann = HelloWorldExtension.class.getAnnotation(com.houjiang.plugin.sdk.annotation.HouExtension.class);
        assertEquals("custom-goods", ann.code());
        assertEquals("custom", ann.owner());
    }
}
