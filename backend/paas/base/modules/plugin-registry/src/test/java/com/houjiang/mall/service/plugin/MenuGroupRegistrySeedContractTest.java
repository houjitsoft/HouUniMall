package com.houjiang.mall.service.plugin;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分组注册表与宿主种子 SQL 的一致性契约（方案 §8.1 / §12）。
 * 防止「代码里有、库里没有」或「库里有、代码不认」的漂移。
 */
class MenuGroupRegistrySeedContractTest {

    private static final Pattern GROUP_COMMENT =
            Pattern.compile("^--\\s*group:\\s*([a-z]+)\\|([^|]+)\\|(\\d+)\\|([A-Za-z]+)\\s*$");

    private static Path locateSeedSql() {
        for (Path directory = Path.of("").toAbsolutePath(); directory != null; directory = directory.getParent()) {
            Path candidate = directory.resolve("backend/paas/permission/src/main/resources/db/permission-plugin-menu-groups.sql");
            if (Files.isRegularFile(candidate)) return candidate;
        }
        throw new IllegalStateException("找不到分组根种子 SQL，契约测试无法校验注册表与种子的一致性");
    }

    @Test
    void registryKeysMatchSeedSqlExactly() throws IOException {
        String sql = Files.readString(locateSeedSql(), StandardCharsets.UTF_8);

        Map<String, SeedRow> seeded = new HashMap<>();
        for (String line : sql.split("\n")) {
            Matcher matcher = GROUP_COMMENT.matcher(line.trim());
            if (matcher.matches()) {
                seeded.put(matcher.group(1), new SeedRow(matcher.group(1), matcher.group(2),
                        Integer.parseInt(matcher.group(3)), matcher.group(4)));
            }
        }

        Map<String, MenuGroupRegistry.MenuGroup> registry = MenuGroupRegistry.all().stream()
                .collect(Collectors.toMap(MenuGroupRegistry.MenuGroup::key, group -> group));

        assertEquals(seeded.keySet(), registry.keySet(),
                "种子 SQL 与 MenuGroupRegistry 的分组 key 必须完全一致（含 other 兜底组）");

        for (Map.Entry<String, MenuGroupRegistry.MenuGroup> entry : registry.entrySet()) {
            MenuGroupRegistry.MenuGroup group = entry.getValue();
            SeedRow row = seeded.get(entry.getKey());
            assertEquals(row.name, group.name(), "分组 " + entry.getKey() + " 名称不一致");
            assertEquals(row.sort, group.sort(), "分组 " + entry.getKey() + " sort 不一致");
            assertEquals("shell:group:" + entry.getKey(), group.perms(), "分组 " + entry.getKey() + " perms 不一致");
            assertTrue(sql.contains("params = 'host:menu-group'") || sql.contains("'host:menu-group',"),
                    "种子 SQL 必须写 host:menu-group 归属标记");
            assertTrue(sql.contains("WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE perms = 'shell:group:"
                            + entry.getKey() + "')"),
                    "分组 " + entry.getKey() + " 缺少幂等 NOT EXISTS 防重插");
        }
    }

    @Test
    void fallbackGroupMustExistInRegistry() {
        assertTrue(MenuGroupRegistry.isRegistered(MenuGroupRegistry.FALLBACK_KEY),
                "兜底分组 other 必须在注册表内");
        assertEquals(MenuGroupRegistry.FALLBACK_KEY, MenuGroupRegistry.resolveDeclared("不存在的分组"));
        assertEquals(MenuGroupRegistry.FALLBACK_KEY, MenuGroupRegistry.resolveDeclared(null));
    }

    @Test
    void reservedPluginCodesCannotOwnGroupNamespace() {
        for (String reserved : MenuGroupRegistry.RESERVED_PLUGIN_CODES) {
            assertTrue(MenuGroupRegistry.isReservedPluginCode(reserved), "保留字未生效: " + reserved);
            assertTrue(MenuGroupRegistry.isReservedPluginCode(reserved.toUpperCase()), "保留字应忽略大小写: " + reserved);
        }
        assertFalse(MenuGroupRegistry.isReservedPluginCode("oa"));
        assertFalse(MenuGroupRegistry.isReservedPluginCode("crm"));
    }

    @Test
    void categoryMappingCoversExistingCatalogCategories() {
        assertEquals("biz", MenuGroupRegistry.resolveByCategory("企业管理"));
        assertEquals("growth", MenuGroupRegistry.resolveByCategory("营销工具"));
        assertEquals("org", MenuGroupRegistry.resolveByCategory("协同办公"));
        assertEquals("fin", MenuGroupRegistry.resolveByCategory("财务合规"));
        assertEquals("ai", MenuGroupRegistry.resolveByCategory("运维监控"));
        assertEquals(MenuGroupRegistry.FALLBACK_KEY, MenuGroupRegistry.resolveByCategory("未知分类"));
        assertEquals(MenuGroupRegistry.FALLBACK_KEY, MenuGroupRegistry.resolveByCategory(null));
    }

    @Test
    void legacyEnglishCategoriesMatchOfficialManifestGroups() {
        Map<String, String> expected = Map.ofEntries(
                Map.entry("Collaboration", "org"), Map.entry("Marketing", "growth"),
                Map.entry("Customer Management", "org"), Map.entry("Growth", "growth"),
                Map.entry("Manufacturing", "biz"), Map.entry("Content", "growth"),
                Map.entry("Supply Chain", "org"), Map.entry("Finance", "fin"),
                Map.entry("Customer Service", "growth"), Map.entry("Commerce", "biz"),
                Map.entry("Office", "org"), Map.entry("Developer Tools", "ai"),
                Map.entry("Operations", "ai"));
        expected.forEach((category, group) -> {
            assertEquals(group, MenuGroupRegistry.resolveByCategory(category));
            assertEquals(group, MenuGroupRegistry.resolveByCategory(" " + category.toUpperCase() + " "));
        });
        assertEquals("other", MenuGroupRegistry.resolveByCategory("Custom Category"));
    }

    private record SeedRow(String key, String name, int sort, String icon) {
    }
}
