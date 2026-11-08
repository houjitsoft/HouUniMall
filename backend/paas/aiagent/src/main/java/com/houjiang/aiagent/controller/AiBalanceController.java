package com.houjiang.aiagent.controller;

import com.houjiang.aiagent.service.AiBalanceService;
import com.houjiang.commons.util.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 中台 - 余额与配额。数据来自 {@code hou_ai_balance}（迁移自 GEO 的 hou_geo_ai_balance）。
 */
@RestController
@RequestMapping("/ai/balance")
@RequiredArgsConstructor
@Tag(name = "AI 中台 - 余额配额")
public class AiBalanceController {

    private final AiBalanceService balanceService;

    @PostMapping("/list")
    public ResponseResult<Map<String, Object>> list(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(balanceService.list(params == null ? Map.of() : params));
    }

    @GetMapping("/detail")
    public ResponseResult<Map<String, Object>> detail(@RequestParam String subjectType,
                                                      @RequestParam String subjectKey) {
        return ResponseResult.success(balanceService.detail(subjectType, subjectKey));
    }

    /**
     * 充值已购余额。{@code tokens} 为 token 数量。
     */
    @PostMapping("/recharge")
    public ResponseResult<Map<String, Object>> recharge(@RequestBody Map<String, Object> data) {
        String subjectType = str(data, "subjectType");
        String subjectKey = str(data, "subjectKey");
        long tokens = longOf(data, "tokens");
        boolean ok = balanceService.recharge(subjectType, subjectKey, tokens);
        return ResponseResult.success(Map.of("success", ok));
    }

    /**
     * 扣减额度。原子执行，余额不足返回 success=false（不会超扣）。
     */
    @PostMapping("/consume")
    public ResponseResult<Map<String, Object>> consume(@RequestBody Map<String, Object> data) {
        String subjectType = str(data, "subjectType");
        String subjectKey = str(data, "subjectKey");
        long tokens = longOf(data, "tokens");
        boolean ok = balanceService.consume(subjectType, subjectKey, tokens);
        return ResponseResult.success(Map.of("success", ok));
    }

    private static String str(Map<String, Object> data, String key) {
        Object v = data.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static long longOf(Map<String, Object> data, String key) {
        Object v = data.get(key);
        if (v == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
