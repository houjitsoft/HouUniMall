package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.service.AiPromptService;
import com.houjiang.aiagent.support.AiPageSupport;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 提示词与分类管理。
 */
@RestController
@RequestMapping("/ai/prompt")
@RequiredArgsConstructor
@Tag(name = "AI 提示词")
public class AiPromptController {

    private final AiPromptService promptService;

    @PostMapping("/list")
    public ResponseResult<Map<String, Object>> list(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(promptService.list(params == null ? Map.of() : params));
    }

    @PostMapping("/detail")
    public ResponseResult<Map<String, Object>> detail(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(promptService.detail(AiPageSupport.toLong(params, "id")));
    }

    @PostMapping("/create")
    public ResponseResult<Map<String, Object>> create(@RequestBody(required = false) Map<String, Object> data) {
        Long id = promptService.create(data == null ? Map.of() : data);
        return ResponseResult.success(Map.of("id", id == null ? 0L : id));
    }

    @PostMapping("/update")
    public ResponseResult<Map<String, Object>> update(@RequestBody(required = false) Map<String, Object> data) {
        promptService.update(data == null ? Map.of() : data);
        return ResponseResult.success(Map.of("success", true));
    }

    @PostMapping("/delete")
    public ResponseResult<Map<String, Object>> delete(@RequestBody(required = false) Map<String, Object> params) {
        promptService.delete(AiPageSupport.toLong(params, "id"));
        return ResponseResult.success(Map.of("success", true));
    }

    @GetMapping("/category/list")
    public ResponseResult<List<Map<String, Object>>> categoryList() {
        return ResponseResult.success(promptService.categories());
    }

    @PostMapping("/category/create")
    public ResponseResult<Map<String, Object>> categoryCreate(@RequestBody(required = false) Map<String, Object> data) {
        Long id = promptService.createCategory(data == null ? Map.of() : data);
        return ResponseResult.success(Map.of("id", id == null ? 0L : id));
    }

    @PostMapping("/category/update")
    public ResponseResult<Map<String, Object>> categoryUpdate(@RequestBody(required = false) Map<String, Object> data) {
        promptService.updateCategory(data == null ? Map.of() : data);
        return ResponseResult.success(Map.of("success", true));
    }

    @PostMapping("/category/delete")
    public ResponseResult<Map<String, Object>> categoryDelete(@RequestBody(required = false) Map<String, Object> params) {
        promptService.deleteCategory(AiPageSupport.toLong(params, "id"));
        return ResponseResult.success(Map.of("success", true));
    }
}
