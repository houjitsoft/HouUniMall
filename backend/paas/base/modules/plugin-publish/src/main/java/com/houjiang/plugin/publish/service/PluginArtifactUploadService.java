package com.houjiang.plugin.publish.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.artifact.PluginArtifact;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.artifact.PluginArtifactMapper;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.key.ArtifactKeys;
import com.houjiang.plugin.artifact.model.StoredObject;
import com.houjiang.plugin.artifact.spi.ArtifactStorage;
import com.houjiang.commons.version.PluginVersionComparator;
import com.houjiang.plugin.publish.config.PluginPublishProperties;
import com.houjiang.plugin.publish.exception.PluginPublishException;
import com.houjiang.plugin.publish.manifest.PluginManifestReader;
import com.houjiang.plugin.publish.model.PluginManifest;
import com.houjiang.plugin.publish.model.UploadCommand;
import com.houjiang.plugin.publish.model.UploadResult;
import com.houjiang.plugin.publish.model.ValidationResult;
import com.houjiang.plugin.publish.state.PluginProductStatus;
import com.houjiang.plugin.publish.state.PluginVersionStateMachine;
import com.houjiang.plugin.publish.state.PluginVersionStatus;
import com.houjiang.plugin.publish.util.ArtifactIds;
import com.houjiang.plugin.publish.validator.PluginManifestValidator;
import com.houjiang.plugin.publish.validator.ValidationContext;
import com.houjiang.plugin.signature.ArtifactSignaturePayload;
import com.houjiang.plugin.signature.ArtifactSigner;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * 制品上传服务（方案 §5.1「发布与审核上架」时序图的上传段）。
 *
 * <p>执行顺序（刻意与时序图一致）：
 * <ol>
 *   <li>{@code uploadId} 命中 {@code uk_upload_id} → 直接返回首次结果（<b>幂等，不是 500</b>）；</li>
 *   <li>同 {@code code+version} 已 published/offline/pending/rejected → 3012（同版本不接受新制品）；
 *       已 draft 且上传完成 → 返回已有 versionId（幂等）；</li>
 *   <li>清单校验 → 不通过<b>当场拒绝，不落存储、不改数据</b>；</li>
 *   <li>{@code ArtifactStorage.putIfAbsent} 落存储（先 .tmp 后原子 move，绝不覆盖）；</li>
 *   <li>服务端<b>重算</b> md5 + sha256 + size（不信任客户端传值）；</li>
 *   <li>签名（payload 为 {@code sha256|code|version|md5|size}）；</li>
 *   <li>写 {@code hou_plugin_artifact} + upsert {@code hou_market_plugin_version}(status=draft)。</li>
 * </ol>
 *
 * @author Amos
 * @since M1
 */
@Slf4j
@Service
public class PluginArtifactUploadService {

    /**
     * 上传完成状态值（hou_plugin_artifact.upload_status / 版本表 upload_status）。
     */
    private static final int UPLOAD_STATUS_DONE = 1;

    /**
     * 制品存储 SPI。
     */
    private final ArtifactStorage artifactStorage;

    /**
     * 制品签名器。
     */
    private final ArtifactSigner artifactSigner;

    /**
     * 制品存储配置（签名开关与生效 kid）。
     */
    private final ArtifactProperties artifactProperties;

    /**
     * 发布侧配置（体积上限）。
     */
    private final PluginPublishProperties publishProperties;

    /**
     * 制品表 Mapper。
     */
    private final PluginArtifactMapper artifactMapper;

    /**
     * 版本表 Mapper。
     */
    private final MarketPluginVersionMapper versionMapper;

    /**
     * 商品表 Mapper。
     */
    private final MarketPluginMapper pluginMapper;

    /**
     * 清单校验器。
     */
    private final PluginManifestValidator validator;

    /**
     * 构造上传服务。
     *
     * @param artifactStorage   制品存储
     * @param artifactSigner    制品签名器
     * @param artifactProperties 制品配置
     * @param publishProperties 发布配置
     * @param artifactMapper    制品 Mapper
     * @param versionMapper     版本 Mapper
     * @param pluginMapper      商品 Mapper
     * @param validator         清单校验器
     */
    public PluginArtifactUploadService(ArtifactStorage artifactStorage,
                                       ArtifactSigner artifactSigner,
                                       ArtifactProperties artifactProperties,
                                       PluginPublishProperties publishProperties,
                                       PluginArtifactMapper artifactMapper,
                                       MarketPluginVersionMapper versionMapper,
                                       MarketPluginMapper pluginMapper,
                                       PluginManifestValidator validator) {
        this.artifactStorage = Objects.requireNonNull(artifactStorage, "artifactStorage 不能为空");
        this.artifactSigner = Objects.requireNonNull(artifactSigner, "artifactSigner 不能为空");
        this.artifactProperties = Objects.requireNonNull(artifactProperties, "artifactProperties 不能为空");
        this.publishProperties = Objects.requireNonNull(publishProperties, "publishProperties 不能为空");
        this.artifactMapper = Objects.requireNonNull(artifactMapper, "artifactMapper 不能为空");
        this.versionMapper = Objects.requireNonNull(versionMapper, "versionMapper 不能为空");
        this.pluginMapper = Objects.requireNonNull(pluginMapper, "pluginMapper 不能为空");
        this.validator = Objects.requireNonNull(validator, "validator 不能为空");
    }

