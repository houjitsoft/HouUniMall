package com.houjiang.thirdparty.dingtalk.support;

import com.houjiang.thirdparty.dingtalk.dto.AttendancePlatformConfigDTO;
import com.houjiang.thirdparty.dingtalk.service.DingtalkIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 考勤平台配置按认证组织解析，不跨组织读取配置。
 * @author Amos
 */
@Component
@RequiredArgsConstructor
public class AttendancePlatformConfigSupport {

    private final DingtalkIntegrationService dingtalkIntegrationService;

    /** 读取考勤来源配置时使用当前请求组织；无法确定时返回空。 */
    public String resolveConfigOrgId(String requestOrgId) {
        return StringUtils.hasText(requestOrgId) ? requestOrgId.trim() : null;
    }

    public boolean isWechatWorkEnabled(String orgId) {
        AttendancePlatformConfigDTO p = dingtalkIntegrationService.getAttendancePlatformConfig(orgId);
        return p != null
                && "wechat-work".equals(p.getSource())
                && Boolean.TRUE.equals(p.getEnabled());
    }
}
