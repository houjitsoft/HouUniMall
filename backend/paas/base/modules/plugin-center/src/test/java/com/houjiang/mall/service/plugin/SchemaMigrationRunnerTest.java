package com.houjiang.mall.service.plugin;

import com.houjiang.mall.entity.plugin.AddonPluginMigration;
import com.houjiang.mall.mapper.plugin.AddonPluginMigrationMapper;
import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link SchemaMigrationRunner} 单测（P0-4）。
 *
 * <p>用真实临时 jar 覆盖「读包内 plugin.yaml / schema」路径；用 Mockito spy 打桩 {@code executeSql}
 * 规避真实数据库；重点验证：tables 解析、前缀隔离校验（执行前拦截）、幂等跳过、成功审计、
 * 失败留痕 + 补偿脚本、无产物/无 tables 的优雅跳过。
 *
 * @author Amos
 */
class SchemaMigrationRunnerTest {

    private static final String YAML_WITH_TABLES = """
            code: erp
            version: 1.0.0
            tables:
              prefix: hou_erp_
              migrate:
                - db/V1__test_schema.sql
            """;

    private static final String OWN_SCHEMA = """
            -- 只碰自己前缀的表
            DROP TABLE IF EXISTS `hou_erp_goods`;
            CREATE TABLE `hou_erp_goods` (`id` bigint PRIMARY KEY);
            INSERT INTO `hou_erp_goods` (`id`) VALUES (1);
            """;

    private static final String FOREIGN_SCHEMA = """
            CREATE TABLE `hou_erp_goods` (`id` bigint PRIMARY KEY);
            UPDATE `hou_user` SET name = 'x';
            """;

    /**
     * 藏在 rollback.sql 里的越权语句：这是安全审查第 8 项要挡的场景 ——
     * 正向脚本可以完全合规并通过人工审计，破坏只写在无人复核的补偿脚本里。
     */
    private static final String FOREIGN_ROLLBACK = "UPDATE `hou_user` SET deleted = 1;";

    // ------------------------------------------------------------------
    // parseTables
    // ------------------------------------------------------------------

    @Test
    void parseTablesExtractsPrefixAndMigrateList() {
        SchemaMigrationRunner runner = newRunner(mock(DataSource.class), mock(PluginArtifactStore.class),
                mock(AddonPluginMigrationMapper.class));

        SchemaMigrationRunner.TablesDecl tables = runner.parseTables(YAML_WITH_TABLES);

        assertNotNull(tables);
        assertEquals("hou_erp_", tables.prefix());
        assertEquals(List.of("db/V1__test_schema.sql"), tables.migrate());
        assertEquals("hou_erp_", tables.effectivePrefix("erp"));
    }

    @Test
    void parseTablesReturnsNullWhenAbsentOrBlank() {
        SchemaMigrationRunner runner = newRunner(mock(DataSource.class), mock(PluginArtifactStore.class),
                mock(AddonPluginMigrationMapper.class));

        assertNull(runner.parseTables(null));
        assertNull(runner.parseTables("   "));
        assertNull(runner.parseTables("code: erp\nversion: 1.0.0\n"), "无 tables 段应返回 null");
    }

    @Test
    void effectivePrefixFallsBackToConventionWhenUndeclared() {
        SchemaMigrationRunner runner = newRunner(mock(DataSource.class), mock(PluginArtifactStore.class),
                mock(AddonPluginMigrationMapper.class));

        SchemaMigrationRunner.TablesDecl tables = runner.parseTables("tables:\n  migrate:\n    - db/x.sql\n");

        assertNotNull(tables);
        assertNull(tables.prefix());
        assertEquals("hou_plugin_erp_", tables.effectivePrefix("erp"), "未声明 prefix 回落约定 hou_plugin_{code}_");
    }

    // ------------------------------------------------------------------
    // 前缀隔离校验
    // ------------------------------------------------------------------

