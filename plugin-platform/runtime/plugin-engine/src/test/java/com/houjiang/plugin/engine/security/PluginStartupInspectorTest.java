package com.houjiang.plugin.engine.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 启动期字节码合规扫描。
 *
 * <p>用 {@code DangerousSample} / {@code SensitiveSample} 两个桩类提供真实字节码：
 * 桩类里的方法不会被真正执行（只加载类声明），但常量池里会留下目标方法的符号引用，
 * 这正是扫描器要看的东西。
 *
 * @author HouCloud
 */
class PluginStartupInspectorTest {

    private final PluginStartupInspector inspector = new PluginStartupInspector();

    /** 桩：引用阻断级 API（System.exit / Runtime.getRuntime）。 */
    static class DangerousSample {
        void doExit() throws Exception {
            Runtime.getRuntime();
            System.exit(1);
        }
    }

    /** 桩：只引用告警级 API（Class.forName）。 */
    static class SensitiveSample {
        void doLoad() throws Exception {
            Class.forName("com.houjiang.anything");
        }
    }

    /** 桩：不引用任何受控 API。 */
    static class CleanSample {
        int add(int left, int right) {
            return left + right;
        }
    }

    @Test
    void 引用SystemExit被判阻断() {
        List<PluginStartupInspector.Violation> violations = inspector.scan(DangerousSample.class);
        assertTrue(inspector.hasBlockingViolation(violations));
        assertTrue(violations.stream().anyMatch(v -> v.detail.contains("java/lang/System#exit")));
    }

    @Test
    void 引用Runtime被判阻断() {
        List<PluginStartupInspector.Violation> violations = inspector.scan(DangerousSample.class);
        assertTrue(violations.stream().anyMatch(v -> v.detail.contains("java/lang/Runtime#getRuntime")));
    }

    @Test
    void 引用ClassForName只告警不阻断() {
        List<PluginStartupInspector.Violation> violations = inspector.scan(SensitiveSample.class);
        assertFalse(inspector.hasBlockingViolation(violations));
        assertTrue(violations.stream().anyMatch(v ->
                v.severity == PluginStartupInspector.Severity.WARN
                        && v.detail.contains("java/lang/Class#forName")));
    }

    @Test
    void 无受控引用的类返回空列表() {
        assertTrue(inspector.scan(CleanSample.class).isEmpty());
    }

    @Test
    void 入参为null时安全返回() {
        assertTrue(inspector.scan((Class<?>) null).isEmpty());
        assertFalse(inspector.hasBlockingViolation(null));
    }

    @Test
    void 空字节码返回空列表() {
        assertTrue(inspector.scanClassBytes(new byte[0], "empty").isEmpty());
        assertTrue(inspector.scanClassBytes(null, "null").isEmpty());
    }

    @Test
    void 非class字节被判扫描失败且只告警() {
        List<PluginStartupInspector.Violation> violations =
                inspector.scanClassBytes(new byte[]{1, 2, 3, 4}, "broken.class");
        assertTrue(violations.stream().anyMatch(v -> "SCAN_FAILED".equals(v.rule)));
        assertFalse(inspector.hasBlockingViolation(violations));
    }

    @Test
    void 安装包不存在时返回扫描失败() {
        List<PluginStartupInspector.Violation> violations =
                inspector.scan(java.nio.file.Path.of("definitely-not-exists.jar"));
        assertTrue(violations.stream().anyMatch(v -> "SCAN_FAILED".equals(v.rule)));
    }

    @Test
    void 违规项保留历史两参构造的阻断语义() {
        PluginStartupInspector.Violation legacy = new PluginStartupInspector.Violation("RULE", "detail");
        assertNotNull(legacy.severity);
        org.junit.jupiter.api.Assertions.assertEquals(
                PluginStartupInspector.Severity.BLOCK, legacy.severity);
    }
}
