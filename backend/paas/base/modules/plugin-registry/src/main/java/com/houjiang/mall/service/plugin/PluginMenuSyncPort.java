package com.houjiang.mall.service.plugin;

import com.houjiang.plugin.engine.spi.PluginMenuPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.jar.JarFile;

/** Synchronizes plugin.yaml menus into the shared Cloud permission tables. */
@Slf4j
@Component
public class PluginMenuSyncPort implements PluginMenuPort {

    private static final java.util.Set<Integer> CORE_ROOT_IDS = java.util.Set.of(19, 1, 1009, 4, 18);
    private static final String OWNER_PARAM = "plugin:";
    /** 分组根行的宿主归属标记（种子 SQL 写入），任何插件的 ownership 判定都不得命中它。 */
    public static final String HOST_GROUP_PARAM = "host:menu-group";
    private static final Map<String, String> PLUGIN_DISPLAY_NAMES = Map.of(
            "activity", "营销活动",
            "aicode", "AI 开发",
            "finance", "财务中心",
            "logging", "日志管理",
            "orderfood", "餐饮管理",
            "pm", "项目管理",
            "supplier", "供应商管理",
            "unipay", "支付中心");

    /** 支付中心由 payment 服务直接提供，不再作为 unipay 插件登记菜单。 */
    private static boolean isPaymentService(String pluginCode) {
        return "unipay".equals(pluginCode);
    }
    private final JdbcTemplate jdbcTemplate;
    private final List<PluginSupplementalMenuPort> supplementalMenus;

