package com.houjiang.mall.service.setting;

import java.util.Map;

/**
 * 商城后台 - 热门搜索 Service。
 * 对应 like-admin SettingSearchService。
 *
 * @author Amos
 */
public interface SettingSearchService {

    /**
     * 热门搜索详情：返回 {status, data:[{name, sort, id}]}。
     */
    Map<String, Object> getConfig();

    /**
     * 热门搜索保存：保存 status + 重置关键词列表（先全量删除再批量插入）。
     */
    void setConfig(Map<String, Object> params);
}
