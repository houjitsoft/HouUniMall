package com.houjiang.goods.plugin;

import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import com.houjiang.plugin.engine.artifact.PluginChecksumVerifier;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import com.houjiang.plugin.engine.core.PluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * goods 宿主侧安装端点：接收 base（插件市场）派发过来的制品并完成本进程的装载。
 *
 * <p><b>为什么必须有这个端点</b>：嵌入式插件由「声明 hostService 的那个宿主进程」的 PF4J 引擎装载，
 * base 自己的引擎只能装 hostService=base 的插件。goods 域的嵌入式插件声明的宿主是 goods，安装动作必须发生在 goods 进程内；base 若直接落盘到自己的 plugins 目录，产物既不会被
 * order 扫描到，也破坏了目录契约。
 *
 * <p>落盘契约由 {@link PluginArtifactStore} 保证：{@code {pluginsRoot}/{code}/{version}/{code}-{version}.jar}，
 * 与引擎启动扫描（{@code PluginEngineBootstrap}）用的是同一套路径规则，因此装完重启也能恢复。
 *
 * <p>鉴权：内部令牌 {@code X-Internal-Job-Token}，取值 {@code houcloud.plugin.internal-job-token}。
 * 未配置令牌时 fail-closed（一律拒绝），绝不允许「没配令牌就当内部调用」。
 *
 * <p>实现与 tool 宿主的 {@code com.houjiang.tool.plugin.PluginHostInstallController} 同构，
 * 包名随宿主，以便落在各自组件的扫描范围内。
 */
@RestController
@RequestMapping("/internal/plugin")
public class PluginHostInstallController {

    private static final Logger log = LoggerFactory.getLogger(PluginHostInstallController.class);

    private static final String TOKEN_HEADER = "X-Internal-Job-Token";

    private final PluginArtifactStore artifactStore;
    private final PluginChecksumVerifier checksumVerifier;
    private final PluginLifecycleManager lifecycleManager;
    private final String internalJobToken;

    public PluginHostInstallController(PluginArtifactStore artifactStore,
                                       PluginChecksumVerifier checksumVerifier,
                                       PluginLifecycleManager lifecycleManager,
                                       @Value("${houcloud.plugin.internal-job-token:}") String internalJobToken) {
        this.artifactStore = artifactStore;
        this.checksumVerifier = checksumVerifier;
        this.lifecycleManager = lifecycleManager;
        this.internalJobToken = internalJobToken == null ? "" : internalJobToken.trim();
    }

    /**
     * 安装 / 升级插件：落盘 → MD5 校验 → 已装载则先卸载 → PF4J 装载。
     */
    @PostMapping(value = "/install", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> install(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                       @RequestParam("pluginCode") String pluginCode,
                                       @RequestParam("version") String version,
                                       @RequestParam(value = "md5", required = false) String md5,
                                       @RequestPart("file") MultipartFile file) {
        requireInternalToken(token);
        byte[] content;
        try {
            content = file.getBytes();
        } catch (Exception e) {
            return fail("读取安装包失败: " + e.getMessage());
        }
        if (content.length == 0) {
            return fail("安装包为空: " + pluginCode + "@" + version);
        }
        if (!checksumVerifier.verify(content, md5)) {
            return fail("安装包校验失败（MD5 不一致）: " + pluginCode + "@" + version);
        }
        Path jarPath;
        try {
            jarPath = artifactStore.store(pluginCode, version, content);
        } catch (Exception e) {
            return fail("插件安装包落盘失败: " + e.getMessage());
        }
        try {
            // 重复安装（升级）先卸载旧实例：PF4J 不允许同一个 pluginId 装载两次。
            if (lifecycleManager.isLoaded(pluginCode)) {
                lifecycleManager.uninstall(pluginCode);
            }
            PluginState state = lifecycleManager.install(pluginCode, jarPath);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("pluginCode", pluginCode);
            data.put("version", version);
            data.put("jarPath", jarPath.toString());
            data.put("state", state == null ? null : state.name());
            log.info("[宿主安装] 已装载插件 {}@{} <- {}", pluginCode, version, jarPath);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("code", 0);
            result.put("message", "success");
            result.put("data", data);
            return result;
        } catch (RuntimeException e) {
            // 装载失败不留半成品：产物删掉，避免下次启动扫描到一个坏包。
            try {
                artifactStore.delete(pluginCode);
            } catch (Exception cleanupError) {
                e.addSuppressed(cleanupError);
            }
            log.error("[宿主安装] 装载插件失败 {}@{}: {}", pluginCode, version, e.getMessage(), e);
            return fail("装载插件失败: " + e.getMessage());
        }
    }

    /**
     * 卸载插件（引擎侧停插件 + 清注册表 + 删落盘产物）。
     */
    @PostMapping("/uninstall")
    public Map<String, Object> uninstall(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                         @RequestParam("pluginCode") String pluginCode) {
        requireInternalToken(token);
        try {
            lifecycleManager.uninstall(pluginCode);
            try {
                artifactStore.delete(pluginCode);
            } catch (Exception cleanupError) {
                log.warn("[宿主安装] 卸载后清理落盘产物失败 {}: {}", pluginCode, cleanupError.getMessage());
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("code", 0);
            result.put("message", "success");
            result.put("data", Map.of("pluginCode", pluginCode));
            return result;
        } catch (RuntimeException e) {
            log.error("[宿主安装] 卸载插件失败 {}: {}", pluginCode, e.getMessage());
            return fail("卸载插件失败: " + e.getMessage());
        }
    }

    private void requireInternalToken(String token) {
        if (internalJobToken.isEmpty()) {
            throw new IllegalStateException("宿主未配置 houcloud.plugin.internal-job-token，拒绝内部安装调用");
        }
        if (token == null || !constantTimeEquals(token.trim(), internalJobToken)) {
            throw new SecurityException("内部调用令牌无效");
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    private static Map<String, Object> fail(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 500);
        result.put("message", message);
        return result;
    }
}
