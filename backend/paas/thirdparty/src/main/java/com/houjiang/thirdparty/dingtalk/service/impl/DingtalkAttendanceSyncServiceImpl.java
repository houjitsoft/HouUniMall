package com.houjiang.thirdparty.dingtalk.service.impl;

import com.houjiang.thirdparty.dingtalk.dto.AttendancePlatformConfigDTO;
import com.houjiang.thirdparty.dingtalk.service.DingtalkAttendanceSyncService;
import com.houjiang.thirdparty.dingtalk.service.DingtalkIntegrationService;
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
public class DingtalkAttendanceSyncServiceImpl implements DingtalkAttendanceSyncService {

    private final DingtalkIntegrationService dingtalkIntegrationService;

    @Override
    public Map<String, Object> syncMonth(String orgId, String month) {
        AttendancePlatformConfigDTO platform = dingtalkIntegrationService.getAttendancePlatformConfig();
        Map<String, Object> result = new HashMap<>();
        result.put("orgId", orgId);
        result.put("month", month);
        result.put("dataSource", "dingtalk");
        if (platform == null || !Boolean.TRUE.equals(platform.getDingtalkConfigured())) {
            result.put("skipped", true);
            result.put("message", "请先在系统设置配置并启用钉钉集成（AppKey/AppSecret）");
            result.put("records", List.of());
            result.put("rawCount", 0);
            result.put("recordCount", 0);
            result.put("userCount", 0);
            return result;
        }
        if (!"dingtalk".equals(platform.getSource())) {
            result.put("skipped", true);
            result.put("message", "当前考勤来源非钉钉，请在「考勤设置」切换为钉钉");
            result.put("records", List.of());
            result.put("rawCount", 0);
            result.put("recordCount", 0);
            result.put("userCount", 0);
            return result;
        }
        // TODO: 对接钉钉考勤报表 API（attendance/listRecord 等），返回与企微一致的 records 结构
        result.put("skipped", true);
        result.put("message", "钉钉考勤拉取 API 对接中；落库表 hou_oa_attendance_dingtalk 已就绪，接口打通后将自动写入");
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
