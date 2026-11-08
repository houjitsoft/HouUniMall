package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.service.AiGenerateService;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 文本类能力：生成 / 摘要 / 翻译 / 纠错。全部经 AI 网关调用模型，并落生成记录与用量。
 */
@RestController
@RequestMapping("/ai/text")
@RequiredArgsConstructor
@Tag(name = "AI 文本生成")
public class AiTextController {

    private final AiGenerateService generateService;

    @PostMapping("/generate")
    public ResponseResult<Map<String, Object>> generate(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.generate("text_generate", params));
    }

    @PostMapping("/summary")
    public ResponseResult<Map<String, Object>> summary(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.generate("summary", params));
    }

    @PostMapping("/translate")
    public ResponseResult<Map<String, Object>> translate(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.generate("translate", params));
    }

    @PostMapping("/correct")
    public ResponseResult<Map<String, Object>> correct(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.generate("correct", params));
    }
}
