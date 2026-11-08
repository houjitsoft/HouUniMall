package com.houjiang.mall.service.plugin;

import com.alibaba.fastjson.JSON;
import com.houjiang.mall.entity.plugin.AddonPlugin;
import com.houjiang.mall.mapper.plugin.AddonPluginMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * plugin.yaml → hou_addon_plugin 单向同步（P0-3）。
 *
 * <p>plugin.yaml 是插件元数据的唯一事实来源；本服务把包内声明同步到站点目录表，消灭
 * 「库表元数据与包内 plugin.yaml 两套、靠人工保持一致」的 R1 风险。
 *
 * <p>边界（诚实说明）：
 * <ul>
 *   <li>同步是<b>单向</b>的（yaml → 库），库表不会反向写回 plugin.yaml；</li>
 *   <li>只同步 hou_addon_plugin 既有列（code/name/version/category/description/terminals/
 *       dependencies/entryPath/configSchema）；license/owner/runtime 属市场与运行期语义，
 *       站点目录表未建模这些列，故不落库（授权以 hou_market_plugin_license 为准）；</li>
 *   <li>本服务只负责「目录元数据同步」，插件装载由「安装状态驱动」（PluginRuntimeBootstrap），
 *       两者职责分离；引擎不触碰站点库，同步一律在宿主 mall 侧完成。</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@Service
public class AddonPluginSyncService {

    /** 缺省配置面板 Schema，与静态目录 CATALOG 形状一致 */
    private static final String EMPTY_JSON_OBJECT = "{}";
    /** 缺省空数组 JSON */
    private static final String EMPTY_JSON_ARRAY = "[]";
    /** 前端产物统一服务前缀：frontend.entry 形如 /plugin-assets/{code}/{version}/{bare} */
    private static final String PLUGIN_ASSET_PREFIX = "/plugin-assets/";

    private final AddonPluginMapper addonPluginMapper;

    public AddonPluginSyncService(AddonPluginMapper addonPluginMapper) {
        this.addonPluginMapper = addonPluginMapper;
    }

    /**
     * 依据清单中的 {@code hostService} 幂等同步站点目录。
     *
     * <p>{@code hostService} 是架构规定的唯一业务归属。发布、安装和升级链路都应使用本入口，
     * 避免调用方继续把历史统一宿主 {@code mall} 写进正式目录。
     *
     * @param pluginYaml 包内 plugin.yaml 原文
     * @return 同步成功返回 true；清单为空或缺少 code/hostService 时返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean syncFromPluginYaml(String pluginYaml) {
        if (addonPluginMapper == null || !StringUtils.hasText(pluginYaml)) {
            return false;
        }
        Map<String, Object> declaration = parseDeclaration(pluginYaml);
        String code = text(declaration.get("code"));
        String hostService = text(declaration.get("hostService"));
        if (!StringUtils.hasText(code) || !StringUtils.hasText(hostService)) {
            log.warn("plugin.yaml 缺少 code 或 hostService，跳过站点目录同步: code={}, hostService={}",
                    code, hostService);
            return false;
        }
        return syncFromPluginYaml(hostService, pluginYaml);
    }

    /**
     * 依据 plugin.yaml 幂等同步一条站点目录行。
     *
     * @param serviceCode 归属服务（兼容历史调用；新链路应由清单中的 hostService 决定）
     * @param pluginYaml  包内 plugin.yaml 原文（市场版本行副本或制品内声明）
     * @return 同步成功返回 true；yaml 为空或缺 code 时跳过并返回 false（不阻断安装）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean syncFromPluginYaml(String serviceCode, String pluginYaml) {
        if (addonPluginMapper == null || !StringUtils.hasText(serviceCode) || !StringUtils.hasText(pluginYaml)) {
            return false;
        }
        Map<String, Object> declaration = parseDeclaration(pluginYaml);
        String code = text(declaration.get("code"));
        if (!StringUtils.hasText(code)) {
            log.warn("plugin.yaml 缺少 code，跳过站点目录同步: serviceCode={}", serviceCode);
            return false;
        }
        AddonPlugin row = toAddonPlugin(serviceCode, code, declaration);
        addonPluginMapper.softDeleteOtherServiceRows(serviceCode, code);
        addonPluginMapper.upsertFromManifest(row);
        log.info("plugin.yaml → hou_addon_plugin 同步完成: {}#{}@{}", serviceCode, code, row.getVersion());
        return true;
    }

    /**
     * 显式移出站点目录（deleted=1），保留数据可追溯；重新发布可由 upsert 复活。
     * 插件卸载只改变安装态，不得调用本方法。
     *
     * @param serviceCode 归属服务（如 mall）
     * @param pluginCode  插件唯一编码
     * @return 命中并软删返回 true
     */
    public boolean softDeleteByCode(String serviceCode, String pluginCode) {
        if (addonPluginMapper == null || !StringUtils.hasText(serviceCode) || !StringUtils.hasText(pluginCode)) {
            return false;
        }
        return addonPluginMapper.softDeleteByCode(serviceCode, pluginCode) > 0;
    }

