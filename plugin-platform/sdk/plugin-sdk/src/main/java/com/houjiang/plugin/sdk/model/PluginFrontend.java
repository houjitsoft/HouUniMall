package com.houjiang.plugin.sdk.model;

import java.io.Serializable;

/**
 * 插件前端资源声明。
 *
 * <p>对应 <code>plugin.yaml</code> 的 <code>frontend</code> 节点：
 * <pre>{@code
 * frontend:
 *   entry: dist/index.mjs
 *   style: dist/index.css
 * }</pre>
 *
 * <p>产物为 ESM，默认导出 <code>{ routes, menus, setup(ctx), teardown() }</code>；
 * 由宿主加载器动态 import，卸载时移除路由并调用 <code>teardown()</code>。
 *
 * @author HouCloud
 */
public class PluginFrontend implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 前端入口，相对插件资源目录，如 {@code dist/index.mjs} */
    private String entry;

    /** 样式文件，如 {@code dist/index.css} */
    private String style;

    /** 可选语言包入口，运行时由宿主注册到插件 i18n 命名空间。 */
    private String locales;

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getLocales() {
        return locales;
    }

    public void setLocales(String locales) {
        this.locales = locales;
    }

    public boolean hasEntry() {
        return entry != null && !entry.isBlank();
    }
}
