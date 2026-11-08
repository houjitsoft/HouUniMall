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
 * 图像类能力。
 *
 * <p>文生图与图像处理属于独立的成本与合规议题（生成图必须按《标识办法》加显式标识与元数据），
 * 不混在本期实现，因此统一返回「未开放」提示而不是抛错——前端展示为提示，不是系统错误。
 * 图像识别（OCR）由独立 OCR 插件提供，待通过内部链路接入后再替换此处。
 */
@RestController
@RequestMapping("/ai/image")
@RequiredArgsConstructor
@Tag(name = "AI 图像")
public class AiImageController {

    private final AiGenerateService generateService;

    @PostMapping("/generate")
    public ResponseResult<Map<String, Object>> generate(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.notSupported("image_generate",
                "文生图能力尚未开放。该能力涉及生成内容标识合规与成本评估，已排入后续版本。"));
    }

    @PostMapping("/recognize")
    public ResponseResult<Map<String, Object>> recognize(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.notSupported("image_recognize",
                "图像识别尚未在此入口开放。请通过已安装的 OCR 插件使用。"));
    }

    @PostMapping("/process")
    public ResponseResult<Map<String, Object>> process(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.notSupported("image_process",
                "图像处理（抠图/美化）属于图像算法能力，不由大模型入口提供，暂未开放。"));
    }
}