    /**
     * plugin.yaml 声明 → hou_addon_plugin 行（纯映射，无副作用，便于单测）。
     */
    AddonPlugin toAddonPlugin(String serviceCode, String code, Map<String, Object> declaration) {
        AddonPlugin row = new AddonPlugin();
        row.setServiceCode(serviceCode);
        row.setPluginCode(code);
        String name = text(declaration.get("name"));
        row.setName(StringUtils.hasText(name) ? name : code);
        // hou_addon_plugin.category NOT NULL：清单缺分类（Yaml 空值→null）时兜底，
        // 避免发布/安装链路在目录同步处被数据库约束炸断（2026-10-27 装机实测）。
        String category = text(declaration.get("category"));
        row.setCategory(StringUtils.hasText(category) ? category : "未分类");
        row.setVersion(text(declaration.get("version")));
        row.setDescription(text(declaration.get("description")));
        row.setTerminalsJson(toStringArrayJson(declaration.get("terminals")));
        row.setDependenciesJson(toDependencyArrayJson(declaration.get("dependencies")));
        // hou_addon_plugin.entry_path 同样 NOT NULL：清单缺 entry/frontend.entry 时
        // 依次回落 首个菜单 path → 默认 /plugin/{code}，别让发布链路倒在约束上
        // （2026-10-27 装机实测：无前端的最小制品缺 entry 直接 500）。
        String entryPath = resolveEntryPath(declaration);
        if (!StringUtils.hasText(entryPath)) {
            entryPath = firstMenuPath(declaration);
        }
        if (!StringUtils.hasText(entryPath)) {
            entryPath = "/plugin/" + code;
        }
        row.setEntryPath(entryPath);
        row.setConfigSchema(toConfigSchemaJson(declaration.get("configSchema")));
        row.setStatus(1);
        row.setSort(0);
        row.setDeleted(0);
        return row;
    }

    /** 解析 plugin.yaml；容错历史市场行可能是 JSON 片段。 */
    private Map<String, Object> parseDeclaration(String yaml) {
        try {
            Object value = new Yaml().load(yaml);
            if (value instanceof Map<?, ?> raw) {
                Map<String, Object> result = new LinkedHashMap<>();
                raw.forEach((key, item) -> result.put(String.valueOf(key), item));
                return result;
            }
        } catch (RuntimeException ignored) {
            try {
                Map<String, Object> fromJson = JSON.parseObject(yaml);
                if (fromJson != null) {
                    return fromJson;
                }
            } catch (RuntimeException ignoredJson) {
                // 无效声明按空处理，交由调用方跳过同步
            }
        }
        return Map.of();
    }

    /** terminals: [admin, ...] → JSON 字符串数组 */
    private String toStringArrayJson(Object value) {
        if (!(value instanceof Iterable<?> iterable)) {
            return EMPTY_JSON_ARRAY;
        }
        List<String> list = new ArrayList<>();
        for (Object item : iterable) {
            String text = text(item);
            if (StringUtils.hasText(text)) {
                list.add(text);
            }
        }
        return JSON.toJSONString(list);
    }

    /**
     * dependencies 可能是字符串数组或对象数组（{code, version}）；统一压成编码字符串数组，
     * 与 PluginCatalogItem.dependencies（List&lt;String&gt;）及静态目录 JSON 形状一致。
     */
    private String toDependencyArrayJson(Object value) {
        if (!(value instanceof Iterable<?> iterable)) {
            return EMPTY_JSON_ARRAY;
        }
        List<String> list = new ArrayList<>();
        for (Object item : iterable) {
            String code = dependencyCode(item);
            if (StringUtils.hasText(code)) {
                list.add(code);
            }
        }
        return JSON.toJSONString(list);
    }

    private String dependencyCode(Object item) {
        if (item instanceof Map<?, ?> map) {
            Object code = map.get("code");
            if (code == null) {
                code = map.get("name");
            }
            return text(code);
        }
        return text(item);
    }

    /**
     * 归一化前端入口为「裸入口」（如 index.mjs）。
     *
     * <p>plugin.yaml 的 frontend.entry 常写成完整服务路径 /plugin-assets/{code}/{version}/{bare}，
     * 而站点目录 entry_path 与静态 CATALOG 都存裸入口（resolveFrontendUrl / PluginAssetResolver
     * 会再拼版本化前缀）。此处剥掉 /plugin-assets/{code}/{version}/ 前缀，避免前缀重复；
     * 顶层 entryPath 或内置页面路径（如 /points/index）原样保留。
     */
    private String resolveEntryPath(Map<String, Object> declaration) {
        String direct = text(declaration.get("entryPath"));
        if (StringUtils.hasText(direct)) {
            return direct;
        }
        Object frontend = declaration.get("frontend");
        String entry = frontend instanceof Map<?, ?> map ? text(map.get("entry")) : null;
        if (!StringUtils.hasText(entry)) {
            return null;
        }
        String normalized = entry.trim();
        if (!normalized.startsWith(PLUGIN_ASSET_PREFIX)) {
            return normalized;
        }
        String rest = normalized.substring(PLUGIN_ASSET_PREFIX.length());
        int codeSlash = rest.indexOf('/');
        if (codeSlash < 0) {
            return rest;
        }
        rest = rest.substring(codeSlash + 1);
        int versionSlash = rest.indexOf('/');
        return versionSlash < 0 ? rest : rest.substring(versionSlash + 1);
    }

    /**
     * 清单 menus[].path 的第一个非空值——entry 缺失时的回落来源。
     */
    private String firstMenuPath(Map<String, Object> declaration) {
        Object menus = declaration.get("menus");
        if (menus instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                String path = item instanceof Map<?, ?> map ? text(map.get("path")) : null;
                if (StringUtils.hasText(path)) {
                    return path.trim();
                }
            }
        }
        return null;
    }

    /** configSchema：对象序列化为 JSON 字符串；已是字符串则原样；缺省 {}。 */
    private String toConfigSchemaJson(Object value) {
        if (value == null) {
            return EMPTY_JSON_OBJECT;
        }
        if (value instanceof String str) {
            return StringUtils.hasText(str) ? str.trim() : EMPTY_JSON_OBJECT;
        }
        return JSON.toJSONString(value);
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String str = String.valueOf(value).trim();
        return str.isEmpty() ? null : str;
    }
}
