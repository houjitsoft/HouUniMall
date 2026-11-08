package com.houjiang.thirdparty.dingtalk.controller;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.controller.BaseController;
import com.houjiang.thirdparty.dingtalk.dto.AttendancePlatformConfigDTO;
import com.houjiang.thirdparty.dingtalk.service.DingtalkIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 钉钉集成 API
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/dingtalk")
public class DingtalkController extends BaseController {

    @Autowired
    private DingtalkIntegrationService dingtalkIntegrationService;

    @GetMapping("/config")
    public ResponseResult<ThirdPartyConfigDTO> getConfig() {
        try {
            return ResponseResult.success(dingtalkIntegrationService.getConfig());
        } catch (Exception e) {
            log.error("获取钉钉配置失败: {}", e.getMessage(), e);
            return ResponseResult.error("获取钉钉配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/config")
    public ResponseResult<String> saveConfig(@RequestBody ThirdPartyConfigDTO config) {
        try {
            boolean success = dingtalkIntegrationService.saveConfig(config);
            return success ? ResponseResult.success("保存成功") : ResponseResult.error("保存失败");
        } catch (Exception e) {
            log.error("保存钉钉配置失败: {}", e.getMessage(), e);
            return ResponseResult.error("保存失败: " + e.getMessage());
        }
    }

    @PostMapping("/config/test")
    public ResponseResult<String> testConfig(@RequestBody ThirdPartyConfigDTO config) {
        try {
            boolean success = dingtalkIntegrationService.testConnection(config);
            return success ? ResponseResult.success("连接测试成功") : ResponseResult.error("连接测试失败，请检查 AppKey / AppSecret");
        } catch (Exception e) {
            log.error("测试钉钉连接失败: {}", e.getMessage(), e);
            return ResponseResult.error("连接测试失败: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseResult<Map<String, Object>> healthCheck() {
        try {
            return ResponseResult.success(dingtalkIntegrationService.healthCheck());
        } catch (Exception e) {
            log.error("钉钉健康检查失败: {}", e.getMessage(), e);
            return ResponseResult.error("健康检查失败: " + e.getMessage());
        }
    }

    @GetMapping("/attendance-platform")
    public ResponseResult<AttendancePlatformConfigDTO> getAttendancePlatformConfig() {
        try {
            return ResponseResult.success(dingtalkIntegrationService.getAttendancePlatformConfig());
        } catch (Exception e) {
            log.error("获取考勤平台配置失败: {}", e.getMessage(), e);
            return ResponseResult.error("获取考勤平台配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/attendance-platform")
    public ResponseResult<String> saveAttendancePlatformConfig(@RequestBody AttendancePlatformConfigDTO config) {
        try {
            boolean success = dingtalkIntegrationService.saveAttendancePlatformConfig(config);
            return success ? ResponseResult.success("保存成功") : ResponseResult.error("保存失败");
        } catch (Exception e) {
            log.error("保存考勤平台配置失败: {}", e.getMessage(), e);
            return ResponseResult.error("保存失败: " + e.getMessage());
        }
    }
}
