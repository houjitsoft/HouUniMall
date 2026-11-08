package com.houjiang.plugin.artifact.download;

import com.houjiang.mall.service.market.ArtifactUrlResolver;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.identity.PluginIdentityResolver;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * {@link ArtifactUrlResolver} 的实现：用与
 * {@code POST /base/plugin-center/artifact/download-url} 完全相同的签发逻辑换票。
 *
 * <p>身份取当前登录态（{@link PluginIdentityResolver#fromCurrentContext()}），
 * 不接受调用方传入的 instanceId —— 否则任何人都能借别人的站点下载付费制品。
 * 换到的是「10 分钟 + 限次 + 绑定本站点」的令牌地址，不会绕过授权判定。
 *
 * @author Amos
 * @since 2026-10-27
 */
@Service
public class ArtifactUrlResolverImpl implements ArtifactUrlResolver {

    private final PluginArtifactDownloadService downloadService;

    public ArtifactUrlResolverImpl(PluginArtifactDownloadService downloadService) {
        this.downloadService = Objects.requireNonNull(downloadService, "PluginArtifactDownloadService 不能为空");
    }

    @Override
    public String resolveTokenizedUrl(String pluginCode, String version) {
        PluginIdentity identity = PluginIdentityResolver.fromCurrentContext();
        if (identity == null || !identity.hasInstance()) {
            throw new IllegalStateException("未识别当前站点实例，无法换取安装包下载令牌: "
                    + pluginCode + "@" + version);
        }
        DownloadUrlResult issued = downloadService.issueDownloadUrl(pluginCode, version, "jar", identity, null, null);
        return issued.getUrl();
    }
}