    /**
     * 执行上传。
     *
     * @param command 上传命令
     * @return 上传结果
     * @throws PluginPublishException 业务拒绝（3012/3013/3015/3017 等）
     */
    public UploadResult upload(UploadCommand command) {
        Objects.requireNonNull(command, "上传命令不能为空");
        String pluginCode = requireText(command.getPluginCode(), "pluginCode 不能为空");
        String version = requireText(command.getVersion(), "version 不能为空");
        String uploadId = requireText(command.getUploadId(), "uploadId 不能为空");

        // ① uploadId 幂等：重传直接返回首次结果
        PluginArtifact existed = findByUploadId(uploadId);
        if (existed != null) {
            log.info("制品上传命中幂等 uploadId={}, artifactId={}", uploadId, existed.getArtifactId());
            UploadResult result = new UploadResult();
            result.setArtifactId(existed.getArtifactId());
            result.setMd5(existed.getMd5());
            result.setSha256(existed.getSha256());
            result.setSize(existed.getSizeBytes());
            result.setVersionId(resolveVersionId(pluginCode, version));
            result.setIdempotent(true);
            return result;
        }

        // 商品必须先在市场登记，否则版本会变成孤儿行
        MarketPlugin plugin = findPlugin(pluginCode);
        if (plugin == null) {
            throw new PluginPublishException(PluginDistErrorCode.MANIFEST_INVALID,
                    "插件编码 " + pluginCode + " 尚未在插件市场登记，请先创建商品");
        }

        // ② 同 code+version 互斥 / 幂等判定
        MarketPluginVersion versionRow = findVersion(pluginCode, version);
        if (versionRow != null) {
            PluginVersionStatus status = PluginVersionStatus.fromCode(versionRow.getStatus());
            if (!PluginVersionStateMachine.allowsArtifactOverwrite(status)) {
                throw new PluginPublishException(PluginDistErrorCode.VERSION_CONFLICT,
                        "版本 " + pluginCode + "@" + version + " 已存在且状态为「"
                                + status.getDescription() + "」，同 code+version 不接受新制品");
            }
            if (Objects.equals(Integer.valueOf(UPLOAD_STATUS_DONE), versionRow.getUploadStatus())) {
                // draft 且已完成 → 幂等返回
                UploadResult result = new UploadResult();
                result.setVersionId(versionRow.getId());
                result.setMd5(versionRow.getJarMd5());
                result.setSha256(versionRow.getJarSha256());
                result.setSize(versionRow.getJarSize() == null ? 0L : versionRow.getJarSize());
                result.setKid(versionRow.getSignatureKid());
                result.setIdempotent(true);
                return result;
            }
            // draft 但 upload_status != 1：上次上传中断留下的僵尸行，允许本次覆盖重写
            log.warn("版本 {}@{} 存在未完成的 draft 记录（upload_status={}），本次上传将覆盖重写",
                    pluginCode, version, versionRow.getUploadStatus());
        }

        Path tempFile = createTempFile();
        try {
            long declaredSize = drainToTempFile(command.getJarStream(), tempFile);

            // ③ 清单校验（不通过当场拒绝，此时还没写存储、没改数据）
            PluginManifest manifest = readManifest(tempFile);
            ValidationContext context = new ValidationContext(pluginCode, version, declaredSize,
                    maxExistingVersion(pluginCode, version), publishProperties.getMaxArtifactBytes());
            context.setDependencyPublished(this::isDependencyPublished);
            ValidationResult validation = validator.validate(manifest, declaredSize, context);
            if (validation.hasError()) {
                throw new PluginPublishException(validation.firstErrorCode(),
                        "清单校验未通过：" + firstMessage(validation), validation);
            }

            // ④ 落存储（内部重算 md5/sha256/size，并与 declaredSize 对账）
            String storageKey = ArtifactKeys.jarKey(pluginCode, version);
            StoredObject stored;
            try (InputStream in = Files.newInputStream(tempFile, StandardOpenOption.READ)) {
                stored = artifactStorage.putIfAbsent(storageKey, in, declaredSize);
            } catch (com.houjiang.plugin.artifact.exception.ArtifactStorageException e) {
                // 存储层冲突（如「DB 无记录但存储已有文件」）统一转成发布侧异常，保留原错误码
                throw new PluginPublishException(e.getCode(), e.getMessage());
            }

            // ⑤ 签名
            String kid = null;
            String signature = null;
            if (artifactProperties.getSign().isEnabled()) {
                kid = artifactProperties.getSign().getActiveKid();
                String payload = ArtifactSignaturePayload.build(
                        stored.getSha256(), pluginCode, version, stored.getMd5(), stored.getSizeBytes());
                signature = artifactSigner.sign(kid, ArtifactSignaturePayload.toSignBytes(payload));
            }

            // ⑥ 落库
            String artifactId = ArtifactIds.generate(pluginCode, version, stored.getMd5());
            PluginArtifact artifact = buildArtifact(artifactId, pluginCode, version, stored, uploadId,
                    command.getCreatedBy(), manifest);
            artifactMapper.insert(artifact);

            Long versionId = upsertVersion(pluginCode, version, versionRow, artifactId, stored,
                    signature, kid, manifest, command.getChangelog());

            UploadResult result = new UploadResult();
            result.setArtifactId(artifactId);
            result.setVersionId(versionId);
            result.setMd5(stored.getMd5());
            result.setSha256(stored.getSha256());
            result.setSize(stored.getSizeBytes());
            result.setKid(kid);
            result.setValidations(validation.getIssues());
            result.setIdempotent(false);
            return result;
        } catch (IOException e) {
            throw new PluginPublishException(PluginDistErrorCode.MANIFEST_INVALID,
                    "制品落盘失败：" + e.getMessage());
        } finally {
            deleteQuietly(tempFile);
        }
    }

