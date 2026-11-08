package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.service.ISettingStorageService;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.YmlUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 存储配置服务实现类
 */
@Service
public class SettingStorageServiceImpl implements ISettingStorageService {

    /**
     * 存储列表
     *
     * @author Amos
     * @return List<Map<String, Object>>
     */
    @Override
    public List<Map<String, Object>> list() {
        String engine = ConfigUtils.get("storage", "default", "local");
        List<Map<String, Object>> list = new LinkedList<>();

        Map<String, Object> local = new LinkedHashMap<>();
        local.put("name", "本地存储");
        local.put("engine", "local");
        local.put("path", "存储在本地服务器");
        local.put("status", engine.equals("local") ? 1 : 0);
        list.add(local);

        Map<String, Object> qiniu = new LinkedHashMap<>();
        qiniu.put("name", "七牛云存储");
        qiniu.put("engine", "qiniu");
        qiniu.put("path", "存储在七牛云，请前往七牛云开通存储服务");
        qiniu.put("status", engine.equals("qiniu") ? 1 : 0);
        list.add(qiniu);

        Map<String, Object> aliyun = new LinkedHashMap<>();
        aliyun.put("name", "阿里云OSS");
        aliyun.put("engine", "aliyun");
        aliyun.put("path", "存储在阿里云，请前往阿里云开通存储服务");
        aliyun.put("status", engine.equals("aliyun") ? 1 : 0);
        list.add(aliyun);

        Map<String, Object> qcloud = new LinkedHashMap<>();
        qcloud.put("name", "腾讯云COS");
        qcloud.put("engine", "qcloud");
        qcloud.put("path", "存储在腾讯云，请前往腾讯云开通存储服务");
        qcloud.put("status", engine.equals("qcloud") ? 1 : 0);
        list.add(qcloud);

        return list;
    }

    /**
     * 存储详情
     *
     * @author Amos
     * @param engine 存储别名
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> detail(String engine) {
        String env = YmlUtils.get("merchant.production");
        boolean envStatus = StringUtils.isNotNull(env) && env.equals("true");

        String defaultEngine = ConfigUtils.get("storage", "default", "local");
        Map<String, String> config = ConfigUtils.getMap("storage", engine);
        config = StringUtils.isNotNull(config) ? config : Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", config.getOrDefault("name", ""));
        map.put("engine", engine);
        map.put("status", defaultEngine.equals(engine) ? 1 : 0);
        if (!engine.equals("local")) {
            map.put("bucket", config.getOrDefault("bucket", ""));
            map.put("secret_key", envStatus ? "******" : config.getOrDefault("secret_key", ""));
            map.put("access_key", envStatus ? "******" : config.getOrDefault("access_key", ""));
            map.put("domain", config.getOrDefault("domain", ""));
            if (engine.equals("qcloud")) {
                map.put("region", config.getOrDefault("region", ""));
            }
            if (engine.equals("aliyun")) {
                map.put("endpoint", config.getOrDefault("endpoint", ""));
            }
        }

        return map;
    }

    /**
     * 存储编辑
     *
     * @author Amos
     * @param params 参数
     */
    @Override
    public void setup(Map<String, String> params) {
        Assert.notNull(params.get("engine"), "engine参数缺失");
        Assert.notNull(params.get("status"), "status参数缺失");
        Map<String, String> map = new LinkedHashMap<>();

        map.put("name", "本地存储");
        if (!params.get("engine").equals("local")) {
            map.put("bucket", params.getOrDefault("bucket", ""));
            map.put("secret_key", params.getOrDefault("secret_key", ""));
            map.put("access_key", params.getOrDefault("access_key", ""));
            map.put("domain", params.getOrDefault("domain", ""));
            switch (params.get("engine")) {
                case "qcloud":
                    map.put("name", "腾讯云存储");
                    map.put("region", params.getOrDefault("region", ""));
                    break;
                case "qiniu":
                    map.put("name", "七牛云存储");
                    break;
                case "aliyun":
                    map.put("name", "阿里云存储");
                    map.put("endpoint", params.getOrDefault("endpoint", ""));
                    break;
            }
        }

        ConfigUtils.set("storage", params.get("engine"), JSON.toJSONString(map));

        String engine = ConfigUtils.get("storage", "default", "local");
        if (Integer.parseInt(params.get("status")) == 1) {
            ConfigUtils.set("storage", "default", params.get("engine"));
        } else if (engine.equals(params.get("engine")) && Integer.parseInt(params.get("status")) == 0) {
            ConfigUtils.set("storage", "default", "");
        }
    }

    /**
     * 引擎切换
     *
     * @author Amos
     * @param alias 引擎别名
     * @param status 状态
     */
    @Override
    public void change(String alias, Integer status) {
        String engine = ConfigUtils.get("storage", "default", "local");
        if (engine.equals(alias) && status == 0) {
            ConfigUtils.set("storage", "default", "");
        } else {
            ConfigUtils.set("storage", "default", alias);
        }
    }

}
