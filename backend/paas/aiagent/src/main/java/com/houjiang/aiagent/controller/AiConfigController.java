package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.service.AiConfigService;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AI 配置。前端「AI设置」按扁平 map 读写，因此 all 返回 {@code {key: value}}。
 *
 * <p>其中 {@code ai_gateway_base_url} 与 {@code ai_gateway_profile_key} 决定模型出口，
 * 改完立即生效（ModelClient 每次调用都读最新配置），不需要重启服务。
 */
@RestController
@RequestMapping("/ai/config")
@RequiredArgsConstructor
@Tag(name = "AI 配置")
public class AiConfigController {

    private final AiConfigService configService;

    @GetMapping("/all")
    public ResponseResult<Map<String, String>> all() {
        return ResponseResult.success(configService.all());
    }

    @PostMapping("/list")
    public ResponseResult<List<Map<String, Object>>> list() {
        return ResponseResult.success(configService.list());
    }

    @PostMapping("/save")
    public ResponseResult<Map<String, Object>> save(@RequestBody(required = false) Map<String, Object> data) {
        return ResponseResult.success(Map.of("count", configService.save(data)));
    }

    @PostMapping("/batch")
    public ResponseResult<Map<String, Object>> batch(@RequestBody(required = false) Map<String, Object> data) {
        return ResponseResult.success(Map.of("count", configService.batch(data)));
    }
}
