package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.client.ModelClient;
import com.houjiang.aiagent.service.AiModelService;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/model")
@RequiredArgsConstructor
@Tag(name = "AI 模型")
public class AiModelController {

    private final AiModelService modelService;
    private final ModelClient modelClient;

    @GetMapping("/enabled")
    public ResponseResult<List<Map<String, Object>>> enabled() {
        return ResponseResult.success(modelService.enabled());
    }

    /**
     * 网关连通性自检。后台排错用：未配置网关时返回 configured=false 并给出明确提示。
     */
    @GetMapping("/health")
    public ResponseResult<Map<String, Object>> health() {
        ModelClient.ModelEndpoint endpoint = modelClient.endpoint();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("configured", endpoint.isConfigured());
        data.put("baseUrl", endpoint.getBaseUrl());
        data.put("model", endpoint.getModel());
        if (endpoint.isConfigured()) {
            ModelClient.ModelHealth health = modelClient.health();
            data.put("available", health.isAvailable());
            data.put("message", health.getMessage());
        } else {
            data.put("available", false);
            data.put("message", "AI 网关未配置：请在「AI 设置」填写网关地址与档案密钥");
        }
        return ResponseResult.success(data);
    }
}
