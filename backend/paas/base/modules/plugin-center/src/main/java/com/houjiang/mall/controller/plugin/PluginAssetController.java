package com.houjiang.mall.controller.plugin;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.plugin.PluginAssetAccessService;
import com.houjiang.mall.service.plugin.PluginAssetException;
import com.houjiang.mall.service.plugin.PluginAssetResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

/**
 * 插件前端产物统一托管端点（T11）。
 *
 * <p>BFF 的 {@code /web/plugin-assets/{code}/{version}/{entry}} 泛化转发到这里，
 * 由宿主从「目录形态」或「市场安装落盘的 JAR 形态」读取产物并回传（解析与安全约束见
 * {@link PluginAssetResolver}，准入规则见 {@link PluginAssetAccessService}）。
 *
 * <p>响应约定：
 * <ul>
 *   <li>命中：200 + 正确 {@code Content-Type} + {@code Cache-Control: public, max-age=31536000, immutable}
 *       （URL 自带版本，可长期缓存）+ {@code X-Content-Type-Options: nosniff}；</li>
 *   <li>请求非法（穿越 / 后缀未白名单 / 格式不合法）：400；</li>
 *   <li>未登记 / 已卸载 / 产物不存在：404；已停用：423；读取失败：500。</li>
 * </ul>
 *
 * <p>该端点是**公开只读**的（浏览器的 {@code import()} 无法携带 token），
 * 已在 {@code SecurityConfig} 与 {@code MallAdminTokenAuthenticationFilter} 白名单中放行。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/plugin-assets")
public class PluginAssetController {

    private static final String PREFIX = "/plugin-assets";

    private final PluginAssetResolver resolver;

    private final PluginAssetAccessService accessService;

    public PluginAssetController(@Value("${houcloud.plugin.plugins-dir:plugins}") String pluginsDir,
                                 PluginAssetAccessService accessService) {
        this.resolver = new PluginAssetResolver(Path.of(pluginsDir));
        this.accessService = accessService;
    }

    /**
     * 读取插件前端产物。
     */
    @GetMapping("/{code}/{version}/**")
    public ResponseEntity<?> getAsset(@PathVariable String code,
                                      @PathVariable String version,
                                      HttpServletRequest request) {
        try {
            String safeCode = PluginAssetResolver.requireCode(code);
            String safeVersion = PluginAssetResolver.requireVersion(version);
            String entry = PluginAssetResolver.normalizeEntry(extractEntry(request, safeCode, safeVersion));

            PluginAssetAccessService.Access access = accessService.check(safeCode);
            if (access == PluginAssetAccessService.Access.NOT_REGISTERED) {
                return notFound("插件未登记: " + safeCode);
            }
            if (access == PluginAssetAccessService.Access.UNINSTALLED) {
                return notFound("插件已卸载: " + safeCode);
            }
            if (access == PluginAssetAccessService.Access.DISABLED) {
                return ResponseEntity.status(423).body(ResponseResult.fail(423, "插件未启用: " + safeCode));
            }

            PluginAssetResolver.PluginAsset asset = resolver.resolve(safeCode, safeVersion, entry);
            if (asset == null) {
                return notFound("插件产物不存在: " + entry);
            }
            log.debug("插件产物命中：{}/{}/{} -> {}", safeCode, safeVersion, entry, asset.source());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, asset.contentType())
                    .header(HttpHeaders.CACHE_CONTROL, asset.cacheControl())
                    .header("X-Content-Type-Options", "nosniff")
                    .body(asset.content());
        } catch (PluginAssetException e) {
            log.warn("插件产物请求被拒绝：code={}, version={}, uri={}, reason={}",
                    code, version, request.getRequestURI(), e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(ResponseResult.fail(e.getStatus(), e.getMessage()));
        } catch (Exception e) {
            log.error("插件产物读取失败：uri={}", request.getRequestURI(), e);
            return ResponseEntity.status(500).body(ResponseResult.fail(500, "插件产物读取失败"));
        }
    }

    private ResponseEntity<ResponseResult<?>> notFound(String message) {
        return ResponseEntity.status(404).body(ResponseResult.fail(404, message));
    }

    /**
     * 从请求 URI 中截取产物相对路径（{@code /plugin-assets/{code}/{version}/} 之后的部分）。
     */
    private String extractEntry(HttpServletRequest request, String code, String version) {
        String uri = request.getRequestURI();
        String prefix = PREFIX + "/" + code + "/" + version + "/";
        if (uri.startsWith(prefix)) {
            return uri.substring(prefix.length());
        }
        int idx = uri.indexOf(PREFIX + "/");
        String tail = idx >= 0 ? uri.substring(idx + PREFIX.length() + 1) : "";
        String[] segments = tail.split("/", 3);
        return segments.length >= 3 ? segments[2] : "";
    }
}
