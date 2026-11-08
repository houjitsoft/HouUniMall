package com.houjiang.plugin.hello;

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
        assertEquals("Hello World", ctx.extra.get("hello-world.plugin"));
        assertEquals(new BigDecimal("120.00"), ctx.basePrice);
        assertEquals(new BigDecimal("105.00"), ctx.finalPrice);
    }
    @Test void helloEndpointReturnsGreeting() {
        var response = (java.util.Map<?, ?>) new HelloWorldController().hello();
        assertEquals("Hello World", response.get("message"));
        assertEquals("plugin", response.get("mode"));
    }
}
