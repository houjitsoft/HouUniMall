package com.houjiang.plugin.engine.observability;

import com.houjiang.plugin.sdk.extension.ExtensionMode;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.util.concurrent.TimeUnit;

/**
 * 扩展点可观测（R3.4 / §1.3）。
 *
 * <p>每次扩展执行输出一行结构化日志：
 * <pre>[ext] source= point= mode= costMs= result=ok|abort|error|timeout|disabled</pre>
 *
 * <p>同时可选上报 Spring Boot Micrometer（{@link MeterRegistry}）：宿主存在即上报，
 * 缺失则自动降级为纯日志，<b>零新增三方依赖</b>（Micrometer 由 Spring Boot 生态提供，
 * 本模块以 provided 软依赖声明，不向下传递）。
 *
 * @author HouCloud
 */
public class ExtensionMetrics {

    private static final Logger log = LoggerFactory.getLogger(ExtensionMetrics.class);

    /** 指标名 */
    private static final String TIMER_NAME = "houcloud.ext.invoke";

    private final ObjectProvider<MeterRegistry> meterRegistryProvider;

    /** 直连注册表（测试/嵌入方）。 */
    private final MeterRegistry directRegistry;

    /** 无 Micrometer 的构造器（纯日志）。 */
    public ExtensionMetrics() {
        this((ObjectProvider<MeterRegistry>) null);
    }

    /** 软依赖注入 Micrometer。 */
    public ExtensionMetrics(ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.meterRegistryProvider = meterRegistryProvider;
        this.directRegistry = null;
    }

    /** 直连 Micrometer（测试/嵌入方）。 */
    public ExtensionMetrics(MeterRegistry directRegistry) {
        this.meterRegistryProvider = null;
        this.directRegistry = directRegistry;
    }

    /**
     * 记录一次扩展执行。
     *
     * @param source 实现来源（插件编码）
     * @param point  扩展点短名
     * @param mode   参与方式（APPEND / REPLACE）
     * @param costMs 耗时（毫秒）
     * @param result 结果：{@code ok} / {@code abort} / {@code error} / {@code timeout} / {@code disabled}
     */
    public void record(String source, String point, ExtensionMode mode, long costMs, String result) {
        String safeSource = source == null ? "" : source;
        String safePoint = point == null ? "" : point;
        String safeResult = result == null ? "" : result;
        // 结构化日志：固定字段顺序，便于日志检索与告警
        log.info("[ext] source={} point={} mode={} costMs={} result={}",
                safeSource, safePoint, mode == null ? "" : mode, costMs, safeResult);
        reportToMicrometer(safeSource, safePoint, mode, costMs, safeResult);
    }

    private void reportToMicrometer(String source, String point, ExtensionMode mode, long costMs, String result) {
        MeterRegistry registry = directRegistry;
        if (registry == null && meterRegistryProvider != null) {
            registry = meterRegistryProvider.getIfAvailable();
        }
        if (registry == null) {
            return;
        }
        try {
            registry.timer(TIMER_NAME,
                            "point", point,
                            "source", source,
                            "mode", mode == null ? "" : mode.name(),
                            "result", result)
                    .record(costMs, TimeUnit.MILLISECONDS);
        } catch (RuntimeException ex) {
            // 指标上报绝不阻断业务
            log.debug("[ext] Micrometer 指标上报失败，已忽略：point={}, result={}", point, result, ex);
        }
    }
}
