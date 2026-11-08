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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.houjiang.plugin.update.model.InstallResult;
import com.houjiang.plugin.update.model.TaskStatus;
import com.houjiang.plugin.update.precheck.PluginCompatibilityPrecheckService;
import com.houjiang.plugin.update.precheck.PrecheckResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.dao.DuplicateKeyException;

/**
 * 安装编排测试（M3 核心）。
 *
 * <p><b>刻意不 mock 掉关键校验</b>：md5 / sha256 / Ed25519 验签全部走真实实现——
 * 摘要用 JDK 的 {@code MessageDigest} 实算，签名用真实密钥对（测试内现场生成）签与验，
 * 只有「下载 IO」「引擎装载」「数据库」这些外部依赖才是 mock。
 * 把校验本身 mock 掉等于什么都没测，篡改制品必然通过。
 *
 * <p><b>失败路径统一断言「不留半成品」</b>：旧版本 jar 内容未被改动、
 * 新版本目录没有残留、引擎没有被装载、安装态没有被改。
 *
 * @author Amos
 * @since M3
 */
class PluginUpdateServiceTest {

    private static final String CODE = "demo";

    private static final String VERSION = "2.0.0";

    private static final String OLD_VERSION = "1.0.0";

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

    private PluginArtifactDownloader artifactDownloader;

    private PluginLifecycleManager lifecycleManager;

    private SchemaMigrationRunner schemaMigrationRunner;

    private AddonPluginSyncService addonPluginSyncService;

    private PluginArtifactStore artifactStore;

    private UpdateProperties properties;

    private PluginUpdateService service;

    private byte[] jarBytes;

