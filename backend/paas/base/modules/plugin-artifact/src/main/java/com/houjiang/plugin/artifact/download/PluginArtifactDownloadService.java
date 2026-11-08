package com.houjiang.plugin.artifact.download;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.artifact.PluginArtifact;
import com.houjiang.mall.mapper.artifact.PluginArtifactMapper;
import com.houjiang.plugin.artifact.authorization.DownloadDecision;
import com.houjiang.plugin.artifact.authorization.PluginDownloadAuthorizationService;
import com.houjiang.plugin.artifact.exception.ArtifactRangeNotSatisfiableException;
import com.houjiang.plugin.artifact.exception.DownloadDeniedException;
import com.houjiang.plugin.artifact.exception.DownloadTokenException;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.key.ArtifactKeys;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import com.houjiang.plugin.artifact.spi.ArtifactStorage;
import com.houjiang.plugin.artifact.token.DownloadTokenPayload;
import com.houjiang.plugin.artifact.token.DownloadTokenService;
import com.houjiang.plugin.artifact.token.IssueTokenCommand;
import com.houjiang.plugin.artifact.util.HttpRanges;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 制品下载链路：换票（download-url）与回源（raw）。
 *
 * <p><b>为什么要「先换票再下载」两段式</b>：
 * 制品的真实地址如果直接暴露，任何人拿到链接就能无限次下载，且绕过了授权与限次。
 * 两段式把「授权判定」放在换票这一刻（有登录态、有站点身份），
 * 换到的令牌只绑定「一个制品 + 一个站点 + 10 分钟 + 5 次命中」，
 * 回源端点因此可以做进鉴权白名单（无需会话），由令牌自证身份。
 *
 * @author Amos
 * @since M2
 */
@Slf4j
@Service
public class PluginArtifactDownloadService {

    /**
     * 回源端点路径（令牌挂在 query 上）。
     */
    private static final String RAW_ENDPOINT = "/base/plugin-center/artifact/raw?token=";

    /**
     * 制品上传完成状态（只有完成的制品才允许下载）。
     */
    private static final int UPLOAD_STATUS_DONE = 1;

    private final PluginDownloadAuthorizationService authorizationService;

    private final DownloadTokenService tokenService;

    private final PluginArtifactMapper artifactMapper;

    private final ArtifactStorage artifactStorage;
    private com.houjiang.plugin.artifact.delivery.CommercialReleaseService commercial;
    @org.springframework.beans.factory.annotation.Autowired
    public void setCommercial(com.houjiang.plugin.artifact.delivery.CommercialReleaseService commercial) {
        this.commercial = commercial;
    }


    /**
     * 构造下载服务。
     *
     * @param authorizationService 授权门控
     * @param tokenService         令牌服务
     * @param artifactMapper       制品 Mapper
     * @param artifactStorage      制品存储 SPI
     */
    public PluginArtifactDownloadService(PluginDownloadAuthorizationService authorizationService,
                                         DownloadTokenService tokenService,
                                         PluginArtifactMapper artifactMapper,
                                         ArtifactStorage artifactStorage) {
        this.authorizationService = Objects.requireNonNull(authorizationService, "授权服务不能为空");
        this.tokenService = Objects.requireNonNull(tokenService, "令牌服务不能为空");
        this.artifactMapper = Objects.requireNonNull(artifactMapper, "PluginArtifactMapper 不能为空");
        this.artifactStorage = Objects.requireNonNull(artifactStorage, "ArtifactStorage 不能为空");
    }

    /**
     * 授权判定 → 定位制品 → 签发有时效、限次、绑租户的下载令牌。
     *
     * @param pluginCode 插件编码
     * @param version    版本
     * @param identity   服务端解析的调用方身份
     * @param clientIp   客户端 IP（留痕溯源）
     * @param userAgent  客户端 UA（留痕溯源）
     * @return 下载链接与制品指纹
     * @throws DownloadDeniedException 未授权（3002/3003/3004/3005/3006/3009）
     * @throws IllegalStateException   未配置下载令牌密钥
     */
    public DownloadUrlResult issueDownloadUrl(String pluginCode,
                                              String version,
                                              PluginIdentity identity,
                                              String clientIp,
                                              String userAgent) {
        return issueDownloadUrl(pluginCode, version, "jar", identity, clientIp, userAgent);
    }

