package com.houjiang.mall.service.plugin;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.service.market.PluginLicenseService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 授权心跳单测（M5b）。
 *
 * <p>关注四件事：① 过期 → 软停；② 续费 → 原地恢复；③ 还有有效授权 / 免费插件 → 绝不动手；
 * ④ 软停失败只记日志不往上抛（一次失败不该让整个心跳周期崩掉）。
 *
 * @author Amos
 * @since M5b
 */
@SuppressWarnings("unchecked")
class PluginLicenseHeartbeatTest {

    private static final String CODE = "erp";

    /**
     * 过期授权 → 插件被软停（toggle(false)），且不卸载。
     */
    @Test
    void sweepDisablesPluginWhenLicenseExpired() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        PluginCenterService centerService = mock(PluginCenterService.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketPluginMapper = mock(MarketPluginMapper.class);

        MarketPluginLicense expired = license("L-1", CODE, null);
        when(licenseService.collectExpired(any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(expired));
        when(licenseService.collectRenewed(any(LocalDateTime.class), eq(100))).thenReturn(List.of());
        when(licenseService.findValid(CODE)).thenReturn(null);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install(CODE, 1, 1));
        when(marketPluginMapper.selectOne(any(Wrapper.class)))
                .thenReturn(marketPlugin(CODE, PluginLicenseService.TYPE_PAID));

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService, centerService,
                installMapper, marketPluginMapper, schedulerProvider(null), true, 1000L, 1000L, 100);

        PluginLicenseHeartbeat.SweepResult result = heartbeat.sweep(LocalDateTime.now());

