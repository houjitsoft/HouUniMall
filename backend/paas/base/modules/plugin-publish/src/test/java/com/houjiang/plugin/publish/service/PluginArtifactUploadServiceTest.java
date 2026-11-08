package com.houjiang.plugin.publish.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.artifact.PluginArtifact;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.artifact.PluginArtifactMapper;
import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.local.LocalDiskArtifactStorage;
import com.houjiang.plugin.artifact.util.ArtifactDigests;
import com.houjiang.plugin.publish.config.PluginPublishProperties;
import com.houjiang.plugin.publish.exception.PluginPublishException;
import com.houjiang.plugin.publish.model.UploadCommand;
import com.houjiang.plugin.publish.model.UploadResult;
import com.houjiang.plugin.publish.validator.PluginManifestValidator;
import com.houjiang.plugin.signature.ArtifactSignaturePayload;
import com.houjiang.plugin.signature.Ed25519ArtifactSigner;
import com.houjiang.plugin.signature.Ed25519KeyGen;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link PluginArtifactUploadService} 单测。
 *
 * <p>组合策略：Mapper 全部 mock，存储与签名用真实实现
 * （存储 root 指向 {@code @TempDir}，签名密钥在测试内现生成），
 * 这样才能真正验证「原子落盘 / 服务端重算摘要 / 幂等 / 冲突拒绝」这些关键点。
 *
 * @author Amos
 * @since M1
 */
class PluginArtifactUploadServiceTest {

    private static final String CODE = "coupon-demo";

    private static final String VERSION = "1.0.0";

    private static final String KID = "k1";

    @TempDir
    Path tempDir;

    private PluginArtifactMapper artifactMapper;

    private MarketPluginVersionMapper versionMapper;

    private MarketPluginMapper pluginMapper;

    private LocalDiskArtifactStorage storage;

    private Ed25519ArtifactSigner signer;

    private PluginArtifactUploadService service;

    private Path storageRoot;

    /**
     * 捕获写入制品表的行。
     */
    private final List<PluginArtifact> insertedArtifacts = new ArrayList<>();

    /**
     * 捕获写入/更新版本表的行。
     */
    private final List<MarketPluginVersion> savedVersions = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        artifactMapper = mock(PluginArtifactMapper.class);
        versionMapper = mock(MarketPluginVersionMapper.class);
        pluginMapper = mock(MarketPluginMapper.class);

        storageRoot = tempDir.resolve("artifacts");
        ArtifactProperties artifactProperties = new ArtifactProperties();
        artifactProperties.setRoot(storageRoot.toString());
        storage = new LocalDiskArtifactStorage(artifactProperties);

