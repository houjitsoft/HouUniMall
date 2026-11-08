package com.houjiang.mall.model.plugin;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 插件目录项，表示系统内置支持的扩展应用。
 *
 * <p>{@code cateId} 为插件的分类外键（hou_addon_plugin.cate_id → hou_addon_cate.id），
 * 由自 2026-09-18 的迁移脚本补齐；静态 CATALOG 未声明时不传，按「未分类」处理。
 *
 * @author Amos
 */
@Data
@NoArgsConstructor
public class PluginCatalogItem {

    private String code;

    /** 唯一业务归属服务；历史静态项为空时由既有目录行或兼容宿主补足。 */
    private String hostService;

    private String name;

    private String category;

    /** 分类外键；null 或 0 表示未分类（静态 CATALOG 项不传）。 */
    private Integer cateId;

    private String version;

    private String description;

    private List<String> terminals;

    private List<String> dependencies;

    private String entryPath;

    private String configSchema;

    /** 历史签名：不带分类外键，按未分类处理。 */
    public PluginCatalogItem(String code, String name, String category, String version,
                             String description, List<String> terminals, List<String> dependencies,
                             String entryPath, String configSchema) {
        this(code, name, category, null, version, description, terminals, dependencies,
                entryPath, configSchema);
    }

    public PluginCatalogItem(String code, String name, String category, Integer cateId, String version,
                             String description, List<String> terminals, List<String> dependencies,
                             String entryPath, String configSchema) {
        this(code, null, name, category, cateId, version, description, terminals, dependencies,
                entryPath, configSchema);
    }

    public PluginCatalogItem(String code, String hostService, String name, String category, Integer cateId,
                             String version, String description, List<String> terminals,
                             List<String> dependencies, String entryPath, String configSchema) {
        this.code = code;
        this.hostService = hostService;
        this.name = name;
        this.category = category;
        this.cateId = cateId;
        this.version = version;
        this.description = description;
        this.terminals = terminals;
        this.dependencies = dependencies;
        this.entryPath = entryPath;
        this.configSchema = configSchema;
    }
}
