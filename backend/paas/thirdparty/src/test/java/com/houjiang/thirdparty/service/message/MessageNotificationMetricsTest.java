package com.houjiang.thirdparty.service.message;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageNotificationMetricsTest {

    @Test
    void startsWithoutAnExportedMeterRegistry() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(MessageNotificationMetrics.class);
            context.refresh();

            assertNotNull(context.getBean(MessageNotificationMetrics.class));
        }
    }
}
