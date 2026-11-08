package com.houjiang.plugin.update.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.entity.update.PluginUpdateTask;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.mapper.update.PluginUpdateTaskMapper;
import com.houjiang.mall.service.market.PluginArtifactDownloader;
import com.houjiang.mall.service.plugin.AddonPluginSyncService;
import com.houjiang.mall.service.plugin.SchemaMigrationRunner;
import com.houjiang.plugin.artifact.authorization.DownloadDecision;
import com.houjiang.plugin.artifact.authorization.PluginDownloadAuthorizationService;
import com.houjiang.plugin.artifact.download.DownloadUrlResult;
import com.houjiang.plugin.artifact.download.PluginArtifactDownloadService;
import com.houjiang.plugin.artifact.download.RawArtifact;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import com.houjiang.plugin.engine.core.PluginDrainGate;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import com.houjiang.plugin.engine.core.PluginState;
import com.houjiang.plugin.signature.ArtifactSignaturePayload;
import com.houjiang.plugin.signature.Ed25519ArtifactSigner;
import com.houjiang.plugin.update.UpdateOpType;
import com.houjiang.plugin.update.UpdateTaskState;
import com.houjiang.plugin.update.config.UpdateProperties;
import com.houjiang.plugin.update.exception.UpdateRejectedException;
import com.houjiang.plugin.update.model.UpdateOutcome;
import com.houjiang.plugin.update.precheck.PluginCompatibilityPrecheckService;
import com.houjiang.plugin.update.precheck.PrecheckResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

/**
 * 升级 / 回滚编排测试（M4 核心）。
 *
 * <p><b>M4 的价值全在失败路径</b>，因此 happy path 只占一条，其余全部是故障注入：
 * 版本倒退、制品被篡改、Windows 句柄未释放、schema 失败、健康检查失败、回滚豁免、孤儿扫描。
 * 每条都断言「<b>没有留下半生效状态</b>」——这才是升级编排真正要证明的事。
 *
 * <p>与 M3 同口径：<b>不 mock 掉校验本身</b>。md5 / sha256 / Ed25519 全部走真实实现，
 * 只 mock 下载 IO、引擎、数据库这些外部依赖。
 *
 * @author Amos
 * @since M4
 */
class PluginUpdateServiceUpgradeTest {

    private static final String CODE = "demo";

    private static final String OLD_VERSION = "1.0.0";

    private static final String NEW_VERSION = "2.0.0";

    private static final String INSTANCE = "org-1";

    private static final String KID = "k-test";

    private static KeyPair keyPair;

    @TempDir
    Path tempDir;

    private MarketPluginVersionMapper versionMapper;

    private MallPluginInstallMapper installMapper;

    private PluginUpdateTaskMapper taskMapper;

    private PluginCompatibilityPrecheckService precheckService;

    private PluginDownloadAuthorizationService authorizationService;

    private PluginArtifactDownloadService downloadService;

    private PluginLifecycleManager lifecycleManager;

    private SchemaMigrationRunner schemaMigrationRunner;

    private PluginArtifactStore artifactStore;

    private PluginDrainGate drainGate;

    private MallPluginInstall installRow;

    private PluginUpdateService service;

    private byte[] oldJarBytes;

    private byte[] newJarBytes;

    /**
     * 生成测试用 Ed25519 密钥对。
     */
    @BeforeAll
    static void initKeys() throws Exception {
        keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
    }

