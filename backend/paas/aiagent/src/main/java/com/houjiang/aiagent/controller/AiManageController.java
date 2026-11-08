package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.service.AiModelService;
import com.houjiang.aiagent.service.AiStatisticsService;
import com.houjiang.aiagent.support.AiPageSupport;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 模型池管理。后台「AI模型管理」与「AI设置 - 模型配置」共用这一组接口。
 *
 * <p>路径与 BFF 的 AiServiceFeign 严格对应（{@code /ai/manage/**}），改动前先看 Feign 定义。
 */
@RestController
@RequestMapping("/ai/manage")
@RequiredArgsConstructor
@Tag(name = "AI 模型池管理")
public class AiManageController {

    private final AiModelService modelService;
    private final AiStatisticsService statisticsService;

    @PostMapping("/statistics")
    public ResponseResult<Map<String, Object>> statistics() {
        return ResponseResult.success(statisticsService.statistics());
    }

    @PostMapping("/list")
    public ResponseResult<Map<String, Object>> list(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(modelService.list(params == null ? Map.of() : params));
    }

    @PostMapping("/detail")
    public ResponseResult<Map<String, Object>> detail(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(modelService.detail(AiPageSupport.toLong(params, "id")));
    }

    @PostMapping("/create")
    public ResponseResult<Map<String, Object>> create(@RequestBody(required = false) Map<String, Object> data) {
        Long id = modelService.create(data == null ? Map.of() : data);
        return ResponseResult.success(Map.of("id", id == null ? 0L : id));
    }

    @PostMapping("/update")
    public ResponseResult<Map<String, Object>> update(@RequestBody(required = false) Map<String, Object> data) {
        modelService.update(data == null ? Map.of() : data);
        return ResponseResult.success(Map.of("success", true));
    }

    @PostMapping("/delete")
    public ResponseResult<Map<String, Object>> delete(@RequestBody(required = false) Map<String, Object> params) {
        modelService.delete(AiPageSupport.toLong(params, "id"));
        return ResponseResult.success(Map.of("success", true));
    }

    @PostMapping("/pause")
    public ResponseResult<Map<String, Object>> pause(@RequestBody(required = false) Map<String, Object> params) {
        modelService.updateStatus(AiPageSupport.toLong(params, "id"), 0);
        return ResponseResult.success(Map.of("success", true));
    }

    @PostMapping("/resume")
    public ResponseResult<Map<String, Object>> resume(@RequestBody(required = false) Map<String, Object> params) {
        modelService.updateStatus(AiPageSupport.toLong(params, "id"), 1);
        return ResponseResult.success(Map.of("success", true));
    }

    @PostMapping("/copy")
    public ResponseResult<Map<String, Object>> copy(@RequestBody(required = false) Map<String, Object> params) {
        Long id = modelService.copy(AiPageSupport.toLong(params, "id"));
        return ResponseResult.success(Map.of("id", id == null ? 0L : id));
    }

    @GetMapping("/types")
    @SuppressWarnings("unchecked")
    public ResponseResult<java.util.List<java.util.Map<String, String>>> types() {
        return ResponseResult.success(modelService.types());
    }

    @PostMapping("/batch/status")
    public ResponseResult<Map<String, Object>> batchStatus(@RequestBody(required = false) Map<String, Object> params) {
        Object ids = params == null ? null : params.get("ids");
        Integer status = AiPageSupport.toInt(params, "status");
        int target = status == null ? 0 : status;
        if (ids instanceof Iterable<?> iterable) {
            for (Object id : iterable) {
                try {
                    modelService.updateStatus(Long.parseLong(String.valueOf(id)), target);
                } catch (NumberFormatException ignored) {
                    // 忽略非法 id
                }
            }
        }
        return ResponseResult.success(Map.of("success", true));
    }

    @PostMapping("/generation/list")
    public ResponseResult<Map<String, Object>> generationList(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(statisticsService.generations(params == null ? Map.of() : params));
    }
}
