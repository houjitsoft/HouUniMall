package com.houjiang.plugin.update.bootstrap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.houjiang.plugin.update.service.PluginUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;

/**
 * 孤儿任务扫描器测试（M4）。
 *
 * <p>这里要证明的是两件事：扫描确实在应用就绪后跑了一次；
 * 以及<b>扫描失败绝不能把应用拖挂</b>——扫描是恢复性动作，
 * 它自己失败最坏结果是「继续锁着」（与不扫描相同），
 * 而让应用起不来则是把局部问题放大成全局故障。
 */
class PluginOrphanTaskScannerTest {

    @Test
    void 应用就绪后扫描一次() {
        PluginUpdateService service = mock(PluginUpdateService.class);
        PluginOrphanTaskScanner scanner = new PluginOrphanTaskScanner(service);

        scanner.scanOnReady(mock(ApplicationReadyEvent.class));

        verify(service).scanOrphanTasks();
    }

    @Test
    void 扫描异常被吞掉_绝不影响启动() {
        PluginUpdateService service = mock(PluginUpdateService.class);
        doThrow(new IllegalStateException("数据库连不上")).when(service).scanOrphanTasks();
        PluginOrphanTaskScanner scanner = new PluginOrphanTaskScanner(service);

        assertDoesNotThrow(() -> scanner.scanOnReady(mock(ApplicationReadyEvent.class)));
    }

    @Test
    void 服务为空时构造即失败() {
        assertThrows(NullPointerException.class, () -> new PluginOrphanTaskScanner(null));
    }
}
