package com.houjiang.mall.service.plugin;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link OfficialPluginNavigation} 行为约束。
 *
 * <p>2026-10-19 重构：注入判据从「隐式的 runtime=embedded」改为「显式的 menuSource: catalog」，
 * 并新增 perms 去重。本测试同时是那两次 GEO 侧栏事故的回归防线。
 */
class OfficialPluginNavigationTest {

    /** 显式声明使用目录的声明体（不再依赖 runtime）。 */
    private final Map<String, Object> catalogDeclared =
            Map.of("owner", "official", "runtime", "standalone", "version", "1.0.0", "menuSource", "catalog");

    /** 未声明 menuSource：行为必须与重构前一致（不注入）。 */
    private final Map<String, Object> notDeclared =
            Map.of("owner", "official", "runtime", "embedded", "version", "1.0.0");

    @Test
    void injectsCatalogOnlyWhenExplicitlyDeclared() {
        var root = List.<Map<String, Object>>of(Map.of("perms", "shell:finance:apps", "path", "/plugin/finance"));

        // 显式声明 → 注入目录（哪怕 runtime=standalone，这是本次根治的核心）
        var completed = OfficialPluginNavigation.complete("finance", catalogDeclared, root);
        assertTrue(completed.size() > 1);
        assertTrue(completed.stream().anyMatch(m -> "/plugin/finance/fund/receipt".equals(m.get("path"))));

        // 未声明 menuSource → 原样返回，即使 runtime=embedded 也不再注入（不猜、不误伤存量插件）
        assertSame(root, OfficialPluginNavigation.complete("finance", notDeclared, root));
        assertSame(root, OfficialPluginNavigation.complete("finance", Map.of(), root));

        // menuSource 写错值等同未声明（fail-safe）
        assertSame(root, OfficialPluginNavigation.complete("finance",
                Map.of("owner", "official", "menuSource", "whatever"), root));

        // 幂等：注入后的结果再次注入不应继续增长
        assertEquals(completed.size(), OfficialPluginNavigation.complete("finance", catalogDeclared, completed).size());
    }

    /** 回归防线：standalone 插件（geo）必须能拿到完整目录，否则会重演 2026-09-22 删库事故。 */
    @Test
    void standaloneGeoGetsFullCatalog() {
        var root = List.<Map<String, Object>>of(Map.of("perms", "shell:geo:apps", "path", "/plugin/geo"));
        var menus = OfficialPluginNavigation.complete("geo", catalogDeclared, root);

        // 2026-10-20 移除「小红书平台设置」后目录为 124 条（15 组 + 109 项）
        assertEquals(125, menus.size(), "geo 应为 1 条根入口 + 124 条目录条目（15 组 + 109 项）");
        long groups = menus.stream().filter(m -> "M".equals(m.get("type"))).count();
        long leaves = menus.stream().filter(m -> "C".equals(m.get("type"))).count();
        assertEquals(15, groups, "15 个二级分组");
        assertEquals(109, leaves, "109 个三级菜单项");
    }

    /** 重复声明不能产生重复行：plugin.yaml 与目录 perms 重叠时按 perms 去重。 */
    @Test
    void deduplicatesByPerms() {
        var root = new ArrayList<Map<String, Object>>();
        root.add(Map.of("perms", "shell:geo:apps", "path", "/plugin/geo"));
        root.add(Map.of("perms", "shell:geo:nav:geo-plate-brand", "name", "品牌建档", "type", "M"));

        var menus = OfficialPluginNavigation.complete("geo", catalogDeclared, root);
        var seen = new HashSet<String>();
        for (var menu : menus) {
            assertTrue(seen.add((String) menu.get("perms")), "perms 重复: " + menu);
        }
        assertEquals(125, menus.size(), "重复 perms 应被去重，声明集仍为 1 根 + 124 目录条目");
    }

    /** 目录缺失/插件不在目录内时不得抛异常阻断插件安装。 */
    @Test
    void missingCatalogEntryIsNoop() {
        var root = List.<Map<String, Object>>of(Map.of("perms", "shell:nosuch:apps"));
        assertSame(root, OfficialPluginNavigation.complete("nosuch", catalogDeclared, root));
        assertSame(root, OfficialPluginNavigation.complete(null, catalogDeclared, root));
        assertFalse(OfficialPluginNavigation.usesCatalog(null));
    }

    @Test
    void everyModuleHasUniquePermissionsAndParentBeforeChild() {
        for (String code : List.of("activity", "aicode", "cms", "crm", "erp", "finance", "geo", "im",
                "logging", "mes", "oa", "orderfood", "pm", "supplier")) {
            var root = List.<Map<String, Object>>of(Map.of("perms", "shell:" + code + ":apps"));
            var menus = OfficialPluginNavigation.complete(code, catalogDeclared, root);
            assertTrue(menus.size() > 1, code);
            var seen = new HashSet<String>();
            seen.add("shell:" + code + ":apps");
            for (var menu : menus.subList(1, menus.size())) {
                assertTrue(seen.contains(menu.get("parentPerms")), menu.toString());
                assertTrue(seen.add((String) menu.get("perms")), menu.toString());
                String path = (String) menu.get("path");
                assertTrue("M".equals(menu.get("type")) || path.startsWith("/plugin/" + code + "/")
                        || path.equals("/plugin/" + code), menu.toString());
                assertFalse(path.contains(":"), "parameterized forms are not navigation entries");
            }
        }
    }
}