    /**
     * Mall and permission share the same Redis namespace.  Menu lifecycle
     * changes must evict the permission service's per-user snapshots instead
     * of waiting for their 60 minute TTL to expire.
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public PluginMenuSyncPort(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, List.of());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public PluginMenuSyncPort(JdbcTemplate jdbcTemplate,
                              List<PluginSupplementalMenuPort> supplementalMenus) {
        this.jdbcTemplate = jdbcTemplate;
        this.supplementalMenus = supplementalMenus == null ? List.of() : List.copyOf(supplementalMenus);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void sync(String pluginCode, Path pluginPath) {
        assertAllowedPluginCode(pluginCode);
        if (isPaymentService(pluginCode)) {
            log.info("支付中心是 payment 独立服务，跳过插件菜单同步: {}", pluginCode);
            return;
        }
        Map<String, Object> declaration = readDeclaration(pluginPath);
        List<Map<String, Object>> menus = OfficialPluginNavigation.complete(pluginCode, declaration, extractMenus(declaration));
        if (menus.isEmpty()) {
            log.info("插件 {} 未声明 menus，跳过权限菜单同步", pluginCode);
            return;
        }
        int cateId = resolveCategoryId(pluginCode, text(declaration.get("category")));
        String owner = OWNER_PARAM + pluginCode;
        java.util.Set<String> declaredPerms = new java.util.LinkedHashSet<>();
        for (Map<String, Object> menu : menus) {
            String perms = text(menu.get("perms"));
            String parentPerms = text(menu.get("parentPerms"));
            String path = normalizePluginPath(pluginCode, text(menu.get("path")));
            boolean directory = "M".equals(value(menu, "type", "C")) && StringUtils.hasText(parentPerms);
            if (!StringUtils.hasText(perms) || (!StringUtils.hasText(path) && !directory)) {
                continue;
            }
            // 子菜单若与父级共用同一 perms，按 perms 查重会命中父行并 UPDATE 覆盖掉
            // display_pos=plugin 的根入口（组合促销等 order-* 历史 yaml 即此形态）。
            // 这里按 path 末段派生独立 perms，保证根入口不被踩掉。
            if (StringUtils.hasText(parentPerms) && parentPerms.equals(perms)) {
                String leaf = path;
                int slash = leaf != null ? leaf.lastIndexOf('/') : -1;
                String suffix = slash >= 0 && slash + 1 < leaf.length() ? leaf.substring(slash + 1) : "page";
                perms = parentPerms + ":" + suffix;
                menu.put("perms", perms);
                log.warn("插件 {} 子菜单 perms 与 parentPerms 相同，已派生为 {}", pluginCode, perms);
            }
            String menuName = displayName(pluginCode, menu);
            Integer pid = resolveParentId(pluginCode, menu);
            int sortValue = resolveSort(menu, pid);
            List<Integer> existing = jdbcTemplate.query("SELECT id FROM hou_permission_menu WHERE perms=? "
                            + "ORDER BY CASE WHEN display_pos='hidden' THEN 1 ELSE 0 END,id LIMIT 1",
                    (rs, rowNum) -> rs.getInt(1), perms);
            if (existing.isEmpty() && pid == 0) {
                existing = jdbcTemplate.query("SELECT id FROM hou_permission_menu WHERE id NOT IN (19,1,1009,4,18) "
                                + "AND (params IS NULL OR params<>'host:menu-group') "
                                + "AND ((plugin_code=? AND pid=0) OR paths=? OR paths=?) ORDER BY id LIMIT 1",
                        (rs, rowNum) -> rs.getInt(1), pluginCode, "/plugin/" + pluginCode, "/" + pluginCode);
            }
            if (!existing.isEmpty() && CORE_ROOT_IDS.contains(existing.get(0))) {
                throw new IllegalArgumentException("插件不得覆盖固定核心入口: " + perms);
            }
            String displayPos = pid == 0 ? "plugin" : "left";
            int menuCateId = pid == 0 ? cateId : 0;
            if (existing.isEmpty()) {
                // plugin_code 与 params 双写：params='plugin:{code}' 是历史口径（字符串标记），
                // plugin_code 是 2026-09-19 新增的一等字段（可索引、可 JOIN）。两者同时写，
                // 读取侧先按 plugin_code、取不到再回退 params，保证新老数据都能认。
                jdbcTemplate.update("INSERT INTO hou_permission_menu "
                                + "(pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,is_cache,is_show,is_disable,display_pos,cate_id,create_time,update_time) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,UNIX_TIMESTAMP(),UNIX_TIMESTAMP())",
                        pid, value(menu, "type", "C"), menuName,
                        value(menu, "icon", "Document"), sortValue, perms, path,
                        value(menu, "component", ""), value(menu, "selected", ""), owner, pluginCode, 0, 1, 0, displayPos, menuCateId);
            } else {
                jdbcTemplate.update("UPDATE hou_permission_menu SET pid=?,type=?,name=?,icon=?,sort=IF(pid=0,sort,?),paths=?,component=?,params=?,plugin_code=?,display_pos=IF(pid=0 AND display_pos='hidden','hidden',?),cate_id=?,perms=?,update_time=UNIX_TIMESTAMP() WHERE id=?",
                        pid, value(menu, "type", "C"), menuName,
                        value(menu, "icon", "Document"), sortValue, path,
                        value(menu, "component", ""), owner, pluginCode, displayPos, menuCateId, perms, existing.get(0));
            }
            declaredPerms.add(perms);
            grantAdminRoles(perms);
        }
        removeOrphanMenus(pluginCode, declaredPerms);
        clearPermissionCaches();
        supplementalMenus.forEach(port -> port.sync(pluginCode));
    }

    /** 只有显式 parentPerms 表示内部子菜单；分类不再产生菜单父节点。 */
    private Integer resolveParentId(String pluginCode, Map<String, Object> menu) {
        String parentPerms = text(menu.get("parentPerms"));
        if (!StringUtils.hasText(parentPerms)) return 0;
        List<Integer> parents = jdbcTemplate.query(
                "SELECT id FROM hou_permission_menu WHERE perms=? AND (plugin_code=? OR "
                        + "((plugin_code IS NULL OR plugin_code='') AND (params=? OR paths=? OR paths LIKE ?))) LIMIT 1",
                (rs, rowNum) -> rs.getInt(1), parentPerms, pluginCode, OWNER_PARAM + pluginCode,
                "/plugin/" + pluginCode, "/plugin/" + pluginCode + "/%");
        int parent = parents.isEmpty() ? 0 : parents.get(0);
        if (parent == 0 || CORE_ROOT_IDS.contains(parent)) {
            throw new IllegalArgumentException("插件子菜单父级不存在或不属于当前插件: " + parentPerms);
        }
        return parent;
    }

    /** 已登记分类优先，首次安装只按同名字典推导，不依赖固定分类 ID。 */
    private int resolveCategoryId(String pluginCode, String category) {
        List<Integer> catalog = jdbcTemplate.query(
                "SELECT COALESCE(cate_id,0) FROM hou_addon_plugin WHERE plugin_code=? AND deleted=0 ORDER BY id LIMIT 1",
                (rs, rowNum) -> rs.getInt(1), pluginCode);
        if (!catalog.isEmpty() && catalog.get(0) > 0) return catalog.get(0);
        if (!StringUtils.hasText(category)) return 0;
        List<Integer> categories = jdbcTemplate.query(
                "SELECT id FROM hou_addon_cate WHERE name=? ORDER BY sort ASC,id ASC LIMIT 1",
                (rs, rowNum) -> rs.getInt(1), category);
        return categories.isEmpty() ? 0 : categories.get(0);
    }

