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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * PluginMenuSyncPort 分组写入 / 差集回收 / 保留字 / ownership 排除的单测（方案 §8.2/§12）。
 * 用记录型 JdbcTemplate 桩，锁定发出的 SQL 与参数语义，不依赖真实库。
 */
class PluginMenuSyncPortTest {

    @TempDir
    Path tempDir;

    private RecordingJdbcTemplate jdbc;
    private PluginMenuSyncPort port;

    @BeforeEach
    void setUp() {
        jdbc = new RecordingJdbcTemplate();
        port = new PluginMenuSyncPort(jdbc);
    }

    private Path writeDeclaration(String yaml) throws Exception {
        Path dir = Files.createTempDirectory(tempDir, "plugin-");
        Files.write(dir.resolve("plugin.yaml"), yaml.getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    private void groupRootExists(String key, int id) {
        jdbc.queryResults.put(existingPermQueryKey(
                MenuGroupRegistry.GROUP_PERMS_PREFIX + key), List.of(id));
    }

    private String existingPermQueryKey(String perms) {
        return "SELECT id FROM hou_permission_menu WHERE perms=? "
                + "ORDER BY CASE WHEN display_pos='hidden' THEN 1 ELSE 0 END,id LIMIT 1|" + perms;
    }

    @Test
    void syncKeepsTopLevelPluginOutsideLegacyGroups() throws Exception {
        groupRootExists("growth", 555);
        Path path = writeDeclaration("""
                code: activity
                name: 营销活动
                category: Marketing
                menus:
                  - type: M
                    name: 营销活动
                    perms: shell:activity:apps
                    path: /plugin/activity
                    group: growth
                    groupSort: 100
                """);

        port.sync("activity", path);

        String insert = jdbc.firstSqlContaining("INSERT INTO hou_permission_menu");
        assertEquals(List.of(0, "M", "营销活动", "Document", 900, "shell:activity:apps"),
                jdbc.argsOf(insert).subList(0, 6),
                "插件根必须独立 pid=0，旧 groupSort 不应覆盖根排序");
    }

    @Test
    void unknownLegacyGroupDoesNotChangeRootPlacement() throws Exception {
        groupRootExists("other", 666);
        Path path = writeDeclaration("""
                code: activity
                menus:
                  - type: M
                    name: 营销活动
                    perms: shell:activity:apps
                    path: /plugin/activity
                    group: nope
                """);

        port.sync("activity", path);

        String insert = jdbc.firstSqlContaining("INSERT INTO hou_permission_menu");
        assertEquals(0, jdbc.argsOf(insert).get(0), "分类分组不改变菜单父级");
    }

    @Test
    void categoryDoesNotBecomeMenuParent() throws Exception {
        groupRootExists("biz", 777);
        Path path = writeDeclaration("""
                code: erp
                category: 企业管理
                menus:
                  - type: M
                    name: ERP
                    perms: shell:erp:apps
                    path: /plugin/erp
                """);

        port.sync("erp", path);

        String insert = jdbc.firstSqlContaining("INSERT INTO hou_permission_menu");
        assertEquals(0, jdbc.argsOf(insert).get(0), "分类分组不改变菜单父级");
    }

    @Test
    void resyncRepairsExistingMenuFromLegacyEnglishCategory() throws Exception {
        groupRootExists("org", 777);
        jdbc.queryResults.put(existingPermQueryKey("shell:oa:apps"), List.of(100317));
        Path path = writeDeclaration("""
                code: oa
                category: Office
                menus:
                  - type: M
                    name: OA办公
                    perms: shell:oa:apps
                    path: /plugin/oa/apps
                """);
        port.sync("oa", path);
        String update = jdbc.firstSqlContaining("UPDATE hou_permission_menu SET pid=");
        assertEquals(0, jdbc.argsOf(update).get(0));
        // 参数顺序：pid,type,name,icon,sort,path,component,params,plugin_code,id
        assertEquals("oa", jdbc.argsOf(update).get(8), "plugin_code 与 params 必须双写");
        assertEquals(100317, jdbc.argsOf(update).get(jdbc.argsOf(update).size()-1), "existing menu ID and its grants must survive resync");
    }

    @Test
    void explicitParentPermsWinsOverGroupDeclaration() throws Exception {
        groupRootExists("growth", 555);
        jdbc.queryResults.put(existingPermQueryKey("shell:activity:apps"), List.of(888));
        ownedParent("activity", "shell:activity:apps", 888);
        Path path = writeDeclaration("""
                code: activity
                menus:
                  - type: C
                    name: 优惠券
                    perms: shell:activity:coupon
                    path: /plugin/activity/coupon
                    group: growth
                    parentPerms: shell:activity:apps
                """);

        port.sync("activity", path);

        String insert = jdbc.firstSqlContaining("INSERT INTO hou_permission_menu");
        assertEquals(888, jdbc.argsOf(insert).get(0), "显式 parentPerms 必须优先于 group");
    }

    @Test
    void syncGarbageCollectsOwnedRowsMissingFromDeclaration() throws Exception {
        groupRootExists("growth", 555);
        // 库内已有若干本插件旧菜单不在本次声明集中 → 差集回收应照常执行
        jdbc.orphanCount = 4L;
        Path path = writeDeclaration("""
                code: activity
                menus:
                  - type: M
                    name: 营销活动
                    perms: shell:activity:apps
                    path: /plugin/activity
                    group: growth
                """);

        port.sync("activity", path);

        // 先数后删：1 次孤儿计数 + 2 张表的 DELETE
        assertTrue(jdbc.sqls.stream().anyMatch(s -> s.contains("COUNT(*)") && s.contains("NOT IN")),
                "差集回收前必须先统计孤儿行数");
        List<String> deleteSqls = jdbc.sqls.stream()
                .filter(sql -> sql.startsWith("DELETE") && sql.contains("NOT IN"))
                .toList();
        assertEquals(2, deleteSqls.size(), "差集回收必须同时清理 role_menu 与 menu 两张表");
        for (String sql : deleteSqls) {
            assertTrue(sql.contains("params <> 'host:menu-group'"), "回收 SQL 必须排除宿主分组根");
            assertTrue(sql.contains("perms NOT IN (?)"));
            assertTrue(sql.contains("params='plugin:activity'"), "不得仅凭路径或权限前缀回收历史业务菜单");
        }
        assertEquals(List.of("shell:activity:apps"), jdbc.argsOf(deleteSqls.get(0)));
    }

    /** 孤儿数为 0 时不必发 DELETE（避免无谓的两条写操作）。 */
    @Test
    void skipsOrphanDeleteWhenNothingToCollect() throws Exception {
        groupRootExists("growth", 555);
        jdbc.orphanCount = 0L;
        port.sync("activity", writeDeclaration("""
                code: activity
                menus:
                  - type: M
                    name: 营销活动
                    perms: shell:activity:apps
                    path: /plugin/activity
                    group: growth
                """));
        assertTrue(jdbc.sqls.stream().noneMatch(sql -> sql.startsWith("DELETE FROM hou_permission_menu")),
                "无孤儿时不应执行删除");
    }

    @Test
    void syncSkipsGarbageCollectionWhenDeclarationSetIsEmpty() throws Exception {
        Path path = writeDeclaration("""
                code: activity
                menus:
                  - name: 缺少 perms 与 path 的声明
                """);

        port.sync("activity", path);

        assertTrue(jdbc.sqls.stream().noneMatch(sql -> sql.contains("NOT IN")),
                "声明集为空时绝不能执行差集删除");
    }

    @Test
    void reservedPluginCodeIsRejectedOnSyncAndRemove() throws Exception {
        Path path = writeDeclaration("""
                code: group
                menus:
                  - type: M
                    name: 伪装分组
                    perms: shell:group:evil
                    path: /plugin/group
                """);

        assertThrows(IllegalArgumentException.class, () -> port.sync("group", path));
        assertThrows(IllegalArgumentException.class, () -> port.remove("group"));
        assertThrows(IllegalArgumentException.class, () -> port.sync("dashboard", path));
    }

    @Test
    void removeIsIdempotentAndNeverTouchesHostGroupRows() {
        port.remove("activity");
        port.remove("activity");

        List<String> deleteSqls = jdbc.sqls.stream()
                .filter(sql -> sql.startsWith("DELETE"))
                .toList();
        List<String> ownedDeletes = deleteSqls.stream()
                .filter(sql -> !sql.contains("LEFT JOIN"))
                .toList();
        assertEquals(4, ownedDeletes.size(), "两次 remove 各产生 role_menu + menu 两条按归属的 DELETE");
        for (String sql : ownedDeletes) {
            assertTrue(sql.contains("params <> 'host:menu-group'"),
                    "ownership 判定必须排除宿主分组根，防止插件卸载连带删除分组根");
        }
        assertEquals(2, deleteSqls.stream().filter(sql -> sql.contains("LEFT JOIN")).count(),
                "每次 remove 兜底清理一次孤儿授权，防止卸载后权限残留");
    }

    @Test
    void disableOnlyTouchesPluginOwnedRows() throws Exception {
        port.disable("activity");

        String sql = jdbc.sqls.get(0);
        assertTrue(sql.startsWith("UPDATE hou_permission_menu"));
        assertTrue(sql.contains("params <> 'host:menu-group'"));
    }

    @Test
    void delegatesEveryMenuLifecycleTransitionToSupplementalMenus() throws Exception {
        PluginSupplementalMenuPort supplemental = mock(PluginSupplementalMenuPort.class);
        port = new PluginMenuSyncPort(jdbc, List.of(supplemental));
        Path declaration = writeDeclaration("""
                code: mall-advertising
                menus:
                  - type: M
                    name: 广告管理
                    perms: mall-advertising:root
                    path: /plugin/mall-advertising
                """);

        port.sync("mall-advertising", declaration);
        port.enable("mall-advertising");
        port.disable("mall-advertising");
        port.remove("mall-advertising");

        verify(supplemental).sync("mall-advertising");
        verify(supplemental).enable("mall-advertising");
        verify(supplemental).disable("mall-advertising");
        verify(supplemental).remove("mall-advertising");
    }

    @Test
    void subsectionNamesAndEmptyDirectoryPathsSurviveSync() throws Exception {
        jdbc.queryResults.put(existingPermQueryKey("shell:finance:apps"), List.of(888));
        ownedParent("finance", "shell:finance:apps", 888);
        Path path = writeDeclaration("""
                code: finance
                menus:
                  - type: M
                    name: 资金管理
                    perms: shell:finance:nav:fund
                    path: ''
                    parentPerms: shell:finance:apps
                """);
        port.sync("finance", path);
        String insert = jdbc.firstSqlContaining("INSERT INTO hou_permission_menu");
        assertEquals(888, jdbc.argsOf(insert).get(0));
        assertEquals("资金管理", jdbc.argsOf(insert).get(2));
    }

    @Test
    void rootWritesCanonicalCategoryAndPluginPosition() throws Exception {
        jdbc.queryResults.put("SELECT COALESCE(cate_id,0) FROM hou_addon_plugin WHERE plugin_code=? AND deleted=0 ORDER BY id LIMIT 1|activity", List.of(999));
        port.sync("activity", writeDeclaration("""
                code: activity
                category: 旧文案
                menus:
                  - type: M
                    perms: shell:activity:apps
                    path: /plugin/activity
                """));
        String insert = jdbc.firstSqlContaining("INSERT INTO hou_permission_menu");
        assertTrue(insert.contains("display_pos,cate_id"));
        assertEquals(List.of("plugin",999), jdbc.argsOf(insert).subList(14,16));
    }

    @Test
    void existingCoreMenuCannotBeClaimedByPluginManifest() throws Exception {
        jdbc.queryResults.put(existingPermQueryKey("shell:mall"), List.of(1));
        assertThrows(IllegalArgumentException.class, () -> port.sync("activity", writeDeclaration("""
                code: activity
                menus:
                  - type: M
                    perms: shell:mall
                    path: /plugin/activity
                """)));
        assertTrue(jdbc.sqls.isEmpty());
    }

    @Test
    void unipayClaimsLegacyPaymentRootWithoutChangingItsMenuId() throws Exception {
        jdbc.queryResults.put(existingPermQueryKey("shell:payment"), List.of(15));
        jdbc.queryResults.put("SELECT id FROM hou_addon_cate WHERE name=? ORDER BY sort ASC,id ASC LIMIT 1|办公与财务", List.of(303));

        port.sync("unipay", writeDeclaration("""
                code: unipay
                category: 办公与财务
                menus:
                  - type: M
                    name: 支付中心
                    perms: shell:payment
                    path: /plugin/unipay
                """));

        String update = jdbc.firstSqlContaining("UPDATE hou_permission_menu SET pid=");
        List<Object> args = jdbc.argsOf(update);
        assertEquals(0, args.get(0));
        assertEquals("支付中心", args.get(2));
        assertEquals("/plugin/unipay", args.get(5));
        assertEquals("plugin:unipay", args.get(7));
        assertEquals("unipay", args.get(8));
        assertEquals("plugin", args.get(9));
        assertEquals(303, args.get(10));
        assertEquals(15, args.get(args.size() - 1), "must preserve role-menu references to id=15");
    }

    @Test
    void lowcodeKeepsItsCoreRoute() throws Exception {
        port.sync("lowcode", writeDeclaration("""
                code: lowcode
                menus:
                  - type: M
                    perms: shell:lowcode:apps
                    path: /lowcode
                """));
        String insert = jdbc.firstSqlContaining("INSERT INTO hou_permission_menu");
        assertEquals("/lowcode", jdbc.argsOf(insert).get(6));
    }

    @Test
    void explicitParentCannotBorrowAnotherPluginsRoot() throws Exception {
        jdbc.queryResults.put(existingPermQueryKey("shell:finance:apps"), List.of(888));
        ownedParent("finance", "shell:finance:apps", 888);
        Path path = writeDeclaration("""
                code: activity
                menus:
                  - type: C
                    perms: shell:activity:coupon
                    path: /plugin/activity/coupon
                    parentPerms: shell:finance:apps
                """);
        assertThrows(IllegalArgumentException.class, () -> port.sync("activity", path));
        assertTrue(jdbc.sqls.isEmpty());
    }

    @Test
    void resyncPreservesAdministratorHiddenAndDisabledSettings() throws Exception {
        jdbc.queryResults.put(existingPermQueryKey("shell:activity:apps"), List.of(888));
        ownedParent("activity", "shell:activity:apps", 888);
        port.sync("activity", writeDeclaration("""
                code: activity
                menus:
                  - type: M
                    perms: shell:activity:apps
                    path: /plugin/activity
                """));
        String update = jdbc.firstSqlContaining("UPDATE hou_permission_menu SET pid=");
        assertTrue(update.contains("display_pos=IF(pid=0 AND display_pos='hidden','hidden',?)"));
        assertTrue(!update.contains("is_show=1") && !update.contains("is_disable=0"));
    }

    private void ownedParent(String code, String perms, int id) {
        jdbc.queryResults.put("SELECT id FROM hou_permission_menu WHERE perms=? AND (plugin_code=? OR "
                + "((plugin_code IS NULL OR plugin_code='') AND (params=? OR paths=? OR paths LIKE ?))) LIMIT 1|"
                + perms + "," + code + ",plugin:" + code + ",/plugin/" + code + ",/plugin/" + code + "/%", List.of(id));
    }

    /**
     * 为使用 menuSource: catalog 的插件把目录里所有分组父级一次性打好桩，
     * 使 sync 能走完整棵目录树（父级解析逐条查库，未打桩会抛「父级不存在」）。
     */
    private void stubCatalogParents(String code, int firstId) {
        List<Map<String, Object>> catalog = OfficialPluginNavigation.complete(code,
                Map.of("menuSource", "catalog"), List.of(Map.of("perms", "shell:" + code + ":apps")));
        int id = firstId;
        for (Map<String, Object> menu : catalog) {
            Object perms = menu.get("perms");
            if (perms == null || !String.valueOf(perms).contains(":nav:")) continue;
            ownedParent(code, String.valueOf(perms), id++);
        }
    }

    /**
     * 回归防线（2026-10-19）：只声明根入口、而库里已有 126 条本插件菜单时，
     * 差集回收必须中止并抛异常，绝不能把整棵菜单删平。
     * 这正是2026-09-22 GEO 侧栏被拍平的失败路径。
     */
    @Test
    void refusesToWipeMenusWhenDeclarationLooksTruncated() throws Exception {
        jdbc.queryResults.put(existingPermQueryKey("shell:geo:apps"), List.of(100942));
        ownedParent("geo", "shell:geo:apps", 100942);
        jdbc.orphanCount = 126L;

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> port.sync("geo", writeDeclaration("""
                        code: geo
                        owner: official
                        runtime: standalone
                        version: 1.0.0
                        menus:
                          - type: M
                            perms: shell:geo:apps
                            path: /plugin/geo
                        """)));

        assertTrue(error.getMessage().contains("已中止差集回收"), error.getMessage());
        assertTrue(error.getMessage().contains("plugin.yaml"), "异常须提示排查 plugin.yaml");
        // 关键：一条 DELETE 都不能发出去
        assertTrue(jdbc.sqls.stream().noneMatch(s -> s.startsWith("DELETE rm FROM hou_permission_role_menu")),
                "角色授权不得被删除");
        assertTrue(jdbc.sqls.stream().noneMatch(s -> s.startsWith("DELETE FROM hou_permission_menu")),
                "菜单行不得被删除");
    }

    /** 正常场景：孤儿数在阈值内（少量下线菜单）仍应执行回收，不误伤产品决策。 */
    @Test
    void stillRecoversSmallNumberOfOrphans() throws Exception {
        jdbc.queryResults.put(existingPermQueryKey("shell:geo:apps"), List.of(100942));
        ownedParent("geo", "shell:geo:apps", 100942);
        stubCatalogParents("geo", 110000);
        jdbc.orphanCount = 3L;

        port.sync("geo", writeDeclaration("""
                code: geo
                owner: official
                menuSource: catalog
                runtime: standalone
                version: 1.0.0
                menus:
                  - type: M
                    perms: shell:geo:apps
                    path: /plugin/geo
                """));

        assertTrue(jdbc.sqls.stream().anyMatch(s -> s.startsWith("DELETE rm FROM hou_permission_role_menu")),
                "少量孤儿应照常回收");
    }

    /** 声明集为空时维持既有防御：跳过回收。 */
    @Test
    void emptyDeclarationSkipsOrphanRemoval() throws Exception {
        port.sync("activity", writeDeclaration("""
                code: activity
                menus: []
                """));
        assertTrue(jdbc.sqls.stream().noneMatch(s -> s.contains("NOT IN")), "空声明不得触发回收");
    }

    /** 记录型 JdbcTemplate 桩：记录 SQL/参数，按 SQL+参数返回预置查询结果。 */
    private static class RecordingJdbcTemplate extends JdbcTemplate {
        final List<String> sqls = new ArrayList<>();
        final Map<String, List<Object>> argsBySql = new LinkedHashMap<>();
        final Map<String, List<?>> queryResults = new LinkedHashMap<>();
        /** 孤儿行数查询（SELECT COUNT(*) ... NOT IN）返回值，null 表示未设置 → 默认 0。 */
        Long orphanCount;

        void putQueryResult(String sql, Object[] args, List<?> rows) {
            queryResults.put(sql + "|" + String.join(",", toStrings(args)), rows);
        }

        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
            List<?> rows = queryResults.get(sql + "|" + String.join(",", toStrings(args)));
            return (List<T>) (rows == null ? List.of() : rows);
        }

        /** 供差集回收前的规模校验计数（同时记录 SQL，便于断言「先数后删」）。 */
        @Override
        @SuppressWarnings("unchecked")
        public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
            sqls.add(sql);
            argsBySql.put(sql, List.of(args));
            if (sql.contains("COUNT(*)") && sql.contains("NOT IN")) {
                return (T) (orphanCount == null ? Long.valueOf(0L) : orphanCount);
            }
            return requiredType.cast(0L);
        }

        @Override
        public int update(String sql, Object... args) {
            sqls.add(sql);
            argsBySql.put(sql, List.of(args));
            return 1;
        }

        @Override
        public int update(String sql) {
            sqls.add(sql);
            argsBySql.put(sql, List.of());
            return 1;
        }

        String firstSqlContaining(String fragment) {
            return sqls.stream().filter(sql -> sql.contains(fragment)).findFirst().orElseThrow();
        }

        List<Object> argsOf(String sql) {
            return argsBySql.get(sql);
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
