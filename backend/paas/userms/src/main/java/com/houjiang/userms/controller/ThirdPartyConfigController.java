package com.houjiang.userms.controller;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.service.ThirdPartyConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 第三方集成配置Controller
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
@RestController
@RequestMapping("/oa/thirdparty/oa")
@Tag(name = "第三方集成配置", description = "第三方集成配置管理接口")
public class ThirdPartyConfigController {

    @Autowired
    private ThirdPartyConfigService thirdPartyConfigService;

    /**
     * 获取第三方集成配置
     */
    @GetMapping("/{platform}")
    @Operation(summary = "获取第三方集成配置", description = "根据平台名称获取第三方集成配置")
    public ResponseResult<ThirdPartyConfigDTO> getConfig(
            @Parameter(description = "平台名称", required = true) @PathVariable String platform) {
        try {
            ThirdPartyConfigDTO config = thirdPartyConfigService.getConfigByPlatform(platform);
            return ResponseResult.success(config);
        } catch (Exception e) {
            log.error("获取第三方配置失败: {}", e.getMessage());
            return ResponseResult.error("获取配置失败");
        }
    }

    /**
     * 保存第三方集成配置
     */
    @PostMapping("/{platform}")
    @Operation(summary = "保存第三方集成配置", description = "保存第三方集成配置")
    public ResponseResult<String> saveConfig(
            @Parameter(description = "平台名称", required = true) @PathVariable String platform,
            @RequestBody ThirdPartyConfigDTO config) {
        try {
            config.setPlatform(platform);
            boolean success = thirdPartyConfigService.saveConfig(platform, config);
            if (success) {
                return ResponseResult.success("保存成功");
            } else {
                return ResponseResult.error("保存失败");
            }
        } catch (Exception e) {
            log.error("保存第三方配置失败: {}", e.getMessage());
            return ResponseResult.error("保存失败");
        }
    }

    /**
     * 测试第三方集成连接
     */
    @PostMapping("/{platform}/test")
    @Operation(summary = "测试第三方集成连接", description = "测试第三方集成连接")
    public ResponseResult<String> testConnection(
            @Parameter(description = "平台名称", required = true) @PathVariable String platform,
            @RequestBody ThirdPartyConfigDTO config) {
        try {
            config.setPlatform(platform);
            boolean success = thirdPartyConfigService.testConnection(platform, config);
            if (success) {
                return ResponseResult.success("连接测试成功");
            } else {
                return ResponseResult.error("连接测试失败");
            }
        } catch (Exception e) {
            log.error("测试第三方连接失败: {}", e.getMessage());
            return ResponseResult.error("连接测试失败");
        }
    }

    /**
     * 删除第三方集成配置
     */
    @DeleteMapping("/{platform}")
    @Operation(summary = "删除第三方集成配置", description = "删除第三方集成配置")
    public ResponseResult<String> deleteConfig(
            @Parameter(description = "平台名称", required = true) @PathVariable String platform) {
        try {
            boolean success = thirdPartyConfigService.deleteConfig(platform);
            if (success) {
                return ResponseResult.success("删除成功");
            } else {
                return ResponseResult.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除第三方配置失败: {}", e.getMessage());
            return ResponseResult.error("删除失败");
        }
    }
}