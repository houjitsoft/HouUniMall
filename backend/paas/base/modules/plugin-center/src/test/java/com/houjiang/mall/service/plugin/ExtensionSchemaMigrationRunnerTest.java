package com.houjiang.mall.service.plugin;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link ExtensionSchemaMigrationRunner} 单测（R2.1）。
 *
 * <p>用 <b>真实 classpath 资源</b>（{@code src/test/resources/db/migration/{demo,bad}/}）覆盖脚本发现，
 * 用 Mockito spy 打桩 {@link ExtensionSchemaMigrationRunner#executeSql(String)} 规避真实数据库，
 * 用 mock 的 {@link ExtensionMigrationRepository} 验证幂等与审计。重点：
 * 升序执行、幂等（重复零副作用）、越权表执行前拦截、失败留痕 + rollback、migrateAll 发现 code。
 *
 * @author Amos
 */
class ExtensionSchemaMigrationRunnerTest {

    private static final String DEMO = "demo";
    private static final String BAD = "bad";
    private static final String VERSION = "1.0.0";

    // ------------------------------------------------------------------
    // 脚本发现
    // ------------------------------------------------------------------

    @Test
    void discoversScriptsSortedByFilenameForCode() {
        ExtensionSchemaMigrationRunner runner = newRunner(mock(ExtensionMigrationRepository.class));

        List<String> names = runner.discoverScripts(DEMO).stream().map(Resource::getFilename).toList();

        assertEquals(List.of("V1__create_order.sql", "V2__order_index.sql", "rollback.sql"), names);
    }

    @Test
    void discoversNothingForUnknownCode() {
        ExtensionSchemaMigrationRunner runner = newRunner(mock(ExtensionMigrationRepository.class));

        assertTrue(runner.discoverScripts("no-such-code").isEmpty());
    }

    // ------------------------------------------------------------------
    // 正向迁移 + 幂等
    // ------------------------------------------------------------------

    @Test
    void migratesScriptsInOrderAndRecordsSuccess() throws IOException {
        ExtensionMigrationRepository repository = mock(ExtensionMigrationRepository.class);
        when(repository.isApplied(eq(DEMO), anyString(), anyString())).thenReturn(false);
        ExtensionSchemaMigrationRunner runner = newRunner(repository);
        doNothing().when(runner).executeSql(anyString());

        ExtensionSchemaMigrationRunner.Result result = runner.migrate(DEMO, VERSION);

        assertEquals(2, result.executed(), "应执行 V1 + V2 两个脚本");
        assertEquals(0, result.skipped());
        assertFalse(result.noop());

        String v1 = readClasspath("db/migration/demo/V1__create_order.sql");
        String v2 = readClasspath("db/migration/demo/V2__order_index.sql");
        InOrder order = inOrder(runner);
        order.verify(runner).executeSql(v1);
        order.verify(runner).executeSql(v2);
        verify(repository, times(2)).markSuccess(eq(DEMO), eq(VERSION), anyString(), anyString());
    }

    @Test
    void secondRunIsNoopWhenAlreadyApplied() {
        ExtensionMigrationRepository repository = mock(ExtensionMigrationRepository.class);
        when(repository.isApplied(eq(DEMO), anyString(), anyString())).thenReturn(true);
        ExtensionSchemaMigrationRunner runner = newRunner(repository);

        ExtensionSchemaMigrationRunner.Result result = runner.migrate(DEMO, VERSION);

        assertEquals(0, result.executed(), "幂等：已应用脚本不得重复执行");
        assertEquals(2, result.skipped());
        assertTrue(result.noop());
        verify(runner, never()).executeSql(anyString());
        verify(repository, never()).markSuccess(anyString(), anyString(), anyString(), anyString());
        verify(repository, never()).markFailure(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void unknownCodeIsNoopWithoutRepositoryInteraction() {
        ExtensionMigrationRepository repository = mock(ExtensionMigrationRepository.class);
        ExtensionSchemaMigrationRunner runner = newRunner(repository);

        ExtensionSchemaMigrationRunner.Result result = runner.migrate("no-such-code", VERSION);

        assertTrue(result.noop());
        verifyNoInteractions(repository);
    }

    @Test
    void blankCodeIsNoop() {
        ExtensionMigrationRepository repository = mock(ExtensionMigrationRepository.class);
        ExtensionSchemaMigrationRunner runner = newRunner(repository);

        assertTrue(runner.migrate("   ", VERSION).noop());
        verifyNoInteractions(repository);
    }

    // ------------------------------------------------------------------
    // 越权拦截 + 失败回滚
    // ------------------------------------------------------------------

    @Test
    void rejectsForeignTableBeforeExecutingAndRollsBack() throws IOException {
        ExtensionMigrationRepository repository = mock(ExtensionMigrationRepository.class);
        when(repository.isApplied(eq(BAD), anyString(), anyString())).thenReturn(false);
        ExtensionSchemaMigrationRunner runner = newRunner(repository);
        doNothing().when(runner).executeSql(anyString());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> runner.migrate(BAD, VERSION));
        assertTrue(ex.getMessage().contains("sys_user"), "应点名越权表: " + ex.getMessage());

        // 越权脚本本体不得执行；仅执行 rollback
        String rollback = readClasspath("db/migration/bad/rollback.sql");
        verify(runner, times(1)).executeSql(rollback);
        verify(repository, never()).markSuccess(anyString(), anyString(), anyString(), anyString());
        verify(repository, times(1)).markFailure(eq(BAD), eq(VERSION), anyString(), anyString(), anyString());
    }

    @Test
    void recordsFailureAndRunsRollbackWhenExecuteThrows() throws IOException {
        ExtensionMigrationRepository repository = mock(ExtensionMigrationRepository.class);
        when(repository.isApplied(eq(DEMO), anyString(), anyString())).thenReturn(false);
        ExtensionSchemaMigrationRunner runner = newRunner(repository);
        // 首个脚本执行抛错，rollback 放行
        doThrow(new IllegalStateException("建表失败")).doNothing().when(runner).executeSql(anyString());

        assertThrows(IllegalStateException.class, () -> runner.migrate(DEMO, VERSION));

        // 一次失败脚本 + 一次 rollback
        verify(runner, times(2)).executeSql(anyString());
        verify(repository, never()).markSuccess(anyString(), anyString(), anyString(), anyString());
        verify(repository, times(1)).markFailure(eq(DEMO), eq(VERSION), eq("V1__create_order.sql"), anyString(),
                eq("建表失败"));
    }

    // ------------------------------------------------------------------
    // migrateAll
    // ------------------------------------------------------------------

    @Test
    void migrateAllDiscoversDescriptorsAndMigratesEachCode() {
        ExtensionMigrationRepository repository = mock(ExtensionMigrationRepository.class);
        when(repository.isApplied(anyString(), anyString(), anyString())).thenReturn(false);
        ExtensionSchemaMigrationRunner runner = newRunner(repository);
        doReturn(Map.of(DEMO, VERSION)).when(runner).discoverExtensionDescriptors();
        doNothing().when(runner).executeSql(anyString());

        List<ExtensionSchemaMigrationRunner.Result> results = runner.migrateAll();

        assertEquals(1, results.size());
        assertEquals(DEMO, results.get(0).code());
        assertEquals(2, results.get(0).executed());
    }

    // ------------------------------------------------------------------
    // 表名抽取 + 幂等表脚本就位
    // ------------------------------------------------------------------

    @Test
    void extractTableNamesCoversDdlAndDmlAndIgnoresComments() {
        ExtensionSchemaMigrationRunner runner = newRunner(mock(ExtensionMigrationRepository.class));

        var names = runner.extractTableNames(
                "-- 注释里的 hou_other 不应被抽取\n"
                        + "CREATE TABLE IF NOT EXISTS `hou_ext_a` (id int);\n"
                        + "ALTER TABLE hou_ext_b ADD COLUMN c int;\n"
                        + "INSERT INTO `hou_ext_c` VALUES (1);\n");

        assertTrue(names.containsAll(List.of("hou_ext_a", "hou_ext_b", "hou_ext_c")), "应抽取表名: " + names);
        assertFalse(names.contains("hou_other"), "注释中的表名不应被抽取: " + names);
    }

    @Test
    void idempotentTableDdlResourceIsPresentAndCreateIfNotExists() throws IOException {
        String ddl = readClasspath(JdbcExtensionMigrationRepository.DDL_RESOURCE);

        assertNotNull(ddl);
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS `hou_ext_migration`"), "DDL 必须幂等: " + ddl);
        assertTrue(ddl.contains("PRIMARY KEY (`code`, `script`)"), "幂等键必须为 (code, script)");
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private ExtensionSchemaMigrationRunner newRunner(ExtensionMigrationRepository repository) {
        return spy(new ExtensionSchemaMigrationRunner(null, repository, new PathMatchingResourcePatternResolver()));
    }

    private String readClasspath(String path) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, "classpath 缺少资源: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
