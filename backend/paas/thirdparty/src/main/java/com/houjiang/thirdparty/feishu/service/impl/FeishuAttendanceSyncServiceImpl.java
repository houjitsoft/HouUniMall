package com.houjiang.thirdparty.feishu.service.impl;

import com.houjiang.thirdparty.dingtalk.dto.AttendancePlatformConfigDTO;
import com.houjiang.thirdparty.dingtalk.service.DingtalkIntegrationService;
import com.houjiang.thirdparty.feishu.service.FeishuAttendanceSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Service
public class FeishuAttendanceSyncServiceImpl implements FeishuAttendanceSyncService {

    private final DingtalkIntegrationService dingtalkIntegrationService;

    @Override
    public Map<String, Object> syncMonth(String orgId, String month) {
        AttendancePlatformConfigDTO platform = dingtalkIntegrationService.getAttendancePlatformConfig();
        Map<String, Object> result = new HashMap<>();
        result.put("orgId", orgId);
        result.put("month", month);
        result.put("dataSource", "feishu");
        if (platform == null || !Boolean.TRUE.equals(platform.getFeishuConfigured())) {
            result.put("skipped", true);
            result.put("message", "请先在系统设置配置并启用飞书集成");
            result.put("records", List.of());
            result.put("rawCount", 0);
            result.put("recordCount", 0);
            result.put("userCount", 0);
            return result;
        }
        if (!"feishu".equals(platform.getSource())) {
            result.put("skipped", true);
            result.put("message", "当前考勤来源非飞书，请在「考勤设置」切换为飞书");
            result.put("records", List.of());
            result.put("rawCount", 0);
            result.put("recordCount", 0);
            result.put("userCount", 0);
            return result;
        }
        result.put("skipped", true);
        result.put("message", "飞书考勤拉取 API 对接中；落库表 hou_oa_attendance_feishu 已就绪，接口打通后将自动写入");
        result.put("records", List.of());
        result.put("rawCount", 0);
        result.put("recordCount", 0);
        result.put("userCount", 0);
        if (StringUtils.hasText(month)) {
            result.put("syncMonth", month);
        }
        return result;
    }
}
