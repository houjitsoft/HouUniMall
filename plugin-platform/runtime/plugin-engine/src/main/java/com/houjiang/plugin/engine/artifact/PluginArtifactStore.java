package com.houjiang.plugin.engine.artifact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 插件安装产物落盘仓库。
 *
 * <p>目录契约（宿主与引擎共用，是「市场在线安装」与「引擎装载」之间唯一的衔接点）：
 * <pre>
 * {pluginsRoot}/{pluginCode}/{version}/{pluginCode}-{version}.jar
 * </pre>
 *
 * <p>本类只做文件 I/O，<b>不依赖 Spring / PF4J</b>，因此既可被宿主（mall）在市场安装时调用，
 * 也可被引擎在装载阶段按同一路径规则定位安装包。
 *
 * <p>落盘采用「先写 .tmp 再原子 move」，避免半包文件被引擎误加载。
 *
 * <p><b>路径安全不变式</b>：{@code pluginCode} 与 {@code version} 均只被当作<b>一层目录名</b>，
 * 一律先过 {@link #requireSegment(String, String)} 白名单再参与拼接——
 * {@link Path#resolve(String)} 传入绝对路径时会直接返回该绝对路径
 * （{@code root.resolve("/etc/passwd")} 就是 {@code /etc/passwd}），
 * 只判空等于把「写到制品目录之外的任意位置」开放给任何能控制 code / version 的调用方。
 *
 * @author HouCloud
 */
public class PluginArtifactStore {

    private static final Logger log = LoggerFactory.getLogger(PluginArtifactStore.class);

    private static final String JAR_SUFFIX = ".jar";

    /**
     * 单段路径名白名单：只允许字母、数字、点、下划线、连字符。
     *
     * <p>之所以用白名单而不是「拉黑 {@code / \ ..}」：拉黑只能挡住已经想到的写法，
     * 而绝对路径、盘符路径（{@code C:\...}）等形态是枚举不完的；白名单反过来把
     * 「一层目录名」限定成一个在结构上不可能越界的形状。
     */
    private static final Pattern SAFE_SEGMENT = Pattern.compile("[A-Za-z0-9._-]+");

    /** 单段路径名长度上限：插件编码与版本号正常都在 64 字符以内，128 留足余量。 */
    private static final int MAX_SEGMENT_LENGTH = 128;

    private final Path pluginsRoot;

    public PluginArtifactStore(Path pluginsRoot) {
        if (pluginsRoot == null) {
            throw new IllegalArgumentException("pluginsRoot 不能为空");
        }
        this.pluginsRoot = pluginsRoot.toAbsolutePath().normalize();
    }

    public Path pluginsRoot() {
        return pluginsRoot;
    }

    /**
     * 安装包目标路径：{root}/{code}/{version}/{code}-{version}.jar
     *
     * @throws IllegalArgumentException code 或 version 不是合法的单层目录名
     */
    public Path resolve(String pluginCode, String version) {
        String code = requireSegment(pluginCode, "插件编码");
        String versionText = requireSegment(version, "插件版本");
        return pluginsRoot.resolve(code).resolve(versionText)
                .resolve(code + "-" + versionText + JAR_SUFFIX);
    }

    /**
     * 落盘安装包（原子写入）。
     *
     * @return 落盘后的绝对路径
     */
    public Path store(String pluginCode, String version, byte[] content) throws IOException {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("安装包内容为空，拒绝落盘");
        }
        Path target = resolve(pluginCode, version);
        Files.createDirectories(target.getParent());
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(tmp, content);
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("插件安装包已落盘：{}（{} bytes）", target, content.length);
        return target;
    }

    public boolean exists(String pluginCode, String version) {
        return Files.isRegularFile(resolve(pluginCode, version));
    }

    /**
     * 列出某插件已落盘的版本目录（按字典序升序）。
     *
     * <p><b>只认「非隐藏目录」</b>：以 {@code .} 开头的目录是备份点
     * （升级前置备份写成 {@code {root}/{code}/.backup-{ts}/}），
     * 它不是「一个可安装的版本」，混进版本列表会让
     * 「保留最近 N 个版本」的清理逻辑把回滚锚点一起删掉——这正是方案 §7 P0-8 要防的事。
     *
     * @param pluginCode 插件编码
     * @return 版本目录名列表，插件目录不存在时为空列表
     */
    public List<String> listVersions(String pluginCode) {
        Path dir = pluginsRoot.resolve(requireSegment(pluginCode, "插件编码"));
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> children = Files.list(dir)) {
            return children.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> !name.startsWith("."))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.warn("列出插件版本目录失败：{}（{}）", pluginCode, e.getMessage());
            return List.of();
        }
    }

    /**
     * 列出某插件的备份目录（{@code .backup-{ts}}），按名称升序（名称含时间戳，升序即时间升序）。
     *
     * @param pluginCode 插件编码
     * @return 备份目录名列表
     */
    public List<String> listBackups(String pluginCode) {
        Path dir = pluginsRoot.resolve(requireSegment(pluginCode, "插件编码"));
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> children = Files.list(dir)) {
            return children.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(".backup-"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.warn("列出插件备份目录失败：{}（{}）", pluginCode, e.getMessage());
            return List.of();
        }
    }

    /**
     * 只删某一个版本的目录（{root}/{code}/{version}/）。
     *
     * <p><b>升级/回滚链路必须用它，绝不能用 {@link #delete(String)}</b>：
     * 后者删的是整个插件目录，会把历史版本与 {@code .backup-*} 回滚锚点一起抹掉，
     * 直接违背方案 §7 P0-8「清理必须排除回滚锚点」。
     * 新增这个方法的目的就是把「删一个版本」和「删整个插件」在 API 层面分开，
     * 让调用方没有误用整目录删除的机会。
     *
     * @param pluginCode 插件编码
     * @param version    版本号（非法版本名一律拒绝，避免拼出越界路径）
     * @return true 表示目录原本存在且已被删除
     * @throws IllegalArgumentException code 或 version 不是合法的单层目录名
     * @throws IOException 删除失败（Windows 上 jar 句柄未释放时会抛 {@link java.nio.file.AccessDeniedException}）
     */
    public boolean deleteVersion(String pluginCode, String version) throws IOException {
        Path dir = pluginsRoot.resolve(requireSegment(pluginCode, "插件编码"))
                .resolve(requireSegment(version, "版本"));
        if (!Files.exists(dir)) {
            return false;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("删除版本目录内容失败：{}（{}）", path, e.getMessage());
                }
            });
        }
        log.info("插件版本目录已删除：{}", dir);
        return true;
    }

    /**
     * 删除某插件的全部落盘产物（{root}/{code} 整目录）。
     *
     * <p>⚠️ <b>整目录删除：会连同历史版本与 {@code .backup-*} 回滚锚点一起删掉。</b>
     * 只适用于「插件被彻底卸载、不再需要任何回滚点」的场景（如市场卸载）。
     * 升级、回滚、版本清理一律用 {@link #deleteVersion(String, String)}——
     * 用这个方法做版本清理会直接违背方案 §7 P0-8。
     */
    public void delete(String pluginCode) throws IOException {
        Path dir = pluginsRoot.resolve(requireSegment(pluginCode, "插件编码"));
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("删除插件落盘产物失败：{}（{}）", path, e.getMessage());
                }
            });
        }
        log.info("插件落盘产物已删除：{}", dir);
    }

    /**
     * 校验「一层目录名」并返回去空白后的取值。
     *
     * <p><b>本类是全类唯一的口径</b>：所有把外部传入的 code / version 拼进路径的地方
     * 都必须走这里。此前 {@link #deleteVersion(String, String)} 自己写了一套
     * 「含 {@code /} 或 {@code \} 或以 {@code .} 开头就拒」的判定，而 {@link #resolve(String, String)}
     * 只判空——同一类里两套口径，严的那套形同虚设，因为写路径走的是松的那套。
     *
     * @param value 待校验的取值
     * @param label 取值名称（用于异常信息）
     * @return 去空白后的合法目录名
     * @throws IllegalArgumentException 空值、超长、含白名单外字符、或为 {@code .} / 含 {@code ..} 时抛出
     */
    private String requireSegment(String value, String label) {
        requireText(value, label);
        String segment = value.trim();
        if (segment.length() > MAX_SEGMENT_LENGTH) {
            throw new IllegalArgumentException(label + "超长（上限 " + MAX_SEGMENT_LENGTH + " 字符）：" + segment);
        }
        if (!SAFE_SEGMENT.matcher(segment).matches()) {
            throw new IllegalArgumentException(label + "只允许字母、数字、点、下划线、连字符：" + segment);
        }
        // 白名单里保留了「.」（版本号需要），因此 . 与 .. 必须单独拒掉
        if (segment.equals(".") || segment.contains("..")) {
            throw new IllegalArgumentException(label + "不得为 . 或包含 ..：" + segment);
        }
        // 以「.」开头的是备份点（.backup-*）/ 隐藏目录，不是可安装版本。
        // 这里必须拒掉：listVersions 已把 .backup-* 排除在版本列表外，
        // 若 deleteVersion 放行 ".backup-20260101"，就能绕过那层过滤直接删掉回滚锚点，
        // 与方案 §7 P0-8「历史版本与备份点是回滚锚点」直接冲突。
        if (segment.startsWith(".")) {
            throw new IllegalArgumentException(label + "不得以 . 开头（备份目录不是可安装版本）：" + segment);
        }
        return segment;
    }

    private void requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
    }
}
