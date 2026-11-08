package com.houjiang.merchant.service;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.validate.config.ConfigSetServiceValidate;

import java.util.Map;

/**
 * 主页服务接口类
 * @author Amos
 */
public interface IConfigService {
    /**
     * 公共配置
     */
    JSONObject getConfigData();

    /**
     * 获取配置
     * @param shopId
     * @return
     */
    JSONObject settlementGet(Integer shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    JSONObject getMallOrderSettlementG1et();

    /**
     * 设置客服
     * @param serviceValidate
     */
    void setService(ConfigSetServiceValidate serviceValidate);

    /**
     * 获取客服
     * @param shopId
     * @return
     */
    JSONObject getService(Integer shopId);

    /**
     * 获取店铺配置
     * @param shopId
     * @param type
     * @param name
     * @param defaultValue
     * @return
     */
    JSONObject getShopConfigName(Integer shopId, String type, String name);
}
