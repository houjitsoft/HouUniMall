package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.service.AiUsageRecordService;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 中台 - 调用计量（{@code hou_ai_token_usage}）。
 *
 * <p>历史数据由 GEO 写入（module_name=geo），中台接管后统一在这里查询，
 * 实现「一套账」：GEO 的消耗与中台新链路的消耗在同一张表里对得上。
 */
@RestController
@RequestMapping("/ai/usage")
@RequiredArgsConstructor
@Tag(name = "AI 中台 - 调用计量")
public class AiUsageController {

    private final AiUsageRecordService usageRecordService;

    @PostMapping("/records")
    public ResponseResult<Map<String, Object>> records(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(usageRecordService.list(params == null ? Map.of() : params));
    }

    @GetMapping("/summary")
    public ResponseResult<Map<String, Object>> summary() {
        return ResponseResult.success(usageRecordService.summary());
    }
}
