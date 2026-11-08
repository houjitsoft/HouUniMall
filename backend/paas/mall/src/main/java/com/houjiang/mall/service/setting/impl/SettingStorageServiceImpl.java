package com.houjiang.mall.service.setting.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.mall.service.setting.CommonConfigService;
import com.houjiang.mall.service.setting.SettingStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 商城后台 - 存储引擎配置 Service 实现。
 * 对应 like-admin SettingStorageServiceImpl，但去除文件系统 yml 依赖，
 * 全量基于 hou_merchant_config（type='storage'）持久化。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingStorageServiceImpl implements SettingStorageService {

    /** 配置 type 分组名 */
    private static final String TYPE = "storage";
    /** 默认引擎键名 */
    private static final String DEFAULT_KEY = "default";
    /** 缺省引擎 */
    private static final String DEFAULT_ENGINE = "local";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CommonConfigService commonConfigService;

    @Override
    public List<Map<String, Object>> list() {
        String currentEngine = commonConfigService.getOrDefault(TYPE, DEFAULT_KEY, DEFAULT_ENGINE);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildItem("本地存储", "local", "存储在本地服务器", currentEngine));
        list.add(buildItem("阿里云OSS", "aliyun", "存储在阿里云，请前往阿里云开通存储服务", currentEngine));
        list.add(buildItem("七牛云存储", "qiniu", "存储在七牛云，请前往七牛云开通存储服务", currentEngine));
        list.add(buildItem("腾讯云COS", "qcloud", "存储在腾讯云，请前往腾讯云开通存储服务", currentEngine));
        return list;
    }

    @Override
    public Map<String, Object> detail(String engine) {
        if (engine == null || engine.isEmpty()) {
            throw new IllegalArgumentException("engine 不能为空");
        }
        String currentEngine = commonConfigService.getOrDefault(TYPE, DEFAULT_KEY, DEFAULT_ENGINE);
        Map<String, String> config = commonConfigService.getJsonMap(TYPE, engine);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", config.getOrDefault("name", ""));
        map.put("engine", engine);
        map.put("status", currentEngine.equals(engine) ? 1 : 0);
        if (!"local".equals(engine)) {
            map.put("bucket", config.getOrDefault("bucket", ""));
            map.put("secret_key", config.getOrDefault("secret_key", ""));
            map.put("access_key", config.getOrDefault("access_key", ""));
            map.put("domain", config.getOrDefault("domain", ""));
            if ("qcloud".equals(engine)) {
                map.put("region", config.getOrDefault("region", ""));
            }
            if ("aliyun".equals(engine)) {
                map.put("endpoint", config.getOrDefault("endpoint", ""));
            }
        }
        return map;
    }

    @Override
    public void setup(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        String engine = params.get("engine");
        String statusStr = params.get("status");
        if (engine == null || engine.isEmpty()) {
            throw new IllegalArgumentException("engine 参数缺失");
        }
        if (statusStr == null || statusStr.isEmpty()) {
            throw new IllegalArgumentException("status 参数缺失");
        }

        Map<String, String> map = new LinkedHashMap<>();
        if ("local".equals(engine)) {
            map.put("name", "本地存储");
        } else {
            map.put("bucket", params.getOrDefault("bucket", ""));
            map.put("secret_key", params.getOrDefault("secret_key", ""));
            map.put("access_key", params.getOrDefault("access_key", ""));
            map.put("domain", params.getOrDefault("domain", ""));
            switch (engine) {
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
                default:
                    map.put("name", engine);
                    break;
            }
        }

        try {
            commonConfigService.set(TYPE, engine, OBJECT_MAPPER.writeValueAsString(map));
        } catch (Exception e) {
            throw new IllegalStateException("存储配置序列化失败: " + e.getMessage(), e);
        }

        int status;
        try {
            status = Integer.parseInt(statusStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("status 必须是整数");
        }
        String currentDefault = commonConfigService.getOrDefault(TYPE, DEFAULT_KEY, DEFAULT_ENGINE);
        if (status == 1) {
            commonConfigService.set(TYPE, DEFAULT_KEY, engine);
        } else if (currentDefault.equals(engine)) {
            commonConfigService.set(TYPE, DEFAULT_KEY, "");
        }
    }

    @Override
    public void change(String alias, Integer status) {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("alias 参数缺失");
        }
        if (status == null) {
            throw new IllegalArgumentException("status 参数缺失");
        }
        String currentDefault = commonConfigService.getOrDefault(TYPE, DEFAULT_KEY, DEFAULT_ENGINE);
        if (currentDefault.equals(alias) && status == 0) {
            commonConfigService.set(TYPE, DEFAULT_KEY, "");
        } else {
            commonConfigService.set(TYPE, DEFAULT_KEY, alias);
        }
    }

    private Map<String, Object> buildItem(String name, String engine, String desc, String currentEngine) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("engine", engine);
        item.put("path", desc);
        item.put("status", engine.equals(currentEngine) ? 1 : 0);
        return item;
    }
}
