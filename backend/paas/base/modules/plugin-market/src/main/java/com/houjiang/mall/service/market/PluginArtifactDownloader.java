package com.houjiang.mall.service.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 插件安装包下载器（在线安装第一步）。
 *
 * <p>支持三类地址：
 * <ul>
 *   <li>{@code http(s)://...} —— 正式市场分发（CDN）</li>
 *   <li>{@code file:///...} —— 内网离线分发 / 验收环境（无需外网即可跑通全链路）</li>
 *   <li>以 {@code /} 开头的站内相对路径 —— 制品回源端点
 *       （{@code /base/plugin-center/artifact/raw?token=...}），按
 *       {@code houcloud.plugin.artifact.internal-base} 补成绝对地址</li>
 * </ul>
 *
 * <p>🔴 2026-10-27：第三类是补的。上传侧把 {@code jar_url} 写成站内相对路径，
 * 而这里原本只认 http/file，市场在线安装因此 100% 抛「不支持的安装包地址协议」。
 *
 * <p>体积上限由 {@code houcloud.plugin.max-artifact-bytes} 控制（默认 64MB），
 * 边读边计数，超限立即中断，避免恶意大包打满磁盘与内存。
 *
 * @author Amos
 */
@Service
public class PluginArtifactDownloader {

    private static final Logger log = LoggerFactory.getLogger(PluginArtifactDownloader.class);

    /** 默认体积上限 64MB */
    public static final long DEFAULT_MAX_BYTES = 64L * 1024 * 1024;

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 30000;

    private final long maxBytes;

    /**
     * 站内回源地址的前缀（本服务自己的入口）。
     *
     * <p>默认 {@code http://127.0.0.1:8214}：base 服务端口。容器内（模式 A）与
     * host 网络（模式 B）下 127.0.0.1 都指向本服务所在网络命名空间，无需额外配置；
     * 端口被改过时用 {@code houcloud.plugin.artifact.internal-base} 覆盖。
     */
    private final String internalBase;

    @Autowired
    public PluginArtifactDownloader(@Value("${houcloud.plugin.max-artifact-bytes:67108864}") long maxBytes,
                                    @Value("${houcloud.plugin.artifact.internal-base:http://127.0.0.1:8214}")
                                    String internalBase) {
        this.maxBytes = maxBytes <= 0 ? DEFAULT_MAX_BYTES : maxBytes;
        this.internalBase = StringUtils.hasText(internalBase)
                ? internalBase.trim().replaceAll("/+$", "")
                : "http://127.0.0.1:8214";
    }

    /**
     * 无参构造：仅供 CGLIB 代理子类使用，正常注入路径不走它。
     *
     * <p>Spring 6.2（Boot 3.5 基线）移除 Objenesis：被 AOP 切面代理的无接口 bean
     * 必须提供无参构造器，否则启动即崩（No default constructor found）。
     */
    PluginArtifactDownloader() {
        this.maxBytes = DEFAULT_MAX_BYTES;
        this.internalBase = "http://127.0.0.1:8214";
    }

    /**
     * 单参数构造：保留给既有单测与内部直接 new 的场景。
     */
    public PluginArtifactDownloader(long maxBytes) {
        this(maxBytes, "http://127.0.0.1:8214");
    }

    /**
     * 站内相对地址 → 绝对地址。非相对地址原样返回。
     */
    String toAbsolute(String url) {
        if (url == null) {
            return null;
        }
        String trimmed = url.trim();
        return trimmed.startsWith("/") ? internalBase + trimmed : trimmed;
    }

    public long maxBytes() {
        return maxBytes;
    }

    /**
     * 地址协议是否受支持。
     */
    public boolean supports(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String lower = url.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("file:")
                // 站内回源：相对路径，由 toAbsolute 补 base 主机
                || lower.startsWith("/");
    }

    /**
     * 下载安装包到内存（供校验与落盘使用）。
     *
     * @param url 安装包地址（{@code hou_market_plugin_version.jar_url}）
     */
    public Artifact download(String url) throws IOException {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("插件安装包地址为空");
        }
        String trimmed = toAbsolute(url.trim());
        if (!supports(trimmed)) {
            throw new IllegalArgumentException("不支持的安装包地址协议: " + trimmed);
        }
        return trimmed.toLowerCase(Locale.ROOT).startsWith("file:")
                ? readLocal(trimmed)
                : readRemote(trimmed);
    }

    private Artifact readLocal(String url) throws IOException {
        Path path;
        try {
            path = Path.of(URI.create(url));
        } catch (RuntimeException e) {
            path = Path.of(url.substring("file:".length()));
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("安装包文件不存在: " + path);
        }
        long size = Files.size(path);
        checkSize(size, url);
        byte[] content = Files.readAllBytes(path);
        log.info("插件安装包已读取（file）：{}（{} bytes）", path, size);
        return new Artifact(content, url, "file");
    }

    private Artifact readRemote(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("下载插件安装包失败，HTTP " + code + ": " + url);
            }
            long declared = conn.getContentLengthLong();
            if (declared > maxBytes) {
                throw new IOException("插件安装包超出体积上限（" + declared + " > " + maxBytes + "）: " + url);
            }
            try (InputStream in = conn.getInputStream()) {
                byte[] content = readLimited(in, url);
                log.info("插件安装包已下载：{}（{} bytes）", url, content.length);
                return new Artifact(content, url, "http");
            }
        } finally {
            conn.disconnect();
        }
    }

    private byte[] readLimited(InputStream in, String url) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new IOException("插件安装包超出体积上限（>" + maxBytes + " bytes）: " + url);
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private void checkSize(long size, String url) throws IOException {
        if (size > maxBytes) {
            throw new IOException("插件安装包超出体积上限（" + size + " > " + maxBytes + "）: " + url);
        }
    }

    /**
     * 下载结果：内容 + 来源 + 摘要。
     */
    public static class Artifact {

        private final byte[] content;
        private final String sourceUrl;
        private final String protocol;

        public Artifact(byte[] content, String sourceUrl, String protocol) {
            this.content = content;
            this.sourceUrl = sourceUrl;
            this.protocol = protocol;
        }

        public byte[] getContent() {
            return content;
        }

        public String getSourceUrl() {
            return sourceUrl;
        }

        public String getProtocol() {
            return protocol;
        }

        public long size() {
            return content == null ? 0 : content.length;
        }

        /**
         * 内容 MD5（小写十六进制），市场未声明摘要时作为留痕值。
         */
        public String md5() {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                return HexFormat.of().formatHex(digest.digest(content));
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("当前 JVM 不支持 MD5", e);
            }
        }
    }
}
