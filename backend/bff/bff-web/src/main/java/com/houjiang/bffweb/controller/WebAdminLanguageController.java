package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.support.AdminLanguageConfig;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台语言配置。右上角语言菜单只展示这里保存的「支持的语言」。
 */
@RestController
@RequestMapping("/web/base/sys-config/admin-language")
public class WebAdminLanguageController {

    @Autowired
    private BaseServiceFeign baseServiceFeign;

    @GetMapping
    public ResponseResult getAdminLanguage() {
        try {
            String stored = baseServiceFeign.selectConfigValueByKey(AdminLanguageConfig.CONFIG_KEY);
            return ResponseResult.success(AdminLanguageConfig.parseStored(stored));
        } catch (RuntimeException exception) {
            return ResponseResult.error("读取语言配置失败");
        }
    }

    @PostMapping
    public ResponseResult saveAdminLanguage(@RequestBody Map<String, Object> body) {
        Map<String, Object> normalized = AdminLanguageConfig.normalize(body);
        if (normalized == null) {
            return ResponseResult.error("请至少选择一种语言");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("configKey", AdminLanguageConfig.CONFIG_KEY);
        payload.put("configValue", AdminLanguageConfig.toJson(normalized));
        payload.put("configType", "admin_language");
        payload.put("configComment", "后台界面可选语言");
        ResponseResult saved = baseServiceFeign.upsertConfigValue(payload);
        if (saved == null || saved.getCode() != 1) {
            return saved == null ? ResponseResult.error("保存语言配置失败") : saved;
        }
        return ResponseResult.success(normalized);
    }
}
