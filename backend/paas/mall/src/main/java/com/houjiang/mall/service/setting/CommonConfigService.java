package com.houjiang.mall.service.setting;

import java.util.Map;

/**
 * 商城后台 - 通用键值配置 Service（基于 hou_merchant_config 表）。
 * 对应 like-common ConfigUtils 的核心能力，供 storage/website/login/order/user 等
 * 设置类模块（B1-5/8/10/11/12）共用。
 *
 * @author Amos
 */
public interface CommonConfigService {

    /**
     * 取分组内单值；若不存在返回 null。
     */
    String get(String type, String name);

    /**
     * 取分组内单值；若不存在返回 defaults。
     */
    String getOrDefault(String type, String name, String defaults);

    /**
     * 取分组下所有键值（type 维度），key=name。
     */
    Map<String, String> getAll(String type);

    /**
     * 把 value 视为 JSON 字符串并解析为 Map<String,String>，
     * 兼容历史脏数据 "[]" → "{}"；不存在或解析失败返回空 Map。
     */
    Map<String, String> getJsonMap(String type, String name);

    /**
     * 写入或更新单个键值（不存在则新增）。
     */
    void set(String type, String name, String value);
}
