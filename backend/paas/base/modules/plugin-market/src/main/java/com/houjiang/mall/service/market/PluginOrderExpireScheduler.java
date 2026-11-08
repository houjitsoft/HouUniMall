package com.houjiang.mall.service.market;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

/**
 * 插件订单超时关闭的周期扫描（{@code PluginOrderService#closeExpired} 的驱动方）。
 *
 * <p><b>为什么必须有它</b>：{@code closeExpired()} 此前<b>没有任何调用点</b>——
 * 超时未支付的订单会永远停在「待支付」，前端列表里堆满废单，
 * 且这些单占着「同一插件同一站点」的待支付位，用户想重新买会被
 * {@link PluginOrderService#pay} 的过期检查挡住，只能干等。补上扫描后，
 * 超时订单在下一个周期自动落到终态「已关闭」，状态机才真正闭合。
 *
 * <p><b>为什么不用 {@code @Scheduled}</b>：与授权心跳同理——mall 启动类没有
 * {@code @EnableScheduling}，加了注解也不会跑；为了这一个任务打开全局开关，
 * 会把别处（如 goods 模块的第三方轮询）的 {@code @Scheduled} 一起激活。
 * 这里注入 commons 已有的 {@code taskScheduler} Bean 自行排程，作用域只在本类。
 *
 * <p><b>失败策略</b>：单次扫描异常就地吞掉并记 error——关单是清理动作，
 * 一轮失败不影响下一轮，绝不能让异常终止整个周期任务。
 *
 * @author Amos
 */
@Slf4j
@Component
public class PluginOrderExpireScheduler {

    private final PluginOrderService orderService;
    private final TaskScheduler taskScheduler;
    private final boolean enabled;
    private final long initialDelayMs;
    private final long fixedDelayMs;

    private volatile ScheduledFuture<?> scheduled;

    public PluginOrderExpireScheduler(PluginOrderService orderService,
                                      ObjectProvider<TaskScheduler> scheduler,
                                      @Value("${houcloud.plugin.market.pay.expire-sweep-enabled:true}")
                                      boolean enabled,
                                      @Value("${houcloud.plugin.market.pay.expire-sweep-initial-delay-ms:60000}")
                                      long initialDelayMs,
                                      @Value("${houcloud.plugin.market.pay.expire-sweep-fixed-delay-ms:300000}")
                                      long fixedDelayMs) {
        this.orderService = orderService;
        this.taskScheduler = resolveScheduler(scheduler);
        this.enabled = enabled;
        this.initialDelayMs = initialDelayMs > 0 ? initialDelayMs : 60_000L;
        this.fixedDelayMs = fixedDelayMs > 0 ? fixedDelayMs : 300_000L;
    }

    /**
     * 解析调度器：拿不到就降级成「不自动跑」，不能在启动阶段抛异常。
     *
     * <p>不用 {@code getIfAvailable()}：容器里存在多个 TaskScheduler 时它会抛
     * {@code NoUniqueBeanDefinitionException}，一个兜底任务把整个应用启动打挂不划算。
     */
    private static TaskScheduler resolveScheduler(ObjectProvider<TaskScheduler> provider) {
        if (provider == null) {
            return null;
        }
        TaskScheduler unique = provider.getIfUnique();
        if (unique != null) {
            return unique;
        }
        return provider.orderedStream().findFirst().orElse(null);
    }

    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("插件订单超时扫描已关闭（houcloud.plugin.market.pay.expire-sweep-enabled=false），"
                    + "仅保留手动 sweep 入口");
            return;
        }
        if (taskScheduler == null) {
            log.warn("未装配 TaskScheduler，超时插件订单不会自动关闭；可手动调用 sweep() 触发");
            return;
        }
        scheduled = taskScheduler.scheduleWithFixedDelay(
                this::sweepSafely,
                Instant.now().plusMillis(initialDelayMs),
                Duration.ofMillis(fixedDelayMs));
        log.info("插件订单超时扫描已启动：首次 {}ms 后执行，之后每 {}ms 一次", initialDelayMs, fixedDelayMs);
    }

    @PreDestroy
    public void stop() {
        ScheduledFuture<?> future = scheduled;
        if (future != null) {
            future.cancel(false);
            scheduled = null;
        }
    }

    /**
     * 排程入口：异常就地吞掉，保证周期任务不会因为一轮失败而整个停掉。
     */
    private void sweepSafely() {
        try {
            int closed = sweep();
            if (closed > 0) {
                log.info("插件订单超时扫描完成：本次关闭 {} 笔", closed);
            }
        } catch (Exception e) {
            log.error("插件订单超时扫描失败（下一周期自动重试）", e);
        }
    }

    /**
     * 扫描并关闭超时未支付订单。
     *
     * @return 本次关闭的订单数量
     */
    public int sweep() {
        return orderService.closeExpired();
    }
}
