package com.houjiang.thirdparty.feishu.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.controller.BaseController;
import com.houjiang.thirdparty.service.message.MessageRequestContextResolver;
import com.houjiang.thirdparty.feishu.service.FeishuAttendanceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Amos
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/feishu/attendance")
public class FeishuAttendanceController extends BaseController {

    private final FeishuAttendanceSyncService feishuAttendanceSyncService;
    private final MessageRequestContextResolver contextResolver;

    @PostMapping("/sync")
    public ResponseResult<Map<String, Object>> sync(@RequestBody Map<String, Object> body) {
        try {
            String orgId = contextResolver.resolveCurrent().orgId();
            String month = body != null && body.get("month") != null ? body.get("month").toString() : null;
            return ResponseResult.success(feishuAttendanceSyncService.syncMonth(orgId, month));
        } catch (Exception e) {
            log.error("飞书考勤同步失败: {}", e.getMessage(), e);
            return ResponseResult.error("飞书考勤同步失败: " + e.getMessage());
        }
    }
}