    public DownloadUrlResult issueDownloadUrl(String pluginCode, String version, String artifactKind,
                                              PluginIdentity identity, String clientIp, String userAgent) {
        String code = pluginCode == null ? "" : pluginCode.trim();
        String ver = version == null ? "" : version.trim();
        String requestedKind = artifactKind == null ? "jar" : artifactKind.trim();
        if (!java.util.Set.of("jar", "source", "oci").contains(requestedKind))
            throw new DownloadDeniedException(400, "不支持的制品类型");
        com.houjiang.plugin.artifact.delivery.CommercialReleaseService.Release release = null;
        if (!"jar".equals(requestedKind)) {
            if (commercial == null) throw new DownloadDeniedException(503, "商业交付服务不可用");
            release = commercial.authorize(code, ver, requestedKind, identity);
        } else if (com.houjiang.plugin.artifact.delivery.CommercialReleaseService.PAID_SERVICES.contains(code)) {
            throw new DownloadDeniedException(3013, "独立服务不提供可装入其他宿主的 JAR 制品");
        }
        if(release==null) {
            DownloadDecision decision=authorizationService.authorize(code,ver,identity);
            if(!decision.isAllowed())throw new DownloadDeniedException(decision.getCode(),decision.getMessage());
        }
        PluginArtifact artifact = artifactMapper.selectOne(new LambdaQueryWrapper<PluginArtifact>()
                .eq(PluginArtifact::getPluginCode, code)
                .eq(PluginArtifact::getVersion, ver)
                .eq(PluginArtifact::getUploadStatus, UPLOAD_STATUS_DONE)
                .eq(PluginArtifact::getArtifactKind,requestedKind)
                .orderByDesc(PluginArtifact::getId)
                .last("limit 1"));
        if (artifact == null) {
            throw new DownloadDeniedException(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "制品不存在或尚未上传完成：" + code + " " + ver);
        }

        if (release != null) {
            String prefix = "source".equals(requestedKind) ? "source" : "runtime";
            if (!code.equals(artifact.getHostService())
                    || !release.manifest().path(prefix + "Sha256").asText().equals(artifact.getSha256())
                    || artifact.getSizeBytes() == null
                    || release.manifest().path(prefix + "Size").asLong() != artifact.getSizeBytes())
                throw new DownloadDeniedException(3010, "制品与官方发行清单不一致");
        }
        DownloadTokenService.IssuedToken issued = tokenService.issue(new IssueTokenCommand(
                artifact.getArtifactId(),
                code,
                ver,
                identity == null ? "" : identity.getInstanceId(),
                identity == null ? null : identity.getUserId(),
                clientIp,
                userAgent));

        String url = RAW_ENDPOINT + URLEncoder.encode(issued.getToken(), StandardCharsets.UTF_8);
        String kind = artifact.getArtifactKind() == null ? "jar" : artifact.getArtifactKind();
        String suffix = "source".equals(kind) ? "-source.tar.gz" : "oci".equals(kind) ? "-runtime.tar" : ".jar";
        String storageKey = StringUtils.hasText(artifact.getStorageKey())
                ? artifact.getStorageKey()
                : ArtifactKeys.jarKey(code, ver);
        String cloudUrl = null;
        if ("jar".equals(requestedKind)) {
            try {
                // JAR 保留预签名直链；商业 source/oci 必须经 raw 完成传输回执。
                cloudUrl = artifactStorage.signedInternalUrl(storageKey, java.time.Duration.ofMinutes(10));
            } catch (UnsupportedOperationException ignored) {
                // 本地磁盘：仅 raw 回源
            } catch (RuntimeException e) {
                log.warn("签发对象存储预签名失败，降级为 raw 回源：key={}, err={}", storageKey, e.getMessage());
            }
        }
        return new DownloadUrlResult(url, issued.getToken(), artifact.getArtifactId(),
                code, ver, artifact.getMd5(), artifact.getSha256(),
                artifact.getSizeBytes() == null ? 0L : artifact.getSizeBytes(),
                code + "-" + ver + suffix, issued.getExpiresAtMillis(), issued.getMaxHits(), kind, cloudUrl);
    }

