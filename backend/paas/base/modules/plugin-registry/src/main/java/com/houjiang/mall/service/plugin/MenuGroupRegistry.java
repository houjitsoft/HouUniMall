package com.houjiang.mall.service.plugin;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 插件一级菜单分组注册表（方案见《开源插件化架构/插件菜单治理方案.md》§5.2/§8.1）。
 *
 * <p>分组根是宿主资产：种子 SQL（permission 模块 {@code db/permission-plugin-menu-groups.sql}）
 * 与本注册表共同维护同一组 key，两侧由契约测试保证一致。插件在 plugin.yaml 声明
 * {@code group: <key>} 后，{@link PluginMenuSyncPort} 按 {@code shell:group:<key>} 解析父节点 id。
 *
 * <p>规则：
 * <ul>
 *   <li>未声明 {@code group} 的插件按 {@code plugin.yaml 的 category} 映射推导（兼容存量）；</li>
 *   <li>仍无法推导或声明了未知 key 的，一律落 {@link #FALLBACK_KEY}（"其他应用"）并告警，绝不静默铺到一级；</li>
 *   <li>分组根本身不是权限点，不授予角色——用户可见性由组内子菜单授权 + 祖先补全决定。</li>
 * </ul>
 *
 * @author Amos
 */
public final class MenuGroupRegistry {

    /** 未归属/未知分组的兜底分组 key（前端对空分组自动隐藏）。 */
    public static final String FALLBACK_KEY = "other";

    /** 分组根 perms 前缀，与种子 SQL 一致。 */
    public static final String GROUP_PERMS_PREFIX = "shell:group:";

    /**
     * 2026-09-20 菜单重构后，插件模块统一收拢到宿主「插件中心」下，分组根（{@code shell:group:*}）已退役。
     * 注册表与种子 SQL 仅作历史兼容保留，解析分组根失败时一律回落到此父节点，绝不再挂一级（pid=0）。
     */
    public static final String PLUGIN_CENTER_PERMS = "shell:addons";

    /** 「插件中心」菜单 id（线上为 4）；仅作为解析失败时的最后兜底，优先按 perms 查库。 */
    public static final int PLUGIN_CENTER_MENU_ID = 4;

    /**
     * 分组根路径保留命名空间。插件编码不得占用，否则
     * {@link PluginMenuSyncPort} 的 ownershipPredicate（paths LIKE '/{code}/%'）会误删分组根。
     */
    public static final List<String> RESERVED_PLUGIN_CODES = List.of(
            "group", "platform", "setting", "dashboard");

    /** 一个分组根（与种子 SQL 中的行一一对应）。 */
    public record MenuGroup(String key, String name, String perms, int sort) {
    }

    /**
     * 分组清单：key 与 sort 必须与种子 SQL 完全一致（契约测试锁定）。
     *
     * <p>sort 取值带 3300–3800，刻意落在**宿主「插件中心」(4000) 之下、系统设置(1000) 之上**的插件区原位：
     * 一级栏排序为 {@code sort DESC}，如此既满足「官方一级菜单在前、分组根其次」的优先级，
     * 又不打散宿主菜单块（否则分组会插进「项目管理 9500」和「支付中心 7000」之间）。
     */

    private static final Map<String, MenuGroup> GROUPS = Map.ofEntries(
            Map.entry("biz", new MenuGroup("biz", "业务经营", GROUP_PERMS_PREFIX + "biz", 3800)),
            Map.entry("growth", new MenuGroup("growth", "营销增长", GROUP_PERMS_PREFIX + "growth", 3700)),
            Map.entry("org", new MenuGroup("org", "组织协同", GROUP_PERMS_PREFIX + "org", 3600)),
            Map.entry("fin", new MenuGroup("fin", "财务合规", GROUP_PERMS_PREFIX + "fin", 3500)),
            Map.entry("ai", new MenuGroup("ai", "智能工具", GROUP_PERMS_PREFIX + "ai", 3400)),
            Map.entry(FALLBACK_KEY, new MenuGroup(FALLBACK_KEY, "其他应用", GROUP_PERMS_PREFIX + FALLBACK_KEY, 3300)));

    /** 存量插件 category → 分组映射（PluginCatalogItem.category 兜底推导）。 */
    private static final Map<String, String> CATEGORY_TO_GROUP = Map.ofEntries(
            Map.entry("企业管理", "biz"),
            Map.entry("零售收银", "biz"),
            Map.entry("生产制造", "biz"),
            Map.entry("餐饮零售", "biz"),
            Map.entry("商品工具", "biz"),
            Map.entry("营销工具", "growth"),
            Map.entry("内容营销", "growth"),
            Map.entry("内容运营", "growth"),
            Map.entry("增长工具", "growth"),
            Map.entry("协同办公", "org"),
            Map.entry("财务合规", "fin"),
            Map.entry("财务工具", "fin"),
            Map.entry("风险合规", "fin"),
            Map.entry("开发工具", "ai"),
            Map.entry("运维监控", "ai"),
            // Older installed plugin packages predate menus.group and use English categories.
            // Keep these aliases aligned with the official manifests; unknown categories stay other.
            Map.entry("collaboration", "org"),
            Map.entry("marketing", "growth"),
            Map.entry("customer management", "org"),
            Map.entry("growth", "growth"),
            Map.entry("manufacturing", "biz"),
            Map.entry("content", "growth"),
            Map.entry("supply chain", "org"),
            Map.entry("finance", "fin"),
            Map.entry("customer service", "growth"),
            Map.entry("commerce", "biz"),
            Map.entry("office", "org"),
            Map.entry("developer tools", "ai"),
            Map.entry("operations", "ai"));

    private MenuGroupRegistry() {
    }

    /** 按注册表内的 key 查分组；未知 key 返回 empty（调用方应落 fallback 并告警）。 */
    public static Optional<MenuGroup> find(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(GROUPS.get(key.trim().toLowerCase(Locale.ROOT)));
    }

    /** 分组 key 是否在白名单内。 */
    public static boolean isRegistered(String key) {
        return find(key).isPresent();
    }

    /** 全部注册分组（含 fallback）。 */
    public static List<MenuGroup> all() {
        return List.copyOf(GROUPS.values());
    }

    /** 由插件 category 推导分组 key；无法推导时落 fallback。 */
    public static String resolveByCategory(String category) {
        if (category == null || category.isBlank()) {
            return FALLBACK_KEY;
        }
        return CATEGORY_TO_GROUP.getOrDefault(category.trim().toLowerCase(Locale.ROOT), FALLBACK_KEY);
    }

    /** 解析声明值 → 有效分组 key：合法则原样返回，非法（含空）则落 fallback。 */
    public static String resolveDeclared(String declaredKey) {
        return find(declaredKey).map(MenuGroup::key).orElse(FALLBACK_KEY);
    }

    /** 插件编码是否占用分组根/宿主保留命名空间。 */
    public static boolean isReservedPluginCode(String pluginCode) {
        if (pluginCode == null) {
            return false;
        }
        return RESERVED_PLUGIN_CODES.contains(pluginCode.trim().toLowerCase(Locale.ROOT));
    }
}