    private PluginArtifact buildArtifact(String artifactId,
                                         String pluginCode,
                                         String version,
                                         StoredObject stored,
                                         String uploadId,
                                         Long createdBy,
                                         PluginManifest manifest) {
        PluginArtifact artifact = new PluginArtifact();
        artifact.setArtifactId(artifactId);
        artifact.setPluginCode(pluginCode);
        artifact.setVersion(version);
        artifact.setStorageType(stored.getStorageType());
        artifact.setStorageKey(stored.getKey());
        artifact.setMd5(stored.getMd5());
        artifact.setSha256(stored.getSha256());
        artifact.setSizeBytes(stored.getSizeBytes());
        artifact.setUploadStatus(UPLOAD_STATUS_DONE);
        artifact.setUploadId(uploadId);
        artifact.setCreatedBy(createdBy);
        artifact.setCreateTime(LocalDateTime.now());
        // 平台 remote/acquire 直接读这列派发宿主；漏写会导致站点默认装到 base。
        if (manifest != null && StringUtils.hasText(manifest.getRawYaml())) {
            for (String line : manifest.getRawYaml().split("\\R")) {
                String trimmed = line == null ? "" : line.trim();
                if (trimmed.startsWith("hostService:")) {
                    String host = trimmed.substring("hostService:".length()).trim();
                    if (StringUtils.hasText(host)) {
                        artifact.setHostService(host);
                    }
                    break;
                }
            }
        }
        return artifact;
    }

    private Long upsertVersion(String pluginCode,
                               String version,
                               MarketPluginVersion existing,
                               String artifactId,
                               StoredObject stored,
                               String signature,
                               String kid,
                               PluginManifest manifest,
                               String changelog) {
        MarketPluginVersion row = existing == null ? new MarketPluginVersion() : existing;
        row.setPluginCode(pluginCode);
        row.setVersion(version);
        // jar_url 只保留「未来回源端点」的相对路径，不暴露裸存储地址；
        // 真正的下载必须经 /download-url 校验授权后签发（方案 §4.3）。
        row.setJarUrl("/base/plugin-center/artifact/raw?aid=" + artifactId);
        row.setJarMd5(stored.getMd5());
        row.setJarSha256(stored.getSha256());
        row.setJarSize(stored.getSizeBytes());
        row.setJarSignature(signature);
        row.setSignatureKid(kid);
        row.setStorageType(stored.getStorageType());
        row.setStorageKey(stored.getKey());
        row.setUploadStatus(UPLOAD_STATUS_DONE);
        row.setManifestSha256(manifest == null ? null : manifest.getManifestSha256());
        row.setPluginYaml(manifest == null ? null : manifest.getRawYaml());
        if (changelog != null && !changelog.isBlank()) {
            row.setChangelog(changelog);
        }
        if (row.getStatus() == null) {
            row.setStatus(PluginVersionStatus.DRAFT.getCode());
        }
        if (row.getId() == null) {
            row.setCreateTime(LocalDateTime.now());
            versionMapper.insert(row);
        } else {
            versionMapper.updateById(row);
        }
        return row.getId();
    }

