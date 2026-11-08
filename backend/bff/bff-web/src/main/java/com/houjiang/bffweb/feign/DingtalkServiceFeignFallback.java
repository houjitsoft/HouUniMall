package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.dto.ThirdPartyConfigDTO;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Amos
 */
@Component
public class DingtalkServiceFeignFallback implements DingtalkServiceFeign {

    @Override
    public ResponseResult<ThirdPartyConfigDTO> getConfig() {
        return ResponseResult.error("获取钉钉配置失败");
    }

    @Override
    public ResponseResult<String> saveConfig(ThirdPartyConfigDTO config) {
        return ResponseResult.error("保存钉钉配置失败");
    }

    @Override
    public ResponseResult<String> testConfig(ThirdPartyConfigDTO config) {
        return ResponseResult.error("钉钉连接测试失败");
    }

    @Override
    public ResponseResult<Map<String, Object>> healthCheck() {
        return ResponseResult.error("钉钉健康检查失败");
    }

    @Override
    public ResponseResult<Map<String, Object>> getAttendancePlatformConfig() {
        return ResponseResult.error("获取考勤平台配置失败");
    }

    @Override
    public ResponseResult<String> saveAttendancePlatformConfig(Map<String, Object> config) {
        return ResponseResult.error("保存考勤平台配置失败");
    }
}