        // 现生成一对 Ed25519 密钥，私钥落盘、公钥进配置，模拟真实部署形态
        KeyPair keyPair = Ed25519KeyGen.generate();
        Path keyDir = Files.createDirectories(tempDir.resolve("secrets"));
        Files.writeString(keyDir.resolve("artifact-ed25519-" + KID + ".key"),
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        Map<String, String> publicKeys = Map.of(KID,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        signer = new Ed25519ArtifactSigner(keyDir.resolve("artifact-ed25519-{kid}.key").toString(), publicKeys);

        artifactProperties.getSign().setEnabled(true);
        artifactProperties.getSign().setActiveKid(KID);

        service = new PluginArtifactUploadService(storage, signer, artifactProperties,
                new PluginPublishProperties(), artifactMapper, versionMapper, pluginMapper,
                new PluginManifestValidator());

        MarketPlugin plugin = new MarketPlugin();
        plugin.setId(9L);
        plugin.setPluginCode(CODE);
        plugin.setStatus(2);
        when(pluginMapper.selectOne(any())).thenReturn(plugin);
        when(artifactMapper.selectOne(any())).thenReturn(null);
        when(versionMapper.selectOne(any())).thenReturn(null);
        when(versionMapper.selectList(any())).thenReturn(List.of());
        when(artifactMapper.insert(any(PluginArtifact.class))).thenAnswer(invocation -> {
            PluginArtifact entity = invocation.getArgument(0);
            insertedArtifacts.add(entity);
            return 1;
        });
        when(versionMapper.insert(any(MarketPluginVersion.class))).thenAnswer(invocation -> {
            MarketPluginVersion entity = invocation.getArgument(0);
            entity.setId(1001L);
            savedVersions.add(entity);
            return 1;
        });
        when(versionMapper.updateById(any(MarketPluginVersion.class))).thenAnswer(invocation -> {
            savedVersions.add(invocation.getArgument(0));
            return 1;
        });
    }

    @Test
    @DisplayName("首次上传：落存储、服务端重算 md5/sha256/size、签名可验、版本落 draft")
    void uploadSucceeds() throws Exception {
        byte[] jar = jarWithManifest(manifest(CODE, VERSION, "1.0.0", "[]"));

        UploadResult result = service.upload(command("upload-1", jar));

        assertEquals(64, result.getArtifactId().length(), "artifact_id 应为完整 sha256（64 位十六进制）");
        assertEquals(ArtifactDigests.md5Hex(jar), result.getMd5(), "md5 必须由服务端重算");
        assertEquals(ArtifactDigests.sha256Hex(jar), result.getSha256(), "sha256 必须由服务端重算");
        assertEquals(jar.length, result.getSize());
        assertEquals(KID, result.getKid());
        assertFalse(result.isIdempotent());
        assertEquals(Long.valueOf(1001L), result.getVersionId());

        // 制品确实按 {code}/{version}/{code}-{version}.jar 落盘
        Path expected = storageRoot.resolve(CODE).resolve(VERSION).resolve(CODE + "-" + VERSION + ".jar");
        assertTrue(Files.isRegularFile(expected), "制品应落在 " + expected);
        assertEquals(jar.length, Files.size(expected));

        // 版本行：draft + upload_status=1 + 摘要/签名齐备
        MarketPluginVersion saved = savedVersions.get(0);
        assertEquals(0, saved.getStatus());
        assertEquals(1, saved.getUploadStatus());
        assertEquals(result.getMd5(), saved.getJarMd5());
        assertEquals(result.getSha256(), saved.getJarSha256());
        assertEquals(result.getArtifactId(),
                saved.getJarUrl().substring("/base/plugin-center/artifact/raw?aid=".length()));
        assertTrue(saved.getJarUrl().startsWith("/base/plugin-center/artifact/raw?aid="), "jar_url 只保留回源端点，不暴露裸存储地址");
        assertNotNull(saved.getJarSignature());

        // 签名必须能验过，且绑定了 sha256/code/version/md5/size
        String payload = ArtifactSignaturePayload.build(
                result.getSha256(), CODE, VERSION, result.getMd5(), result.getSize());
        assertTrue(signer.verify(KID, ArtifactSignaturePayload.toSignBytes(payload), saved.getJarSignature()));
    }

    @Test
    @DisplayName("同一 uploadId 重传：命中幂等，返回同一 artifactId，不重复写存储")
    void reUploadWithSameUploadIdIsIdempotent() {
        byte[] jar = jarWithManifest(manifest(CODE, VERSION, "1.0.0", "[]"));
        UploadResult first = service.upload(command("upload-same", jar));

        // 第二次：制品表已能查到该 uploadId
        PluginArtifact stored = insertedArtifacts.get(0);
        when(artifactMapper.selectOne(any())).thenReturn(stored);

        UploadResult second = service.upload(command("upload-same", jar));

        assertTrue(second.isIdempotent());
        assertEquals(first.getArtifactId(), second.getArtifactId());
        assertEquals(first.getMd5(), second.getMd5());
        assertEquals(1, insertedArtifacts.size(), "幂等命中时不应再插入制品行");
        assertEquals(1, countJarFiles(), "幂等命中时不应再写一份制品到存储");
    }

    @Test
    @DisplayName("已 published 的版本不可被同 code+version 覆盖 → 3012")
    void publishedVersionCannotBeOverwritten() {
        MarketPluginVersion published = new MarketPluginVersion();
        published.setId(55L);
        published.setPluginCode(CODE);
        published.setVersion(VERSION);
        published.setStatus(1);
        published.setUploadStatus(1);
        when(versionMapper.selectOne(any())).thenReturn(published);

        byte[] jar = jarWithManifest(manifest(CODE, VERSION, "1.0.0", "[]"));
        PluginPublishException e = assertThrows(PluginPublishException.class,
                () -> service.upload(command("upload-2", jar)));

        assertEquals(PluginDistErrorCode.VERSION_CONFLICT, e.getCode());
        assertTrue(insertedArtifacts.isEmpty());
        assertEquals(0, countJarFiles(), "拒绝时不应写存储");
    }

    @Test
    @DisplayName("清单校验失败：当场拒绝，不落存储、不改数据")
    void validationFailureWritesNothing() {
        byte[] jar = jarWithManifest(null);

        PluginPublishException e = assertThrows(PluginPublishException.class,
                () -> service.upload(command("upload-3", jar)));

        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, e.getCode());
        assertNotNull(e.getValidations(), "应带回字段级校验结果");
        assertTrue(insertedArtifacts.isEmpty());
        assertTrue(savedVersions.isEmpty());
        assertEquals(0, countJarFiles(), "校验失败时绝不能写存储");
    }