        assertEquals(1, result.getExpired());
        assertEquals(1, result.getDisabled());
        assertEquals(0, result.getRestored());
        verify(centerService).toggle(eq(CODE), eq(false), isNull());
        verify(licenseService).markExpired(eq(expired), any(LocalDateTime.class));
    }

    /**
     * 同一个插件还有别的有效授权 → 不应停用（多张授权码的典型场景）。
     */
    @Test
    void sweepKeepsPluginWhenAnotherValidLicenseRemains() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        PluginCenterService centerService = mock(PluginCenterService.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketPluginMapper = mock(MarketPluginMapper.class);

        when(licenseService.collectExpired(any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(license("L-1", CODE, null)));
        when(licenseService.collectRenewed(any(LocalDateTime.class), eq(100))).thenReturn(List.of());
        when(licenseService.findValid(CODE)).thenReturn(license("L-2", CODE, "inst-9"));
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install(CODE, 1, 1));
        when(marketPluginMapper.selectOne(any(Wrapper.class)))
                .thenReturn(marketPlugin(CODE, PluginLicenseService.TYPE_PAID));

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService, centerService,
                installMapper, marketPluginMapper, schedulerProvider(null), true, 1000L, 1000L, 100);

        PluginLicenseHeartbeat.SweepResult result = heartbeat.sweep(LocalDateTime.now());

        assertEquals(1, result.getExpired());
        assertEquals(0, result.getDisabled());
        verify(centerService, never()).toggle(anyString(), anyBoolean(), any());
    }

    /**
     * 免费插件不参与授权门控，即使查不到授权也不能停。
     */
    @Test
    void sweepLeavesFreePluginAlone() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        PluginCenterService centerService = mock(PluginCenterService.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketPluginMapper = mock(MarketPluginMapper.class);

        when(licenseService.collectExpired(any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(license("L-1", CODE, null)));
        when(licenseService.collectRenewed(any(LocalDateTime.class), eq(100))).thenReturn(List.of());
        when(licenseService.findValid(CODE)).thenReturn(null);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install(CODE, 1, 1));
        when(marketPluginMapper.selectOne(any(Wrapper.class)))
                .thenReturn(marketPlugin(CODE, PluginLicenseService.TYPE_FREE));

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService, centerService,
                installMapper, marketPluginMapper, schedulerProvider(null), true, 1000L, 1000L, 100);

        assertEquals(1, heartbeat.sweep(LocalDateTime.now()).getExpired());
        verify(centerService, never()).toggle(anyString(), anyBoolean(), any());
    }

    /**
     * 续费（同一行被延期）→ 原地恢复，带上该授权绑定的实例号。
     */
    @Test
    void sweepRestoresPluginWhenLicenseRenewed() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        PluginCenterService centerService = mock(PluginCenterService.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketPluginMapper = mock(MarketPluginMapper.class);

        MarketPluginLicense renewed = license("L-1", CODE, "inst-7");
        when(licenseService.collectExpired(any(LocalDateTime.class), eq(100))).thenReturn(List.of());
        when(licenseService.collectRenewed(any(LocalDateTime.class), eq(100))).thenReturn(List.of(renewed));
        when(licenseService.listByPlugin(CODE)).thenReturn(List.of(renewed));
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install(CODE, 1, 0));
        when(marketPluginMapper.selectOne(any(Wrapper.class)))
                .thenReturn(marketPlugin(CODE, PluginLicenseService.TYPE_TRIAL));

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService, centerService,
                installMapper, marketPluginMapper, schedulerProvider(null), true, 1000L, 1000L, 100);

        PluginLicenseHeartbeat.SweepResult result = heartbeat.sweep(LocalDateTime.now());

        assertEquals(1, result.getRenewed());
        assertEquals(1, result.getRestored());
        assertEquals(0, result.getDisabled());
        verify(centerService).toggle(CODE, true, "inst-7");
        verify(licenseService).markRenewed(eq(renewed), any(LocalDateTime.class));
    }

    /**
     * 兜底扫描：已启用但已无任何有效授权（例如紧急下架吊销）→ 软停。
     *
     * <p>这条路径覆盖「下架联动只吊销授权、不碰运行态」留下的那一半。
     */
    @Test
    void sweepFallbackDisablesEnabledPluginWithoutLicense() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        PluginCenterService centerService = mock(PluginCenterService.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketPluginMapper = mock(MarketPluginMapper.class);

        when(licenseService.collectExpired(any(LocalDateTime.class), eq(100))).thenReturn(List.of());
        when(licenseService.collectRenewed(any(LocalDateTime.class), eq(100))).thenReturn(List.of());
        when(licenseService.findValid(CODE)).thenReturn(null);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install(CODE, 1, 1));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of(install(CODE, 1, 1)));
        when(marketPluginMapper.selectOne(any(Wrapper.class)))
                .thenReturn(marketPlugin(CODE, PluginLicenseService.TYPE_PAID));

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService, centerService,
                installMapper, marketPluginMapper, schedulerProvider(null), true, 1000L, 1000L, 100);

        PluginLicenseHeartbeat.SweepResult result = heartbeat.sweep(LocalDateTime.now());

        assertEquals(1, result.getDisabled());
        verify(centerService).toggle(eq(CODE), eq(false), isNull());
    }

    /**
     * 软停失败不能抛出：{@code scheduleWithFixedDelay} 一旦收到异常就取消后续所有执行。
     */
    @Test
    void sweepSwallowsToggleFailure() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        PluginCenterService centerService = mock(PluginCenterService.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketPluginMapper = mock(MarketPluginMapper.class);

        when(licenseService.collectExpired(any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(license("L-1", CODE, null)));
        when(licenseService.collectRenewed(any(LocalDateTime.class), eq(100))).thenReturn(List.of());
        when(licenseService.findValid(CODE)).thenReturn(null);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install(CODE, 1, 1));
        when(marketPluginMapper.selectOne(any(Wrapper.class)))
                .thenReturn(marketPlugin(CODE, PluginLicenseService.TYPE_PAID));
        when(centerService.toggle(anyString(), anyBoolean(), any()))
                .thenThrow(new IllegalStateException("引擎不可用"));

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService, centerService,
                installMapper, marketPluginMapper, schedulerProvider(null), true, 1000L, 1000L, 100);

        PluginLicenseHeartbeat.SweepResult result = heartbeat.sweep(LocalDateTime.now());

        assertEquals(1, result.getExpired());
        assertEquals(0, result.getDisabled(), "软停失败不应计入成功数，也不应抛出");
    }

    /**
     * 未安装的插件不参与：授权可能属于别的站点，这里没有可停的运行态。
     */
    @Test
    void sweepSkipsNotInstalledPlugin() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        PluginCenterService centerService = mock(PluginCenterService.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketPluginMapper = mock(MarketPluginMapper.class);

        when(licenseService.collectExpired(any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(license("L-1", CODE, null)));
        when(licenseService.collectRenewed(any(LocalDateTime.class), eq(100))).thenReturn(List.of());
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install(CODE, 0, 0));

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService, centerService,
                installMapper, marketPluginMapper, schedulerProvider(null), true, 1000L, 1000L, 100);

        assertEquals(0, heartbeat.sweep(LocalDateTime.now()).getDisabled());
        verify(centerService, never()).toggle(anyString(), anyBoolean(), any());
    }

    /**
     * 时间为 null 时直接返回空结果，不查库。
     */
    @Test
    void sweepWithNullNowReturnsEmptyResult() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        PluginCenterService centerService = mock(PluginCenterService.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketPluginMapper = mock(MarketPluginMapper.class);

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService, centerService,
                installMapper, marketPluginMapper, schedulerProvider(null), true, 1000L, 1000L, 100);

        PluginLicenseHeartbeat.SweepResult result = heartbeat.sweep(null);

        assertEquals(0, result.getExpired() + result.getRenewed() + result.getDisabled() + result.getRestored());
        verify(licenseService, never()).collectExpired(any(LocalDateTime.class), eq(100));
    }

    /**
     * 开关关闭时不排程，但 sweep 入口仍然可用（运维可手动触发）。
     */
    @Test
    void startSkipsSchedulingWhenDisabled() {
        TaskScheduler scheduler = mock(TaskScheduler.class);
        ObjectProvider<TaskScheduler> provider = mock(ObjectProvider.class);
        when(provider.getIfUnique()).thenReturn(scheduler);

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(mock(PluginLicenseService.class),
                mock(PluginCenterService.class), mock(MallPluginInstallMapper.class),
                mock(MarketPluginMapper.class), provider, false, 1000L, 1000L, 100);

        heartbeat.start();

        verify(scheduler, never()).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
    }

    /**
     * 开关打开时按固定延迟排程，stop() 能取消掉。
     */
    @Test
    void startSchedulesAndStopCancels() {
        TaskScheduler scheduler = mock(TaskScheduler.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        when(scheduler.scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenReturn((ScheduledFuture) future);

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(mock(PluginLicenseService.class),
                mock(PluginCenterService.class), mock(MallPluginInstallMapper.class),
                mock(MarketPluginMapper.class), schedulerProvider(scheduler), true, 1000L, 2000L, 100);

        heartbeat.start();

        ArgumentCaptor<Duration> delay = ArgumentCaptor.forClass(Duration.class);
        verify(scheduler).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), delay.capture());
        assertEquals(Duration.ofMillis(2000L), delay.getValue());

        heartbeat.stop();
        verify(future).cancel(false);
    }

    /**
     * 调度器缺失时启动不报错——心跳是兜底能力，不该拖垮应用启动。
     */
    @Test
    void startToleratesMissingScheduler() {
        ObjectProvider<TaskScheduler> provider = mock(ObjectProvider.class);
        when(provider.getIfUnique()).thenReturn(null);

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(mock(PluginLicenseService.class),
                mock(PluginCenterService.class), mock(MallPluginInstallMapper.class),
                mock(MarketPluginMapper.class), provider, true, 1000L, 1000L, 100);

        heartbeat.start();

        assertNotNull(heartbeat);
        verify(provider, times(1)).getIfUnique();
    }

    /**
     * 非法参数回落到默认配置，避免把心跳频率配成 0 导致空转刷库。
     */
    @Test
    void invalidDelaysFallBackToDefaults() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        when(licenseService.collectExpired(any(LocalDateTime.class), eq(200))).thenReturn(List.of());
        when(licenseService.collectRenewed(any(LocalDateTime.class), eq(200))).thenReturn(List.of());

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService,
                mock(PluginCenterService.class), mock(MallPluginInstallMapper.class),
                mock(MarketPluginMapper.class), schedulerProvider(null), true, -1L, -1L, -5);

        heartbeat.sweep(LocalDateTime.now());

        verify(licenseService, times(1)).collectExpired(any(LocalDateTime.class), eq(200));
        verify(licenseService, times(1)).collectRenewed(any(LocalDateTime.class), eq(200));
    }

    /**
     * 同一插件多次过期授权只停一次（按 pluginCode 去重）。
     */
    @Test
    void sweepDisablesEachPluginOnce() {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        PluginCenterService centerService = mock(PluginCenterService.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketPluginMapper = mock(MarketPluginMapper.class);

        when(licenseService.collectExpired(any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of(license("L-1", CODE, null), license("L-2", CODE, null)));
        when(licenseService.collectRenewed(any(LocalDateTime.class), eq(100))).thenReturn(List.of());
        when(licenseService.findValid(CODE)).thenReturn(null);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install(CODE, 1, 1));
        when(marketPluginMapper.selectOne(any(Wrapper.class)))
                .thenReturn(marketPlugin(CODE, PluginLicenseService.TYPE_PAID));

        PluginLicenseHeartbeat heartbeat = new PluginLicenseHeartbeat(licenseService, centerService,
                installMapper, marketPluginMapper, schedulerProvider(null), true, 1000L, 1000L, 100);

        PluginLicenseHeartbeat.SweepResult result = heartbeat.sweep(LocalDateTime.now());

        assertEquals(2, result.getExpired());
        assertEquals(1, result.getDisabled());
        verify(centerService, times(1)).toggle(eq(CODE), eq(false), isNull());
    }

    private static ObjectProvider<TaskScheduler> schedulerProvider(TaskScheduler scheduler) {
        ObjectProvider<TaskScheduler> provider = mock(ObjectProvider.class);
        when(provider.getIfUnique()).thenReturn(scheduler);
        return provider;
    }

    private static MarketPluginLicense license(String key, String pluginCode, String instanceId) {
        MarketPluginLicense license = new MarketPluginLicense();
        license.setId(1L);
        license.setLicenseKey(key);
        license.setPluginCode(pluginCode);
        license.setInstanceId(instanceId);
        license.setStatus(1);
        license.setState(PluginLicenseService.STATE_ACTIVATED);
        return license;
    }

    private static MallPluginInstall install(String pluginCode, int installed, int enabled) {
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode(pluginCode);
        install.setInstalled(installed);
        install.setEnabled(enabled);
        return install;
    }

    private static MarketPlugin marketPlugin(String pluginCode, String licenseType) {
        MarketPlugin plugin = new MarketPlugin();
        plugin.setPluginCode(pluginCode);
        plugin.setLicenseType(licenseType);
        return plugin;
    }
}
