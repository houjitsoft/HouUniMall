package com.houjiang.userms.feign;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 第三方集成配置Feign客户端
 * 
 * @author Amos
 * @since 2025-01-07
 */
@FeignClient(name = "thirdparty", path = "/thirdparty")
public interface ThirdPartyConfigFeignClient {

    /**
     * 获取第三方集成配置
     */
    @GetMapping("/config/{platform}")
    ResponseResult<ThirdPartyConfigDTO> getConfig(@PathVariable("platform") String platform);

    /**
     * 保存第三方集成配置
     */
    @PostMapping("/config/{platform}")
    ResponseResult<String> saveConfig(@PathVariable("platform") String platform, @RequestBody ThirdPartyConfigDTO config);

    /**
     * 测试第三方集成连接
     */
    @PostMapping("/config/{platform}/test")
    ResponseResult<String> testConnection(@PathVariable("platform") String platform, @RequestBody ThirdPartyConfigDTO config);

    /**
     * 删除第三方集成配置
     */
    @DeleteMapping("/config/{platform}")
    ResponseResult<String> deleteConfig(@PathVariable("platform") String platform);
}