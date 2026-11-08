package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.dto.ThirdPartyConfigDTO;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 第三方集成配置服务Feign客户端
/**
 *
 * @author Amos
 * @since 2025-01-07
 */
@FeignClient(
        name = "thirdparty",
        url = "${feign.thirdparty.url:http://127.0.0.1:8194}",
        path = "/api/setting/third-party",
        contextId = "thirdpartyConfigServiceFeign",
        fallback = ThirdpartyConfigServiceFeignFallback.class
)
public interface ThirdpartyConfigServiceFeign {

    /**
     * 批量获取第三方集成配置
     */
    @GetMapping("/batch")
    ResponseResult<Map<String, ThirdPartyConfigDTO>> getAllConfigs();

    /**
     * 获取第三方集成配置
     */
    @GetMapping("/{platform}")
    ResponseResult<ThirdPartyConfigDTO> getConfig(@PathVariable("platform") String platform);

    /**
     * 保存第三方集成配置
     */
    @PostMapping("/{platform}")
    ResponseResult<String> saveConfig(@PathVariable("platform") String platform, @RequestBody ThirdPartyConfigDTO config);

    /**
     * 测试第三方集成连接
     */
    @PostMapping("/{platform}/test")
    ResponseResult<String> testConnection(@PathVariable("platform") String platform, @RequestBody ThirdPartyConfigDTO config);

    /**
     * 删除第三方集成配置
     */
    @DeleteMapping("/{platform}")
    ResponseResult<String> deleteConfig(@PathVariable("platform") String platform);
}