    /**
     * 生成测试用 Ed25519 密钥对（全局一次，验签器用公钥，签名用私钥）。
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
        jarBytes = "PKfake-jar-bytes-for-install-test".getBytes(StandardCharsets.UTF_8);

        versionMapper = mock(MarketPluginVersionMapper.class);
        installMapper = mock(MallPluginInstallMapper.class);
        taskMapper = mock(PluginUpdateTaskMapper.class);
        precheckService = mock(PluginCompatibilityPrecheckService.class);
        authorizationService = mock(PluginDownloadAuthorizationService.class);
        downloadService = mock(PluginArtifactDownloadService.class);
        artifactDownloader = mock(PluginArtifactDownloader.class);
        lifecycleManager = mock(PluginLifecycleManager.class);
        schemaMigrationRunner = mock(SchemaMigrationRunner.class);
        addonPluginSyncService = mock(AddonPluginSyncService.class);
        artifactStore = new PluginArtifactStore(tempDir);

        properties = new UpdateProperties();
        properties.setFrameworkVersion("1.0.0");
        properties.setRequireSignature(true);
        properties.setPublicBaseUrl("");

        service = new PluginUpdateService(precheckService, authorizationService, downloadService,
                artifactDownloader, signer(), artifactStore, lifecycleManager, schemaMigrationRunner,
                installMapper, versionMapper, taskMapper, addonPluginSyncService, properties,
                new PluginDrainGate());

        // 默认：一切顺利
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(publishedVersion(md5(), sha256(), sign(CODE, VERSION, md5(), sha256()), KID));
        when(precheckService.precheck(anyString(), anyString())).thenReturn(PrecheckResult.pass());
        when(authorizationService.authorize(anyString(), anyString(), any()))
                .thenReturn(DownloadDecision.allow(null, null));
        when(downloadService.issueDownloadUrl(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ticket(md5(), sha256()));
        // 每次调用都新建流：ByteArrayInputStream 是一次性的，复用同一实例会让第二次安装读到空包
        when(downloadService.openByToken(any(), any())).thenAnswer(invocation -> raw(jarBytes));
        when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(lifecycleManager.install(anyString(), any(Path.class), any())).thenReturn(PluginState.ACTIVE);
        when(lifecycleManager.isHealthy(anyString())).thenReturn(true);
    }

    // ==================================================================
    // 成功路径
    // ==================================================================

    @Test
    void 安装成功_落盘装载登记并置任务成功() {
        InstallResult result = service.install(identity(), CODE, VERSION, "1.2.3.4", "agent");

        assertNotNull(result.getTaskId());
        assertEquals(VERSION, result.getVersion());
        assertEquals(UpdateTaskState.SUCCESS.getCode(), result.getState());
        assertTrue(Files.isRegularFile(artifactStore.resolve(CODE, VERSION)), "新版本安装包应已落盘");
        verify(schemaMigrationRunner).migrate(CODE, VERSION);
        verify(lifecycleManager).install(eq(CODE), eq(artifactStore.resolve(CODE, VERSION)), any());
        verify(installMapper).insert(any(MallPluginInstall.class));
        verify(addonPluginSyncService).syncFromPluginYaml(any());
        assertTaskState(UpdateTaskState.SUCCESS.getCode(), null);
    }

    @Test
    void 卸载成功_保留已发布插件目录() {
        givenInstalledOldVersion();
        properties.setDrainWindowMs(0L);

        service.uninstall(identity(), CODE, false);

        verifyNoInteractions(addonPluginSyncService);
    }

    @Test
    void 配置了对外地址时走下载器取制品() throws Exception {
        properties.setPublicBaseUrl("http://127.0.0.1:8220");
        when(artifactDownloader.download(anyString()))
                .thenReturn(new PluginArtifactDownloader.Artifact(jarBytes, "http://x", "http"));

        InstallResult result = service.install(identity(), CODE, VERSION, null, null);

        assertEquals(VERSION, result.getVersion());
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(artifactDownloader).download(urlCaptor.capture());
        assertTrue(urlCaptor.getValue().startsWith("http://127.0.0.1:8220/base/plugin-center/artifact/raw?token="));
        verify(downloadService, never()).openByToken(any(), any());
    }

    // ==================================================================
    // 三重校验失败：不留半成品
    // ==================================================================

    @Test
    void md5不一致_返回3010且旧版本与新版本目录均无变化() throws Exception {
        Path oldJar = givenInstalledOldVersion();
        byte[] before = Files.readAllBytes(oldJar);
        // 存储里的制品被掉包：声明的 md5 是原包，实际下载到的是篡改包
        when(downloadService.openByToken(any(), any()))
                .thenReturn(raw("tampered-bytes".getBytes(StandardCharsets.UTF_8)));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(PluginDistErrorCode.CHECKSUM_MISMATCH, ex.getCode());
        assertArrayEqualsContent(before, oldJar, "旧版本安装包必须原封不动");
        assertFalse(Files.exists(artifactStore.resolve(CODE, VERSION)), "校验失败不应留下新版本制品");
        verify(lifecycleManager, never()).install(anyString(), any(Path.class), any());
        verify(installMapper, never()).updateById(any(MallPluginInstall.class));
        assertTaskState(UpdateTaskState.FAILED.getCode(), "3010");
    }

    @Test
    void sha256登记值被改_返回3010且旧版本不动() throws Exception {
        Path oldJar = givenInstalledOldVersion();
        byte[] before = Files.readAllBytes(oldJar);
        String wrongSha256 = "f".repeat(64);
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(publishedVersion(md5(), wrongSha256, sign(CODE, VERSION, md5(), sha256()), KID));
        when(downloadService.issueDownloadUrl(anyString(), anyString(), any(), any(), any()))
                .thenReturn(ticket(md5(), wrongSha256));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(PluginDistErrorCode.CHECKSUM_MISMATCH, ex.getCode());
        assertArrayEqualsContent(before, oldJar, "旧版本安装包必须原封不动");
        assertFalse(Files.exists(artifactStore.resolve(CODE, VERSION)));
        assertTaskState(UpdateTaskState.FAILED.getCode(), "3010");
    }

    @Test
    void 签名与版本不匹配_返回3011且旧版本不动() throws Exception {
        Path oldJar = givenInstalledOldVersion();
        byte[] before = Files.readAllBytes(oldJar);
        // 用 1.0.0 的签名冒充 2.0.0 —— 规范化签名串含 version，必然验签失败
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(publishedVersion(md5(), sha256(), sign(CODE, OLD_VERSION, md5(), sha256()), KID));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(PluginDistErrorCode.SIGNATURE_INVALID, ex.getCode());
        assertArrayEqualsContent(before, oldJar, "旧版本安装包必须原封不动");
        assertFalse(Files.exists(artifactStore.resolve(CODE, VERSION)));
        assertTaskState(UpdateTaskState.FAILED.getCode(), "3011");
    }

    @Test
    void 未知kid_返回3011() throws Exception {
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(publishedVersion(md5(), sha256(), sign(CODE, VERSION, md5(), sha256()), "k-unknown"));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(PluginDistErrorCode.SIGNATURE_INVALID, ex.getCode());
    }

    @Test
    void 未签名制品在强制验签下被拒_关闭开关后放行() throws Exception {
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(publishedVersion(md5(), sha256(), null, null));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));
        assertEquals(PluginDistErrorCode.SIGNATURE_INVALID, ex.getCode());

        properties.setRequireSignature(false);
        InstallResult result = service.install(identity(), CODE, VERSION, null, null);
        assertEquals(VERSION, result.getVersion());
    }

    @Test
    void 制品超过体积上限_返回3017() {
        properties.setMaxArtifactBytes(4);

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(PluginDistErrorCode.SIZE_EXCEEDED, ex.getCode());
        verify(lifecycleManager, never()).install(anyString(), any(Path.class), any());
    }

    // ==================================================================
    // 前置门禁：不通过就绝不下载
    // ==================================================================

    @Test
    void 预检不通过_返回3014且不发起下载() {
        when(precheckService.precheck(anyString(), anyString()))
                .thenReturn(PrecheckResult.fail(PluginDistErrorCode.INCOMPATIBLE_FRAMEWORK,
                        "当前框架版本 1.0.0 低于插件要求的最低版本 2.0"));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(PluginDistErrorCode.INCOMPATIBLE_FRAMEWORK, ex.getCode());
        assertTrue(ex.getMessage().contains("2.0"));
        verifyNoInteractions(downloadService);
        verifyNoInteractions(artifactDownloader);
        verify(installMapper, never()).insert(any(MallPluginInstall.class));
        assertTaskState(UpdateTaskState.FAILED.getCode(), "3014");
    }

    @Test
    void 二次授权校验不通过_返回3002且不发起下载() {
        when(authorizationService.authorize(anyString(), anyString(), any()))
                .thenReturn(DownloadDecision.deny(PluginDistErrorCode.NOT_PURCHASED, "当前站点未购买该插件"));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(PluginDistErrorCode.NOT_PURCHASED, ex.getCode());
        verifyNoInteractions(downloadService);
        assertTaskState(UpdateTaskState.FAILED.getCode(), "3002");
    }

    // ==================================================================
    // 任务幂等与并发互斥
    // ==================================================================

    @Test
    void 已有进行中任务_返回3016且不新建任务() {
        service.install(identity(), CODE, VERSION, null, null);
        when(taskMapper.selectActive(eq(INSTANCE), eq(CODE), eq(UpdateOpType.INSTALL.getCode()), eq(VERSION)))
                .thenReturn(activeTask("upd-running"));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(PluginDistErrorCode.TASK_IN_PROGRESS, ex.getCode());
        assertTrue(ex.getMessage().contains("upd-running"));
        verify(taskMapper, times(1)).insert(any(PluginUpdateTask.class));
        // 第二次没有再换票下载：第一次成功的那次交互必须仍是唯一一次
        verify(downloadService, times(1)).issueDownloadUrl(anyString(), anyString(), any(), any(), any());
    }

    @Test
    void 建单撞唯一键_返回3016() {
        when(taskMapper.insert(any(PluginUpdateTask.class)))
                .thenThrow(new DuplicateKeyException("Duplicate entry for key uk_idem"));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(PluginDistErrorCode.TASK_IN_PROGRESS, ex.getCode());
    }

    // ==================================================================
    // 已装同 code：先卸载
    // ==================================================================

    @Test
    void 已安装同一插件时先卸载再装载() {
        givenInstalledOldVersion();

        service.install(identity(), CODE, VERSION, null, null);

        InOrder order = inOrder(lifecycleManager);
        order.verify(lifecycleManager).uninstall(CODE);
        order.verify(lifecycleManager).install(eq(CODE), any(Path.class), any());
    }

    // ==================================================================
    // 补偿：schema / 引擎 / 持久化 失败
    // ==================================================================

    @Test
    void schema迁移失败_置失败并删除新版本目录() {
        doThrow(new IllegalStateException("DDL 语法错误")).when(schemaMigrationRunner).migrate(CODE, VERSION);

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(500, ex.getCode());
        assertFalse(Files.exists(artifactStore.resolve(CODE, VERSION)), "schema 失败必须清掉新版本目录");
        verify(lifecycleManager, never()).install(anyString(), any(Path.class), any());
        assertTaskState(UpdateTaskState.FAILED.getCode(), "SCHEMA_FAILED");
    }

    @Test
    void 引擎装载失败_置失败并尽力恢复旧版本() {
        givenInstalledOldVersion();
        when(lifecycleManager.install(anyString(), any(Path.class), any()))
                .thenThrow(new IllegalStateException("装载失败"));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(500, ex.getCode());
        ArgumentCaptor<Path> paths = ArgumentCaptor.forClass(Path.class);
        verify(lifecycleManager, times(2)).install(eq(CODE), paths.capture(), any());
        assertEquals(artifactStore.resolve(CODE, VERSION), paths.getAllValues().get(0));
        assertEquals(artifactStore.resolve(CODE, OLD_VERSION), paths.getAllValues().get(1),
                "装载失败后必须尝试把旧版本装回去");
        assertFalse(Files.exists(artifactStore.resolve(CODE, VERSION)));
        assertTaskState(UpdateTaskState.FAILED.getCode(), "ENGINE_INSTALL_FAILED");
    }

    @Test
    void 健康检查未通过_置失败并回退() {
        when(lifecycleManager.isHealthy(anyString())).thenReturn(false);

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(500, ex.getCode());
        verify(lifecycleManager).uninstall(CODE);
        assertFalse(Files.exists(artifactStore.resolve(CODE, VERSION)));
        assertTaskState(UpdateTaskState.FAILED.getCode(), "HEALTH_CHECK_FAILED");
    }

    @Test
    void 安装登记失败_回退引擎与文件() {
        doThrow(new RuntimeException("数据库不可用")).when(installMapper).insert(any(MallPluginInstall.class));

        UpdateRejectedException ex = assertThrows(UpdateRejectedException.class,
                () -> service.install(identity(), CODE, VERSION, null, null));

        assertEquals(500, ex.getCode());
        verify(lifecycleManager).uninstall(CODE);
        assertFalse(Files.exists(artifactStore.resolve(CODE, VERSION)), "登记失败必须清理已落盘的新版本");
        assertTaskState(UpdateTaskState.FAILED.getCode(), "PERSIST_FAILED");
    }

    // ==================================================================
    // 查询类
    // ==================================================================

    @Test
    void 按任务号查状态() {
        when(taskMapper.selectByTaskId("upd-1")).thenReturn(activeTask("upd-1"));

        TaskStatus status = service.taskStatus("upd-1", null, INSTANCE);

        assertNotNull(status);
        assertEquals("upd-1", status.getTaskId());
        assertEquals(UpdateTaskState.RUNNING.getCode(), status.getState());
    }

    @Test
    void 任务不存在返回null() {
        assertNull(service.taskStatus("missing", null, INSTANCE));
        assertNull(service.taskStatus(null, null, INSTANCE));
    }

    @Test
    void 历史分页返回条目与总数() {
        Page<PluginUpdateTask> page = new Page<>(1, 20);
        page.setRecords(List.of(activeTask("upd-1"), activeTask("upd-2")));
        page.setTotal(2);
        page.setPages(1);
        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Map<String, Object> result = service.history(CODE, INSTANCE, 1, 20);

        assertEquals(2, ((List<?>) result.get("list")).size());
        assertEquals(2L, result.get("total"));
        assertEquals(1, result.get("page"));
    }

    // ==================================================================
    // 测试数据与断言辅助
    // ==================================================================

    /**
     * 造一条「已安装 1.0.0」的安装记录，并在磁盘上放好旧 jar（回滚锚点）。
     *
     * @return 旧 jar 路径
     */
    private Path givenInstalledOldVersion() {
        try {
            Path oldJar = artifactStore.resolve(CODE, OLD_VERSION);
            Files.createDirectories(oldJar.getParent());
            Files.write(oldJar, "old-version-jar".getBytes(StandardCharsets.UTF_8));
            MallPluginInstall installed = new MallPluginInstall();
            installed.setPluginCode(CODE);
            installed.setVersion(OLD_VERSION);
            installed.setInstalled(1);
            installed.setEnabled(1);
            installed.setJarPath(oldJar.toString());
            when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(installed);
            return oldJar;
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void assertArrayEqualsContent(byte[] expected, Path actual, String message) throws Exception {
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected, Files.readAllBytes(actual), message);
    }

    /**
     * 断言任务最后一次落库的状态与错误码。
     *
     * @param state     期望状态
     * @param errorCode 期望错误码，成功为 null
     */
    private void assertTaskState(String state, String errorCode) {
        ArgumentCaptor<PluginUpdateTask> captor = ArgumentCaptor.forClass(PluginUpdateTask.class);
        verify(taskMapper, org.mockito.Mockito.atLeastOnce()).updateById(captor.capture());
        PluginUpdateTask last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(state, last.getState());
        assertEquals(errorCode, last.getErrorCode());
    }

    private static PluginUpdateTask activeTask(String taskId) {
        PluginUpdateTask task = new PluginUpdateTask();
        task.setTaskId(taskId);
        task.setInstanceId(INSTANCE);
        task.setPluginCode(CODE);
        task.setOpType(UpdateOpType.INSTALL.getCode());
        task.setToVersion(VERSION);
        task.setState(UpdateTaskState.RUNNING.getCode());
        task.setStep("DOWNLOAD");
        return task;
    }

    private static PluginIdentity identity() {
        return new PluginIdentity(INSTANCE, 100L);
    }

    private Ed25519ArtifactSigner signer() {
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        return new Ed25519ArtifactSigner(null, Map.of(KID, publicKey));
    }

    private String sign(String code, String version, String md5, String sha256) throws Exception {
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(keyPair.getPrivate());
        signature.update(ArtifactSignaturePayload.toSignBytes(
                ArtifactSignaturePayload.build(sha256, code, version, md5, jarBytes.length)));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private MarketPluginVersion publishedVersion(String md5, String sha256, String signature, String kid) {
        MarketPluginVersion row = new MarketPluginVersion();
        row.setPluginCode(CODE);
        row.setVersion(VERSION);
        row.setStatus(1);
        row.setJarMd5(md5);
        row.setJarSha256(sha256);
        row.setJarSignature(signature);
        row.setSignatureKid(kid);
        row.setPluginYaml("owner: official\nruntime: embedded\nminFrameworkVersion: 1.0.0\n");
        return row;
    }

    private DownloadUrlResult ticket(String md5, String sha256) {
        return new DownloadUrlResult("/base/plugin-center/artifact/raw?token=t", "t", "aid", CODE, VERSION,
                md5, sha256, jarBytes.length, CODE + "-" + VERSION + ".jar",
                System.currentTimeMillis() + 600_000, 5);
    }

    private RawArtifact raw(byte[] content) {
        ArtifactStream stream = new ArtifactStream(new ByteArrayInputStream(content),
                content.length, content.length, 0, content.length - 1, null);
        return new RawArtifact(stream, "aid", CODE, VERSION, md5(), sha256(), content.length);
    }

    private String md5() {
        return ArtifactSignaturePayload.md5Hex(jarBytes);
    }

    private String sha256() {
        return ArtifactSignaturePayload.sha256Hex(jarBytes);
    }
}
