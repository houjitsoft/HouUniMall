package com.houjiang.plugin.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.houjiang.plugin.engine.config.ExtensionDegradePolicy;
import org.junit.jupiter.api.Test;

/**
 * 升级排空窗口测试（M4）。
 *
 * <p>核心要证明两件事：窗口内请求被拦（配合 {@link PluginDispatcherTest} 的 503 断言），
 * 以及<b>窗口一定会结束</b>——编排无论成功失败都必须解锁，
 * 而最坏情况（进程被杀）下窗口也要自然到期，不能把插件永久锁在排空态。
 */
class PluginDrainGateTest {

    @Test
    void begin后处于排空态_end后立即放行() {
        PluginDrainGate gate = new PluginDrainGate();

        gate.begin("demo", 5000);

        assertTrue(gate.isDraining("demo"));
        assertTrue(gate.drainingPlugins().contains("demo"));

        gate.end("demo");

        assertFalse(gate.isDraining("demo"));
        assertTrue(gate.drainingPlugins().isEmpty());
    }

    @Test
    void 窗口到期后自动放行_编排被中断也不会永久锁死() throws Exception {
        PluginDrainGate gate = new PluginDrainGate();

        gate.begin("demo", 1);
        Thread.sleep(20);

        assertFalse(gate.isDraining("demo"), "窗口必须自然到期，否则编排被杀后插件会永远 503");
        assertTrue(gate.drainingPlugins().isEmpty(), "到期后登记项应被惰性清理");
    }

    @Test
    void 重复begin按更晚的窗口生效() {
        PluginDrainGate gate = new PluginDrainGate();

        gate.begin("demo", 50);
        gate.begin("demo", 5000);
        gate.end("demo");

        assertFalse(gate.isDraining("demo"), "一次 end 必须彻底结束排空，不能被更早的登记续上");
    }

    @Test
    void 不同插件互不影响() {
        PluginDrainGate gate = new PluginDrainGate();

        gate.begin("demo", 5000);

        assertFalse(gate.isDraining("other"), "排空必须按插件隔离");
    }

    @Test
    void 排空期策略为failClosed且携带RetryAfter() {
        PluginDrainGate gate = new PluginDrainGate(10);

        assertEquals(10, gate.retryAfterSeconds());
        assertEquals(ExtensionDegradePolicy.ABORT, gate.degradePolicy(),
                "排空期必须 fail-closed：既不静默穿透，也不回落官方实现");
    }
}
