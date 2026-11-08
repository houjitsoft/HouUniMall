package com.houjiang.mall.service.setting.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.mall.service.setting.CommonConfigService;
import com.houjiang.mall.service.setting.SettingSmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 商城后台 - 短信引擎配置 Service 实现。
 * 对应 like-admin SettingSmsServiceImpl，全量基于 hou_merchant_config（type='sms'）持久化。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingSmsServiceImpl implements SettingSmsService {

    /** 配置 type 分组名 */
    private static final String TYPE = "sms";
    /** 默认引擎键名（注意与 storage 的 default 不同，sms 用 engine） */
    private static final String ENGINE_KEY = "engine";
    /** 缺省引擎 */
    private static final String DEFAULT_ENGINE = "ali";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CommonConfigService commonConfigService;

    @Override
    public List<Map<String, Object>> list() {
        String engine = commonConfigService.getOrDefault(TYPE, ENGINE_KEY, DEFAULT_ENGINE);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildItem("阿里云短信", "ali", engine));
        list.add(buildItem("腾讯云短信", "tencent", engine));
        return list;
    }

    @Override
    public Map<String, Object> detail(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("type 不能为空");
        }
        String engine = commonConfigService.getOrDefault(TYPE, ENGINE_KEY, DEFAULT_ENGINE);
        Map<String, String> config = commonConfigService.getJsonMap(TYPE, type);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", config.getOrDefault("name", ""));
        map.put("type", type);
        map.put("status", engine.equalsIgnoreCase(type) ? 1 : 0);
        map.put("sign", config.getOrDefault("sign", ""));

        switch (type) {
            case "ali":
                map.put("app_key", config.getOrDefault("app_key", ""));
                map.put("secret_key", config.getOrDefault("secret_key", ""));
                break;
            case "tencent":
                map.put("app_id", config.getOrDefault("app_id", ""));
                map.put("secret_id", config.getOrDefault("secret_id", ""));
                map.put("secret_key", config.getOrDefault("secret_key", ""));
                break;
            default:
                break;
        }
        return map;
    }

    @Override
    public void edit(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Object typeObj = params.get("type");
        Object statusObj = params.get("status");
        if (typeObj == null || typeObj.toString().isEmpty()) {
            throw new IllegalArgumentException("type 参数缺失");
        }
        if (statusObj == null || statusObj.toString().isEmpty()) {
            throw new IllegalArgumentException("status 参数缺失");
        }
        String type = typeObj.toString();
        int status;
        try {
            status = Integer.parseInt(statusObj.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("status 必须是整数");
        }

        try {
            commonConfigService.set(TYPE, type, OBJECT_MAPPER.writeValueAsString(params));
        } catch (Exception e) {
            throw new IllegalStateException("短信配置序列化失败: " + e.getMessage(), e);
        }

        String currentEngine = commonConfigService.getOrDefault(TYPE, ENGINE_KEY, "");
        if (status == 1) {
            commonConfigService.set(TYPE, ENGINE_KEY, type);
        } else if (currentEngine.equals(type)) {
            commonConfigService.set(TYPE, ENGINE_KEY, "");
        }
    }

    private Map<String, Object> buildItem(String name, String type, String engine) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("type", type);
        item.put("status", type.equalsIgnoreCase(engine) ? 1 : 0);
        return item;
    }
}