    @Test
    void extractTableNamesCoversDdlAndIgnoresComments() {
        SchemaMigrationRunner runner = newRunner(mock(DataSource.class), mock(PluginArtifactStore.class),
                mock(AddonPluginMigrationMapper.class));

        var names = runner.extractTableNames(
                "-- 说明：hou_other_shared 由别的服务维护\nDROP TABLE IF EXISTS `hou_erp_a`;\n"
                        + "CREATE TABLE hou_erp_b (id int);\nINSERT INTO `hou_erp_c` VALUES (1);\n");

        assertTrue(names.containsAll(List.of("hou_erp_a", "hou_erp_b", "hou_erp_c")), "应抽取 DDL/DML 表名: " + names);
        assertTrue(names.stream().noneMatch(n -> n.equals("hou_other_shared")), "注释中的表名不应被抽取: " + names);
    }

    @Test
    void validatePrefixAcceptsOwnTablesAndRejectsForeign() {
        SchemaMigrationRunner runner = newRunner(mock(DataSource.class), mock(PluginArtifactStore.class),
                mock(AddonPluginMigrationMapper.class));

        runner.validatePrefix(OWN_SCHEMA, "hou_erp_", "erp");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> runner.validatePrefix(FOREIGN_SCHEMA, "hou_erp_", "erp"));
        assertTrue(ex.getMessage().contains("hou_user"), "应点名越权表: " + ex.getMessage());
    }

    @Test
    void validatePrefixRejectsBlankPrefix() {
        SchemaMigrationRunner runner = newRunner(mock(DataSource.class), mock(PluginArtifactStore.class),
                mock(AddonPluginMigrationMapper.class));

        assertThrows(IllegalStateException.class, () -> runner.validatePrefix(OWN_SCHEMA, "  ", "erp"));
    }

    // ------------------------------------------------------------------
    // tables.allow：少数历史建表脚本里不带主前缀、但确由本插件建的表
    // ------------------------------------------------------------------

    @Test
    void parseTablesExtractsAllowListAndDefaultsToEmpty() {
        SchemaMigrationRunner runner = newRunner(mock(DataSource.class), mock(PluginArtifactStore.class),
                mock(AddonPluginMigrationMapper.class));

        SchemaMigrationRunner.TablesDecl withAllow = runner.parseTables("""
                code: oa
                version: 1.0.0
                tables:
                  prefix: hou_oa_
                  allow:
                    - hou_base_position
                  migrate:
                    - db/oa.sql
                """);
        assertNotNull(withAllow);
        assertEquals(List.of("hou_base_position"), withAllow.allow());
        assertEquals(List.of("hou_base_position"), withAllow.effectiveAllow());
        assertEquals(List.of("db/oa.sql"), withAllow.migrate());

        // 未声明 allow 时必须回落成空列表（而不是 null），否则调用侧要处处判空
        SchemaMigrationRunner.TablesDecl without = runner.parseTables(YAML_WITH_TABLES);
        assertNotNull(without);
        assertEquals(List.of(), without.effectiveAllow());
    }

    @Test
    void validatePrefixAllowsDeclaredExtraPrefixesOnly() {
        SchemaMigrationRunner runner = newRunner(mock(DataSource.class), mock(PluginArtifactStore.class),
                mock(AddonPluginMigrationMapper.class));
        String schema = """
                CREATE TABLE `hou_oa_employee` (`id` bigint PRIMARY KEY);
                CREATE TABLE `hou_base_position` (`id` bigint PRIMARY KEY);
                """;

        // 登记后放行：hou_base_position 由 oa 插件建，但按约定前缀规则它不属于 hou_oa_
        runner.validatePrefix(schema, "hou_oa_", List.of("hou_base_position"), "oa");

        // 未登记则照旧拒绝 —— allow 是逐条显式登记的，不是关掉校验
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> runner.validatePrefix(schema, "hou_oa_", List.of(), "oa"));
        assertTrue(ex.getMessage().contains("hou_base_position"), "应点名越权表: " + ex.getMessage());
    }

    @Test
    void extractTableNamesIgnoresOnUpdateTimestampColumnClause() {
        SchemaMigrationRunner runner = newRunner(mock(DataSource.class), mock(PluginArtifactStore.class),
                mock(AddonPluginMigrationMapper.class));
        String schema = """
                CREATE TABLE `hou_cms_article` (
                  `id` bigint NOT NULL,
                  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                  PRIMARY KEY (`id`)
                );
                """;

        // 回归：ON UPDATE CURRENT_TIMESTAMP 是列级自动更新时间戳，不是 UPDATE 语句。
        // 此前正则把它抽成表名 CURRENT_TIMESTAMP，前缀校验据此抛异常，
        // 导致除 erp 之外所有带该子句的插件都无法自动建表。
        assertEquals(Set.of("hou_cms_article"), runner.extractTableNames(schema));
        runner.validatePrefix(schema, "hou_cms_", "cms");
    }

    // ------------------------------------------------------------------
    // migrate 编排
    // ------------------------------------------------------------------

    @Test
    void migrateSkipsWhenJarMissing() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        when(store.resolve("erp", "1.0.0")).thenReturn(Path.of("/definitely/missing/erp-1.0.0.jar"));

        runner.migrate("erp", "1.0.0");

        verifyNoInteractions(mapper);
        verify(runner, never()).executeSql(anyString());
    }

    @Test
    void migrateSkipsWhenNoTablesDeclared() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", "code: erp\nversion: 1.0.0\n"));
        when(store.resolve("erp", "1.0.0")).thenReturn(jar);

        runner.migrate("erp", "1.0.0");

        verifyNoInteractions(mapper);
        verify(runner, never()).executeSql(anyString());
    }

    @Test
    void migrateSkipsWhenAlreadyApplied() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", YAML_WITH_TABLES, "db/V1__test_schema.sql", OWN_SCHEMA));
        when(store.resolve("erp", "1.0.0")).thenReturn(jar);
        when(mapper.countSuccess("erp", "1.0.0")).thenReturn(1);

        runner.migrate("erp", "1.0.0");

        verify(runner, never()).executeSql(anyString());
        verify(mapper, never()).upsert(any());
    }

    @Test
    void migrateExecutesAndRecordsSuccess() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", YAML_WITH_TABLES, "db/V1__test_schema.sql", OWN_SCHEMA));
        when(store.resolve("erp", "1.0.0")).thenReturn(jar);
        when(mapper.countSuccess("erp", "1.0.0")).thenReturn(0);
        doNothing().when(runner).executeSql(anyString());

        runner.migrate("erp", "1.0.0");

        verify(runner).executeSql(OWN_SCHEMA);
        ArgumentCaptor<AddonPluginMigration> captor = ArgumentCaptor.forClass(AddonPluginMigration.class);
        verify(mapper).upsert(captor.capture());
        AddonPluginMigration row = captor.getValue();
        assertEquals("erp", row.getPluginCode());
        assertEquals("1.0.0", row.getVersion());
        assertEquals(AddonPluginMigration.STATUS_SUCCESS, row.getStatus());
        assertEquals("db/V1__test_schema.sql", row.getScript());
        assertNotNull(row.getChecksum());
    }

    @Test
    void migrateRejectsForeignTableBeforeExecuting() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", YAML_WITH_TABLES, "db/V1__test_schema.sql", FOREIGN_SCHEMA));
        when(store.resolve("erp", "1.0.0")).thenReturn(jar);
        when(mapper.countSuccess("erp", "1.0.0")).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> runner.migrate("erp", "1.0.0"));

        verify(runner, never()).executeSql(anyString());
        ArgumentCaptor<AddonPluginMigration> captor = ArgumentCaptor.forClass(AddonPluginMigration.class);
        verify(mapper).upsert(captor.capture());
        assertEquals(AddonPluginMigration.STATUS_FAILED, captor.getValue().getStatus());
    }

    @Test
    void migrateRecordsFailureAndRunsRollbackWhenExecuteThrows() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of(
                "plugin.yaml", YAML_WITH_TABLES,
                "db/V1__test_schema.sql", OWN_SCHEMA,
                "db/rollback.sql", "DROP TABLE IF EXISTS `hou_erp_goods`;"));
        when(store.resolve("erp", "1.0.0")).thenReturn(jar);
        when(mapper.countSuccess("erp", "1.0.0")).thenReturn(0);
        // 首次执行 schema 抛错，随后补偿脚本放行
        doThrow(new IllegalStateException("建表失败")).doNothing().when(runner).executeSql(anyString());

        assertThrows(IllegalStateException.class, () -> runner.migrate("erp", "1.0.0"));

        verify(runner, times(2)).executeSql(anyString());
        ArgumentCaptor<AddonPluginMigration> captor = ArgumentCaptor.forClass(AddonPluginMigration.class);
        verify(mapper).upsert(captor.capture());
        AddonPluginMigration row = captor.getValue();
        assertEquals(AddonPluginMigration.STATUS_FAILED, row.getStatus());
        assertEquals("建表失败", row.getMessage());
    }

    @Test
    void migrateUsesUnknownVersionAndSkipsWhenDescriptorVersionMissing() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        when(store.resolve(eq("erp"), eq("unknown"))).thenReturn(Path.of("/definitely/missing/erp-unknown.jar"));

        runner.migrate("erp", null);

        verify(store).resolve("erp", "unknown");
        verifyNoInteractions(mapper);
    }

    // ------------------------------------------------------------------
    // M4 升级 / 回滚迁移
    // ------------------------------------------------------------------

    @Test
    void 升级迁移_以operation2落审计并记录来源版本() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", YAML_WITH_TABLES, "db/V1__test_schema.sql", OWN_SCHEMA));
        when(store.resolve("erp", "2.0.0")).thenReturn(jar);
        doNothing().when(runner).executeSql(anyString());

        runner.migrateUpgrade("erp", "1.0.0", "2.0.0");

        verify(runner).executeSql(OWN_SCHEMA);
        verify(mapper).upsertOperation(eq("erp"), eq("2.0.0"), eq(SchemaMigrationRunner.OPERATION_UPGRADE),
                eq("1.0.0"), isNull(), eq("db/V1__test_schema.sql"), anyString(),
                eq(AddonPluginMigration.STATUS_SUCCESS), eq("ok"));
    }

    @Test
    void 升级迁移失败_抛异常并落失败审计() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", YAML_WITH_TABLES, "db/V1__test_schema.sql", OWN_SCHEMA));
        when(store.resolve("erp", "2.0.0")).thenReturn(jar);
        doThrow(new IllegalStateException("列类型冲突")).when(runner).executeSql(anyString());

        // 抛异常是契约：升级编排靠它走回滚路径 A，静默吞掉会让插件停在「已卸载、新版本没起」
        assertThrows(IllegalStateException.class, () -> runner.migrateUpgrade("erp", "1.0.0", "2.0.0"));
        verify(mapper).upsertOperation(eq("erp"), eq("2.0.0"), eq(SchemaMigrationRunner.OPERATION_UPGRADE),
                eq("1.0.0"), isNull(), anyString(), anyString(),
                eq(AddonPluginMigration.STATUS_FAILED), anyString());
    }

    @Test
    void 回滚迁移_用回滚到的版本反查被回滚的升级记录() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", YAML_WITH_TABLES, "db/rollback.sql", OWN_SCHEMA));
        // 回滚掉 2.0.0（补偿脚本取自 2.0.0），退回到 1.0.0
        when(store.resolve("erp", "2.0.0")).thenReturn(jar);
        when(mapper.selectId("erp", "2.0.0", SchemaMigrationRunner.OPERATION_UPGRADE, "1.0.0")).thenReturn(9527L);
        doNothing().when(runner).executeSql(anyString());

        assertTrue(runner.migrateRollback("erp", "2.0.0", "1.0.0", "2.0.0", null));

        // 关键断言：from_version 查的是「回滚到的版本」——升级审计行的口径是
        // (version=2.0.0, from_version=1.0.0)。用 2.0.0 去查会永远查不到，rollback_of 就断了。
        verify(mapper).selectId("erp", "2.0.0", SchemaMigrationRunner.OPERATION_UPGRADE, "1.0.0");
        verify(mapper).upsertOperation(eq("erp"), eq("1.0.0"), eq(SchemaMigrationRunner.OPERATION_ROLLBACK),
                eq("2.0.0"), eq(9527L), eq("db/rollback.sql"), anyString(),
                eq(AddonPluginMigration.STATUS_SUCCESS), eq("ok"));
    }

    @Test
    void 回滚迁移_无补偿脚本时不报错且落审计() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", YAML_WITH_TABLES));
        when(store.resolve("erp", "2.0.0")).thenReturn(jar);

        // 没有 rollback.sql 是常态（多数版本只加表不删表）：不能因此判定回滚失败
        assertTrue(runner.migrateRollback("erp", "2.0.0", "1.0.0", "2.0.0", null));
        verify(runner, never()).executeSql(anyString());
        verify(mapper).upsertOperation(eq("erp"), eq("1.0.0"), eq(SchemaMigrationRunner.OPERATION_ROLLBACK),
                eq("2.0.0"), any(), eq(""), anyString(),
                eq(AddonPluginMigration.STATUS_SUCCESS), eq("no-rollback-script"));
    }

    @Test
    void 回滚迁移失败_不抛异常只落失败审计() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", YAML_WITH_TABLES, "db/rollback.sql", OWN_SCHEMA));
        when(store.resolve("erp", "2.0.0")).thenReturn(jar);
        doThrow(new IllegalStateException("补偿脚本语法错误")).when(runner).executeSql(anyString());

        // 刻意不抛：DDL 补偿失败不能阻止「把旧版本重新装回来」，后者才是回滚的真正目标
        assertFalse(runner.migrateRollback("erp", "2.0.0", "1.0.0", "2.0.0", null));
        verify(mapper).upsertOperation(eq("erp"), eq("1.0.0"), eq(SchemaMigrationRunner.OPERATION_ROLLBACK),
                eq("2.0.0"), any(), eq("db/rollback.sql"), anyString(),
                eq(AddonPluginMigration.STATUS_FAILED), anyString());
    }

    @Test
    void 回滚迁移_越权补偿脚本被拒绝执行并落失败审计() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of("plugin.yaml", YAML_WITH_TABLES, "db/rollback.sql", FOREIGN_ROLLBACK));
        when(store.resolve("erp", "2.0.0")).thenReturn(jar);
        doNothing().when(runner).executeSql(anyString());

        // 回滚脚本不是「正向脚本的附属品」，它同样是不可信输入：触碰 hou_user 就必须被挡在数据源之外
        assertFalse(runner.migrateRollback("erp", "2.0.0", "1.0.0", "2.0.0", null));
        verify(runner, never()).executeSql(anyString());
        // 拒绝而不是静默跳过：必须落失败审计，否则运维会以为回滚已经成功
        verify(mapper).upsertOperation(eq("erp"), eq("1.0.0"), eq(SchemaMigrationRunner.OPERATION_ROLLBACK),
                eq("2.0.0"), any(), eq("db/rollback.sql"), anyString(),
                eq(AddonPluginMigration.STATUS_FAILED), contains("hou_user"));
    }

    @Test
    void 迁移失败时_越权补偿脚本被拒绝执行且不覆盖原始异常() throws IOException {
        PluginArtifactStore store = mock(PluginArtifactStore.class);
        AddonPluginMigrationMapper mapper = mock(AddonPluginMigrationMapper.class);
        SchemaMigrationRunner runner = spy(newRunner(mock(DataSource.class), store, mapper));
        Path jar = writeJar(Map.of(
                "plugin.yaml", YAML_WITH_TABLES,
                "db/V1__test_schema.sql", OWN_SCHEMA,
                "db/rollback.sql", FOREIGN_ROLLBACK));
        when(store.resolve("erp", "1.0.0")).thenReturn(jar);
        when(mapper.countSuccess("erp", "1.0.0")).thenReturn(0);
        doThrow(new IllegalStateException("建表失败")).when(runner).executeSql(anyString());

        // 这就是被修复的攻击路径：升级/install 失败一次 → 自动执行包内 rollback.sql
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> runner.migrate("erp", "1.0.0"));

        assertEquals("建表失败", ex.getMessage(), "补偿脚本的校验异常不能替换掉正在传播的原始失败原因");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(runner).executeSql(captor.capture());
        assertTrue(captor.getAllValues().stream().noneMatch(s -> s.contains("hou_user")),
                "越权补偿脚本绝不能被投递到数据源，实际执行了：" + captor.getAllValues());
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private SchemaMigrationRunner newRunner(DataSource dataSource, PluginArtifactStore store,
                                            AddonPluginMigrationMapper mapper) {
        return new SchemaMigrationRunner(dataSource, store, mapper);
    }

    private Path writeJar(Map<String, String> entries) throws IOException {
        Path jar = Files.createTempFile("plugin-", ".jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                out.putNextEntry(new JarEntry(entry.getKey()));
                out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return jar;
    }
}