    /**
     * 仅回收本同步器显式标记归属的行（禁止按路径/权限前缀删除历史菜单及角色授权）。
     * 差集回收（修方案缺口 G1）：sync 只增改会让"插件升级后从 plugin.yaml 删除的旧菜单行"
     * 成为永久孤儿（且已带 role_menu 授权）。这里在 ownership 范围内删除本次声明集之外的行。
     *
     * <p>防御 1：声明集为空时中止——plugin.yaml 解析异常/字段全缺失时绝不能误删全量。
     *
     * <p>防御 2（2026-10-19，根治 GEO 侧栏二次事故）：删除行数超过阈值时中止并抛异常。
     * 历史事故的失败方向是"删库"——目录注入门槛不生效导致声明集只剩根入口，
     * 于是库里整棵分组菜单被差集回收删平。仅靠"声明集非空"这道防线拦不住：
     * 声明集里有 1 条合法记录，回收照样会把另外 126 条清空。真正的信号是
     * <b>单次 sync 试图删除的菜单行数远超声明数</b>，这几乎不可能是真实的产品决策。
     */
    private void removeOrphanMenus(String pluginCode, java.util.Set<String> declaredPerms) {
        if (declaredPerms == null || declaredPerms.isEmpty()) {
            log.warn("插件 {} 本次菜单声明集为空，跳过差集回收", pluginCode);
            return;
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(declaredPerms.size(), "?"));
        Object[] args = declaredPerms.toArray();
        long orphans = countOrphanMenus(pluginCode, placeholders, args);
        assertOrphanRemovalIsSane(pluginCode, declaredPerms.size(), orphans);
        if (orphans == 0) {
            return;
        }
        jdbcTemplate.update("DELETE rm FROM hou_permission_role_menu rm JOIN hou_permission_menu m ON m.id=rm.menu_id WHERE "
                + ownershipPredicate(pluginCode, "m") + " AND m.params='" + OWNER_PARAM + pluginCode + "' AND m.perms NOT IN (" + placeholders + ")", args);
        jdbcTemplate.update("DELETE FROM hou_permission_menu WHERE "
                + ownershipPredicate(pluginCode, "") + " AND params='" + OWNER_PARAM + pluginCode + "' AND perms NOT IN (" + placeholders + ")", args);
        log.warn("插件 {} 差集回收：声明 {} 条，清理孤儿菜单 {} 条", pluginCode, declaredPerms.size(), orphans);
    }

    /**
     * 差集回收的行数上限系数：单次允许清理的孤儿行数 = 声明数 ×该系数 + 固定余量。
     * 允许少量清理（产品确实下线过一两个菜单），但拦住"数量级不对"的整棵删平。
     */
    private static final int ORPHAN_TOLERANCE_FACTOR = 2;
    private static final long ORPHAN_TOLERANCE_FLOOR = 10L;

