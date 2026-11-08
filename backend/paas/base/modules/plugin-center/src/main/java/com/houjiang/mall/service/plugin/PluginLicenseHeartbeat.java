package com.houjiang.mall.service.plugin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.service.market.PluginLicenseService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * 授权心跳：把「过期 / 被吊销」的授权落到插件的<b>软停</b>上（M5b，方案 §7.3 + §7.4）。
 *
 * <p><b>什么是软停</b>：调 {@link PluginCenterService#toggle(String, boolean, String)}
 * 停用插件 —— 底层是 {@code PluginLifecycleManager#disable}（停容器、撤路由、撤扩展点、隐藏菜单）+ 安装表
 * {@code enabled=0}。<b>不卸载、不删 jar、不动业务表</b>，续费后原地 {@code enable} 即可恢复，
 * 租户数据全程可用。这是决策 6「软停不硬卸」的落点：硬卸载会让租户历史数据跟着不可用，
 * 而「装着一个被停用的插件」只是暂时不能用，代价低一个量级。
 *
 * <p><b>为什么这个编排放在 plugin-center</b>：
 * 过期状态迁移属于授权域（{@code plugin-license}），但「停用」要碰到插件引擎
 * （{@code plugin-engine}），而 <b>plugin-license 依赖不到 engine</b>（依赖图：
 * {@code mall → plugin-publish → plugin-market → plugin-license → plugin-registry}，
 * engine 只挂在 market / update / center 上）。
 * 与其为此新增一条反向依赖，不如在能同时看见两者的 plugin-center 里编排——
 * 授权模块只提供「查出来 + 改状态」的原子能力（{@code collectExpired / markExpired}）。
 *
 * <p><b>为什么不用 {@code @Scheduled}</b>：mall 启动类<b>没有</b> {@code @EnableScheduling}，
 * 加了注解也不会跑；而为了这一个任务打开全局开关，会把别人（如 goods 模块 10 秒一次的
 * JD 轮询）的 {@code @Scheduled} 一起激活，属于典型的「开灯把整栋楼的电闸推上去」。
 * 因此这里注入 commons 已有的 {@code taskScheduler} Bean 自行排程，作用域只在本类。
 *
 * <p><b>停用判据</b>：某插件「需要授权（PAID / TRIAL）」且<b>查不到任何有效授权</b> ——
 * 过期、吊销统统落入这条，不需要为每种原因各写一遍；反过来，只要还有一张有效授权就绝不停用。
 *
 * @author Amos
 * @since M5b
 */
@Slf4j
@Component
public class PluginLicenseHeartbeat {

    /**
     * 单次扫描结果（便于单测与排障断言）。
     */
    public static final class SweepResult {

        private int expired;

        private int renewed;

        private int disabled;

        private int restored;

        /**
         * 迁移为 EXPIRED 的授权条数。
         *
         * @return 条数
         */
        public int getExpired() {
            return expired;
        }

        /**
         * 恢复为 ACTIVATED 的授权条数（续费）。
         *
         * @return 条数
         */
        public int getRenewed() {
            return renewed;
        }

        /**
         * 被软停的插件数。
         *
         * @return 数量
         */
        public int getDisabled() {
            return disabled;
        }

        /**
         * 被原地恢复的插件数。
         *
         * @return 数量
         */
        public int getRestored() {
            return restored;
        }

        private void addExpired(int delta) {
            this.expired += delta;
        }

        private void addRenewed(int delta) {
            this.renewed += delta;
        }

        private void addDisabled(int delta) {
            this.disabled += delta;
        }

        private void addRestored(int delta) {
            this.restored += delta;
        }

        @Override
        public String toString() {
            return "SweepResult{expired=" + expired + ", renewed=" + renewed
                    + ", disabled=" + disabled + ", restored=" + restored + "}";
        }
    }

    private final PluginLicenseService licenseService;

    private final PluginCenterService pluginCenterService;

    private final MallPluginInstallMapper installMapper;

    private final MarketPluginMapper marketPluginMapper;

    private final TaskScheduler taskScheduler;

    private final boolean enabled;

    private final long initialDelayMs;

    private final long fixedDelayMs;

    private final int limit;

    private volatile ScheduledFuture<?> scheduled;

    /**
     * 构造心跳。
     *
     * @param licenseService       授权服务
     * @param pluginCenterService  插件中心服务（软停 / 恢复的执行者）
     * @param installMapper        安装态 Mapper
     * @param marketPluginMapper   市场商品 Mapper（判断是否需要授权）
     * @param scheduler            调度器，缺失时不自动排程（仍可手动调 {@link #sweep()}）
     * @param enabled              是否自动排程
     * @param initialDelayMs       首次扫描延迟（毫秒）
     * @param fixedDelayMs         扫描间隔（毫秒）
     * @param limit                单次扫描上限
     */
    @Autowired
    public PluginLicenseHeartbeat(PluginLicenseService licenseService,
                                  PluginCenterService pluginCenterService,
                                  MallPluginInstallMapper installMapper,
                                  MarketPluginMapper marketPluginMapper,
                                  ObjectProvider<TaskScheduler> scheduler,
                                  @Value("${houcloud.license.heartbeat-enabled:true}") boolean enabled,
                                  @Value("${houcloud.license.heartbeat-initial-delay-ms:60000}") long initialDelayMs,
                                  @Value("${houcloud.license.heartbeat-fixed-delay-ms:1800000}") long fixedDelayMs,
                                  @Value("${houcloud.license.heartbeat-limit:200}") int limit) {
        this.licenseService = licenseService;
        this.pluginCenterService = pluginCenterService;
        this.installMapper = installMapper;
        this.marketPluginMapper = marketPluginMapper;
        this.taskScheduler = resolveScheduler(scheduler);
        this.enabled = enabled;
        this.initialDelayMs = initialDelayMs > 0 ? initialDelayMs : 60_000L;
        this.fixedDelayMs = fixedDelayMs > 0 ? fixedDelayMs : 1_800_000L;
        this.limit = limit > 0 ? limit : 200;
    }

    /**
     * 解析调度器：宁可拿不到（降级成「不自动跑」）也不能在启动阶段抛异常。
     *
     * <p>为什么不用 {@code getIfAvailable()}：容器里若存在多个 TaskScheduler，
     * 它会抛 {@code NoUniqueBeanDefinitionException}——一个兜底任务把整个应用启动打挂，
     * 收益和风险完全不成比例。因此先试「唯一」，不唯一就取排序后的第一个，都没有则返回 null。
     *
     * @param provider 调度器提供者，可为 null
     * @return 调度器，缺失时返回 null
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

    /**
     * 启动周期扫描。
     *
     * <p>调度器缺失或开关关闭时只记日志——心跳是兜底能力，
     * 没有它系统仍然可用（下架联动与安装门控都在线），不该拖垮启动。
     */
    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("授权心跳已关闭（houcloud.license.heartbeat-enabled=false），仅保留手动 sweep 入口");
            return;
        }
        if (taskScheduler == null) {
            log.warn("未装配 TaskScheduler，授权心跳不会自动执行；可通过手动调用 sweep() 触发");
            return;
        }
        scheduled = taskScheduler.scheduleWithFixedDelay(
                this::sweepSafely,
                Instant.now().plusMillis(initialDelayMs),
                Duration.ofMillis(fixedDelayMs));
        log.info("授权心跳已启动：首次 {}ms 后执行，之后每 {}ms 一次，单次上限 {} 条",
                initialDelayMs, fixedDelayMs, limit);
    }

    /**
     * 停止周期扫描。
     */
    @PreDestroy
    public void stop() {
        ScheduledFuture<?> future = scheduled;
        if (future != null) {
            future.cancel(false);
            scheduled = null;
        }
    }

    /**
     * 排程入口：异常必须就地吞掉。
     *
     * <p>{@code scheduleWithFixedDelay} 的语义是「任务抛异常就取消后续所有执行」，
     * 一次数据库抖动就会让心跳永久停摆——这比偶发失败严重得多，因此这里必须包住。
     */
    private void sweepSafely() {
        try {
            SweepResult result = sweep(LocalDateTime.now());
            if (result.getExpired() > 0 || result.getRenewed() > 0
                    || result.getDisabled() > 0 || result.getRestored() > 0) {
                log.info("授权心跳扫描完成: {}", result);
            }
        } catch (RuntimeException e) {
            log.error("授权心跳扫描失败，等待下个周期重试: {}", e.getMessage());
        }
    }

    /**
     * 按当前时间执行一次扫描。
     *
     * @return 扫描结果
     */
    public SweepResult sweep() {
        return sweep(LocalDateTime.now());
    }

    /**
     * 执行一次扫描。
     *
     * <p>三步：① 过期迁移 → 停用；② 续费恢复；③ 全量兜底（任何原因导致的「需要授权却没有有效授权」）。
     *
     * @param now 当前时间
     * @return 扫描结果
     */
    public SweepResult sweep(LocalDateTime now) {
        SweepResult result = new SweepResult();
        if (now == null) {
            return result;
        }
        Set<String> disabledCandidates = new LinkedHashSet<>();
        Set<String> restoredCandidates = new LinkedHashSet<>();

        List<MarketPluginLicense> expired = licenseService.collectExpired(now, limit);
        for (MarketPluginLicense license : expired) {
            licenseService.markExpired(license, now);
            result.addExpired(1);
            if (StringUtils.hasText(license.getPluginCode())) {
                disabledCandidates.add(license.getPluginCode().trim());
            }
        }

        List<MarketPluginLicense> renewed = licenseService.collectRenewed(now, limit);
        for (MarketPluginLicense license : renewed) {
            licenseService.markRenewed(license, now);
            result.addRenewed(1);
            if (StringUtils.hasText(license.getPluginCode())) {
                restoredCandidates.add(license.getPluginCode().trim());
            }
        }

        for (String pluginCode : disabledCandidates) {
            if (softDisable(pluginCode, null)) {
                result.addDisabled(1);
            }
        }
        for (String pluginCode : restoredCandidates) {
            if (softEnable(pluginCode, instanceIdOf(pluginCode))) {
                result.addRestored(1);
            }
        }

        // 兜底：扫所有「已启用」的付费插件，凡是查不到有效授权的（吊销 / 过期 / 数据异常）一律软停。
        // 下架联动只吊销授权、不碰运行态，正是靠这一步把「已装实例降级停用」补上（方案 §7.4）。
        for (String pluginCode : enabledLicensedPluginCodes()) {
            if (disabledCandidates.contains(pluginCode)) {
                continue;
            }
            if (softDisable(pluginCode, null)) {
                result.addDisabled(1);
            }
        }
        return result;
    }

    /**
     * 软停：走插件中心的正式停用入口（引擎撤路由 + 安装表 enabled=0），不卸载、不删数据。
     *
     * @param pluginCode 插件编码
     * @param instanceId 站点实例，可为空
     * @return true 表示本次确实停用了
     */
    private boolean softDisable(String pluginCode, String instanceId) {
        if (!shouldTouch(pluginCode, true)) {
            return false;
        }
        try {
            pluginCenterService.toggle(pluginCode, false, instanceId);
            log.warn("授权失效，插件已软停（保留数据与安装包）: plugin={}", pluginCode);
            return true;
        } catch (RuntimeException e) {
            log.error("插件软停失败，下个周期重试: plugin={} msg={}", pluginCode, e.getMessage());
            return false;
        }
    }

    /**
     * 原地恢复：续费后重新启用（只恢复「我们因过期停用」的那些，见 {@code collectRenewed} 的说明）。
     *
     * @param pluginCode 插件编码
     * @param instanceId 站点实例，可为空
     * @return true 表示本次确实恢复了
     */
    private boolean softEnable(String pluginCode, String instanceId) {
        if (!shouldTouch(pluginCode, false)) {
            return false;
        }
        try {
            // toggle(true) 内部会先校验授权：没有有效授权就不会启用，天然满足「续费才恢复」
            pluginCenterService.toggle(pluginCode, true, instanceId);
            log.info("授权恢复，插件已原地启用: plugin={}", pluginCode);
            return true;
        } catch (RuntimeException e) {
            log.error("插件原地恢复失败，下个周期重试: plugin={} msg={}", pluginCode, e.getMessage());
            return false;
        }
    }

    /**
     * 是否需要动手：停用要求「当前已启用且确实没有有效授权」，恢复要求「当前是停用的」。
     *
     * @param pluginCode 插件编码
     * @param forDisable true 表示停用方向，false 表示恢复方向
     * @return true 表示需要动作
     */
    private boolean shouldTouch(String pluginCode, boolean forDisable) {
        if (!StringUtils.hasText(pluginCode)) {
            return false;
        }
        MallPluginInstall install = findInstall(pluginCode.trim());
        if (install == null || !Integer.valueOf(1).equals(install.getInstalled())) {
            return false;
        }
        boolean enabled = Integer.valueOf(1).equals(install.getEnabled());
        if (forDisable) {
            if (!enabled) {
                return false;
            }
            return requiresLicense(pluginCode.trim())
                    && licenseService.findValid(pluginCode.trim()) == null;
        }
        return !enabled && requiresLicense(pluginCode.trim());
    }

    /**
     * 当前处于「已安装且已启用」、且需要授权的插件编码（兜底扫描用）。
     *
     * @return 插件编码列表
     */
    private List<String> enabledLicensedPluginCodes() {
        List<MallPluginInstall> installs = installMapper.selectList(
                new LambdaQueryWrapper<MallPluginInstall>()
                        .eq(MallPluginInstall::getInstalled, 1)
                        .eq(MallPluginInstall::getEnabled, 1));
        if (installs == null || installs.isEmpty()) {
            return List.of();
        }
        List<String> codes = new ArrayList<>();
        for (MallPluginInstall install : installs) {
            String code = install.getPluginCode();
            if (!StringUtils.hasText(code)) {
                continue;
            }
            if (requiresLicense(code.trim()) && licenseService.findValid(code.trim()) == null) {
                codes.add(code.trim());
            }
        }
        return codes;
    }

    private String instanceIdOf(String pluginCode) {
        List<MarketPluginLicense> licenses = licenseService.listByPlugin(pluginCode);
        if (licenses == null) {
            return null;
        }
        for (MarketPluginLicense license : licenses) {
            if (StringUtils.hasText(license.getInstanceId())) {
                return license.getInstanceId().trim();
            }
        }
        return null;
    }

    private boolean requiresLicense(String pluginCode) {
        MarketPlugin plugin = marketPluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, pluginCode)
                .last("limit 1"));
        return plugin != null && PluginLicenseService.requiresLicense(plugin.getLicenseType());
    }

    private MallPluginInstall findInstall(String pluginCode) {
        return installMapper.selectOne(new LambdaQueryWrapper<MallPluginInstall>()
                .eq(MallPluginInstall::getPluginCode, pluginCode)
                .last("limit 1"));
    }
}
