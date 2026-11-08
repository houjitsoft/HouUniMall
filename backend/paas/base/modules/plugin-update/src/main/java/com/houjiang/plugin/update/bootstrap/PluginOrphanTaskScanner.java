package com.houjiang.plugin.update.bootstrap;

import com.houjiang.plugin.update.service.PluginUpdateService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 孤儿任务扫描（M4，方案 §5.3 关键约定 4）。
 *
 * <p><b>要解决的问题</b>：升级跑到一半进程被杀（发版 / OOM / 断电），
 * 任务会永远停在 {@code RUNNING}。而 {@code uk_active} 唯一键只对非终态生效——
 * 于是「同一组合的非终态任务只能有一条」这条约束反过来变成了死锁：
 * 这台站点的这个插件<b>再也升不了级也回不了滚</b>，前端只会一直转圈，
 * 且没有任何自动机制能解开。只能在每次启动时扫一次把它收敛掉。
 *
 * <p><b>为什么挂在 {@link ApplicationReadyEvent} 而不是 {@code @PostConstruct}</b>：
 * 扫描会真的去回滚插件（卸载 + 装载 + 健康检查），必须等数据源、引擎、
 * 事务管理器全部就绪之后再跑；早于此阶段触发会拿到半初始化的 Bean，
 * 把一次「恢复性操作」变成「二次故障」。
 *
 * <p><b>扫描失败绝不影响启动</b>：这里吞掉所有异常——因为一次失败的扫描
 * 最坏结果是「继续锁着」（与不扫描一样），而让应用起不来则是把局部问题放大成全局故障。
 *
 * @author Amos
 * @since M4
 */
@Slf4j
@Component
public class PluginOrphanTaskScanner {

    private final PluginUpdateService updateService;

    /**
     * 构造扫描器。
     *
     * @param updateService 安装 / 升级编排服务
     */
    public PluginOrphanTaskScanner(PluginUpdateService updateService) {
        this.updateService = Objects.requireNonNull(updateService, "PluginUpdateService 不能为空");
    }

    /**
     * 应用就绪后扫描一次孤儿任务。
     *
     * @param event 就绪事件
     */
    @EventListener(ApplicationReadyEvent.class)
    public void scanOnReady(ApplicationReadyEvent event) {
        try {
            int handled = updateService.scanOrphanTasks();
            if (handled > 0) {
                log.warn("启动扫描发现并收敛了 {} 条孤儿任务", handled);
            }
        } catch (RuntimeException e) {
            // 扫描是尽力而为：失败只记录，绝不让应用起不来
            log.error("孤儿任务扫描失败，已跳过（不影响启动）: {}", e.toString(), e);
        }
    }
}
