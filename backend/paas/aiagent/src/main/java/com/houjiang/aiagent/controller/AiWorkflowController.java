package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.service.AiWorkflowService;
import com.houjiang.aiagent.support.AiPageSupport;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 中台 - 工作流（可视化编排）。
 *
 * <p>后台侧：列表 / 详情 / 保存 / 复制 / 启停 / 删除 / 图校验 / 试运行 / 运行记录。
 * 调用侧：{@code POST /ai/workflow/run} 给入参即可跑一张画布，智能体 mode=workflow 时走这里。
 */
@RestController
@RequestMapping("/ai/workflow")
@RequiredArgsConstructor
@Tag(name = "AI 中台 - 工作流")
public class AiWorkflowController {

    private final AiWorkflowService workflowService;

    @PostMapping("/list")
    public ResponseResult<Map<String, Object>> list(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(workflowService.list(params == null ? Map.of() : params));
    }

    @GetMapping("/detail")
    public ResponseResult<Map<String, Object>> detail(@RequestParam("code") String code) {
        return ResponseResult.success(workflowService.detail(code));
    }

    /** 新增或更新：按 code 命中即更新，否则新增 */
    @PostMapping("/save")
    public ResponseResult<Map<String, Object>> save(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(workflowService.save(data));
    }

    @PostMapping("/copy")
    public ResponseResult<Map<String, Object>> copy(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(workflowService.copy(AiPageSupport.str(data, "code")));
    }

    @PostMapping("/delete")
    public ResponseResult<Map<String, Object>> delete(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(workflowService.delete(AiPageSupport.toLong(data, "id")));
    }

    @PostMapping("/status")
    public ResponseResult<Map<String, Object>> status(@RequestBody Map<String, Object> data) {
        Integer status = AiPageSupport.toInt(data, "status");
        return ResponseResult.success(
                workflowService.updateStatus(AiPageSupport.str(data, "code"), status == null ? 0 : status));
    }

    /** 只校验不落库，画布编辑器保存前先跑一遍 */
    @PostMapping("/validate")
    public ResponseResult<Map<String, Object>> validate(@RequestBody Map<String, Object> data) {
        List<String> errors = workflowService.validateGraph(AiPageSupport.str(data, "graph"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        return ResponseResult.success(result);
    }

    /** 试运行：给入参跑一次，返回每个节点的轨迹 */
    @PostMapping("/run")
    public ResponseResult<Map<String, Object>> run(@RequestBody Map<String, Object> data) {
        String code = AiPageSupport.str(data, "code");
        Map<String, Object> inputs = new LinkedHashMap<>();
        Object raw = data.get("inputs");
        if (raw instanceof Map<?, ?> m) {
            m.forEach((k, v) -> inputs.put(String.valueOf(k), v));
        }
        return ResponseResult.success(workflowService.run(code, inputs, AiPageSupport.str(data, "scene")));
    }

    @PostMapping("/runs")
    public ResponseResult<Map<String, Object>> runs(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(workflowService.runs(params == null ? Map.of() : params));
    }

    @GetMapping("/run/detail")
    public ResponseResult<Map<String, Object>> runDetail(@RequestParam("runNo") String runNo) {
        return ResponseResult.success(workflowService.runDetail(runNo));
    }
}
