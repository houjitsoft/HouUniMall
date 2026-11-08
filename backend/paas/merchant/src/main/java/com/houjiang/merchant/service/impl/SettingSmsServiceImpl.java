package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.service.ISettingSmsService;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.YmlUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 短信配置服务实现类
 */
@Service
public class SettingSmsServiceImpl implements ISettingSmsService {

    /**
     * 短信引擎列表
     *
     * @author Amos
     * @return List<Map<String, Object>>
     */
    @Override
    public List<Map<String, Object>> getConfig() {
        String engine = ConfigUtils.get("sms", "engine", "ali");
        List<Map<String, Object>> list = new LinkedList<>();

        Map<String, Object> aliyun = new LinkedHashMap<>();
        aliyun.put("name", "阿里云短信");
        aliyun.put("type", "ali");
        aliyun.put("status", engine.equalsIgnoreCase("ali") ? 1 : 0);
        list.add(aliyun);

        Map<String, Object> tencent = new LinkedHashMap<>();
        tencent.put("name", "腾讯云短信");
        tencent.put("type", "tencent");
        tencent.put("status", engine.equalsIgnoreCase("tencent") ? 1 : 0);
        list.add(tencent);
        return list;
    }

    /**
     * 短信引擎详情
     *
     * @author Amos
     * @param alias 别名
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> detail(String type) {
        String env = YmlUtils.get("merchant.production");
        boolean envStatus = StringUtils.isNotNull(env) && env.equals("true");

        String engine = ConfigUtils.get("sms", "engine", "ali");
        Map<String, String> config = ConfigUtils.getMap("sms", type);
        config = StringUtils.isNotNull(config) ? config : Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", config.getOrDefault("name", ""));
        map.put("status", engine.equalsIgnoreCase(type) ? 1 : 0);
        map.put("type", type);
        map.put("sign", config.getOrDefault("sign", ""));

        switch (type) {
            case "ali":
                map.put("app_key", envStatus ? "******" : config.getOrDefault("app_key", ""));
                map.put("secret_key", envStatus ? "******" : config.getOrDefault("secret_key", ""));
                break;
            case "tencent":
                map.put("app_id", envStatus ? "******" : config.getOrDefault("app_id", ""));
                map.put("secret_id", envStatus ? "******" : config.getOrDefault("secret_id", ""));
                map.put("secret_key", envStatus ? "******" : config.getOrDefault("secret_key", ""));
                break;
            case "huawei":
                break;
        }

        return map;
    }

    /**
     * 短信引擎保存
     *
     * @author Amos
     * @param params 参数
     */
    @Override
    public void setConfig(JSONObject params) {
        String type = params.getString("type");

        ConfigUtils.set("sms", type, params.toJSONString());

        String engine = ConfigUtils.get("sms", "engine", "");
        if (Integer.parseInt(params.getString("status")) == 1) {
            ConfigUtils.set("sms", "engine", type);
        } else if (engine.equals(type) && Integer.parseInt(params.getString("status")) == 0) {
            ConfigUtils.set("sms", "engine", "");
        }
    }

}
