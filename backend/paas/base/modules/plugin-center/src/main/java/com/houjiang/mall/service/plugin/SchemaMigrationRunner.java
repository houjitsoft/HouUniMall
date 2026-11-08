package com.houjiang.mall.service.plugin;

import com.houjiang.mall.entity.plugin.AddonPluginMigration;
import com.houjiang.mall.mapper.plugin.AddonPluginMigrationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import com.houjiang.plugin.engine.spi.PluginMigrationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 插件 schema 迁移执行器（P0-4，宿主 mall 侧实现引擎 SPI {@link PluginMigrationPort}）。
 *
 * <p>引擎在 {@code PluginLifecycleManager.install()} 的 {@code VERIFIED → MIGRATED} 之间回调
 * {@link #migrate(String, String)}；引擎不持有数据源，真正的建表/校验/审计由本类在宿主侧完成，
 * 迁移失败抛出 {@link RuntimeException} 交引擎整体回滚并置 {@code FAILED}。
 *
 * <p>执行步骤：
 * <ol>
 *   <li>按 {@code PluginArtifactStore.resolve(code, version)} 定位落盘安装包；</li>
 *   <li>读包内 {@code plugin.yaml} 的 {@code tables}（{@code prefix} + {@code allow} + {@code migrate}
 *       脚本列表）；未声明 {@code tables.migrate} 则跳过（内置目录插件无 schema）；</li>
 *   <li><b>幂等</b>：{@code hou_addon_plugin_migration} 已有 {@code (code, version)} 成功记录则跳过；</li>
 *   <li><b>前缀隔离校验</b>：脚本内所有被引用表名必须以声明前缀开头（或以 {@code tables.allow}
 *       中显式登记的额外前缀开头），杜绝越权触碰宿主/他插件表。正向脚本与回滚/补偿脚本
 *       <b>都要过</b>这一关（见 {@link #validatePrefix(String, String, List, String)} 的调用点清单）；</li>
 *   <li>逐脚本执行（{@link ResourceDatabasePopulator}，遇错即停）→ 写成功审计；</li>
 *   <li>失败：写 {@code status=0} 审计 + 尽力执行包内 {@code rollback.sql}（通过同样的前缀校验才跑）
 *       → 抛出。</li>
 * </ol>
 *
 * <p>边界（诚实说明）：
 * <ul>
 *   <li>MySQL DDL 非事务，失败回滚<b>不自动删已建表</b>（SPI 契约：卸载/回滚默认保留数据）；
 *       {@code rollback.sql} 是插件作者显式提供的补偿脚本，缺省则仅置 FAILED 留痕；</li>
 *   <li>前缀校验基准取 manifest 声明的 {@code tables.prefix}（唯一事实来源），未声明才回落到约定
 *       {@code hou_plugin_{code}_}；ERP 声明 {@code hou_erp_} 即以其为准。历史上已有插件建表脚本
 *       含少量不带本插件前缀的表（如 oa 的 {@code hou_base_position}、webpos 的 {@code wps_*}、
 *       im 的 {@code im_cs_*}），这类表通过 {@code tables.allow} 逐条登记后才能通过校验——
 *       登记是显式的，不构成「前缀校验可被绕过」；</li>
 *   <li>{@code version} 取不到（描述符无版本，引擎传 {@code unknown}）时按目录契约定位不到安装包，
 *       记 WARN 并跳过，不阻断安装。</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@Service
public class SchemaMigrationRunner implements PluginMigrationPort {

    /** 安装包内清单条目名 */
    private static final String PLUGIN_YAML_ENTRY = "plugin.yaml";
    /** 迁移失败时尽力执行的补偿脚本候选（存在才跑） */
    private static final List<String> ROLLBACK_ENTRIES = List.of("db/rollback.sql", "rollback.sql");

    /** 迁移操作类型：安装（引擎 install 内部回调使用） */
    public static final int OPERATION_INSTALL = 1;

    /** 迁移操作类型：升级（M4 编排显式调用） */
    public static final int OPERATION_UPGRADE = 2;

    /** 迁移操作类型：回滚（M4 回滚路径） */
    public static final int OPERATION_ROLLBACK = 3;
    /** 前缀校验前先剥离行注释，避免注释中的表名/关键字造成误判（仅用于校验，不影响执行原文） */
    private static final Pattern LINE_COMMENT = Pattern.compile("(?:--|#)[^\\n]*");
    /**
     * 列定义里的自动更新时间戳子句（{@code `updated_at` datetime ON UPDATE CURRENT_TIMESTAMP}）。
     *
     * <p><b>为什么必须剥离</b>：{@link #TABLE_REF} 里 {@code UPDATE\s+} 这一支会把
     * {@code ON UPDATE CURRENT_TIMESTAMP} 当成「UPDATE 语句 + 表名 CURRENT_TIMESTAMP」抽出，
     * 于是前缀校验报「触碰非授权前缀表 CURRENT_TIMESTAMP」并抛异常 —— 这不是越权，
     * 但会让整个插件安装失败。生产库结构导出（{@code scripts/sql/init/plugins/*.sql}）里
     * cms / crm / finance / im / oa / orderfood / pm / webpos 都带这个子句，只有 erp 不带，
     * 所以这个误判此前一直没暴露。
     */
    private static final Pattern ON_UPDATE_TIMESTAMP = Pattern.compile(
            "\\bON\\s+UPDATE\\s+(?:CURRENT_TIMESTAMP|LOCAL_TIMESTAMP|LOCALTIME|NOW\\s*\\(\\s*\\))"
                    + "(?:\\s*\\(\\s*\\d*\\s*\\))?",
            Pattern.CASE_INSENSITIVE);
    /** 抽取 DDL/DML 引用的表名（去反引号） */
    private static final Pattern TABLE_REF = Pattern.compile(
            "\\b(?:CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?|DROP\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?|"
                    + "ALTER\\s+TABLE\\s+|INSERT\\s+INTO\\s+|REPLACE\\s+INTO\\s+|DELETE\\s+FROM\\s+|UPDATE\\s+)"
                    + "`?([A-Za-z0-9_]+)`?",
            Pattern.CASE_INSENSITIVE);
    /** message 列宽（varchar(1024)）截断阈值 */
    private static final int MESSAGE_MAX = 1000;

    /** 归档表名标记。 */
    private static final String ARCHIVE_MARKER = "_bak_";

    /** 归档后缀格式：{@code _bak_yyyyMMddHHmmss}。 */
    private static final DateTimeFormatter ARCHIVE_SUFFIX_FORMAT =
            DateTimeFormatter.ofPattern("'_bak_'yyyyMMddHHmmss");

    /** MySQL 表名长度上限。 */
    private static final int MAX_TABLE_NAME_LENGTH = 64;

    /** 合法的表名前缀（用于防注入，前缀来自 plugin.yaml，属外部输入）。 */
    private static final Pattern SAFE_TABLE_PREFIX = Pattern.compile("[A-Za-z0-9_]+");

    /** 合法的表名（information_schema 返回值也再校验一次，防止同名字段被当成标识符拼进 DDL）。 */
    private static final Pattern SAFE_TABLE_NAME = Pattern.compile("[A-Za-z0-9_$]+");

    private final DataSource dataSource;
    private final PluginArtifactStore artifactStore;
    private final AddonPluginMigrationMapper migrationMapper;

    public SchemaMigrationRunner(DataSource dataSource,
                                 PluginArtifactStore artifactStore,
                                 AddonPluginMigrationMapper migrationMapper) {
        this.dataSource = dataSource;
        this.artifactStore = artifactStore;
        this.migrationMapper = migrationMapper;
    }

    @Override
    public void migrate(String pluginCode, String version) {
        if (!StringUtils.hasText(pluginCode)) {
            log.warn("schema 迁移跳过：pluginCode 为空");
            return;
        }
        String ver = StringUtils.hasText(version) ? version.trim() : "unknown";
        Path jarPath = resolveJar(pluginCode, ver);
        if (jarPath == null) {
            log.warn("schema 迁移跳过：未定位到安装包 {}@{}（version=unknown 或产物缺失）", pluginCode, ver);
            return;
        }
        TablesDecl tables = parseTables(readJarEntry(jarPath, PLUGIN_YAML_ENTRY));
        if (tables == null || tables.migrate().isEmpty()) {
            log.info("插件 {}@{} 未声明 tables.migrate，跳过 schema 迁移", pluginCode, ver);
            return;
        }
        if (migrationMapper != null && migrationMapper.countSuccess(pluginCode, ver) > 0) {
            log.info("插件 {}@{} 已成功迁移，跳过（幂等）", pluginCode, ver);
            return;
        }

        String prefix = tables.effectivePrefix(pluginCode);
        List<String> allow = tables.effectiveAllow();
        List<String> applied = new ArrayList<>();
        StringBuilder executed = new StringBuilder();
        try {
            for (String script : tables.migrate()) {
                String sql = readJarEntry(jarPath, script);
                if (sql == null) {
                    log.warn("迁移脚本 {} 不在安装包内，跳过该脚本：{}@{}", script, pluginCode, ver);
                    continue;
                }
                validatePrefix(sql, prefix, allow, pluginCode);
                executeSql(sql);
                applied.add(script);
                executed.append(sql).append('\n');
            }
            record(pluginCode, ver, String.join(",", applied), sha256(executed.toString()),
                    AddonPluginMigration.STATUS_SUCCESS, "ok");
            log.info("插件 {}@{} schema 迁移完成：执行 {}（前缀 {}）", pluginCode, ver, applied, prefix);
        } catch (RuntimeException e) {
            record(pluginCode, ver, String.join(",", applied), sha256(executed.toString()),
                    AddonPluginMigration.STATUS_FAILED, truncate(e.getMessage()));
            // 复用正向迁移同一份 prefix/allow：回滚脚本与正向脚本必须符合同一套隔离口径。
            runRollback(jarPath, pluginCode, prefix, allow);
            // 重新抛出：由引擎触发整体回滚（unloadPlugin）并置 FAILED，不留「表已建、插件未生效」中间态。
            throw e;
        }
    }

    /**
     * 升级迁移（M4，方案 §5.3 步骤 10）：执行目标版本的 {@code tables.migrate}，
     * 并以 {@code operation=2} + {@code from_version} 落审计。
     *
     * <p><b>为什么与 {@link #migrate} 分开而不是加参数</b>：{@code migrate} 是
     * {@link PluginMigrationPort} 的 SPI 实现，被引擎在每次 {@code install} 内部回调，
     * 语义固定为「安装（operation=1）」。升级是宿主编排层显式发起的另一个动作，
     * 走独立方法可以让两者各自演进，也不会让 SPI 接口为 M4 加参数。
     *
     * <p>幂等与 install 一致：{@code (code, version, operation, from_version)} 命中即覆盖，
     * 重复升级只保留最终态一行。
     *
     * @param pluginCode  插件编码
     * @param fromVersion 来源版本（升级前版本）
     * @param toVersion   目标版本（迁移脚本取自该版本的安装包）
     * @throws RuntimeException 迁移失败（调用方据此走回滚路径 A）
     */
    public void migrateUpgrade(String pluginCode, String fromVersion, String toVersion) {
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(toVersion)) {
            log.warn("升级迁移跳过：pluginCode 或 toVersion 为空");
            return;
        }
        String ver = toVersion.trim();
        String from = fromVersion == null ? "" : fromVersion.trim();
        Path jarPath = resolveJar(pluginCode, ver);
        if (jarPath == null) {
            log.warn("升级迁移跳过：未定位到安装包 {}@{}", pluginCode, ver);
            return;
        }
        TablesDecl tables = parseTables(readJarEntry(jarPath, PLUGIN_YAML_ENTRY));
        if (tables == null || tables.migrate().isEmpty()) {
            log.info("插件 {}@{} 未声明 tables.migrate，升级无 schema 变更，仅留审计", pluginCode, ver);
            recordOperation(pluginCode, ver, OPERATION_UPGRADE, from, null, "", sha256(""),
                    AddonPluginMigration.STATUS_SUCCESS, "no-migrate-script");
            return;
        }
        String prefix = tables.effectivePrefix(pluginCode);
        List<String> allow = tables.effectiveAllow();
        List<String> applied = new ArrayList<>();
        StringBuilder executed = new StringBuilder();
        try {
            for (String script : tables.migrate()) {
                String sql = readJarEntry(jarPath, script);
                if (sql == null) {
                    log.warn("迁移脚本 {} 不在安装包内，跳过该脚本：{}@{}", script, pluginCode, ver);
                    continue;
                }
                validatePrefix(sql, prefix, allow, pluginCode);
                executeSql(sql);
                applied.add(script);
                executed.append(sql).append('\n');
            }
            recordOperation(pluginCode, ver, OPERATION_UPGRADE, from, null, String.join(",", applied),
                    sha256(executed.toString()), AddonPluginMigration.STATUS_SUCCESS, "ok");
            log.info("插件 {}@{} 升级迁移完成：{}（前缀 {}，来源 {}）", pluginCode, ver, applied, prefix, from);
        } catch (RuntimeException e) {
            recordOperation(pluginCode, ver, OPERATION_UPGRADE, from, null, String.join(",", applied),
                    sha256(executed.toString()), AddonPluginMigration.STATUS_FAILED, truncate(e.getMessage()));
            runRollback(jarPath, pluginCode, prefix, allow);
            throw e;
        }
    }

    /**
     * 回滚迁移（M4，方案 §5.3 回滚路径）：执行补偿脚本并以 {@code operation=3} +
     * {@code rollback_of} 落审计。
     *
     * <p><b>回滚脚本取自哪个包</b>：取自<b>被回滚掉的那个版本</b>（{@code scriptVersion}）——
     * 要撤销的是它带来的 DDL，补偿脚本只可能由它的作者提供；
     * 而审计记录的 {@code version} 填的是<b>回滚到的版本</b>（{@code targetVersion}），
     * 这样「某版本当前处于什么迁移状态」按 version 一查即得。
     *
     * <p><b>为什么回滚脚本也必须过前缀隔离校验</b>（不然这里是最容易被忽略的攻击面）：
     * 正向脚本过审不等于整个包安全。攻击者可以提交一份<b>完全合规、能通过人工审计</b>的
     * {@code tables.migrate} 脚本，把破坏性语句（如 {@code DROP TABLE hou_user}）藏在
     * {@code rollback.sql} 里 —— 触发条件只需要「让这一次升级失败」，系统就会<b>自动</b>
     * 回滚并执行那段语句，全程无需人工介入。审计人员看的是正向脚本，回滚脚本是审计盲区。
     * 且这些语句以 mall 主数据源账号执行，一旦放行没有第二道防线。
     * 因此本方法执行前调用的是<b>与正向迁移同一个</b> {@link #validatePrefix(String, String, List, String)}，
     * 而不是一套回滚专用规则；校验不通过则拒绝执行并落 status=0 审计，<b>不静默跳过</b>
     * （静默跳过会让运维误以为回滚已成功）。
     *
     * <p><b>本方法刻意不抛异常</b>：schema 回滚失败不能阻止「把旧版本重新装回来」——
     * 后者才是回滚的真正目标。失败只落 status=0 审计 + ERROR 日志，交人工处理。
     * 若在这里抛出，一次「DDL 补偿脚本缺失」就会让插件永远停在已卸载状态，
     * 那是把可恢复的故障变成不可恢复的故障。
     *
     * @param pluginCode    插件编码
     * @param scriptVersion 提供 rollback.sql 的版本（被回滚掉的新版本）
     * @param targetVersion 回滚到的版本（审计记录的 version）
     * @param fromVersion   回滚自哪个版本（审计记录的 from_version）
     * @param rollbackOf    回滚自哪条迁移记录 ID，可为 null
     * @return true 表示补偿脚本存在且通过校验且执行成功
     */
    public boolean migrateRollback(String pluginCode, String scriptVersion, String targetVersion,
                                   String fromVersion, Long rollbackOf) {
        if (!StringUtils.hasText(pluginCode) || !StringUtils.hasText(targetVersion)) {
            log.warn("回滚迁移跳过：pluginCode 或 targetVersion 为空");
            return false;
        }
        String target = targetVersion.trim();
        String from = fromVersion == null ? "" : fromVersion.trim();
        String scriptSource = StringUtils.hasText(scriptVersion) ? scriptVersion.trim() : target;
        // rollback_of 缺省时自己查：被回滚掉的那条「升级迁移」记录的 ID。
        // 放在这里查而不是让调用方传，是因为只有本类知道 operation 与 from_version 的组合规则。
        Long effectiveRollbackOf = rollbackOf;
        if (effectiveRollbackOf == null && migrationMapper != null) {
            // ⚠️ 这里必须用 target（回滚到的版本）而不是 from 去反查被回滚的升级记录：
            // 「升级」审计行的口径是 (version=升到的版本, from_version=从哪里升上来)，
            // 而本方法的 from 指的是「这次回滚是从哪个版本退下来的」——恰好是那条升级记录的 version。
            // 若误用 from 去查 from_version，查的是「从自己升到自己」，必然查不到，
            // rollback_of 会一直为空，回滚链就断了（排障时无法回答「这次回滚撤销了哪次升级」）。
            effectiveRollbackOf = migrationMapper.selectId(pluginCode, scriptSource, OPERATION_UPGRADE, target);
        }
        Path jarPath = resolveJar(pluginCode, scriptSource);
        if (jarPath == null) {
            log.warn("回滚迁移跳过：未定位到安装包 {}@{}（无法取得 rollback.sql）", pluginCode, scriptSource);
            recordOperation(pluginCode, target, OPERATION_ROLLBACK, from, effectiveRollbackOf, "", sha256(""),
                    AddonPluginMigration.STATUS_FAILED, "rollback-script-missing");
            return false;
        }
        // 前缀口径必须来自正向迁移那一份 plugin.yaml（tables.prefix + tables.allow），
        // 而不是另立一套回滚专用规则：两套口径本身就是漏洞来源。
        TablesDecl tables = parseTables(readJarEntryQuietly(jarPath, PLUGIN_YAML_ENTRY));
        // 包内没有 tables 声明时 prefix 为 null，交给 validatePrefix 判空抛出 —— 正向迁移同样会在
        // 「未声明 tables.prefix」时拒绝执行，此时本来也不存在需要补偿的正向 DDL，fail-closed。
        String prefix = tables == null ? null : tables.effectivePrefix(pluginCode);
        List<String> allow = tables == null ? List.of() : tables.effectiveAllow();
        String sql = null;
        String entry = null;
        for (String candidate : ROLLBACK_ENTRIES) {
            String content = readJarEntryQuietly(jarPath, candidate);
            if (StringUtils.hasText(content)) {
                sql = content;
                entry = candidate;
                break;
            }
        }
        if (sql == null) {
            log.info("插件 {}@{} 无 rollback.sql，回滚不执行 DDL 补偿（保留已建表数据）", pluginCode, scriptSource);
            recordOperation(pluginCode, target, OPERATION_ROLLBACK, from, effectiveRollbackOf, "", sha256(""),
                    AddonPluginMigration.STATUS_SUCCESS, "no-rollback-script");
            return true;
        }
        try {
            validatePrefix(sql, prefix, allow, pluginCode);
            executeSql(sql);
            recordOperation(pluginCode, target, OPERATION_ROLLBACK, from, effectiveRollbackOf, entry, sha256(sql),
                    AddonPluginMigration.STATUS_SUCCESS, "ok");
            log.info("插件 {} 回滚迁移完成：执行 {}（回滚到 {}，回滚自 {}）", pluginCode, entry, target, from);
            return true;
        } catch (RuntimeException e) {
            recordOperation(pluginCode, target, OPERATION_ROLLBACK, from, effectiveRollbackOf, entry, sha256(sql),
                    AddonPluginMigration.STATUS_FAILED, truncate(e.getMessage()));
            log.error("插件 {} 回滚迁移失败（不阻断重装旧版本），需人工处理 DDL：{}", pluginCode, e.getMessage());
            return false;
        }
    }

    /**
     * 清除插件的全部迁移审计记录（卸载时调用）。
     *
     * <p><b>为什么卸载必须清这行</b>：迁移是幂等的（按 {@code (plugin_code, version, script)} 判重），
     * 卸载后业务表被归档或保留，但审计行若还留着，重新安装同一版本时会判定
     * 「已迁移过」直接跳过建表 —— 结果就是插件装上了、菜单有了，一查数据报「表不存在」。
     * 这个故障现象离根因很远，很难排查，因此卸载必须把审计一起清掉。
     *
     * <p>清的是审计元数据，不是业务数据，风险可控。
     *
     * @param pluginCode 插件编码
     * @return 清除的记录条数
     */
    public int forgetMigrations(String pluginCode) {
        if (!StringUtils.hasText(pluginCode)) {
            log.warn("清除迁移审计跳过：插件编码为空");
            return 0;
        }
        if (migrationMapper == null) {
            return 0;
        }
        String code = pluginCode.trim();
        int deleted = migrationMapper.delete(new LambdaQueryWrapper<AddonPluginMigration>()
                .eq(AddonPluginMigration::getPluginCode, code));
        log.info("插件 {} 卸载：已清除 {} 条迁移审计记录", code, deleted);
        return deleted;
    }

    /**
     * 归档插件业务表（{@code RENAME} 而不是 {@code DROP}）。
     *
     * <p><b>为什么是 RENAME 不是 DROP</b>：卸载是高频误操作场景（试装、装错版本、
     * 清理空间后马上又装回来）。DROP 一旦执行就无法挽回，而 RENAME 留了退路：
     * 需要时把 {@code _bak_} 表改回原名即可完整恢复。归档表会被 mysqldump 一起带走，
     * 由运维按保留策略定期清理。
     *
     * <p><b>为什么不在卸载流程里默认调用</b>：删表是不可逆动作，且部分插件的表可能
     * 被别的模块引用。因此它只在调用方显式要求回收数据时才执行，默认卸载保留业务表。
     *
     * @param tablePrefix 插件声明的表名前缀（{@code plugin.yaml} 的 {@code tables.prefix}）
     * @return 成功归档的表数量
     * @throws IllegalArgumentException 前缀为空或含非法字符时抛出
     */
    public int archiveTables(String tablePrefix) {
        if (!StringUtils.hasText(tablePrefix)) {
            throw new IllegalArgumentException("插件表名前缀不能为空");
        }
        String prefix = tablePrefix.trim();
        if (!SAFE_TABLE_PREFIX.matcher(prefix).matches()) {
            throw new IllegalArgumentException("插件表名前缀只允许字母、数字、下划线，实际值：" + prefix);
        }
        String suffix = ARCHIVE_SUFFIX_FORMAT.format(LocalDateTime.now());
        List<String> tables = listTables(prefix);
        int archived = 0;
        for (String table : tables) {
            // 上一次卸载留下的归档表不再二次改名，否则会出现 _bak_2026..._bak_2026... 的无限堆叠
            if (table.contains(ARCHIVE_MARKER)) {
                continue;
            }
            String target = fitTableName(table, suffix);
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("RENAME TABLE `" + table + "` TO `" + target + "`");
                archived++;
                log.info("插件业务表已归档：{} -> {}", table, target);
            } catch (SQLException e) {
                // 单表失败不中断其它表：部分归档也比全部失败好，且失败原因逐表可见
                log.error("归档插件业务表失败：table={} message={}", table, e.getMessage());
            }
        }
        return archived;
    }

    /**
     * 列出当前库下匹配前缀的表名。
     *
     * <p>用 {@code information_schema} 而不是 {@code SHOW TABLES}：前者可以用参数化查询，
     * 前缀只作为 {@code LIKE} 的绑定值，不存在拼字符串被注入的空间。
     */
    private List<String> listTables(String prefix) {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_name LIKE ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prefix + "%");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (StringUtils.hasText(name) && SAFE_TABLE_NAME.matcher(name).matches()) {
                        tables.add(name);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("查询插件业务表失败：prefix={} message={}", prefix, e.getMessage());
        }
        return tables;
    }

    /**
     * 生成归档表名；超过 MySQL 64 字符上限时截断原名再拼后缀。
     */
    private static String fitTableName(String table, String suffix) {
        if (table.length() + suffix.length() <= MAX_TABLE_NAME_LENGTH) {
            return table + suffix;
        }
        int keep = MAX_TABLE_NAME_LENGTH - suffix.length();
        return table.substring(0, Math.max(1, keep)) + suffix;
    }

    /**
     * 落一条带操作类型的迁移审计。
     *
     * <p>写入失败的处理与 {@link #record} 同口径：成功却写不进审计会破坏幂等保证，必须暴露；
     * 失败留痕写不进只告警（不能因为审计写不进去而把原故障掩盖掉）。
     */
    private void recordOperation(String pluginCode, String version, int operation, String fromVersion,
                                 Long rollbackOf, String script, String checksum, int status, String message) {
        if (migrationMapper == null) {
            return;
        }
        try {
            migrationMapper.upsertOperation(pluginCode, version, operation, fromVersion, rollbackOf,
                    script, checksum, status, message);
        } catch (RuntimeException e) {
            if (status == AddonPluginMigration.STATUS_SUCCESS) {
                throw new IllegalStateException("迁移已成功但写入 hou_addon_plugin_migration 失败: "
                        + e.getMessage(), e);
            }
            log.warn("写入迁移失败留痕出错 {}@{} op={}：{}", pluginCode, version, operation, e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 安装包 / 清单读取
    // ------------------------------------------------------------------

    private Path resolveJar(String pluginCode, String version) {
        if (artifactStore == null) {
            return null;
        }
        try {
            Path path = artifactStore.resolve(pluginCode, version);
            return Files.isRegularFile(path) ? path : null;
        } catch (RuntimeException e) {
            log.warn("解析安装包路径失败 {}@{}：{}", pluginCode, version, e.getMessage());
            return null;
        }
    }

    /** 从安装包（jar/zip）读取文本条目；条目不存在返回 {@code null}，读取 IO 异常抛出。 */
    private String readJarEntry(Path jarPath, String entryName) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry entry = jar.getJarEntry(entryName);
            if (entry == null) {
                return null;
            }
            try (InputStream in = jar.getInputStream(entry)) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取安装包条目失败: " + entryName + "（" + e.getMessage() + "）", e);
        }
    }

    // ------------------------------------------------------------------
    // plugin.yaml tables 解析（包级可见，便于单测）
    // ------------------------------------------------------------------

    /** 解析 plugin.yaml 的 {@code tables} 段（prefix + allow + migrate 列表）；无 tables 返回 {@code null}。 */
    TablesDecl parseTables(String pluginYaml) {
        if (!StringUtils.hasText(pluginYaml)) {
            return null;
        }
        Object root;
        try {
            root = new Yaml().load(pluginYaml);
        } catch (RuntimeException e) {
            log.warn("plugin.yaml 解析失败，按无 tables 处理：{}", e.getMessage());
            return null;
        }
        if (!(root instanceof Map<?, ?> map) || !(map.get("tables") instanceof Map<?, ?> tables)) {
            return null;
        }
        Object prefixObj = tables.get("prefix");
        String prefix = prefixObj == null ? null : String.valueOf(prefixObj).trim();
        List<String> allow = readStringList(tables.get("allow"));
        List<String> migrate = readStringList(tables.get("migrate"));
        return new TablesDecl(prefix, allow, migrate);
    }

    /** 把 YAML 值读成字符串列表：支持列表与单值两种写法，过滤空白项。 */
    private List<String> readStringList(Object value) {
        List<String> items = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && StringUtils.hasText(String.valueOf(item))) {
                    items.add(String.valueOf(item).trim());
                }
            }
        } else if (value != null && StringUtils.hasText(String.valueOf(value))) {
            items.add(String.valueOf(value).trim());
        }
        return items;
    }

    // ------------------------------------------------------------------
    // 前缀隔离校验（包级可见，便于单测）
    // ------------------------------------------------------------------

    /**
     * 前缀隔离校验：schema 内所有被 DDL/DML 引用的表名必须以声明前缀开头，
     * 防止插件越权触碰宿主或其它插件的表（说明文档 §7.4.2 数据隔离）。违规抛异常触发整体回滚。
     *
     * @see #validatePrefix(String, String, List, String)
     */
    void validatePrefix(String sql, String prefix, String pluginCode) {
        validatePrefix(sql, prefix, List.of(), pluginCode);
    }

    /**
     * 前缀隔离校验（带额外放行前缀）。
     *
     * <p>{@code allow} 是 manifest 里 {@code tables.allow} 显式登记的表名前缀，用于少数历史建表
     * 脚本中确实由该插件创建、但不带本插件主前缀的表（oa 的 {@code hou_base_position}、
     * webpos 的 {@code wps_*}、im 的 {@code im_cs_*} 等）。空则行为与单前缀版完全一致。
     *
     * <p><b>所有投递到数据源的插件脚本都必须过这一关，正向和回滚都不能例外</b>。
     * 调用点：{@link #migrate}、{@link #migrateUpgrade}（正向脚本）、
     * {@link #migrateRollback}、{@link #runRollback}（回滚/补偿脚本）。
     * 回滚路径尤其不能漏 —— 它是机器自动触发、人工不复核的路径，详见
     * {@link #migrateRollback} 的说明。新增任何执行分支时，必须同步在这里的调用点清单里补齐。
     */
    void validatePrefix(String sql, String prefix, List<String> allow, String pluginCode) {
        if (!StringUtils.hasText(prefix)) {
            throw new IllegalStateException("插件 " + pluginCode + " 未声明 tables.prefix，拒绝迁移（无法保证表隔离）");
        }
        Set<String> offenders = new LinkedHashSet<>();
        for (String table : extractTableNames(sql)) {
            if (table.startsWith(prefix)) {
                continue;
            }
            if (allow != null && allow.stream().anyMatch(a -> StringUtils.hasText(a) && table.startsWith(a))) {
                continue;
            }
            offenders.add(table);
        }
        if (!offenders.isEmpty()) {
            throw new IllegalStateException("插件 " + pluginCode + " 的 schema 触碰了非授权前缀表（应全部以 '"
                    + prefix + "' 开头" + (allow == null || allow.isEmpty() ? "" : "，或 tables.allow 已登记的前缀")
                    + "）：" + offenders);
        }
    }

    /** 抽取 CREATE/DROP/ALTER TABLE、INSERT/REPLACE INTO、DELETE FROM、UPDATE 引用的表名（去反引号）。 */
    Set<String> extractTableNames(String sql) {
        Set<String> names = new LinkedHashSet<>();
        if (sql == null) {
            return names;
        }
        // 先剥注释，再剥列级 `ON UPDATE CURRENT_TIMESTAMP`（否则会被当成 UPDATE 语句抽表名），
        // 两步都只作用于校验副本，执行的 SQL 原文不受影响。
        String normalized = ON_UPDATE_TIMESTAMP.matcher(LINE_COMMENT.matcher(sql).replaceAll(" ")).replaceAll(" ");
        Matcher matcher = TABLE_REF.matcher(normalized);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (StringUtils.hasText(name)) {
                names.add(name.replace("`", "").trim());
            }
        }
        return names;
    }

    // ------------------------------------------------------------------
    // 执行 / 审计 / 回滚
    // ------------------------------------------------------------------

    /** 执行一段多语句 SQL（默认经 {@link ResourceDatabasePopulator}，遇错即停）。protected 便于单测打桩。 */
    protected void executeSql(String sql) {
        if (dataSource == null) {
            throw new IllegalStateException("未装配 DataSource，无法执行插件 schema 迁移");
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
        populator.setSeparator(";");
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }

    private void record(String pluginCode, String version, String script, String checksum, int status, String message) {
        if (migrationMapper == null) {
            return;
        }
        AddonPluginMigration row = new AddonPluginMigration();
        row.setPluginCode(pluginCode);
        row.setVersion(version);
        row.setScript(script);
        row.setChecksum(checksum);
        row.setStatus(status);
        row.setMessage(message);
        try {
            migrationMapper.upsert(row);
        } catch (RuntimeException e) {
            // 成功迁移却写不进审计会破坏幂等保证，必须暴露让引擎整体回滚；失败留痕写不进则只告警。
            if (status == AddonPluginMigration.STATUS_SUCCESS) {
                throw new IllegalStateException("迁移已成功但写入 hou_addon_plugin_migration 失败: " + e.getMessage(), e);
            }
            log.warn("写入迁移失败留痕出错 {}@{}：{}", pluginCode, version, e.getMessage());
        }
    }

    /**
     * 尽力执行包内 rollback.sql（存在才跑）；缺省则仅置 FAILED，保留已建表数据（默认不删表）。
     *
     * <p><b>为什么这里也必须做前缀隔离校验</b>：这是一个自动触发、无人复核的执行路径 ——
     * 只要正向迁移失败一次，本方法就会自动读取并执行包内的补偿脚本。攻击者可以提交一份
     * <b>正向脚本完全合规、能通过人工审计</b>的插件，把 {@code DROP TABLE hou_user} 这类破坏性语句
     * 只写在 {@code rollback.sql} 里，再故意让这一次升级失败：本方法就会被自动触发并立刻
     * 执行那段语句，全程无需人工介入，而且是以 mall 主数据源账号执行。
     * 审计看的是正向脚本，回滚脚本是天然盲区。
     *
     * <p><b>为什么复用调用方传进来的 prefix/allow 而不是自己再解析一次 plugin.yaml</b>：
     * 这两个入参就是正向迁移刚刚用来判定「哪些表允许碰」的同一组值，
     * 从调用方透传可以保证两者口径永远一致 —— 一旦各解一份，就是两份可能漂移的事实来源。
     *
     * @param prefix 正向迁移已算出的有效前缀（{@code tables.prefix} 或约定回落值）
     * @param allow  正向迁移已算出的 {@code tables.allow} 额外放行前缀
     */
    private void runRollback(Path jarPath, String pluginCode, String prefix, List<String> allow) {
        for (String candidate : ROLLBACK_ENTRIES) {
            String sql = readJarEntryQuietly(jarPath, candidate);
            if (!StringUtils.hasText(sql)) {
                continue;
            }
            try {
                validatePrefix(sql, prefix, allow, pluginCode);
            } catch (RuntimeException e) {
                // 拒绝执行，不做任何降级尝试：越权的补偿脚本一旦投递到主库就无法收回。
                // 这里不能抛 —— 本方法被 migrate/migrateUpgrade 的 catch 块调用，抛出会替换掉
                // 正在向上传播的原始迁移异常，把真正的失败原因弄丢；迁移审计此时已是 FAILED，
                // 「补偿脚本被拒」这件事本身不算新的故障状态，因此记 ERROR 交人工核查即可。
                log.error("插件 {} 的补偿脚本 {} 未通过前缀隔离校验，已拒绝执行（疑似越权 DDL，请立即人工核查该插件包）：{}",
                        pluginCode, candidate, e.getMessage());
                return;
            }
            try {
                executeSql(sql);
                log.info("插件 {} 迁移失败，已执行补偿脚本 {}", pluginCode, candidate);
            } catch (RuntimeException e) {
                log.warn("插件 {} 补偿脚本 {} 执行失败：{}", pluginCode, candidate, e.getMessage());
            }
            return;
        }
        log.info("插件 {} 无 rollback.sql，迁移失败仅置 FAILED，保留已建表数据（默认不删表）", pluginCode);
    }

    private String readJarEntryQuietly(Path jarPath, String entryName) {
        try {
            return readJarEntry(jarPath, entryName);
        } catch (RuntimeException e) {
            log.warn("读取补偿脚本 {} 失败：{}", entryName, e.getMessage());
            return null;
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
        return message.length() <= MESSAGE_MAX ? message : message.substring(0, MESSAGE_MAX);
    }

    /**
     * plugin.yaml 的 {@code tables} 声明。
     *
     * @param prefix  表前缀（可空，空时由 {@link #effectivePrefix} 回落到约定）
     * @param allow   额外放行的表名前缀（可空），用于少数不带主前缀但确由本插件建的表
     * @param migrate 迁移脚本在包内的相对路径列表
     */
    record TablesDecl(String prefix, List<String> allow, List<String> migrate) {
        /** 有效前缀：manifest 声明优先，缺省回落约定 {@code hou_plugin_{code}_}。 */
        String effectivePrefix(String pluginCode) {
            return StringUtils.hasText(prefix) ? prefix : ("hou_plugin_" + pluginCode + "_");
        }

        /** 额外放行前缀，永不为 null，便于调用方直接用。 */
        List<String> effectiveAllow() {
            return allow == null ? List.of() : allow;
        }
    }
}
