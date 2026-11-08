package com.houjiang.mall.service.plugin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.plugin.AddonPlugin;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.plugin.AddonCateMapper;
import com.houjiang.mall.mapper.plugin.AddonPluginMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.plugin.PluginCatalogItem;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 插件登记对账器：启动时把「统一插件目录 + 安装状态表」对齐到权威数据源。
 *
 * <p>三张表的职责与权威来源（本类只做单向修补，不引入新事实）：
 * <ul>
 *   <li>{@code hou_addon_cate} —— 分类唯一数据源；插件 ↔ 分类的绑定存在
 *       插件位一级菜单的 {@code cate_id} 上（与顶栏「插件应用」同口径）；</li>
 *   <li>{@code hou_addon_plugin} —— 统一插件目录，权威来源是静态目录
 *       （与 plugins/&lt;code&gt;/plugin.yaml 保持一致）∪ 市场同步的既有行；</li>
 *   <li>{@code hou_addon_plugin_install} —— 安装状态，权威信号是
 *       「插件位菜单存在 ∪ 运行健康 ∪ 已有 installed=1 记录」三者任一。
 *       安装建菜单、卸载删菜单，因此菜单存在 ⇔ 本地已安装且在用。</li>
 * </ul>
 *
 * <p>修复的历史缺口：随宿主发布的本地插件（webpos/mes/oa/pm/supplier/orderfood/aicode 等）
 * 只建了菜单、从没写安装记录，插件中心只能靠「运行健康」假装已安装；目录表也只有
 * mall-* 六行。本对账器在每次启动时幂等补齐，之后展示与筛选全部回到表驱动。
 *
 * <p>反向保护：显式卸载会软删目录行 + 置 installed=0，对账器不复活这类插件
 * （除非它重新出现插件位菜单或重新健康，说明又被装回来了）。
 *
 * @author Amos
 */
@Slf4j
@Component
public class PluginRegistryReconciler {

    /** 历史路由别名：与前端 pluginRouteConfig.js 的 LEGACY_PLUGIN_ROUTE_ALIASES 保持一致。 */
    private static final Map<String, String> ROUTE_ALIASES = Map.of("aicloud", "aicode");

    /**
     * 宿主服务编码兜底值（只有当插件自己的声明与市场/架构清单都拿不到时才会用到）。
     *
     * <p>🔴 2026-10-27：原值是 {@code "mall"}。但 2026-09-20 的架构基线已经取消 mall
     * 业务宿主（插件二开只能归属各自的 24 个业务服务），继续把目录行写成 mall
     * 会与「禁止 mall 归属」直接冲突，也会让按宿主分组的读侧统计失真。
     * 兜底改为 {@code base}（插件中心自身所在服务），声明值优先不变。
     */
    private static final String HOST_SERVICE_CODE = "base";

    /** 插件位菜单路径前缀。 */
    private static final String PLUGIN_MENU_PREFIX = "/plugin/";

    private final MallPluginInstallMapper installMapper;
    private final AddonPluginMapper addonPluginMapper;
    private final AddonCateMapper addonCateMapper;
    private final PluginCenterService centerService;
    private final PluginLifecycleManager lifecycleManager;

    public PluginRegistryReconciler(MallPluginInstallMapper installMapper,
                                    AddonPluginMapper addonPluginMapper,
                                    AddonCateMapper addonCateMapper,
                                    PluginCenterService centerService,
                                    PluginLifecycleManager lifecycleManager) {
        this.installMapper = installMapper;
        this.addonPluginMapper = addonPluginMapper;
        this.addonCateMapper = addonCateMapper;
        this.centerService = centerService;
        this.lifecycleManager = lifecycleManager;
    }

