package com.houjiang.mall.service.plugin;

import com.houjiang.plugin.api.ext.ExtensionTableNaming;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * classpath 版二开 schema 迁移执行器（R2.1）。
 *
 * <p><b>与既有 {@link SchemaMigrationRunner} 并列，互不改动</b>：既有执行器只处理 <b>PF4J 安装包 jar 内</b>
 * {@code plugin.yaml} 声明的 {@code tables.migrate}；而二开是<b>构建期模块、没有安装包</b>，
 * 其迁移脚本直接落在自身 JAR 的 classpath 里，因此需要本执行器扫描 classpath 资源。
 *
 * <p>脚本落点约定：{@code backend/extensions/{module}/src/main/resources/db/migration/{code}/}
 * → classpath 上为 {@code db/migration/{code}/*.sql}；回滚脚本固定名 {@value #ROLLBACK_SCRIPT}。
 * 采用 {@code {code}} 子目录而非扁平 {@code db/migration/}，是因为宿主 classpath 会聚合
 * <b>全部</b>二开模块的 {@code db/migration/}，扁平会导致脚本归属（code）无法判定、并跨模块撞名。
 *
 * <p>执行语义：
 * <ol>
 *   <li>按文件名升序（{@code V1__} → {@code V2__}）逐脚本执行；</li>
 *   <li>执行前用 {@link ExtensionTableNaming} 校验脚本引用的每张表均为 {@code hou_ext_} 前缀，
 *       越权表名<b>执行前拦截</b>并 fail-fast；</li>
 *   <li>幂等：{@link ExtensionMigrationRepository#isApplied} 命中（成功 + 校验和一致）则跳过，
 *       <b>重复执行零副作用</b>；脚本体变更（校验和变化）会重新执行；</li>
 *   <li>失败：记录 {@code FAILED} 留痕，并尽力执行同目录 {@value #ROLLBACK_SCRIPT}，随后抛出交由上层处理；</li>
 *   <li>{@link #migrateAll()} 从 {@code META-INF/houcloud-extension.properties} 发现全部二开 code 并逐个迁移。</li>
 * </ol>
 *
 * <p><b>边界（诚实说明）</b>：MySQL DDL 非事务，失败回滚<b>不自动删已建表</b>；{@value #ROLLBACK_SCRIPT}
 * 是二开作者显式提供的补偿脚本，缺省则仅置 {@code FAILED} 留痕（默认不删表）。真实 DB 执行属
 * 「必须真实环境验收」项。
 *
 * @author Amos
 */
@Slf4j
public class ExtensionSchemaMigrationRunner {

    /** 二开迁移脚本 classpath 定位模板，{@code %s} 为二开 code。 */
    public static final String SCRIPT_LOCATION_PATTERN = "classpath*:db/migration/%s/*.sql";

    /** 二开交付描述 classpath 定位（用于 {@link #migrateAll()} 发现 code）。 */
    public static final String DESCRIPTOR_PATTERN = "classpath*:META-INF/houcloud-extension.properties";

    /** 回滚脚本约定名。 */
    public static final String ROLLBACK_SCRIPT = "rollback.sql";

    /** 校验表名前先剥离行注释，避免注释中的表名造成误判（仅用于校验，不影响执行原文）。 */
    private static final Pattern LINE_COMMENT = Pattern.compile("(?:--|#)[^\\n]*");

    /** 抽取 DDL/DML 引用的表名（与既有 {@link SchemaMigrationRunner} 同款策略，去掉反引号）。 */
    private static final Pattern TABLE_REF = Pattern.compile(
            "\\b(?:CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?|DROP\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?|"
                    + "ALTER\\s+TABLE\\s+|INSERT\\s+INTO\\s+|REPLACE\\s+INTO\\s+|DELETE\\s+FROM\\s+|UPDATE\\s+)"
                    + "`?([A-Za-z0-9_]+)`?",
            Pattern.CASE_INSENSITIVE);

    private final DataSource dataSource;
    private final ExtensionMigrationRepository repository;
    private final ResourcePatternResolver resourceResolver;

    public ExtensionSchemaMigrationRunner(DataSource dataSource, ExtensionMigrationRepository repository) {
        this(dataSource, repository, new PathMatchingResourcePatternResolver());
    }

    public ExtensionSchemaMigrationRunner(DataSource dataSource,
                                          ExtensionMigrationRepository repository,
                                          ResourcePatternResolver resourceResolver) {
        this.dataSource = dataSource;
        this.repository = repository;
        this.resourceResolver = resourceResolver;
    }

    // ------------------------------------------------------------------
    // 编排
    // ------------------------------------------------------------------

    /**
     * 迁移单个二开模块（幂等）。
     *
     * @param code    二开模块 code（等于 {@code houcloud-extension.properties} 的 {@code code}）
     * @param version 二开版本
     * @return 本次执行结果
     */
    public Result migrate(String code, String version) {
        if (!StringUtils.hasText(code)) {
            log.warn("二开 schema 迁移跳过：code 为空");
            return new Result(code, version, 0, 0, 0);
        }
        List<Resource> scripts = discoverScripts(code);
        if (scripts.isEmpty()) {
            log.info("二开 {} 无 db/migration/{} 脚本，跳过 schema 迁移", code, code);
            return new Result(code, version, 0, 0, 0);
        }

        int executed = 0;
        int skipped = 0;
        int failed = 0;
        String currentScript = null;
        String currentChecksum = null;
        try {
            for (Resource script : scripts) {
                String name = script.getFilename();
                if (ROLLBACK_SCRIPT.equals(name)) {
                    continue; // 回滚脚本仅失败时执行，不参与正向迁移
                }
                String sql = read(script);
                currentScript = name;
                currentChecksum = sha256(sql);
                validateTables(code, name, sql);
                if (repository.isApplied(code, name, currentChecksum)) {
                    skipped++;
                    log.debug("二开 {} 迁移脚本 {} 已应用（校验和一致），跳过", code, name);
                    continue;
                }
                executeSql(sql);
                repository.markSuccess(code, version, name, currentChecksum);
                executed++;
                log.info("二开 {} 迁移脚本 {} 执行成功", code, name);
            }
            if (executed == 0) {
                log.info("二开 {} schema 已是最新（跳过 {} 个脚本，零副作用）", code, skipped);
            }
            return new Result(code, version, executed, skipped, failed);
        } catch (RuntimeException e) {
            failed = 1;
            if (currentScript != null) {
                repository.markFailure(code, version, currentScript, currentChecksum, truncate(e.getMessage()));
            }
            runRollback(code, scripts);
            throw e;
        }
    }

    /**
     * 发现 classpath 上全部二开模块并逐个迁移（幂等）。
     *
     * @return 每个二开模块的结果
     */
    public List<Result> migrateAll() {
        List<Result> results = new ArrayList<>();
        for (Map.Entry<String, String> entry : discoverExtensionDescriptors().entrySet()) {
            results.add(migrate(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    // ------------------------------------------------------------------
    // classpath 发现
    // ------------------------------------------------------------------

    /** 扫描 classpath 上某二开模块的迁移脚本，按文件名升序返回（{@value #ROLLBACK_SCRIPT} 亦包含在内）。 */
    protected List<Resource> discoverScripts(String code) {
        if (resourceResolver == null) {
            return List.of();
        }
        String pattern = String.format(SCRIPT_LOCATION_PATTERN, code);
        try {
            Resource[] resources = resourceResolver.getResources(pattern);
            List<Resource> scripts = new ArrayList<>();
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (resource.isReadable() && filename != null && filename.endsWith(".sql")) {
                    scripts.add(resource);
                }
            }
            scripts.sort(Comparator.comparing(Resource::getFilename));
            return scripts;
        } catch (IOException e) {
            throw new IllegalStateException("扫描二开 " + code + " 迁移脚本失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从 {@code META-INF/houcloud-extension.properties} 发现全部二开 code → version。
     *
     * <p>实现委托给与专属配置装载器共用的 {@link ExtensionDescriptorScanner}，避免两处发现口径漂移。
     */
    protected Map<String, String> discoverExtensionDescriptors() {
        return ExtensionDescriptorScanner.discover(resourceResolver);
    }

    // ------------------------------------------------------------------
    // 校验 / 执行 / 回滚
    // ------------------------------------------------------------------

    /** 校验脚本引用的每张表均为 {@code hou_ext_} 前缀（复用契约侧唯一事实来源）。 */
    void validateTables(String code, String script, String sql) {
        for (String table : extractTableNames(sql)) {
            try {
                ExtensionTableNaming.validate(table);
            } catch (IllegalStateException e) {
                throw new IllegalStateException(
                        "二开 " + code + " 迁移脚本 " + script + " 触碰非授权表名：" + table
                                + "（应全部以 '" + ExtensionTableNaming.PREFIX + "' 开头）", e);
            }
        }
    }

    /** 抽取 CREATE/DROP/ALTER TABLE、INSERT/REPLACE INTO、DELETE FROM、UPDATE 引用的表名（去反引号）。 */
    Set<String> extractTableNames(String sql) {
        Set<String> names = new LinkedHashSet<>();
        if (sql == null) {
            return names;
        }
        Matcher matcher = TABLE_REF.matcher(LINE_COMMENT.matcher(sql).replaceAll(" "));
        while (matcher.find()) {
            String name = matcher.group(1);
            if (StringUtils.hasText(name)) {
                names.add(name.replace("`", "").trim());
            }
        }
        return names;
    }

    /** 执行一段多语句 SQL（默认经 {@link ResourceDatabasePopulator}，遇错即停）；protected 便于单测打桩。 */
    protected void executeSql(String sql) {
        if (dataSource == null) {
            throw new IllegalStateException("未装配 DataSource，无法执行二开 schema 迁移");
        }
        ResourceDatabasePopulator populator =
                new ResourceDatabasePopulator(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
        populator.setSeparator(";");
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }

    /** 尽力执行同目录 {@value #ROLLBACK_SCRIPT}（存在才跑）；缺省则仅留痕，默认不删表。 */
    private void runRollback(String code, List<Resource> scripts) {
        for (Resource resource : scripts) {
            if (!ROLLBACK_SCRIPT.equals(resource.getFilename())) {
                continue;
            }
            try {
                executeSql(read(resource));
                log.info("二开 {} 迁移失败，已执行补偿脚本 {}", code, ROLLBACK_SCRIPT);
            } catch (RuntimeException e) {
                log.warn("二开 {} 补偿脚本 {} 执行失败：{}", code, ROLLBACK_SCRIPT, e.getMessage());
            }
            return;
        }
        log.info("二开 {} 无 {}，迁移失败仅置 FAILED，保留已建表数据（默认不删表）", code, ROLLBACK_SCRIPT);
    }

    private String read(Resource script) {
        try (InputStream in = script.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取迁移脚本失败：" + script.getFilename() + "（" + e.getMessage() + "）", e);
        }
    }

    private String sha256(String content) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    /**
     * 单次迁移结果。
     *
     * @param code     二开 code
     * @param version  二开版本
     * @param executed 本次新执行的脚本数
     * @param skipped  因幂等跳过的脚本数
     * @param failed   失败标记（0=无，1=有）
     */
    public record Result(String code, String version, int executed, int skipped, int failed) {

        /** 未产生任何写入（幂等命中或无脚本）。 */
        public boolean noop() {
            return executed == 0 && failed == 0;
        }
    }
}