    private PluginManifest readManifest(Path jarFile) {
        try {
            return PluginManifestReader.read(jarFile);
        } catch (IOException | YAMLException e) {
            // 读不出来交给校验器产出 3013，这里不提前抛异常，保证返回结构一致
            log.warn("读取 plugin.yaml 失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 统计该插件的「历史最大版本」。
     *
     * <p><b>刻意排除本次上传的版本号本身</b>：同 code+version 的重复上传由上面的
     * 「状态互斥 / 幂等」分支判定（3012 或幂等返回），
     * 这里再把它算进 max 会让「上次传一半中断的 draft」永远无法通过校验、再也传不上来。
     *
     * @param pluginCode 插件编码
     * @param selfVersion 本次上传的版本号（会被排除）
     * @return 历史最大版本；没有历史版本时返回 null
     */
    private String maxExistingVersion(String pluginCode, String selfVersion) {
        List<MarketPluginVersion> versions = versionMapper.selectList(
                new LambdaQueryWrapper<MarketPluginVersion>()
                        .eq(MarketPluginVersion::getPluginCode, pluginCode));
        String max = null;
        if (versions == null) {
            return null;
        }
        for (MarketPluginVersion item : versions) {
            String candidate = item.getVersion();
            if (candidate == null || !PluginVersionComparator.isValid(candidate)) {
                continue;
            }
            if (candidate.equals(selfVersion)) {
                continue;
            }
            if (max == null || PluginVersionComparator.compare(candidate, max) > 0) {
                max = candidate;
            }
        }
        return max;
    }

    /**
     * 判断被依赖插件是否已在市场存在且上架（status=2）。
     *
     * @param pluginCode 被依赖插件编码
     * @return true 表示已上架
     */
    private boolean isDependencyPublished(String pluginCode) {
        MarketPlugin dependency = findPlugin(pluginCode);
        return dependency != null
                && Integer.valueOf(PluginProductStatus.PUBLISHED.getCode()).equals(dependency.getStatus());
    }

    private MarketPlugin findPlugin(String pluginCode) {
        return pluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, pluginCode)
                .last("LIMIT 1"));
    }

    private MarketPluginVersion findVersion(String pluginCode, String version) {
        return versionMapper.selectOne(new LambdaQueryWrapper<MarketPluginVersion>()
                .eq(MarketPluginVersion::getPluginCode, pluginCode)
                .eq(MarketPluginVersion::getVersion, version)
                .last("LIMIT 1"));
    }

    private PluginArtifact findByUploadId(String uploadId) {
        return artifactMapper.selectOne(new LambdaQueryWrapper<PluginArtifact>()
                .eq(PluginArtifact::getUploadId, uploadId)
                .last("LIMIT 1"));
    }

    private Long resolveVersionId(String pluginCode, String version) {
        MarketPluginVersion row = findVersion(pluginCode, version);
        return row == null ? null : row.getId();
    }

    private static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new PluginPublishException(PluginDistErrorCode.MANIFEST_INVALID, message);
        }
        return value.trim();
    }

    private static String firstMessage(ValidationResult validation) {
        return validation.getIssues().stream()
                .filter(com.houjiang.plugin.publish.model.ValidationIssue::isError)
                .map(com.houjiang.plugin.publish.model.ValidationIssue::getMsg)
                .findFirst()
                .orElse("未知校验错误");
    }

    private static long drainToTempFile(InputStream in, Path tempFile) throws IOException {
        Objects.requireNonNull(in, "制品输入流不能为空");
        long total = 0L;
        byte[] buffer = new byte[64 * 1024];
        try (OutputStream out = Files.newOutputStream(tempFile,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
                total += read;
            }
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
                // 输入流关闭失败不影响主流程
            }
        }
        return total;
    }

    private static Path createTempFile() {
        try {
            return Files.createTempFile("houcloud-artifact-", ".jar");
        } catch (IOException e) {
            throw new PluginPublishException(PluginDistErrorCode.MANIFEST_INVALID,
                    "创建制品临时文件失败：" + e.getMessage());
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 临时文件清理失败不阻断上传
        }
    }
}