    /**
     * 先于 {@code PluginRuntimeBootstrap} 执行：先把登记补齐，安装态恢复才拿得到完整记录。
     * 任何异常只跳过本轮对账，不影响商城启动。
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void reconcile() {
        try {
            doReconcile();
        } catch (Exception e) {
            log.error("插件登记对账失败，跳过本轮（不影响商城启动）", e);
        }
    }

    void doReconcile() {
        List<PluginCatalogItem> catalog = centerService.catalog();
        if (catalog == null || catalog.isEmpty()) {
            log.info("[PluginReconcile] 目录为空，跳过对账");
            return;
        }
        Map<String, String> categories = centerService.canonicalCategories();
        Map<String, Integer> menuCateIds = pluginMenuCateIds();
        Set<String> healthy = healthyCodes(catalog);
        Map<String, MallPluginInstall> installs = existingInstalls();
        Map<String, String> registeredServices = existingRegisteredServices();
        Set<String> softDeleted = softDeletedCodes();

        // 本地已安装判定：插件位菜单 ∪ 运行健康 ∪ 已有 installed=1 记录
        Set<String> installedCodes = new LinkedHashSet<>();
        for (PluginCatalogItem item : catalog) {
            String code = item.getCode();
            if (StringUtils.hasText(code) && (menuCateIds.containsKey(code) || healthy.contains(code))) {
                installedCodes.add(code);
            }
        }
        for (MallPluginInstall install : installs.values()) {
            if (install != null && Integer.valueOf(1).equals(install.getInstalled())
                    && StringUtils.hasText(install.getPluginCode())) {
                installedCodes.add(install.getPluginCode());
            }
        }

        reconcileCatalog(catalog, categories, installedCodes, registeredServices, softDeleted);
        reconcileInstalls(catalog, installedCodes, installs);
        log.info("[PluginReconcile] 对账完成: 目录={} 已安装={} 菜单关联={} 健康={} 软删保护={}",
                catalog.size(), installedCodes.size(), menuCateIds.keySet(), healthy, softDeleted);
    }

    /** ① 目录补全：hou_addon_plugin = 目录全量（显式移出目录且无在用信号的行不由静态目录复活）。 */
    private void reconcileCatalog(List<PluginCatalogItem> catalog,
                                  Map<String, String> categories,
                                  Set<String> installedCodes,
                                  Map<String, String> registeredServices,
                                  Set<String> softDeleted) {
        for (PluginCatalogItem item : catalog) {
            String code = item.getCode();
            if (!StringUtils.hasText(code)) {
                continue;
            }
            if (!installedCodes.contains(code) && !registeredServices.containsKey(code)) {
                continue;
            }
            if (softDeleted.contains(code) && !installedCodes.contains(code)) {
                continue;
            }
            AddonPlugin row = new AddonPlugin();
            String declaredService = item.getHostService();
            row.setServiceCode(StringUtils.hasText(declaredService)
                    ? declaredService : registeredServices.getOrDefault(code, HOST_SERVICE_CODE));
            row.setPluginCode(code);
            row.setName(item.getName());
            row.setCategory(categories.getOrDefault(code, ""));
            row.setVersion(item.getVersion());
            row.setDescription(item.getDescription());
            row.setTerminalsJson(toJsonArray(item.getTerminals()));
            row.setDependenciesJson(toJsonArray(item.getDependencies()));
            row.setEntryPath(item.getEntryPath());
            row.setConfigSchema(StringUtils.hasText(item.getConfigSchema()) ? item.getConfigSchema() : "{}");
            row.setStatus(1);
            row.setSort(0);
            row.setDeleted(0);
            addonPluginMapper.upsertFromManifest(row);
        }
    }

    /** ② 安装登记补全：在用插件一律有 installed=1 记录；显式卸载（installed=0 且无在用信号）不动。 */
    private void reconcileInstalls(List<PluginCatalogItem> catalog,
                                   Set<String> installedCodes,
                                   Map<String, MallPluginInstall> installs) {
        Map<String, String> versions = new LinkedHashMap<>();
        for (PluginCatalogItem item : catalog) {
            versions.put(item.getCode(), item.getVersion());
        }
        LocalDateTime now = LocalDateTime.now();
        for (String code : installedCodes) {
            MallPluginInstall install = installs.get(code);
            if (install == null) {
                MallPluginInstall created = new MallPluginInstall();
                created.setPluginCode(code);
                created.setInstalled(1);
                created.setEnabled(1);
                created.setConfigJson("{}");
                created.setVersion(versions.getOrDefault(code, "1.0.0"));
                created.setInstallTime(now);
                created.setUpdateTime(now);
                created.setDeleted(0);
                installMapper.insert(created);
                log.info("[PluginReconcile] 补建本地插件安装登记: {}", code);
            } else if (!Integer.valueOf(1).equals(install.getInstalled())) {
                install.setInstalled(1);
                if (!StringUtils.hasText(install.getVersion())) {
                    install.setVersion(versions.getOrDefault(code, "1.0.0"));
                }
                install.setUpdateTime(now);
                installMapper.updateById(install);
                log.info("[PluginReconcile] 复位安装登记（在用信号存在）: {}", code);
            }
        }
    }

