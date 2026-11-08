package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.dto.ThirdPartyConfigDTO;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 第三方集成配置服务Feign客户端降级处理
/**
 *
 * @author Amos
 * @since 2025-01-07
 */
@Component
public class ThirdpartyConfigServiceFeignFallback implements ThirdpartyConfigServiceFeign {

    @Override
    public ResponseResult<Map<String, ThirdPartyConfigDTO>> getAllConfigs() {
        return ResponseResult.error("获取第三方配置失败");
    }

    @Override
    public ResponseResult<ThirdPartyConfigDTO> getConfig(String platform) {
        return ResponseResult.error("获取第三方配置失败");
    }

    @Override
    public ResponseResult<String> saveConfig(String platform, ThirdPartyConfigDTO config) {
        return ResponseResult.error("保存第三方配置失败");
    }

    @Override
    public ResponseResult<String> testConnection(String platform, ThirdPartyConfigDTO config) {
        return ResponseResult.error("测试第三方连接失败");
    }

    @Override
    public ResponseResult<String> deleteConfig(String platform) {
        return ResponseResult.error("删除第三方配置失败");
    }
}