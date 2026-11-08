package com.houjiang.mall.service.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 插件前端产物解析器（T11）。
 *
 * <p>把 {@code /web/plugin-assets/{code}/{version}/{entry}} 收敛到宿主磁盘上的真实产物，
 * 解析顺序（先目录后 JAR，便于开发期直接改文件）：
 * <ol>
 *   <li>目录形态：{@code {pluginsRoot}/{code}/{version}/frontend/{entry}}，未命中再试同层 {@code {entry}}；</li>
 *   <li>JAR 形态（市场在线安装落盘）：{@code {pluginsRoot}/{code}/{version}/{code}-{version}.jar} 内的 {@code frontend/{entry}}。</li>
 * </ol>
 *
 * <p><b>安全约束</b>（对应《开源插件化架构》8.2 安全红线）：
 * <ul>
 *   <li>{@code code} / {@code version} 必须匹配白名单字符集，杜绝拼接注入；</li>
 *   <li>{@code entry} 先 URL 解码再做穿越校验，{@code ..}、{@code \}、{@code //}、绝对路径、冒号一律拒绝；</li>
 *   <li>后缀必须在 {@link #ALLOWED_EXTENSIONS} 内，避免产物通道被用来回传脚本或可执行内容；</li>
 *   <li>目录解析结果必须落在版本目录之内；JAR 内条目名必须与期望名完全相等。</li>
 * </ul>
 *
 * <p>本类纯文件 I/O、不依赖 Spring，便于单测与在宿主任意位置复用。
 *
 * @author Amos
 */
public class PluginAssetResolver {

    /** 允许回传的产物后缀（前端资源白名单） */
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "mjs", "js", "css", "json", "map", "svg",
            "png", "jpg", "jpeg", "gif", "webp", "ico", "bmp",
            "woff", "woff2", "ttf", "eot", "txt");

    /**
     * 带版本号的产物可长期缓存：URL 随版本变化，无需协商缓存。
     * 与前端加载器「URL 带版本」的契约保持一致。
     */
    public static final String CACHE_CONTROL = "public, max-age=31536000, immutable";

    /** JAR 内前端资源目录（插件包规范，见《开源插件化架构》4.5 / 8.2） */
    public static final String FRONTEND_DIR = "frontend";

    /** 单个产物文件上限：8 MB，避免产物通道被当成大文件分发 */
    public static final long MAX_ASSET_BYTES = 8L * 1024 * 1024;

    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]{0,63}$");

    private static final Pattern VERSION_PATTERN = Pattern.compile("^[0-9A-Za-z][0-9A-Za-z._-]{0,31}$");

    private static final Map<String, String> CONTENT_TYPES = contentTypes();

    private final Path pluginsRoot;

    public PluginAssetResolver(Path pluginsRoot) {
        if (pluginsRoot == null) {
            throw new IllegalArgumentException("pluginsRoot 不能为空");
        }
        this.pluginsRoot = pluginsRoot.toAbsolutePath().normalize();
    }

    public Path pluginsRoot() {
        return pluginsRoot;
    }

    /**
     * 解析产物内容。
     *
     * @return 命中返回产物；未命中返回 {@code null}（调用方映射 404）
     * @throws PluginAssetException 请求非法（400）或读取失败（500）
     */
    public PluginAsset resolve(String code, String version, String entryPath) {
        String safeCode = requireCode(code);
        String safeVersion = requireVersion(version);
        String entry = normalizeEntry(entryPath);
        for (Path pluginDir : PluginSourceLayout.productDirectories(pluginsRoot, safeCode)) {
            Path versionDir = pluginDir.resolve(safeVersion).normalize();
            if (!versionDir.startsWith(pluginsRoot)) throw new PluginAssetException("插件产物路径越界");
            PluginAsset fromVersion = readFromDir(versionDir, entry);
            if (fromVersion != null) return fromVersion;
            PluginAsset fromSource = readFromDir(pluginDir, entry);
            if (fromSource != null) return fromSource;
            PluginAsset fromJar = readFromJar(versionDir, safeCode, safeVersion, entry);
            if (fromJar != null) return fromJar;
        }
        return null;
    }

    /**
     * 产物是否已落盘（不读取内容，用于快速探活/排障）。
     */
    public boolean exists(String code, String version, String entryPath) {
        return resolve(code, version, entryPath) != null;
    }

    private PluginAsset readFromDir(Path versionDir, String entry) {
        for (String prefix : List.of(FRONTEND_DIR + "/", "")) {
            Path candidate = versionDir.resolve(prefix + entry).normalize();
            if (!candidate.startsWith(versionDir) || !Files.isRegularFile(candidate)) {
                continue;
            }
            try {
                long size = Files.size(candidate);
                if (size > MAX_ASSET_BYTES) {
                    throw new PluginAssetException("插件产物文件超过上限 " + MAX_ASSET_BYTES + " 字节");
                }
                return new PluginAsset(Files.readAllBytes(candidate), contentType(entry), CACHE_CONTROL,
                        "dir:" + candidate);
            } catch (IOException e) {
                throw new PluginAssetException("读取插件产物失败: " + e.getMessage(), 500);
            }
        }
        return null;
    }

    private PluginAsset readFromJar(Path versionDir, String code, String version, String entry) {
        Path jar = versionDir.resolve(code + "-" + version + ".jar");
        if (!Files.isRegularFile(jar)) {
            return null;
        }
        String zipName = FRONTEND_DIR + "/" + entry;
        try (ZipFile zipFile = new ZipFile(jar.toFile())) {
            ZipEntry zipEntry = zipFile.getEntry(zipName);
            if (zipEntry == null || zipEntry.isDirectory()) {
                return null;
            }
            if (zipEntry.getSize() > MAX_ASSET_BYTES) {
                throw new PluginAssetException("插件产物文件超过上限 " + MAX_ASSET_BYTES + " 字节");
            }
            try (InputStream in = zipFile.getInputStream(zipEntry)) {
                byte[] content = in.readNBytes((int) MAX_ASSET_BYTES + 1);
                if (content.length > MAX_ASSET_BYTES) {
                    throw new PluginAssetException("插件产物文件超过上限 " + MAX_ASSET_BYTES + " 字节");
                }
                return new PluginAsset(content, contentType(entry), CACHE_CONTROL,
                        "jar:" + jar + "!/" + zipName);
            }
        } catch (IOException e) {
            throw new PluginAssetException("读取插件产物失败: " + e.getMessage(), 500);
        }
    }

    /**
     * 归一化并校验产物相对路径。
     *
     * <p>先解码（保留字面量 {@code +}）再校验，保证编码绕过（{@code %2e%2e}）同样被拒绝。
     */
    public static String normalizeEntry(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new PluginAssetException("插件产物路径不能为空");
        }
        String value;
        try {
            value = URLDecoder.decode(raw.trim().replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new PluginAssetException("插件产物路径编码非法");
        }
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.isEmpty()) {
            throw new PluginAssetException("插件产物路径不能为空");
        }
        if (value.contains("..") || value.contains("\\") || value.contains("//") || value.contains(":")) {
            throw new PluginAssetException("插件产物路径非法: " + raw);
        }
        if (value.chars().anyMatch(ch -> ch < 0x20 || ch == 0x7f)) {
            throw new PluginAssetException("插件产物路径含控制字符");
        }
        if (!isAllowedExtension(value)) {
            throw new PluginAssetException("插件产物后缀不在白名单内: " + extensionOf(value));
        }
        return value;
    }

    public static String requireCode(String code) {
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new PluginAssetException("插件编码格式非法: " + code);
        }
        return code;
    }

    public static String requireVersion(String version) {
        if (version == null || !VERSION_PATTERN.matcher(version).matches()) {
            throw new PluginAssetException("插件版本格式非法: " + version);
        }
        return version;
    }

    public static boolean isAllowedExtension(String entry) {
        return ALLOWED_EXTENSIONS.contains(extensionOf(entry));
    }

    /**
     * 后缀（小写、不含点）；无后缀返回空串。
     */
    public static String extensionOf(String entry) {
        if (entry == null) {
            return "";
        }
        String name = entry;
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 扩展名 → Content-Type。
     *
     * <p>{@code .mjs} 与线上静态托管的 {@code .js} 保持一致的 {@code application/javascript}，
     * 浏览器可直接作为 ESM 模块加载。
     */
    public static String contentType(String entry) {
        String ext = extensionOf(entry);
        return CONTENT_TYPES.getOrDefault(ext, "application/octet-stream");
    }

    private static Map<String, String> contentTypes() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("mjs", "application/javascript; charset=UTF-8");
        map.put("js", "application/javascript; charset=UTF-8");
        map.put("css", "text/css; charset=UTF-8");
        map.put("json", "application/json; charset=UTF-8");
        map.put("map", "application/json; charset=UTF-8");
        map.put("txt", "text/plain; charset=UTF-8");
        map.put("svg", "image/svg+xml");
        map.put("png", "image/png");
        map.put("jpg", "image/jpeg");
        map.put("jpeg", "image/jpeg");
        map.put("gif", "image/gif");
        map.put("webp", "image/webp");
        map.put("ico", "image/x-icon");
        map.put("bmp", "image/bmp");
        map.put("woff", "font/woff");
        map.put("woff2", "font/woff2");
        map.put("ttf", "font/ttf");
        map.put("eot", "application/vnd.ms-fontobject");
        return Map.copyOf(map);
    }

    /**
     * 产物载荷。
     *
     * @param content      文件内容
     * @param contentType  响应用的 Content-Type
     * @param cacheControl 响应用的 Cache-Control
     * @param source       命中来源（排障用，如 {@code jar:...!/frontend/index.mjs}）
     */
    public record PluginAsset(byte[] content, String contentType, String cacheControl, String source) {

        public int length() {
            return content == null ? 0 : content.length;
        }
    }
}
