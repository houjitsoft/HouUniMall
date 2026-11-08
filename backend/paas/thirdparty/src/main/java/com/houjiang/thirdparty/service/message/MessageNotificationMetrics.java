package com.houjiang.thirdparty.service.message;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Amos
 */
@Component
public class MessageNotificationMetrics {

    private final MeterRegistry registry;

    @Autowired
    public MessageNotificationMetrics(ObjectProvider<MeterRegistry> registryProvider) {
        this(registryProvider.getIfAvailable(SimpleMeterRegistry::new));
    }

    public MessageNotificationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordRetry(String result) {
        registry.counter("message.record.retry", "result", result).increment();
    }

    public void recordIdempotentReplay() {
        registry.counter("message.send.idempotent.replay").increment();
    }
}
