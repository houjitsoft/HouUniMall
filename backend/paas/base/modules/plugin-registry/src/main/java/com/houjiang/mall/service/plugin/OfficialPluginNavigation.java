package com.houjiang.mall.service.plugin;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 官方插件导航目录补全。
 *
 * <p><b>2026-10-19 重构（根治 GEO 侧栏二次事故）</b><br>
 * 旧实现用 {@code runtime == "embedded"} 作为「能否注入目录」的隐式判据，且要求
 * {@code menus.size() == 1}（即「只声明根入口，其余交给目录」）。这个约定有两个致命缺陷：
 * <ol>
 *   <li><b>隐式且易误判</b>：{@code runtime=standalone} 的插件（geo/crm/erp/finance/im/
 *       logging/mes/oa/orderfood/supplier 共10 个）全部命中不了门槛，而plugin.yaml
 *       里的完整菜单树又常常只写 1 条入口 —— 两者叠加，目录里的菜单永远读不到。</li>
 *   <li><b>失败方向是「删库」</b>：目录没注入 → {@link PluginMenuSyncPort#removeOrphanMenus}
 *       按 {@code perms NOT IN (declaredPerms)} 做差集回收 → 库里已有的整棵分组菜单
 *       被当成孤儿连同角色授权一起删除，菜单被拍平（2026-09-22 GEO 事故）。</li>
 * </ol>
 *
 * <p>新实现把「是否使用目录」变成 plugin.yaml 里<b>显式声明</b>的 {@code menuSource: catalog}，
 * 不再从 runtime 猜。同时注入结果按 perms 去重，保证与 plugin.yaml 中已显式写出的条目
 * 叠加时幂等。
 *
 * <p><b>未声明 {@code menuSource} 的插件行为完全不变</b>（仍走原menus，不注入目录），
 * 因此存量插件不会被本改动意外改变菜单结构。
 */
final class OfficialPluginNavigation {
    /** plugin.yaml 中显式声明使用导航目录的开关值。 */
    static final String MENU_SOURCE_KEY = "menuSource";
    static final String MENU_SOURCE_CATALOG = "catalog";

    private OfficialPluginNavigation() { }

    /**
     * 判断该插件是否显式声明使用导航目录。
     *
     * <p>只认 {@code menuSource: catalog} 这一个明确取值；缺省或写错都按「不使用目录」处理，
     * 从而 fail-safe：宁可不注入、也不误注入覆盖手工菜单。
     */
    static boolean usesCatalog(Map<String, Object> declaration) {
        if (declaration == null) return false;
        Object raw = declaration.get(MENU_SOURCE_KEY);
        return raw != null && MENU_SOURCE_CATALOG.equals(String.valueOf(raw).trim());
    }

    /**
     * 若 plugin.yaml 显式声明 {@code menuSource: catalog}，则用官方导航目录补全菜单。
     *
     * <p>补全按 perms 去重：目录条目与 plugin.yaml 已写出的条目若perms 相同只保留一份，
     * 因此「写了一半 + 目录补全」不会产生重复行，sync 保持幂等。
     */
    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> complete(String code, Map<String, Object> declaration,
                                               List<Map<String, Object>> menus) {
        if (!usesCatalog(declaration)) {
            return menus;
        }
        List<Map<String, Object>> catalogEntries = readCatalog(code);
        if (catalogEntries.isEmpty()) {
            // 声明了 catalog 但目录里没有对应条目：不算错误（该插件可能确实无需二级菜单），
            // 但必须留下线索，否则会退化成「以为补全了其实没补全」。
            return menus;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (Map<String, Object> menu : menus) {
            Object perms = menu.get("perms");
            if (perms != null) seen.add(String.valueOf(perms).trim());
        }
        List<Map<String, Object>> completed = new ArrayList<>(menus);
        for (Map<String, Object> entry : catalogEntries) {
            Object perms = entry.get("perms");
            String key = perms == null ? null : String.valueOf(perms).trim();
            if (key == null || key.isEmpty() || !seen.add(key)) continue;
            completed.add(entry);
        }
        return completed;
    }

    /** 读取指定插件的目录条目；资源缺失或格式异常均返回空列表（不阻断 sync）。 */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> readCatalog(String code) {
        if (code == null || code.isEmpty()) return List.of();
        try (InputStream in = OfficialPluginNavigation.class.getResourceAsStream("/plugin-navigation-v1.json")) {
            if (in == null) return List.of();
            Object parsed = new Yaml().load(in);
            if (!(parsed instanceof Map<?, ?> catalog)) return List.of();
            Object entries = catalog.get(code);
            if (!(entries instanceof List<?> list)) return List.of();
            List<Map<String, Object>> result = new ArrayList<>(list.size());
            for (Object item : list) {
                if (item instanceof Map<?, ?> raw) {
                    java.util.LinkedHashMap<String, Object> menu = new java.util.LinkedHashMap<>();
                    raw.forEach((k, v) -> menu.put(String.valueOf(k), v));
                    result.add(menu);
                }
            }
            return result;
        } catch (java.io.IOException | RuntimeException e) {
            // 目录读不出来时不能抛异常阻断插件安装——此时等同于「不使用目录」，
            // 配合 PluginMenuSyncPort 的声明完整性校验，缺声明会被显式拦下而不是静默删库。
            return List.of();
        }
    }
}