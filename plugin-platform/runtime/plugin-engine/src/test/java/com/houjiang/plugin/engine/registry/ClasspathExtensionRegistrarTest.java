package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import com.houjiang.plugin.sdk.annotation.HouExtension;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClasspathExtensionRegistrarTest {

    @Test
    void registersAnnotatedClasspathBeanIntoTheSharedSnapshot() {
        ExtensionRegistry registry = new ExtensionRegistry();
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ExtensionRegistry.class, () -> registry);
            context.register(ClasspathConfig.class);
            context.registerBean(ClasspathExtensionRegistrar.class,
                    () -> new ClasspathExtensionRegistrar(
                            context.getDefaultListableBeanFactory(), registry, true));
            context.refresh();

            var extensions = registry.getExtensions(PriceCalcExtension.class);
            assertEquals(1, extensions.size());
            assertSame(context.getBean(CustomClasspathPriceExtension.class), extensions.get(0));

            PriceCalcExtension.Context calculation = new PriceCalcExtension.Context();
            calculation.basePrice = new BigDecimal("100");
            calculation.finalPrice = calculation.basePrice;
            extensions.get(0).execute(calculation);
            assertEquals(0, calculation.finalPrice.compareTo(new BigDecimal("80")));
        }
    }

    @Test
    void rejectsCustomClasspathBeanWithoutStableCode() {
        ExtensionRegistry registry = new ExtensionRegistry();
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ExtensionRegistry.class, () -> registry);
            context.register(MissingCodeConfig.class);
            context.registerBean(ClasspathExtensionRegistrar.class,
                    () -> new ClasspathExtensionRegistrar(
                            context.getDefaultListableBeanFactory(), registry, true));

            assertThrows(IllegalStateException.class, context::refresh);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ClasspathConfig {
        @Bean
        CustomClasspathPriceExtension customClasspathPriceExtension() {
            return new CustomClasspathPriceExtension();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MissingCodeConfig {
        @Bean
        MissingCodeExtension missingCodeExtension() {
            return new MissingCodeExtension();
        }
    }

    @HouExtension(point = PriceCalcExtension.class, code = "custom-test", priority = 100, owner = "custom")
    static class CustomClasspathPriceExtension implements PriceCalcExtension {
        @Override
        public void execute(Context ctx) {
            if (ctx != null && ctx.finalPrice != null) {
                ctx.finalPrice = ctx.finalPrice.multiply(new BigDecimal("0.8"));
            }
        }
    }

    @HouExtension(point = PriceCalcExtension.class, owner = "custom")
    static class MissingCodeExtension implements PriceCalcExtension {
        @Override
        public void execute(Context ctx) {
        }
    }
}
