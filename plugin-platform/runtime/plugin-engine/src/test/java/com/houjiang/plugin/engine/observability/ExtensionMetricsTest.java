package com.houjiang.plugin.engine.observability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.houjiang.plugin.sdk.extension.ExtensionMode;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 扩展点可观测单测（T01 / R3.4）。
 *
 * <p>验证结构化日志字段（source/point/mode/costMs/result）可被捕获，且 Micrometer 指标可上报。
 *
 * @author HouCloud
 */
class ExtensionMetricsTest {

    @Test
    void emitsStructuredLogWithResultAndCost() {
        ExtensionMetrics metrics = new ExtensionMetrics();
        Logger logger = (Logger) LoggerFactory.getLogger(ExtensionMetrics.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            metrics.record("custom-goods", "PriceCalc", ExtensionMode.APPEND, 42, "timeout");
        } finally {
            logger.detachAppender(appender);
        }

        List<ILoggingEvent> events = appender.list;
        assertTrue(events.size() >= 1, "应输出至少一行日志");
        String message = events.get(events.size() - 1).getFormattedMessage();
        assertTrue(message.contains("[ext]"), "日志应带 [ext] 前缀，实际=" + message);
        assertTrue(message.contains("source=custom-goods"), message);
        assertTrue(message.contains("point=PriceCalc"), message);
        assertTrue(message.contains("mode=APPEND"), message);
        assertTrue(message.contains("costMs=42"), message);
        assertTrue(message.contains("result=timeout"), message);
    }

    @Test
    void recordsMicrometerTimerPerResult() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ExtensionMetrics metrics = new ExtensionMetrics(registry);

        metrics.record("custom-goods", "PriceCalc", ExtensionMode.APPEND, 12, "ok");
        metrics.record("custom-goods", "PriceCalc", ExtensionMode.APPEND, 34, "ok");
        metrics.record("custom-goods", "PriceCalc", ExtensionMode.APPEND, 56, "error");

        Timer ok = registry.find("houcloud.ext.invoke")
                .tag("result", "ok").tag("point", "PriceCalc").tag("source", "custom-goods").timer();
        Timer error = registry.find("houcloud.ext.invoke")
                .tag("result", "error").tag("point", "PriceCalc").timer();
        assertNotNull(ok, "应为 ok 结果生成 timer");
        assertNotNull(error, "应为 error 结果生成 timer");
        assertTrue(ok.count() == 2, "ok 计时器应累计 2 次");
        assertTrue(error.count() == 1);
    }

    @Test
    void everyResultValueIsRepresentable() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ExtensionMetrics metrics = new ExtensionMetrics(registry);

        for (String result : List.of("ok", "abort", "error", "timeout", "disabled")) {
            metrics.record("src", "Point", ExtensionMode.APPEND, 1, result);
            assertNotNull(registry.find("houcloud.ext.invoke").tag("result", result).timer(),
                    "result=" + result + " 应可被记录");
        }
    }
}
