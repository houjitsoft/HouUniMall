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
 * 数据分析类能力。
 *
 * <p>只开放 analyze（对既有数据做解读）。predict 与 recommend 不做：
 * 销售预测是时序建模问题，推荐是召回+排序工程问题，都不属于大模型职责，
 * 用 LLM 硬做会给出看起来合理但不可用的结果。
 */
@RestController
@RequestMapping("/ai/data")
@RequiredArgsConstructor
@Tag(name = "AI 数据分析")
public class AiDataController {

    private final AiGenerateService generateService;

    @PostMapping("/analyze")
    public ResponseResult<Map<String, Object>> analyze(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.generate("data_analyze", params));
    }

    @PostMapping("/predict")
    public ResponseResult<Map<String, Object>> predict(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.notSupported("data_predict",
                "销售预测暂未开放。预测需要基于历史时序建模，属于数据分析能力，不在大模型职责范围内。"));
    }

    @PostMapping("/recommend")
    public ResponseResult<Map<String, Object>> recommend(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(generateService.notSupported("data_recommend",
                "智能推荐暂未开放。推荐依赖召回与排序工程，请沿用商城现有的推荐位配置。"));
    }
}
