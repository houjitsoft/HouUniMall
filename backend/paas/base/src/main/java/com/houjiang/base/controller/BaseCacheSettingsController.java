package com.houjiang.base.controller;

import com.houjiang.commons.dto.cache.CacheSettingsDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 缓存设置
 * @author Amos
 */
@RestController
@RequestMapping("base/cache/settings")
public class BaseCacheSettingsController extends BaseController {

    @PostMapping("/get")
    public ResponseResult getSettings() {
        return cacheSettingsService.getSettings();
    }

    @PostMapping("/save")
    public ResponseResult saveSettings(@RequestBody CacheSettingsDTO settings) {
        return cacheSettingsService.saveSettings(settings);
    }

    @PostMapping("/test")
    public ResponseResult testConnection() {
        return cacheSettingsService.testConnection();
    }
}
