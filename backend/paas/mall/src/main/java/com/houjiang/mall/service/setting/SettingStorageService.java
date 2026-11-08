package com.houjiang.mall.service.setting;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 存储引擎配置 Service。
 * 对应 like-admin SettingStorageService（hou_merchant_config 中 type='storage' 分组）。
 *
 * @author Amos
 */
public interface SettingStorageService {

    /**
     * 存储引擎列表（硬编码 4 类：local/aliyun/qiniu/qcloud），
     * 状态由 hou_merchant_config(type='storage', name='default') 决定。
     */
    List<Map<String, Object>> list();

    /**
     * 取某引擎的配置详情。
     */
    Map<String, Object> detail(String engine);

    /**
     * 编辑/保存某引擎的配置；status=1 时同时切为默认引擎。
     */
    void setup(Map<String, String> params);

    /**
     * 引擎切换：alias=别名, status=1 切为默认；如果当前默认 alias 且 status=0 则清空默认。
     */
    void change(String alias, Integer status);
}
