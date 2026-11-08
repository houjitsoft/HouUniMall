package com.houjiang.mall.service.plugin;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.AddonPlugin;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.AddonPluginMapper;
import com.houjiang.mall.mapper.plugin.AddonCateMapper;
import lombok.extern.slf4j.Slf4j;
import com.houjiang.plugin.sdk.model.PluginServiceOwnership;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** 按服务归属创建插件：plugins/{hostService}/{code}，并登记插件目录与市场草稿。 */
@Slf4j
@Service
public class PluginScaffoldService {

    /** 插件编码：小写字母开头，仅小写字母/数字/连字符，2-32 位，不以连字符结尾。 */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9-]{1,31}$");
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private static final String DEFAULT_VERSION = "1.0.0";

    private final AddonPluginMapper addonPluginMapper;
    private final MarketPluginMapper marketPluginMapper;
    private final MarketPluginVersionMapper marketPluginVersionMapper;
    private final Path scaffoldRoot;

    @Autowired
    private AddonCateMapper addonCateMapper;

    /**
     * 生产构造器：{@code scaffoldDir} 由 {@code houcloud.plugin.scaffold-dir} 注入。
     * 类内同时存在供测试使用的 Path 构造器，Spring 无法唯一确定候选构造器，
     * 因此必须显式标注 {@code @Autowired}，否则回退默认构造器并报
     * "No default constructor found"（base 启动失败）。
     */
    @Autowired
    public PluginScaffoldService(AddonPluginMapper addonPluginMapper,
                                 MarketPluginMapper marketPluginMapper,
                                 MarketPluginVersionMapper marketPluginVersionMapper,
                                 @Value("${houcloud.plugin.scaffold-dir:plugins}") String scaffoldDir) {
        this(addonPluginMapper, marketPluginMapper, marketPluginVersionMapper, Path.of(scaffoldDir));
    }

    PluginScaffoldService(AddonPluginMapper addonPluginMapper,
                          MarketPluginMapper marketPluginMapper,
                          MarketPluginVersionMapper marketPluginVersionMapper,
                          Path scaffoldRoot) {
        this.addonPluginMapper = addonPluginMapper;
        this.marketPluginMapper = marketPluginMapper;
        this.marketPluginVersionMapper = marketPluginVersionMapper;
        this.scaffoldRoot = scaffoldRoot.toAbsolutePath().normalize();
    }

    /** 校验后的插件规格。 */
    record ScaffoldSpec(String code, String name, String version, String category, int cateId, String description,
                        String licenseType, int priceCents, String hostService, String runtime, String menuPath,
                        String menuTitle, String tablePrefix, List<String> terminals, List<String> tags) {
        String pascalCode() {
            StringBuilder sb = new StringBuilder();
            for (String part : code.split("-")) {
                if (!part.isEmpty()) {
                    sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
                }
            }
            return sb.toString();
        }

        String packageName() {
            return "com.houjiang.plugin." + code.replace('-', '_');
        }

        String artifactId() {
            return code;
        }
    }

    record GeneratedFile(String relativePath, String content) {
    }

    /**
     * 创建插件（真实落盘 + 三表入库）。任一环节失败即整体失败：
     * 库事务回滚，已写文件尽力清理。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPlugin(Map<String, Object> params) {
        ScaffoldSpec spec = parseSpec(params == null ? Map.of() : params);
        ensureNotExists(spec);

        List<GeneratedFile> files = render(spec);
        Path serviceDir = scaffoldRoot.resolve(spec.hostService()).resolve(spec.code());
        writeFiles(serviceDir, files);
        try {
            insertRows(spec);
        } catch (Exception dbError) {
            cleanupQuietly(serviceDir);
            throw dbError;
        }

        log.info("插件脚手架生成成功: code={} dir={} files={} db=addon+market+version",
                spec.code(), serviceDir, files.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", spec.code());
        result.put("name", spec.name());
        result.put("version", spec.version());
        result.put("serviceDir", serviceDir.toString());
        result.put("files", files.stream().map(GeneratedFile::relativePath).toList());
        result.put("pluginYaml", pluginYaml(spec));
        result.put("menuPath", spec.menuPath());
        result.put("pluginDir", serviceDir.toString());
        result.put("hostService", spec.hostService());
        result.put("runtime", spec.runtime());
        result.put("implementationDir", "embedded".equals(spec.runtime())
                ? serviceDir + "/runtime/java"
                : "backend/paas/" + spec.hostService());
        result.put("nextSteps", List.of(
                "standalone".equals(spec.runtime())
                        ? "在已有服务 backend/paas/" + spec.hostService() + " 中实现业务逻辑"
                        : "在 " + serviceDir + "/runtime/java 中实现扩展，并由 " + spec.hostService() + " 服务装配",
                "在 plugin.yaml 中补全 permissions 与 menus 契约",
                "mvn package 构建 JAR，由所属服务加载验证后再提交市场审核"));
        return result;
    }

    // ---------------------------------------------------------------- 校验

    ScaffoldSpec parseSpec(Map<String, Object> params) {
        String code = text(params.get("code"));
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("插件编码（code）不能为空");
        }
        code = code.trim();
        if (!CODE_PATTERN.matcher(code).matches() || code.endsWith("-") || code.contains("--")) {
            throw new IllegalArgumentException("插件编码不合法：需小写字母开头，仅含小写字母/数字/连字符，2-32 位且不以连字符结尾（请勿使用大写或下划线）");
        }
        if (PluginScaffoldTemplates.reservedCodes().contains(code)) {
            throw new IllegalArgumentException("插件编码与现有插件/核心模块冲突: " + code);
        }

        String name = text(params.get("name"));
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("插件名称（name）不能为空");
        }
        if (name.length() > 128) {
            throw new IllegalArgumentException("插件名称过长（≤128 字符）");
        }

        String version = text(params.getOrDefault("version", DEFAULT_VERSION));
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new IllegalArgumentException("版本号不合法：应为 x.y.z（如 1.0.0）");
        }

        String licenseType = text(params.getOrDefault("licenseType", "FREE")).toUpperCase(Locale.ROOT);
        if (!"FREE".equals(licenseType) && !"PAID".equals(licenseType)) {
            throw new IllegalArgumentException("授权类型仅支持 FREE / PAID");
        }
        int priceCents = intOf(params.get("priceCents"), 0);
        if (priceCents < 0) {
            throw new IllegalArgumentException("价格不能为负数");
        }

        String hostService = text(params.get("hostService"));
        if (!PluginServiceOwnership.SERVICES.contains(hostService == null ? "" : hostService)) {
            throw new IllegalArgumentException("hostService 必须是架构定义的 24 个业务服务之一");
        }
        // 归属形态只认 plugin.yaml 的 runtime：embedded = 由宿主加载扩展包，standalone = 业务在已有服务里实现。
        String runtime = text(params.get("runtime"));
        if (!"embedded".equals(runtime) && !"standalone".equals(runtime)) {
            throw new IllegalArgumentException("插件形态必须是 embedded 或 standalone");
        }

        String menuPath = text(params.getOrDefault("menuPath", "/plugin/" + code));
        if (!menuPath.startsWith("/")) {
            throw new IllegalArgumentException("菜单路径须以 / 开头");
        }

        List<String> terminals = stringList(params.get("terminals"), List.of("admin"));
        List<String> tags = stringList(params.get("tags"), List.of("official"));
        CategorySelection category = resolveCategory(params);
        String description = text(params.getOrDefault("description", name));

        String prefix = "hou_plugin_" + code.replace('-', '_') + "_";
        return new ScaffoldSpec(code, name, version, category.name(), category.id(), description, licenseType,
                priceCents, hostService, runtime, menuPath, text(params.getOrDefault("menuTitle", name)),
                prefix, terminals, tags);
    }

    private record CategorySelection(int id, String name) { }

    /** ID 是创建分类的权威值；旧调用只传名称时也必须匹配 base 字典。 */
    private CategorySelection resolveCategory(Map<String, Object> params) {
        String requestedName = text(params.get("category"));
        boolean idSupplied = params.get("cateId") != null;
        int requestedId = intOf(params.get("cateId"), 0);
        if (requestedId < 0) throw new IllegalArgumentException("分类ID不能为负数");
        if ((idSupplied && requestedId == 0) || (!idSupplied && !StringUtils.hasText(requestedName))) {
            // 未选分类的兜底不能是空串：plugin.yaml 里的「category:」空值经 Yaml 解析回 null，
            // 会在发布同步 hou_addon_plugin（category NOT NULL）时炸断审核（2026-10-27 装机实测）。
            return new CategorySelection(0, "未分类");
        }
        if (addonCateMapper == null) throw new IllegalStateException("分类目录不可用");
        List<Map<String, Object>> rows = addonCateMapper.selectAll();
        if (rows != null) for (Map<String, Object> row : rows) {
            int id = intOf(row.get("id"), 0);
            String name = text(row.get("name"));
            if (id > 0 && StringUtils.hasText(name)
                    && (idSupplied ? id == requestedId : name.equals(requestedName))) {
                return new CategorySelection(id, name);
            }
        }
        throw new IllegalArgumentException("插件分类不存在，请从分类字典中选择");
    }

    void ensureNotExists(ScaffoldSpec spec) {
        Long addonCount = addonPluginMapper.selectCount(new LambdaQueryWrapper<AddonPlugin>()
                .eq(AddonPlugin::getPluginCode, spec.code()));
        if (addonCount != null && addonCount > 0) {
            throw new IllegalStateException("插件编码已存在于插件中心目录: " + spec.code());
        }
        Long marketCount = marketPluginMapper.selectCount(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, spec.code()));
        if (marketCount != null && marketCount > 0) {
            throw new IllegalStateException("插件编码已存在于市场目录: " + spec.code());
        }
        Path legacyDirectory = scaffoldRoot.resolve(spec.code());
        if (Files.exists(legacyDirectory)) {
            throw new IllegalStateException("旧插件目录已存在，拒绝创建重复编码: " + legacyDirectory);
        }
        // 插件 code 是全局标识，不能在另一个服务下重复创建。
        for (String host : PluginServiceOwnership.SERVICES) {
            Path pluginDir = scaffoldRoot.resolve(host).resolve(spec.code());
            if (Files.exists(pluginDir)) {
                throw new IllegalStateException("目录已存在，拒绝覆盖: " + pluginDir);
            }
        }
    }

    // ---------------------------------------------------------------- 生成

    List<GeneratedFile> render(ScaffoldSpec spec) {
        List<GeneratedFile> files = new ArrayList<>();
        files.add(new GeneratedFile("plugin.yaml", pluginYaml(spec)));
        files.add(new GeneratedFile("README.md", PluginScaffoldTemplates.readme(spec)));
        files.add(new GeneratedFile("frontend/src/index.js", PluginScaffoldTemplates.frontendIndexJs(spec)));
        files.add(new GeneratedFile("frontend/src/pages/Home.vue", PluginScaffoldTemplates.frontendHomeVue(spec.menuTitle())));
        if ("embedded".equals(spec.runtime())) {
            files.add(new GeneratedFile("pom.xml", PluginScaffoldTemplates.pomXml(spec)));
            // 源码目录固定 runtime/java：与全部现有插件一致，Maven 默认的 src/main/java 不被构建脚本识别。
            files.add(new GeneratedFile("runtime/java/" + spec.packageName().replace('.', '/')
                    + "/" + spec.pascalCode() + "Plugin.java", PluginScaffoldTemplates.pluginJava(spec)));
        }
        return List.copyOf(files);
    }

    String pluginYaml(ScaffoldSpec spec) {
        return PluginScaffoldTemplates.pluginYaml(spec);
    }

    private void writeFiles(Path serviceDir, List<GeneratedFile> files) {
        try {
            for (GeneratedFile file : files) {
                Path target = serviceDir.resolve(file.relativePath()).normalize();
                if (!target.startsWith(serviceDir)) {
                    throw new IOException("非法生成路径: " + file.relativePath());
                }
                Files.createDirectories(target.getParent());
                Files.writeString(target, file.content(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            cleanupQuietly(serviceDir);
            throw new IllegalStateException("写入插件骨架文件失败: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- 入库

    void insertRows(ScaffoldSpec spec) {
        String terminalsJson = JSON.toJSONString(spec.terminals());

        // 1. 插件中心目录：catalog() 的数据源，status=1 才会出现在列表页
        AddonPlugin addon = new AddonPlugin();
        addon.setServiceCode(spec.hostService());
        addon.setPluginCode(spec.code());
        addon.setName(spec.name());
        addon.setCategory(spec.category());
        addon.setCateId(spec.cateId());
        addon.setVersion(spec.version());
        addon.setDescription(spec.description());
        addon.setTerminalsJson(terminalsJson);
        addon.setDependenciesJson("[]");
        addon.setEntryPath(spec.menuPath());
        addon.setConfigSchema("{}");
        addon.setStatus(1);
        addon.setSort(9999);
        addonPluginMapper.insert(addon);

        // 2. 市场商品：status=0 草稿（不上架、不对外可见），上架走审核流程
        MarketPlugin market = new MarketPlugin();
        market.setPluginCode(spec.code());
        market.setName(spec.name());
        market.setAuthorId(1L);
        market.setCategory(spec.category());
        market.setCateId(spec.cateId());
        market.setDescription(spec.description());
        market.setShortDesc(spec.description().length() > 256
                ? spec.description().substring(0, 256) : spec.description());
        market.setLatestVersion(spec.version());
        market.setLicenseType(spec.licenseType());
        market.setPriceCents(spec.priceCents());
        market.setTrialDays(0);
        market.setStatus(0);
        market.setMinFrameworkVersion(DEFAULT_VERSION);
        market.setTagsJson(JSON.toJSONString(spec.tags()));
        market.setTerminalsJson(terminalsJson);
        market.setDependenciesJson("[]");
        marketPluginMapper.insert(market);

        // 3. 市场版本：保存 plugin.yaml 全文；制品待构建（jar_url/md5 置空，status=0 不参与安装）
        MarketPluginVersion versionRow = new MarketPluginVersion();
        versionRow.setPluginCode(spec.code());
        versionRow.setVersion(spec.version());
        versionRow.setJarUrl("");
        versionRow.setJarMd5("");
        versionRow.setChangelog("脚手架初始化，制品待构建（.houplugin 上架流程见文档 §7）");
        versionRow.setPluginYaml(pluginYaml(spec));
        versionRow.setCompatibilityJson("{\"minFrameworkVersion\":\"" + DEFAULT_VERSION + "\"}");
        versionRow.setStatus(0);
        marketPluginVersionMapper.insert(versionRow);
    }

    // ---------------------------------------------------------------- 工具

    private static String text(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private static int intOf(Object value, int fallback) {
        if (value == null || !StringUtils.hasText(value.toString())) {
            return fallback;
        }
        return Integer.parseInt(value.toString().trim());
    }

    private static List<String> stringList(Object value, List<String> fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).map(String::trim)
                    .filter(StringUtils::hasText).toList();
        }
        return fallback;
    }

    private void cleanupQuietly(Path serviceDir) {
        if (!Files.exists(serviceDir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(serviceDir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    log.warn("清理脚手架目录失败: {}", path);
                }
            });
        } catch (IOException e) {
            log.warn("清理脚手架目录失败: {}", serviceDir, e);
        }
    }
}