    /**
     * 校验令牌并打开制品流（支持 Range）。
     *
     * <p>回源时<b>不再重复做授权判定</b>：令牌签发时已经bind 了制品与站点，
     * 重复查授权只会让已付费用户在 10 分钟内被「授权刚好过期」打回，体验很差；
     * 安全性由「令牌 10 分钟 + 限次 + 租户绑定」保证，而不是靠每次都查库。
     *
     * @param token       下载令牌
     * @param rangeHeader HTTP Range 头，可为 null
     * @return 制品流与元信息
     * @throws DownloadTokenException                令牌无效 / 过期 / 次数用尽
     * @throws DownloadDeniedException               制品不存在（3009）
     * @throws ArtifactRangeNotSatisfiableException  Range 无法满足（416）
     * @throws IOException                           打开制品失败
     */
    public RawArtifact openByToken(String token, String rangeHeader) throws IOException {
        DownloadTokenPayload payload = tokenService.verify(token, null);
        PluginArtifact artifact = artifactMapper.selectOne(new LambdaQueryWrapper<PluginArtifact>()
                .eq(PluginArtifact::getArtifactId, payload.getArtifactId())
                .eq(PluginArtifact::getPluginCode,payload.getPluginCode())
                .eq(PluginArtifact::getVersion,payload.getVersion())
                .last("limit 1"));
        if (artifact == null) {
            throw new DownloadDeniedException(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "制品不存在：" + payload.getArtifactId());
        }
        String key = StringUtils.hasText(artifact.getStorageKey())
                ? artifact.getStorageKey()
                : ArtifactKeys.jarKey(artifact.getPluginCode(), artifact.getVersion());

        ArtifactStream stream = artifactStorage.open(key, null, null);
        long totalSize = stream.getTotalSize();
        if (StringUtils.hasText(rangeHeader)) {
            // 格式非法的 Range 按 RFC 7233「可以忽略」处理，退化为全量下载；
            // 只有「格式合法但超出制品长度」才是真正的 416 Unsatisfiable。
            HttpRanges.RangeSpec spec = null;
            try {
                spec = HttpRanges.parse(rangeHeader);
            } catch (IllegalArgumentException e) {
                log.warn("Range 头非法，退化为全量下载: range={}, err={}", rangeHeader, e.getMessage());
            }
            if (spec != null && !spec.satisfiable(totalSize)) {
                closeQuietly(stream);
                throw new ArtifactRangeNotSatisfiableException(
                        "Range 区间超出制品长度：total=" + totalSize + "，range=" + rangeHeader, totalSize);
            }
            if (spec != null) {
                long start = spec.resolveStart(totalSize);
                long end = spec.resolveEnd(totalSize);
                closeQuietly(stream);
                stream = artifactStorage.open(key, start, end);
            }
        }
        String kind = artifact.getArtifactKind() == null ? "jar" : artifact.getArtifactKind();
        String suffix = "source".equals(kind) ? "-source.tar.gz" : "oci".equals(kind) ? "-runtime.tar" : ".jar";
        ArtifactStream sent=stream;
        Runnable receipt=(!"jar".equals(kind) && commercial!=null) ? () -> commercial.transferred(payload.getTenantId(),payload.getUserId(),
                artifact.getPluginCode(),artifact.getVersion(),kind,artifact.getSha256(),sent.getTotalSize(),sent.getRangeStart(),sent.getRangeEnd()) : () -> {};
        return new RawArtifact(stream, artifact.getArtifactId(), artifact.getPluginCode(),
                artifact.getVersion(), artifact.getMd5(), artifact.getSha256(),
                artifact.getSizeBytes() == null ? 0L : artifact.getSizeBytes(),
                artifact.getPluginCode() + "-" + artifact.getVersion() + suffix,receipt);
    }

    private static void closeQuietly(ArtifactStream stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
            // 关闭失败不影响主流程：底层是文件句柄，最坏情况是等到 GC 回收
        }
    }
}
