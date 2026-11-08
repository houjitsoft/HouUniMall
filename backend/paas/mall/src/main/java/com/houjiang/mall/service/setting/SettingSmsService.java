package com.houjiang.mall.service.setting;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 短信引擎配置 Service。
 * 对应 like-admin SettingSmsService（hou_merchant_config 中 type='sms' 分组）。
 *
 * @author Amos
 */
public interface SettingSmsService {

    /**
     * 短信引擎列表（硬编码 2 类：ali/tencent），
     * 状态由 hou_merchant_config(type='sms', name='engine') 决定。
     */
    List<Map<String, Object>> list();

    /**
     * 取某引擎的配置详情。
     */
    Map<String, Object> detail(String type);

    /**
     * 保存某引擎的配置；status=1 时同时切为默认引擎；status=0 且当前默认是该引擎则清空默认。
     */
    void edit(Map<String, Object> params);
}
