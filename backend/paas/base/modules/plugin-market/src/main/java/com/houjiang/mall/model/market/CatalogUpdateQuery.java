package com.houjiang.mall.model.market;

import lombok.Data;

/**
 * 目录更新判定入参项：本站点「已装插件 + 当前版本」。
 *
 * @author Amos
 * @since M5
 */
@Data
public class CatalogUpdateQuery {

    /**
     * 插件编码（必填）。
     */
    private String code;

    /**
     * 本站点当前已装版本；为空时服务端按安装态回退取值。
     */
    private String version;

    /**
     * 构造空查询项（供 JSON 反序列化使用）。
     */
    public CatalogUpdateQuery() {
    }

    /**
     * 构造查询项。
     *
     * @param code    插件编码
     * @param version 当前版本
     */
    public CatalogUpdateQuery(String code, String version) {
        this.code = code;
        this.version = version;
    }
}
