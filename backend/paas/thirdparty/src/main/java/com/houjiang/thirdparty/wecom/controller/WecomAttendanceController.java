package com.houjiang.thirdparty.wecom.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.controller.BaseController;
import com.houjiang.thirdparty.wecom.service.WecomAttendanceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * 企业微信考勤 API
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/wecom/attendance")
@RequiredArgsConstructor
public class WecomAttendanceController extends BaseController {

    private final WecomAttendanceSyncService wecomAttendanceSyncService;

    @PostMapping("/sync")
    public ResponseResult<Map<String, Object>> sync(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> params = body != null ? new java.util.HashMap<>(body) : new java.util.HashMap<>();
            // 服务间调用（OA Feign）走白名单，无 SecurityContext；禁止再 resolveCurrent 否则会 401
            String orgId = normalizeOrgId(params.get("orgId"));
            if (orgId.isEmpty()) {
                orgId = normalizeOrgId(currentHeaderOrgId());
            }
            if (orgId.isEmpty()) {
                return ResponseResult.error("企业微信考勤同步失败: 缺少 orgId（请传 body.orgId 或 X-Org-Id）");
            }
            params.put("orgId", orgId);
            return ResponseResult.success(wecomAttendanceSyncService.syncMonth(orgId, params));
        } catch (Exception e) {
            log.error("企业微信考勤同步失败: {}", e.getMessage(), e);
            return ResponseResult.error("企业微信考勤同步失败: " + e.getMessage());
        }
    }

    private static String normalizeOrgId(Object raw) {
        if (raw == null) {
            return "";
        }
        String value = String.valueOf(raw).trim();
        if (value.isEmpty() || "null".equalsIgnoreCase(value) || "undefined".equalsIgnoreCase(value)) {
            return "";
        }
        return value;
    }

    private String currentHeaderOrgId() {
        try {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                return normalizeOrgId(servletAttrs.getRequest().getHeader("X-Org-Id"));
            }
        } catch (Exception ignored) {
            // ignore
        }
        return "";
    }
}
