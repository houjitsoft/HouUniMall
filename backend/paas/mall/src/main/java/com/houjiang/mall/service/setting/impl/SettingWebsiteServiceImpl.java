package com.houjiang.mall.service.setting.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.mall.service.setting.CommonConfigService;
import com.houjiang.mall.service.setting.SettingWebsiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 网站设置 Service 实现。对应 like-admin 三个 Service 合并版。
 *
 * 配置分布（hou_merchant_config）：
 *   - website.* : 18 个字段（基础站点配置 + 6 张图片 + certification JSON 数组）
 *   - copyright.config : JSON 数组 [{key,value}]
 *   - agreement.{service_title,service_content,privacy_title,privacy_content}
 *   - siteStatistics.clarity_code
 *
 * 字段命名：使用下划线键保持与 like-admin 序列化输出一致。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingWebsiteServiceImpl implements SettingWebsiteService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String TYPE_WEBSITE = "website";
    private static final String TYPE_COPYRIGHT = "copyright";
    private static final String TYPE_AGREEMENT = "agreement";
    private static final String TYPE_STATISTICS = "siteStatistics";

    /** website 字段清单（按 like-admin 顺序） */
    private static final String[] WEBSITE_KEYS = {
            "name", "admin_login_name", "admin_logo", "web_favicon", "admin_login_logo",
            "mobile_logo", "document_status", "contact", "mobile", "business_license",
            "shop_login_title", "shop_admin_logo", "shop_browser_icon", "shop_login_logo",
            "shop_logo", "shop_background"
    };

    private final CommonConfigService commonConfigService;

    /** ============================== website ============================== */

    @Override
    public Map<String, Object> getWebsite() {
        Map<String, String> all = commonConfigService.getAll(TYPE_WEBSITE);
        Map<String, Object> vo = new LinkedHashMap<>();
        for (String key : WEBSITE_KEYS) {
            vo.put(key, all.getOrDefault(key, ""));
        }
        // document_status 数值化
        Object docStatus = vo.get("document_status");
        vo.put("document_status", parseInt(docStatus == null ? "1" : docStatus.toString(), 1));
        // certification JSON 数组
        vo.put("certification", parseJsonArray(all.getOrDefault("certification", "[]")));
        return vo;
    }

    @Override
    public void setWebsite(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        for (String key : WEBSITE_KEYS) {
            String camel = toCamel(key);
            String val = readString(params, key, camel, "");
            commonConfigService.set(TYPE_WEBSITE, key, val);
        }
        Object certificationObj = params.get("certification");
        commonConfigService.set(TYPE_WEBSITE, "certification", toJson(certificationObj));
    }

    /** ============================== copyright ============================== */

    @Override
    public List<Map<String, String>> getCopyright() {
        String json = commonConfigService.getOrDefault(TYPE_COPYRIGHT, "config", "[]");
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<Map<String, Object>> raw = OBJECT_MAPPER.readValue(
                    json, new TypeReference<List<Map<String, Object>>>() {});
            List<Map<String, String>> list = new ArrayList<>();
            for (Map<String, Object> item : raw) {
                if (item == null) continue;
                Map<String, String> vo = new LinkedHashMap<>();
                vo.put("key", String.valueOf(item.getOrDefault("key", "")));
                vo.put("value", String.valueOf(item.getOrDefault("value", "")));
                list.add(vo);
            }
            return list;
        } catch (Exception e) {
            log.warn("[SettingWebsite] 备案配置解析失败，原值={}, err={}", json, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void setCopyright(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Object cfg = params.get("config");
        commonConfigService.set(TYPE_COPYRIGHT, "config", toJson(cfg));
    }

    /** ============================== agreement ============================== */

    @Override
    public Map<String, Object> getAgreement() {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("service_title", commonConfigService.getOrDefault(TYPE_AGREEMENT, "service_title", ""));
        vo.put("service_content", commonConfigService.getOrDefault(TYPE_AGREEMENT, "service_content", ""));
        vo.put("privacy_title", commonConfigService.getOrDefault(TYPE_AGREEMENT, "privacy_title", ""));
        vo.put("privacy_content", commonConfigService.getOrDefault(TYPE_AGREEMENT, "privacy_content", ""));
        return vo;
    }

    @Override
    public void setAgreement(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        commonConfigService.set(TYPE_AGREEMENT, "service_title",
                readString(params, "service_title", "serviceTitle", ""));
        commonConfigService.set(TYPE_AGREEMENT, "service_content",
                readString(params, "service_content", "serviceContent", ""));
        commonConfigService.set(TYPE_AGREEMENT, "privacy_title",
                readString(params, "privacy_title", "privacyTitle", ""));
        commonConfigService.set(TYPE_AGREEMENT, "privacy_content",
                readString(params, "privacy_content", "privacyContent", ""));
    }

    /** ============================== siteStatistics ============================== */

    @Override
    public Map<String, Object> getSiteStatistics() {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("clarity_code", commonConfigService.getOrDefault(TYPE_STATISTICS, "clarity_code", ""));
        return vo;
    }

    @Override
    public void setSiteStatistics(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        commonConfigService.set(TYPE_STATISTICS, "clarity_code",
                readString(params, "clarity_code", "clarityCode", ""));
    }

    /** ============================== 工具方法 ============================== */

    private List<Object> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<Object>>() {});
        } catch (Exception e) {
            log.warn("[SettingWebsite] 解析 JSON 数组失败，原值={}, err={}", json, e.getMessage());
            return new ArrayList<>();
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return "[]";
        if (obj instanceof String) return ((String) obj).isEmpty() ? "[]" : (String) obj;
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[SettingWebsite] 序列化 JSON 失败：{}", e.getMessage());
            return "[]";
        }
    }

    private String readString(Map<String, Object> params, String snake, String camel, String defaults) {
        Object v = params.get(snake);
        if (v == null) v = params.get(camel);
        return v == null ? defaults : v.toString();
    }

    private int parseInt(String s, int defaults) {
        if (s == null || s.isEmpty()) return defaults;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaults;
        }
    }

    /** 下划线 → 驼峰（名 -> name；admin_login_name -> adminLoginName） */
    private String toCamel(String snake) {
        if (snake == null || !snake.contains("_")) return snake;
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (int i = 0; i < snake.length(); i++) {
            char c = snake.charAt(i);
            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }
}
