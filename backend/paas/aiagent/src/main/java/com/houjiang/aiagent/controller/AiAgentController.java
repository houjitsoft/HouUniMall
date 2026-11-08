package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.service.AiAgentService;
import com.houjiang.aiagent.support.AiPageSupport;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 中台 - 智能体（Agent）统一管理。
 *
 * <p>后台侧：列表 / 详情 / 保存 / 启停 / 删除 / 场景字典 / 调用记录。
 * 前台侧：{@code GET /ai/agent/config?code=} 按编码取回可执行配置（模型、提示词、触发与生效范围）。
 */
@RestController
@RequestMapping("/ai/agent")
@RequiredArgsConstructor
@Tag(name = "AI 中台 - 智能体")
public class AiAgentController {

    private final AiAgentService agentService;

    @PostMapping("/list")
    public ResponseResult<Map<String, Object>> list(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(agentService.list(params == null ? Map.of() : params));
    }

    @GetMapping("/detail")
    public ResponseResult<Map<String, Object>> detail(@RequestParam("code") String code) {
        return ResponseResult.success(agentService.detail(code));
    }

    /** 新增或更新：命中 id 或 code 即更新，否则新增 */
    @PostMapping("/save")
    public ResponseResult<Map<String, Object>> save(@RequestBody Map<String, Object> data) {
        var saved = agentService.save(data);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", saved != null);
        result.put("id", saved == null ? null : saved.getId());
        result.put("code", saved == null ? null : saved.getCode());
        return ResponseResult.success(result);
    }

    /** 启用 / 停用 */
    @PostMapping("/status")
    public ResponseResult<Map<String, Object>> status(@RequestBody Map<String, Object> data) {
        String code = AiPageSupport.str(data, "code");
        Integer status = AiPageSupport.toInt(data, "status");
        boolean ok = agentService.updateStatus(code, status);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", ok);
        result.put("message", ok ? "已更新" : "智能体不存在：" + code);
        return ResponseResult.success(result);
    }

    /** 删除；内置智能体拒绝删除 */
    @PostMapping("/delete")
    public ResponseResult<Map<String, Object>> delete(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(agentService.delete(AiPageSupport.toLong(data, "id")));
    }

    @GetMapping("/scenes")
    public ResponseResult<List<Map<String, Object>>> scenes() {
        return ResponseResult.success(agentService.scenes());
    }

    @PostMapping("/invoke/logs")
    public ResponseResult<Map<String, Object>> invokeLogs(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(agentService.invokeLogs(params == null ? Map.of() : params));
    }

    /** 调用完成后上报一条记录（前台或内部服务调用） */
    @PostMapping("/invoke/log")
    public ResponseResult<Map<String, Object>> recordInvoke(@RequestBody Map<String, Object> data) {
        agentService.recordInvoke(data);
        return ResponseResult.success(Map.of("success", true));
    }

    /**
     * 前台取智能体配置。未启用或未绑定模型时返回 enabled=false 并给出原因，
     * 便于前台走既有兜底逻辑而不是直接报错。
     */
    @GetMapping("/config")
    public ResponseResult<Map<String, Object>> config(@RequestParam("code") String code) {
        return ResponseResult.success(agentService.config(code));
    }
}