    /** 先数一遍将被删除的孤儿菜单行数（不产生副作用）。 */
    private long countOrphanMenus(String pluginCode, String placeholders, Object[] args) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hou_permission_menu WHERE "
                + ownershipPredicate(pluginCode, "") + " AND params='" + OWNER_PARAM + pluginCode + "' AND perms NOT IN (" + placeholders + ")",
                Long.class, args);
        return count == null ? 0L : count;
    }

    /**
     * 回收前的规模合理性校验。超限时抛 {@link IllegalStateException}：
     * sync 处于事务中（{@code @Transactional}），异常会回滚本次菜单变更，
     * 库内既有菜单与角色授权保持原样 —— 用「装不上」换「菜单被清空」，方向明确正确。
     */
    private void assertOrphanRemovalIsSane(String pluginCode, int declaredCount, long orphans) {
        if (orphans == 0) return;
        long tolerance = Math.max(ORPHAN_TOLERANCE_FLOOR, (long) declaredCount * ORPHAN_TOLERANCE_FACTOR);
        if (orphans > tolerance) {
            throw new IllegalStateException(String.format(
                    "插件 %s 菜单声明可能不完整，已中止差集回收以保护现有菜单：本次声明 %d 条，"
                            + "但库内有 %d 条本插件菜单不在声明集中，超过安全上限 %d 条。"
                            + "请检查 plugin.yaml 是否漏写子菜单，或该插件是否误删了 menuSource: catalog 声明。",
                    pluginCode, declaredCount, orphans, tolerance));
        }
    }

    /**
     * 排序取值：挂到分组下的行（pid != 0）优先取 {@code groupSort}（组内排序，方案 §7），
     * 未声明时回退 {@code sort}，再回退 900；未分组行维持原 {@code sort} 语义。
     */
    private int resolveSort(Map<String, Object> menu, Integer pid) {
        String groupSort = text(menu.get("groupSort"));
        if (pid != null && pid != 0 && StringUtils.hasText(groupSort)) {
            return integer(groupSort, 900);
        }
        return integer(menu.get("sort"), 900);
    }

    /** 插件编码占用宿主保留命名空间（如 group）会连带删除分组根，必须在入口拒绝。 */
    private void assertAllowedPluginCode(String pluginCode) {
        if (!StringUtils.hasText(pluginCode) || !pluginCode.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("非法插件编码: " + pluginCode);
        }
        if (MenuGroupRegistry.isReservedPluginCode(pluginCode)) {
            throw new IllegalArgumentException("插件编码占用宿主保留命名空间: " + pluginCode);
        }
    }

    @Override
    public void enable(String pluginCode) {
        updateOwned(pluginCode, "is_show=1,is_disable=0");
        clearPermissionCaches();
        supplementalMenus.forEach(port -> port.enable(pluginCode));
    }

    @Override
    public void disable(String pluginCode) {
        updateOwned(pluginCode, "is_show=0,is_disable=1");
        clearPermissionCaches();
        supplementalMenus.forEach(port -> port.disable(pluginCode));
    }

    /**
     * 卸载：清授权 → 删菜单 → 兜底清孤儿授权 → 清缓存。
     *
     * <p>三步必须同一次完成，保证「菜单注册」与「权限移除」成对：只删菜单不删授权会留下
     * 指向空 id 的权限残留，只删授权不删菜单会留下看得见却点不开的僵尸菜单。
     */
    @Override
    public void remove(String pluginCode) {
        assertAllowedPluginCode(pluginCode);
        if (isPaymentService(pluginCode)) {
            log.info("支付中心是 payment 独立服务，卸载插件时不回收其菜单: {}", pluginCode);
            return;
        }
        String predicate = ownershipPredicate(pluginCode, "m");
        jdbcTemplate.update("DELETE rm FROM hou_permission_role_menu rm JOIN hou_permission_menu m ON m.id=rm.menu_id WHERE " + predicate);
        jdbcTemplate.update("DELETE FROM hou_permission_menu WHERE " + ownershipPredicate(pluginCode, ""));
        purgeOrphanRoleMenu();
        clearPermissionCaches();
        supplementalMenus.forEach(port -> port.remove(pluginCode));
    }

    /**
     * 兜底清理指向已删菜单的孤儿授权行。
     *
     * <p>菜单行若在历史上被外部删除、或路径/归属字段缺失，按 menu JOIN 的清理会漏掉
     * 对应的授权行，卸载后表现为「权限残留」。这里只依据唯一事实——授权指向的菜单已
     * 不存在——来清理，不引入也不依赖任何名单。
     */
    private void purgeOrphanRoleMenu() {
        jdbcTemplate.update("DELETE rm FROM hou_permission_role_menu rm LEFT JOIN hou_permission_menu m ON rm.menu_id = m.id WHERE m.id IS NULL");
    }

    private void updateOwned(String pluginCode, String update) {
        jdbcTemplate.update("UPDATE hou_permission_menu SET " + update + ",update_time=UNIX_TIMESTAMP() WHERE " + ownershipPredicate(pluginCode, ""));
    }

    private String ownershipPredicate(String pluginCode, String alias) {
        if (!pluginCode.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("非法插件编码: " + pluginCode);
        }
        String shellPerm = "shell:" + pluginCode + "%";
        String pluginPath = "/plugin/" + pluginCode;
        String legacyPath = "/" + pluginCode;
        String prefix = alias == null || alias.isBlank() ? "" : alias + ".";
        String hostGuard = "(" + prefix + "params IS NULL OR " + prefix + "params <> '" + HOST_GROUP_PARAM + "')";
        return hostGuard + " AND " + prefix + "id NOT IN (19,1,1009,4,18) AND (" + prefix + "params = '" + OWNER_PARAM + pluginCode + "' OR " + prefix + "perms LIKE '" + shellPerm
                + "' OR " + prefix + "paths = '" + pluginPath + "' OR " + prefix + "paths LIKE '" + pluginPath + "/%'"
                + " OR " + prefix + "paths = '" + legacyPath + "' OR " + prefix + "paths LIKE '" + legacyPath + "/%')";
    }

    /** Menu paths are frontend routes, so every plugin-owned row must use the namespaced route. */
    private String normalizePluginPath(String pluginCode, String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        String normalized = path.trim();
        if ("lowcode".equals(pluginCode) && "/lowcode".equals(normalized)) return normalized;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        String legacyPrefix = "/" + pluginCode;
        String pluginPrefix = "/plugin/" + pluginCode;
        if (normalized.equals(legacyPrefix) || normalized.startsWith(legacyPrefix + "/")) {
            return pluginPrefix + normalized.substring(legacyPrefix.length());
        }
        return normalized;
    }

    private Integer parentId(String parentPerms) {
        if (!StringUtils.hasText(parentPerms)) return 0;
        List<Integer> ids = jdbcTemplate.query("SELECT id FROM hou_permission_menu WHERE perms=? LIMIT 1",
                (rs, rowNum) -> rs.getInt(1), parentPerms);
        return ids.isEmpty() ? 0 : ids.get(0);
    }

    private void grantAdminRoles(String perms) {
        jdbcTemplate.update("INSERT IGNORE INTO hou_permission_role_menu(role_id,menu_id) "
                        + "SELECT r.id,m.id FROM hou_permission_role r JOIN hou_permission_menu m ON m.perms=? "
                        + "WHERE r.delete_time IS NULL AND LOWER(TRIM(r.name)) IN ('admin','super_admin','管理员','超级管理员')",
                perms);
    }

    private void clearPermissionCaches() {
        if (redisTemplate == null) {
            log.warn("Redis 不可用，插件菜单变更将在用户菜单缓存 TTL 到期后生效");
            return;
        }
        try {
            deleteByPattern("user:menus:*");
            deleteByPattern("user:permissions:*");
        } catch (RuntimeException e) {
            // Menu DB changes remain authoritative; a transient Redis failure
            // must not make an otherwise successful plugin install fail.
            log.warn("清理插件菜单权限缓存失败: {}", e.getMessage());
        }
    }

    private void deleteByPattern(String pattern) {
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /** 读取完整 plugin.yaml 声明（含 menus 与 category）；文件缺失或非 Map 时返回空 Map。 */
    private Map<String, Object> readDeclaration(Path pluginPath) {
        try (InputStream in = openDeclaration(pluginPath)) {
            if (in == null) {
                return Map.of();
            }
            Object value = new Yaml().load(in);
            if (value instanceof Map<?, ?> declaration) {
                java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
                declaration.forEach((key, val) -> result.put(String.valueOf(key), val));
                return result;
            }
            return Map.of();
        } catch (Exception e) {
            throw new IllegalStateException("读取插件菜单声明失败: " + pluginPath, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractMenus(Map<String, Object> declaration) {
        Object items = declaration == null ? null : declaration.get("menus");
        if (!(items instanceof Iterable<?> iterable)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : iterable) {
            if (item instanceof Map<?, ?> raw) {
                java.util.LinkedHashMap<String, Object> menu = new java.util.LinkedHashMap<>();
                raw.forEach((key, val) -> menu.put(String.valueOf(key), val));
                result.add(menu);
            }
        }
        return result;
    }

    private InputStream openDeclaration(Path pluginPath) throws Exception {
        if (pluginPath == null) {
            return null;
        }
        if (Files.isDirectory(pluginPath)) {
            return Files.newInputStream(pluginPath.resolve("plugin.yaml"));
        }
        try (JarFile jar = new JarFile(pluginPath.toFile())) {
            var entry = jar.getJarEntry("plugin.yaml");
            if (entry == null) {
                return null;
            }
            try (InputStream in = jar.getInputStream(entry)) {
                return new java.io.ByteArrayInputStream(in.readAllBytes());
            }
        }
    }

    private String text(Object value) { return value == null ? null : String.valueOf(value).trim(); }
    private String value(Map<String, Object> map, String key, String fallback) {
        String value = text(map.get(key));
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String displayName(String pluginCode, Map<String, Object> menu) {
        String type = value(menu, "type", "C");
        if ("M".equalsIgnoreCase(type) && !StringUtils.hasText(text(menu.get("parentPerms")))) {
            String canonical = PLUGIN_DISPLAY_NAMES.get(pluginCode.toLowerCase(Locale.ROOT));
            if (canonical != null) return canonical;
        }
        return value(menu, "name", value(menu, "title", pluginCode));
    }
    private int integer(Object value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(String.valueOf(value)); }
        catch (NumberFormatException e) { return fallback; }
    }
}