    @Test
    @DisplayName("已存在 draft 且上传完成 → 幂等返回已有 versionId")
    void draftCompletedIsIdempotent() {
        MarketPluginVersion draft = new MarketPluginVersion();
        draft.setId(77L);
        draft.setPluginCode(CODE);
        draft.setVersion(VERSION);
        draft.setStatus(0);
        draft.setUploadStatus(1);
        draft.setJarMd5("existing-md5");
        draft.setJarSize(123L);
        when(versionMapper.selectOne(any())).thenReturn(draft);

        byte[] jar = jarWithManifest(manifest(CODE, VERSION, "1.0.0", "[]"));
        UploadResult result = service.upload(command("upload-4", jar));

        assertTrue(result.isIdempotent());
        assertEquals(Long.valueOf(77L), result.getVersionId());
        assertEquals(0, countJarFiles());
    }

    @Test
    @DisplayName("制品已在存储中存在（不同 uploadId）→ 存储层拒绝覆盖，不静默成功")
    void storageConflictPropagates() throws Exception {
        // 先用另一个 uploadId 上传成功，再把制品表查询置空，模拟「DB 无记录但存储已有文件」
        byte[] jar = jarWithManifest(manifest(CODE, VERSION, "1.0.0", "[]"));
        service.upload(command("upload-5", jar));
        when(artifactMapper.selectOne(any())).thenReturn(null);
        when(versionMapper.selectOne(any())).thenReturn(null);

        byte[] sameVersion = jarWithManifest(manifest(CODE, VERSION, "1.0.0", "[]"));
        PluginPublishException e = assertThrows(PluginPublishException.class,
                () -> service.upload(command("upload-6", sameVersion)));
        assertEquals(PluginDistErrorCode.VERSION_CONFLICT, e.getCode());
    }

    private UploadCommand command(String uploadId, byte[] jar) {
        UploadCommand command = new UploadCommand();
        command.setPluginCode(CODE);
        command.setVersion(VERSION);
        command.setUploadId(uploadId);
        command.setJarStream(new ByteArrayInputStream(jar));
        command.setChangelog("首个版本");
        command.setCreatedBy(1L);
        return command;
    }

    private long countJarFiles() {
        if (!Files.isDirectory(storageRoot)) {
            return 0L;
        }
        try (Stream<Path> paths = Files.walk(storageRoot)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".jar")).count();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String manifest(String code, String version, String minFramework, String dependencies) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("code: ").append(code).append('\n');
        yaml.append("version: ").append(version).append('\n');
        yaml.append("minFrameworkVersion: '").append(minFramework).append("'\n");
        if (dependencies != null) {
            yaml.append("dependencies: ").append(dependencies).append('\n');
        }
        return yaml.toString();
    }

    private static byte[] jarWithManifest(String yaml) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (JarOutputStream jos = new JarOutputStream(buffer)) {
            if (yaml != null) {
                jos.putNextEntry(new JarEntry("plugin.yaml"));
                jos.write(yaml.getBytes(StandardCharsets.UTF_8));
                jos.closeEntry();
            }
            jos.putNextEntry(new JarEntry("com/demo/Main.class"));
            jos.write(new byte[] {1, 2, 3, 4});
            jos.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException("构造测试 jar 失败", e);
        }
        return buffer.toByteArray();
    }
}
