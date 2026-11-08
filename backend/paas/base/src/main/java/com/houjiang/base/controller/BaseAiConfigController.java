package com.houjiang.base.controller;

import com.houjiang.base.dto.BaseAiConfigDTO;
import com.houjiang.base.dto.BaseAiProviderCredentialDTO;
import com.houjiang.base.service.BaseAiConfigService;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI服务配置Controller
 *
 * @author Amos
 * @since 2026-01-07
 */
@RestController
@RequestMapping("base/aiConfig")
public class BaseAiConfigController {

    @Autowired
    private BaseAiConfigService aiConfigService;

    @PostMapping("/list")
    public ResponseResult queryAiConfigList(@RequestBody BaseAiConfigDTO param) {
        return aiConfigService.queryAiConfigList(param);
    }

    @PostMapping("/enabled")
    public ResponseResult queryEnabledConfigs() {
        return aiConfigService.queryEnabledConfigs();
    }

    @GetMapping("/default")
    public ResponseResult getDefaultConfig() {
        return aiConfigService.getDefaultConfig();
    }

    @PostMapping("/save")
    public ResponseResult saveOrUpdateConfig(@RequestBody BaseAiConfigDTO param) {
        return aiConfigService.saveOrUpdateConfig(param);
    }

    @PostMapping("/saveProvider")
    public ResponseResult saveProviderCredential(@RequestBody BaseAiProviderCredentialDTO param) {
        return aiConfigService.saveProviderCredential(param);
    }

    @PostMapping("/delete")
    public ResponseResult deleteConfig(@RequestParam("id") Integer id) {
        return aiConfigService.deleteConfig(id);
    }

    @PostMapping("/toggle")
    public ResponseResult toggleEnabled(@RequestParam("id") Integer id, 
                                       @RequestParam("isEnabled") Integer isEnabled) {
        return aiConfigService.toggleEnabled(id, isEnabled);
    }

    @PostMapping("/setDefault")
    public ResponseResult setAsDefault(@RequestParam("id") Integer id) {
        return aiConfigService.setAsDefault(id);
    }

    @PostMapping("/test")
    public ResponseResult testConnection(@RequestParam("id") Integer id) {
        return aiConfigService.testConnection(id);
    }
}
