package com.houjiang.thirdparty.controller;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.service.ThirdPartyConfigService;
import com.houjiang.thirdparty.service.message.MessageRequestContextResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 第三方集成配置Controller
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
@RestController
@RequestMapping("/api/setting/third-party")
public class ThirdPartyConfigController extends BaseController {

    @Autowired
    private ThirdPartyConfigService thirdPartyConfigService;

    @Autowired
    private MessageRequestContextResolver contextResolver;

    /**
     * 批量获取当前组织下全部第三方配置（设置页一次加载，避免多次往返）
     */
    @GetMapping("/batch")
    public ResponseResult<Map<String, ThirdPartyConfigDTO>> getAllConfigs() {
        try {
            contextResolver.resolveCurrent();
            return ResponseResult.success(thirdPartyConfigService.getAllConfigs(null));
        } catch (Exception e) {
            log.error("批量获取第三方配置失败: {}", e.getMessage(), e);
            return ResponseResult.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取第三方集成配置
     */
    @GetMapping("/{platform}")
    public ResponseResult<ThirdPartyConfigDTO> getConfig(@PathVariable("platform") String platform) {
        try {
            contextResolver.resolveCurrent();
            ThirdPartyConfigDTO config = thirdPartyConfigService.getConfigByPlatform(platform);
            return ResponseResult.success(config);
        } catch (Exception e) {
            log.error("获取第三方配置失败: platform={}, error={}", platform, e.getMessage(), e);
            return ResponseResult.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存第三方集成配置
     */
    @PostMapping("/{platform}")
    public ResponseResult<String> saveConfig(@PathVariable("platform") String platform, 
                                             @RequestBody ThirdPartyConfigDTO config) {
        try {
            contextResolver.resolveCurrent();
            config.setPlatform(platform);
            boolean success = thirdPartyConfigService.saveConfig(platform, config);
            if (success) {
                return ResponseResult.success("保存成功");
            } else {
                return ResponseResult.error("保存失败");
            }
        } catch (Exception e) {
            log.error("保存第三方配置失败: platform={}, error={}", platform, e.getMessage(), e);
            return ResponseResult.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 测试第三方集成连接
     */
    @PostMapping("/{platform}/test")
    public ResponseResult<String> testConnection(@PathVariable("platform") String platform, 
                                                @RequestBody ThirdPartyConfigDTO config) {
        try {
            contextResolver.resolveCurrent();
            config.setPlatform(platform);
            boolean success = thirdPartyConfigService.testConnection(platform, config);
            if (success) {
                return ResponseResult.success("连接测试成功");
            } else {
                return ResponseResult.error("连接测试失败");
            }
        } catch (Exception e) {
            log.error("测试第三方连接失败: platform={}, error={}", platform, e.getMessage(), e);
            return ResponseResult.error("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 删除第三方集成配置
     */
    @DeleteMapping("/{platform}")
    public ResponseResult<String> deleteConfig(@PathVariable("platform") String platform) {
        try {
            contextResolver.resolveCurrent();
            boolean success = thirdPartyConfigService.deleteConfig(platform);
            if (success) {
                return ResponseResult.success("删除成功");
            } else {
                return ResponseResult.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除第三方配置失败: platform={}, error={}", platform, e.getMessage(), e);
            return ResponseResult.error("删除失败: " + e.getMessage());
        }
    }
}
