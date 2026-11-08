package com.houjiang.mall.service.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 插件菜单权限生命周期契约测试：安装注册 ↔ 卸载移除必须成对且一致。
 *
 * <p>锁定三条硬约束：
 * <ol>
 *   <li>插件声明的每一条菜单都要落到权限菜单表，并<b>逐条</b>写入角色授权——
 *       菜单是否显示完全由这份授权决定（不是由任何名单决定）；</li>
 *   <li>卸载要同时清掉菜单行与角色授权行，且不得留下指向已删菜单的孤儿授权；</li>
 *   <li>机制只认「菜单权限的增删」，不引入也不依赖任何黑白名单。</li>
 * </ol>
 *
 * <p>用记录型 JdbcTemplate 桩锁定发出的 SQL 与参数语义，不依赖真实库。
 */
class PluginMenuPermissionLifecycleTest {

    @TempDir
    Path tempDir;

    private RecordingJdbc jdbc;
    private PluginMenuSyncPort port;

    @BeforeEach
    void setUp() {
        jdbc = new RecordingJdbc();
        port = new PluginMenuSyncPort(jdbc);
    }

    private Path writeDeclaration(String yaml) throws Exception {
        Path dir = Files.createTempDirectory(tempDir, "plugin-");
        Files.write(dir.resolve("plugin.yaml"), yaml.getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    private Path threeMenuDeclaration() throws Exception {
        return writeDeclaration("""
                code: cms
                category: content
                menus:
                  - type: M
                    name: 内容管理
                    perms: shell:cms:apps
                    path: /plugin/cms
                  - type: C
                    name: 文章列表
                    perms: shell:cms:article:list
                    path: /plugin/cms/article
                    parentPerms: shell:cms:apps
                  - type: C
                    name: 分类管理
                    perms: shell:cms:cate:list
                    path: /plugin/cms/cate
                    parentPerms: shell:cms:apps
                """);
    }

    @Test
    void everyDeclaredMenuIsRegisteredAndGranted() throws Exception {
        port.sync("cms", threeMenuDeclaration());

        assertEquals(3, jdbc.countContaining("INSERT INTO hou_permission_menu"),
                "3 条声明菜单必须全部注册进权限菜单表");

        List<String> grantedPerms = jdbc.firstArgsOfAllContaining("INSERT IGNORE INTO hou_permission_role_menu");
        assertEquals(3, grantedPerms.size(),
                "每条菜单都必须写入一条角色授权——菜单显示由授权决定，只注册不授权等于看不见");
        assertTrue(grantedPerms.contains("shell:cms:apps"));
        assertTrue(grantedPerms.contains("shell:cms:article:list"));
        assertTrue(grantedPerms.contains("shell:cms:cate:list"));
    }

    @Test
    void reinstallAfterRemovalRegistersAgain() throws Exception {
        port.sync("cms", threeMenuDeclaration());
        port.remove("cms");
        jdbc.reset();

        port.sync("cms", threeMenuDeclaration());

        assertEquals(3, jdbc.countContaining("INSERT INTO hou_permission_menu"),
                "卸载后重装必须重新注册全部菜单");
        assertEquals(3, jdbc.firstArgsOfAllContaining("INSERT IGNORE INTO hou_permission_role_menu").size(),
                "重装必须重新授权，否则插件装回来了菜单却不显示");
    }

    @Test
    void removeDropsMenusGrantsAndOrphanGrants() {
        port.remove("cms");

        List<String> deletes = jdbc.sqlsContaining("DELETE");
        assertTrue(deletes.stream().anyMatch(s -> s.contains("DELETE rm FROM hou_permission_role_menu")
                        && s.contains("JOIN hou_permission_menu m ON m.id=rm.menu_id")),
                "卸载必须先按归属清掉角色授权，再删菜单");
        assertTrue(deletes.stream().anyMatch(s -> s.contains("DELETE FROM hou_permission_menu")),
                "卸载必须删掉插件的菜单行");
        assertTrue(deletes.stream().anyMatch(s -> s.contains("LEFT JOIN hou_permission_menu m ON rm.menu_id = m.id")),
                "卸载必须兜底清理指向已删菜单的孤儿授权，防止权限残留");
    }

    @Test
    void syncGrantsPermissionForUpdatedMenusToo() throws Exception {
        // 已存在的菜单走 UPDATE 分支，授权仍必须补写（否则升级后老菜单丢授权）
        jdbc.queryResults.put("SELECT id FROM hou_permission_menu WHERE perms=? "
                + "ORDER BY CASE WHEN display_pos='hidden' THEN 1 ELSE 0 END,id LIMIT 1|shell:cms:apps", List.of(77));
        jdbc.rememberParent("cms", "shell:cms:apps", 77);
        port.sync("cms", threeMenuDeclaration());

        assertEquals(1, jdbc.countContaining("UPDATE hou_permission_menu SET"),
                "已存在的菜单行走 UPDATE 分支");
        List<String> grantedPerms = jdbc.firstArgsOfAllContaining("INSERT IGNORE INTO hou_permission_role_menu");
        assertTrue(grantedPerms.contains("shell:cms:apps"),
                "走 UPDATE 分支的菜单同样要补授权，不能只更新菜单不补权限");
    }

    /** 记录型 JdbcTemplate 桩：记录 SQL 与参数，按 SQL+参数返回预置查询结果。 */
    private static class RecordingJdbc extends JdbcTemplate {
        final List<String> sqls = new ArrayList<>();
        /** 与 sqls 同序记录参数：同一条 SQL 可能执行多次，用 Map 会互相覆盖。 */
        final List<List<Object>> argLog = new ArrayList<>();
        final Map<String, List<?>> queryResults = new LinkedHashMap<>();

        void reset() {
            sqls.clear();
            argLog.clear();
            queryResults.clear();
        }

        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
            List<?> rows = queryResults.get(sql + "|" + String.join(",", toStrings(args)));
            return (List<T>) (rows == null ? List.of() : rows);
        }

        /**
         * 差集回收前的孤儿计数（2026-10-19 安全闸新增）。
         * 默认返回 0 表示「无孤儿」，不阻断任何既有断言。
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
            sqls.add(sql);
            argLog.add(args == null ? List.of() : List.of(args));
            return (T) Long.valueOf(0L);
        }

        @Override
        public int update(String sql, Object... args) {
            sqls.add(sql);
            argLog.add(args == null ? List.of() : List.of(args));
            if (sql.startsWith("INSERT INTO hou_permission_menu")) {
                queryResults.put("SELECT id FROM hou_permission_menu WHERE perms=? "
                        + "ORDER BY CASE WHEN display_pos='hidden' THEN 1 ELSE 0 END,id LIMIT 1|" + args[5], List.of(1000 + sqls.size()));
                rememberParent(String.valueOf(args[10]), String.valueOf(args[5]), 1000 + sqls.size());
            }
            return 1;
        }

        @Override
        public int update(String sql) {
            sqls.add(sql);
            argLog.add(List.of());
            return 1;
        }

        void rememberParent(String code, String perms, int id) {
            queryResults.put("SELECT id FROM hou_permission_menu WHERE perms=? AND (plugin_code=? OR "
                    + "((plugin_code IS NULL OR plugin_code='') AND (params=? OR paths=? OR paths LIKE ?))) LIMIT 1|"
                    + perms + "," + code + ",plugin:" + code + ",/plugin/" + code + ",/plugin/" + code + "/%", List.of(id));
        }

        int countContaining(String fragment) {
            return (int) sqls.stream().filter(s -> s.contains(fragment)).count();
        }

        List<String> sqlsContaining(String fragment) {
            return sqls.stream().filter(s -> s.contains(fragment)).toList();
        }

        /** 取所有命中 SQL 的首个参数（授权/注册语句的首参即 perms）。 */
        List<String> firstArgsOfAllContaining(String fragment) {
            List<String> out = new ArrayList<>();
            for (int i = 0; i < sqls.size(); i++) {
                if (sqls.get(i).contains(fragment)) {
                    List<Object> args = argLog.get(i);
                    if (!args.isEmpty()) {
                        out.add(String.valueOf(args.get(0)));
                    }
                }
            }
            return out;
        }

        private static List<String> toStrings(Object... args) {
            List<String> out = new ArrayList<>();
            if (args != null) {
                for (Object arg : args) {
                    out.add(String.valueOf(arg));
                }
            }
            return out;
        }
    }
}