    /**
     * 装配被测服务与默认放行桩。
     */
    @BeforeEach
    void setUp() throws Exception {
        oldJarBytes = "PK-old-jar-bytes-1.0.0".getBytes(StandardCharsets.UTF_8);
        newJarBytes = "PK-new-jar-bytes-2.0.0".getBytes(StandardCharsets.UTF_8);

        versionMapper = mock(MarketPluginVersionMapper.class);
        installMapper = mock(MallPluginInstallMapper.class);
        taskMapper = mock(PluginUpdateTaskMapper.class);
        precheckService = mock(PluginCompatibilityPrecheckService.class);
        authorizationService = mock(PluginDownloadAuthorizationService.class);
        downloadService = mock(PluginArtifactDownloadService.class);
        PluginArtifactDownloader artifactDownloader = mock(PluginArtifactDownloader.class);
        lifecycleManager = mock(PluginLifecycleManager.class);
        schemaMigrationRunner = mock(SchemaMigrationRunner.class);
        AddonPluginSyncService addonPluginSyncService = mock(AddonPluginSyncService.class);
        artifactStore = new PluginArtifactStore(tempDir);
        drainGate = new PluginDrainGate();

        UpdateProperties properties = new UpdateProperties();
        properties.setFrameworkVersion("1.0.0");
        properties.setRequireSignature(true);
        properties.setPublicBaseUrl("");
        // 排空窗口置 0：单测不等 5 秒；退避置 1ms：句柄重试用例要跑 3 次
        properties.setDrainWindowMs(0);
        properties.setHandleRetryAttempts(3);
        properties.setHandleRetryBackoffMs(1);

        service = new PluginUpdateService(precheckService, authorizationService, downloadService,
                artifactDownloader, signer(), artifactStore, lifecycleManager, schemaMigrationRunner,
                installMapper, versionMapper, taskMapper, addonPluginSyncService, properties, drainGate);

        // 已安装 1.0.0，磁盘上有真实 jar（回滚点）
        installRow = installed(OLD_VERSION);
        artifactStore.store(CODE, OLD_VERSION, oldJarBytes);

        when(versionMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(versionRow(NEW_VERSION, md5(newJarBytes), sha256(newJarBytes),
                        sign(CODE, NEW_VERSION, md5(newJarBytes), sha256(newJarBytes), newJarBytes.length)));
        when(precheckService.precheck(anyString(), anyString())).thenReturn(PrecheckResult.pass());
        when(authorizationService.authorize(anyString(), anyString(), any()))
                .thenReturn(DownloadDecision.allow(null, null));
        when(downloadService.issueDownloadUrl(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ticket(NEW_VERSION, md5(newJarBytes), sha256(newJarBytes), newJarBytes.length));
        when(downloadService.openByToken(any(), any())).thenAnswer(invocation -> raw(newJarBytes, NEW_VERSION));
        when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(installRow);
        when(lifecycleManager.install(anyString(), any(Path.class), any())).thenReturn(PluginState.ACTIVE);
        when(lifecycleManager.isHealthy(anyString())).thenReturn(true);
    }

    // ==================================================================
    // 成功路径（仅一条：M4 的重点在失败路径）
    // ==================================================================

    @Test
    void 升级成功_版本推进且previousVersion记为老版本() {
        UpdateOutcome outcome = service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent");

        assertEquals(UpdateTaskState.SUCCESS.getCode(), outcome.getState());
        assertEquals(NEW_VERSION, outcome.getCurrentVersion());
        assertFalse(outcome.isRolledBack());
        verify(lifecycleManager).disable(CODE);
        verify(lifecycleManager).uninstall(CODE);
        verify(schemaMigrationRunner).migrateUpgrade(CODE, OLD_VERSION, NEW_VERSION);
        verify(lifecycleManager).install(eq(CODE), eq(artifactStore.resolve(CODE, NEW_VERSION)), any());
        // 回滚锚点：升级成功后 previous_version 指向升级前版本
        assertEquals(OLD_VERSION, installRow.getPreviousVersion());
        assertEquals(NEW_VERSION, installRow.getVersion());
        assertEquals(0, installRow.getUpdateStatus());
        // 旧版本目录必须还在（备份+锚点保护），否则下次回滚没退路
        assertTrue(Files.isRegularFile(artifactStore.resolve(CODE, OLD_VERSION)), "旧版本 jar 必须保留为回滚点");
        assertTrue(drainGate.drainingPlugins().isEmpty(), "升级结束后排空窗口必须关闭");
    }

    // ==================================================================
    // 失败路径
    // ==================================================================

    @Test
    void 升级到更低版本_返回3012且绝不发起下载() throws Exception {
        installRow.setVersion(NEW_VERSION);
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(versionRow(OLD_VERSION,
                md5(oldJarBytes), sha256(oldJarBytes),
                sign(CODE, OLD_VERSION, md5(oldJarBytes), sha256(oldJarBytes), oldJarBytes.length)));

        UpdateRejectedException error = assertThrows(UpdateRejectedException.class,
                () -> service.upgrade(identity(), CODE, OLD_VERSION, "1.2.3.4", "agent"));

        assertEquals(PluginDistErrorCode.VERSION_CONFLICT, error.getCode());
        // 关键断言：版本方向必须在下载之前判，否则等于白下一个包再丢掉
        verifyNoInteractions(downloadService);
        verify(lifecycleManager, never()).disable(anyString());
        assertEquals(NEW_VERSION, installRow.getVersion(), "失败不能改动安装记录");
    }

    @Test
    void 新包摘要不匹配_旧版本零影响且未撤流量() throws Exception {
        // 制品在传输途中被篡改：登记值不变、真实字节被改了一个字符，
        // md5 由真实实现计算与比对（不 mock 校验本身），因此必然 3010。
        byte[] tampered = newJarBytes.clone();
        tampered[0] = (byte) (tampered[0] ^ 0x7F);
        when(downloadService.openByToken(any(), any())).thenAnswer(invocation -> raw(tampered, NEW_VERSION));

        UpdateRejectedException error = assertThrows(UpdateRejectedException.class,
                () -> service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent"));

        assertEquals(PluginDistErrorCode.CHECKSUM_MISMATCH, error.getCode());
        // 旧版本逐字节未变
        assertArrayEqualsFile(oldJarBytes, artifactStore.resolve(CODE, OLD_VERSION));
        assertEquals(OLD_VERSION, installRow.getVersion());
        // 校验在撤流量之前，路由必须完好无损
        verify(lifecycleManager, never()).disable(anyString());
        verify(lifecycleManager, never()).uninstall(anyString());
        // 新版本目录没有残留
        assertFalse(Files.exists(artifactStore.resolve(CODE, NEW_VERSION)), "校验失败不得留下新版本文件");
        assertTaskState(UpdateTaskState.FAILED.getCode(), "3010");
        assertTrue(drainGate.drainingPlugins().isEmpty());
    }

    @Test
    void 句柄未释放_重试三次后中止并恢复路由() throws Exception {
        // Mockito 的 doThrow 不允许抛方法签名未声明的受检异常，用 doAnswer 抛真实 AccessDeniedException
        doAnswer(invocation -> {
            throw new AccessDeniedException(artifactStore.resolve(CODE, OLD_VERSION).toString());
        }).when(lifecycleManager).uninstall(CODE);

        UpdateRejectedException error = assertThrows(UpdateRejectedException.class,
                () -> service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent"));

        assertEquals(500, error.getCode());
        // 指数退避重试 3 次后仍失败
        verify(lifecycleManager, times(3)).uninstall(CODE);
        // 路由必须恢复：绝不留「旧版本已停、新版本没起」的半生效状态
        verify(lifecycleManager).enable(CODE);
        verify(lifecycleManager, never()).install(anyString(), any(Path.class), any());
        assertEquals(OLD_VERSION, installRow.getVersion(), "安装记录必须仍是旧版本");
        assertEquals(4, installRow.getUpdateStatus(), "安装态必须标记为上次失败");
        assertTaskState(UpdateTaskState.FAILED.getCode(), "HANDLE_LOCKED");
        assertTrue(drainGate.drainingPlugins().isEmpty(), "中止升级必须关闭排空窗口");
    }

    @Test
    void 预检不通过_必须解开更新锁否则插件永久卡在升级中() {
        // 预检分支是「内部直接抛被拒异常、自己不解锁」的典型：过去外层原样透传，
        // install.update_status 会一直停在 2=升级中，该插件从此再也升不了、卸不了，
        // 而孤儿任务扫描只扫任务表（任务已是终态）救不了它。
        when(precheckService.precheck(anyString(), anyString()))
                .thenReturn(PrecheckResult.fail(PluginDistErrorCode.DEPENDENCY_CYCLE, "依赖成环: demo -> a -> demo"));

        UpdateRejectedException error = assertThrows(UpdateRejectedException.class,
                () -> service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent"));

        assertEquals(PluginDistErrorCode.DEPENDENCY_CYCLE, error.getCode());
        assertEquals(4, installRow.getUpdateStatus(), "被拒时必须把锁落定为上次失败，不能留在升级中");
        assertTrue(installRow.getLastError() != null && installRow.getLastError().contains("升级被拒"),
                "失败原因要落到 last_error，便于运维定位");
        assertEquals(OLD_VERSION, installRow.getVersion(), "被拒不得改动版本");
        verify(lifecycleManager, never()).disable(anyString());
        verifyNoInteractions(downloadService);
    }

    @Test
    void 授权被拒_同样要解开更新锁() {
        when(authorizationService.authorize(anyString(), anyString(), any()))
                .thenReturn(DownloadDecision.deny(PluginDistErrorCode.LICENSE_REVOKED, "当前站点未购买该版本"));

        UpdateRejectedException error = assertThrows(UpdateRejectedException.class,
                () -> service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent"));

        assertEquals(PluginDistErrorCode.LICENSE_REVOKED, error.getCode());
        assertEquals(4, installRow.getUpdateStatus(), "授权被拒同样不能把锁留在升级中");
        assertTrue(drainGate.drainingPlugins().isEmpty());
    }

    @Test
    void schema失败_自动回滚到旧版本() {
        doThrow(new IllegalStateException("列类型冲突")).when(schemaMigrationRunner)
                .migrateUpgrade(CODE, OLD_VERSION, NEW_VERSION);

        UpdateOutcome outcome = service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent");

        assertEquals(UpdateTaskState.ROLLED_BACK.getCode(), outcome.getState());
        assertTrue(outcome.isRolledBack());
        assertEquals(OLD_VERSION, outcome.getCurrentVersion(), "回滚后必须停在使用中的旧版本");
        assertEquals(OLD_VERSION, installRow.getVersion());
        assertEquals(0, installRow.getUpdateStatus());
        // 旧版本被重新装载
        verify(lifecycleManager).install(eq(CODE), eq(artifactStore.resolve(CODE, OLD_VERSION)), any());
        verify(schemaMigrationRunner).migrateRollback(eq(CODE), eq(NEW_VERSION), eq(OLD_VERSION),
                eq(NEW_VERSION), any());
        assertFalse(Files.exists(artifactStore.resolve(CODE, NEW_VERSION)), "失败的新版本目录必须清理");
        assertTrue(drainGate.drainingPlugins().isEmpty());
    }

    @Test
    void 健康检查失败_自动回滚到旧版本() {
        // 第一次健康检查（新版本）失败，第二次（回滚后的旧版本）通过
        when(lifecycleManager.isHealthy(CODE)).thenReturn(false, true);

        UpdateOutcome outcome = service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent");

        assertEquals(UpdateTaskState.ROLLED_BACK.getCode(), outcome.getState());
        assertTrue(outcome.isRolledBack());
        assertEquals(OLD_VERSION, installRow.getVersion());
        verify(lifecycleManager).install(eq(CODE), eq(artifactStore.resolve(CODE, OLD_VERSION)), any());
        assertTaskState(UpdateTaskState.ROLLED_BACK.getCode(), null);
    }

    @Test
    void 回滚豁免授权与下架状态_且从不调用授权() throws Exception {
        installRow.setPreviousVersion(OLD_VERSION);
        installRow.setVersion(NEW_VERSION);
        artifactStore.store(CODE, NEW_VERSION, newJarBytes);
        // 即使授权已吊销、版本已下架，回滚也必须成功：回滚是履约动作
        when(authorizationService.authorize(anyString(), anyString(), any()))
                .thenReturn(DownloadDecision.deny(PluginDistErrorCode.LICENSE_REVOKED, "授权已吊销"));

        UpdateOutcome outcome = service.rollback(CODE, OLD_VERSION, INSTANCE, 100L);

        assertEquals(UpdateTaskState.ROLLED_BACK.getCode(), outcome.getState());
        assertEquals(OLD_VERSION, outcome.getCurrentVersion());
        // 硬断言：回滚链路一次授权都没查，版本发布状态也没查
        verifyNoInteractions(authorizationService);
        verifyNoInteractions(versionMapper);
        verify(lifecycleManager).install(eq(CODE), eq(artifactStore.resolve(CODE, OLD_VERSION)), any());
    }

    @Test
    void 回滚点缺失_返回3018() throws Exception {
        installRow.setPreviousVersion("0.9.0");

        UpdateRejectedException error = assertThrows(UpdateRejectedException.class,
                () -> service.rollback(CODE, "0.9.0", INSTANCE, 100L));

        assertEquals(PluginDistErrorCode.ROLLBACK_POINT_MISSING, error.getCode());
        verify(lifecycleManager, never()).disable(anyString());
    }

    @Test
    void 并发升级_返回3016() {
        PluginUpdateTask active = new PluginUpdateTask();
        active.setTaskId("upd-running");
        active.setState(UpdateTaskState.RUNNING.getCode());
        when(taskMapper.selectActive(eq(INSTANCE), eq(CODE), eq(UpdateOpType.UPGRADE.getCode()),
                eq(NEW_VERSION))).thenReturn(active);

        UpdateRejectedException error = assertThrows(UpdateRejectedException.class,
                () -> service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent"));

        assertEquals(PluginDistErrorCode.TASK_IN_PROGRESS, error.getCode());
        verifyNoInteractions(downloadService);
    }

    @Test
    void 上次升级失败留下的updateStatus4_不是活锁_允许重试() {
        // update_status=4 只是「上次失败」的终态记录。若把它也当成锁，
        // 一次失败的升级会把这台站点的这个插件永久锁死——失败后唯一合理的动作（重试）
        // 会被 3016 拦住，只能人工改库解锁。这里锁定「失败之后还能重试」。
        installRow.setUpdateStatus(4);
        installRow.setLastError("上次升级失败：数据迁移失败");

        UpdateOutcome outcome = service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent");

        assertEquals(UpdateTaskState.SUCCESS.getCode(), outcome.getState());
        assertEquals(NEW_VERSION, installRow.getVersion());
        assertEquals(0, installRow.getUpdateStatus(), "重试成功后锁必须回到空闲");
        assertNull(installRow.getLastError(), "重试成功后必须清掉上次的失败原因");
    }

    @Test
    void 正在升级中的updateStatus2_仍然拦住新的升级() {
        installRow.setUpdateStatus(2);

        UpdateRejectedException error = assertThrows(UpdateRejectedException.class,
                () -> service.upgrade(identity(), CODE, NEW_VERSION, "1.2.3.4", "agent"));

        assertEquals(PluginDistErrorCode.TASK_IN_PROGRESS, error.getCode());
        verify(lifecycleManager, never()).disable(anyString());
    }

    @Test
    void 孤儿任务扫描_置失败并尝试自动回滚() throws Exception {
        PluginUpdateTask orphan = new PluginUpdateTask();
        orphan.setId(9L);
        orphan.setTaskId("upd-orphan");
        orphan.setInstanceId(INSTANCE);
        orphan.setPluginCode(CODE);
        orphan.setOpType(UpdateOpType.UPGRADE.getCode());
        orphan.setFromVersion(OLD_VERSION);
        orphan.setToVersion(NEW_VERSION);
        orphan.setState(UpdateTaskState.RUNNING.getCode());
        orphan.setStartTime(LocalDateTime.now().minusHours(2));
        // 模拟进程在「写库之后、落任务终态之前」被杀：版本已是新版，任务还停在 RUNNING。
        // 这也是最需要回滚的一种残骸——用户看到的是新版本，但这次升级其实没完成。
        installRow.setUpdateStatus(2);
        installRow.setVersion(NEW_VERSION);
        installRow.setPreviousVersion(OLD_VERSION);
        artifactStore.store(CODE, NEW_VERSION, newJarBytes);
        when(taskMapper.selectOrphans(any(LocalDateTime.class))).thenReturn(List.of(orphan));

        int handled = service.scanOrphanTasks();

        assertEquals(1, handled);
        // 尝试了自动回滚（旧版本被重新装载）
        verify(lifecycleManager).install(eq(CODE), eq(artifactStore.resolve(CODE, OLD_VERSION)), any());
        // 原任务无论回滚是否成功都要收敛成终态，否则互斥锁永远解不开
        ArgumentCaptor<PluginUpdateTask> captor = ArgumentCaptor.forClass(PluginUpdateTask.class);
        verify(taskMapper, org.mockito.Mockito.atLeastOnce()).updateById(captor.capture());
        PluginUpdateTask terminal = null;
        for (PluginUpdateTask captured : captor.getAllValues()) {
            if ("upd-orphan".equals(captured.getTaskId())) {
                terminal = captured;
            }
        }
        assertNotNull(terminal, "孤儿任务必须被落终态");
        assertEquals(UpdateTaskState.FAILED.getCode(), terminal.getState());
        assertEquals("ORPHAN_TIMEOUT", terminal.getErrorCode());
    }

    // ==================================================================
    // 辅助
    // ==================================================================

    private static void assertArrayEqualsFile(byte[] expected, Path path) throws Exception {
        assertTrue(Files.isRegularFile(path), "文件应存在: " + path);
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected, Files.readAllBytes(path));
    }

    private void assertTaskState(String state, String errorCode) {
        ArgumentCaptor<PluginUpdateTask> captor = ArgumentCaptor.forClass(PluginUpdateTask.class);
        verify(taskMapper, org.mockito.Mockito.atLeastOnce()).updateById(captor.capture());
        PluginUpdateTask last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(state, last.getState());
        assertEquals(errorCode, last.getErrorCode());
    }

    private static MallPluginInstall installed(String version) {
        MallPluginInstall row = new MallPluginInstall();
        row.setPluginCode(CODE);
        row.setInstalled(1);
        row.setEnabled(1);
        row.setVersion(version);
        row.setUpdateStatus(0);
        return row;
    }

    private static PluginIdentity identity() {
        return new PluginIdentity(INSTANCE, 100L);
    }

    private Ed25519ArtifactSigner signer() {
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        return new Ed25519ArtifactSigner(null, Map.of(KID, publicKey));
    }

    private String sign(String code, String version, String md5, String sha256, int size) throws Exception {
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(keyPair.getPrivate());
        signature.update(ArtifactSignaturePayload.toSignBytes(
                ArtifactSignaturePayload.build(sha256, code, version, md5, size)));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private static MarketPluginVersion versionRow(String version, String md5, String sha256, String signature) {
        MarketPluginVersion row = new MarketPluginVersion();
        row.setPluginCode(CODE);
        row.setVersion(version);
        row.setStatus(1);
        row.setJarMd5(md5);
        row.setJarSha256(sha256);
        row.setJarSignature(signature);
        row.setSignatureKid(KID);
        row.setPluginYaml("owner: official\nruntime: embedded\nminFrameworkVersion: 1.0.0\n");
        return row;
    }

    private static DownloadUrlResult ticket(String version, String md5, String sha256, long size) {
        return new DownloadUrlResult("/base/plugin-center/artifact/raw?token=t", "t", "aid", CODE, version,
                md5, sha256, size, CODE + "-" + version + ".jar",
                System.currentTimeMillis() + 600_000, 5);
    }

    private static RawArtifact raw(byte[] content, String version) {
        ArtifactStream stream = new ArtifactStream(new ByteArrayInputStream(content),
                content.length, content.length, 0, content.length - 1, null);
        return new RawArtifact(stream, "aid", CODE, version, md5(content), sha256(content), content.length);
    }

    private static String md5(byte[] bytes) {
        return ArtifactSignaturePayload.md5Hex(bytes);
    }

    private static String sha256(byte[] bytes) {
        return ArtifactSignaturePayload.sha256Hex(bytes);
    }
}