    /** 插件位菜单 → 插件编码 → 分类 ID（解析 /plugin/{code} 与 /plugin/{code}/子路径，含历史别名）。 */
    private Map<String, Integer> pluginMenuCateIds() {
        Map<String, Integer> mapping = new LinkedHashMap<>();
        try {
            for (Map<String, Object> row : nullSafe(addonCateMapper.selectAllPluginMenus())) {
                Object rawPath = row.get("paths");
                if (!(rawPath instanceof String path) || !path.startsWith(PLUGIN_MENU_PREFIX)) {
                    continue;
                }
                String rest = path.substring(PLUGIN_MENU_PREFIX.length());
                int slash = rest.indexOf('/');
                String segment = slash < 0 ? rest : rest.substring(0, slash);
                String code = ROUTE_ALIASES.getOrDefault(segment, segment);
                Object rawCateId = row.get("cate_id");
                int cateId = rawCateId instanceof Number number ? number.intValue() : parseInt(rawCateId);
                if (StringUtils.hasText(code)) {
                    mapping.put(code, cateId);
                }
            }
        } catch (Exception e) {
            log.warn("[PluginReconcile] 读取插件位菜单失败，安装判定退化为运行健康+既有记录: {}", e.getMessage());
        }
        return mapping;
    }

    private Set<String> healthyCodes(List<PluginCatalogItem> catalog) {
        Set<String> healthy = new HashSet<>();
        if (lifecycleManager == null) {
            return healthy;
        }
        for (PluginCatalogItem item : catalog) {
            try {
                if (lifecycleManager.isHealthy(item.getCode())) {
                    healthy.add(item.getCode());
                }
            } catch (RuntimeException ignored) {
                // 单个插件健康检查异常不影响其余对账
            }
        }
        return healthy;
    }

    private Map<String, MallPluginInstall> existingInstalls() {
        Map<String, MallPluginInstall> result = new LinkedHashMap<>();
        for (MallPluginInstall install : installMapper.selectList(null)) {
            if (install != null && StringUtils.hasText(install.getPluginCode())) {
                result.putIfAbsent(install.getPluginCode(), install);
            }
        }
        return result;
    }

    private Map<String, String> existingRegisteredServices() {
        Map<String, String> result = new LinkedHashMap<>();
        for (AddonPlugin plugin : nullSafePlugins(addonPluginMapper.selectList(null))) {
            if (plugin != null && StringUtils.hasText(plugin.getPluginCode())) {
                String code = plugin.getPluginCode();
                String service = StringUtils.hasText(plugin.getServiceCode())
                        ? plugin.getServiceCode() : HOST_SERVICE_CODE;
                String existing = result.get(code);
                if (existing == null || (HOST_SERVICE_CODE.equals(existing)
                        && !HOST_SERVICE_CODE.equals(service))) {
                    result.put(code, service);
                }
            }
        }
        return result;
    }

    private Set<String> softDeletedCodes() {
        try {
            List<String> codes = addonPluginMapper.selectSoftDeletedCodes();
            return codes == null ? Set.of() : new HashSet<>(codes);
        } catch (Exception e) {
            log.warn("[PluginReconcile] 读取软删目录行失败，按无保护处理: {}", e.getMessage());
            return Set.of();
        }
    }

    private List<Map<String, Object>> nullSafe(List<Map<String, Object>> rows) {
        return rows == null ? List.of() : rows;
    }

    private List<AddonPlugin> nullSafePlugins(List<AddonPlugin> rows) {
        return rows == null ? List.of() : rows;
    }

    private String toJsonArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(values.get(i).replace("\"", "\\\"")).append('"');
        }
        return sb.append(']').toString();
    }

    private int parseInt(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
