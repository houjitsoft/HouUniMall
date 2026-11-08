package com.houjiang.logging.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.logging.dto.ElkLogQueryDTO;
import com.houjiang.logging.service.ElkLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ELK 运行日志查询（系统日志、运行错误等）。
 * @author Amos
 */
@RestController
@RequestMapping("/logging/elk")
@RequiredArgsConstructor
@Tag(name = "ELK运行日志", description = "从 Elasticsearch 查询应用运行日志")
public class LogElkController {

    private final ObjectProvider<ElkLogService> elkLogServiceProvider;

    @Operation(summary = "运行日志分页查询")
    @PostMapping("/page")
    public ResponseResult<Map<String, Object>> page(
            @RequestBody ElkLogQueryDTO dto,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") long size) {
        ElkLogService elk = elkLogServiceProvider.getIfAvailable();
        if (elk == null || !elk.isEnabled()) {
            return ResponseResult.success(Map.of(
                    "records", List.of(),
                    "total", 0,
                    "current", current,
                    "size", size,
                    "elkEnabled", false
            ));
        }
        try {
            return ResponseResult.success(elk.queryPage(dto, current, size));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(400, e.getMessage());
        }
    }

    @Operation(summary = "运行日志级别分布")
    @PostMapping("/level-stats")
    public ResponseResult<Map<String, Long>> levelStats(@RequestBody Map<String, String> params) {
        ElkLogService elk = elkLogServiceProvider.getIfAvailable();
        if (elk == null || !elk.isEnabled()) {
            return ResponseResult.success(Map.of());
        }
        return ResponseResult.success(elk.levelStats(params.get("startTime"), params.get("endTime")));
    }
}
