package com.houjiang.activity.feign;

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
// contextId 必须显式指定：同包下 UserServiceClient 也是 name=userms，
// 两者会各注册一个 userms.FeignClientSpecification，宿主开启插件扫描后直接 BeanDefinitionOverrideException。
@FeignClient(name = "userms", contextId = "activityThirdPartyConfigClient", configuration = ActivityFeignConfig.class)
public interface ThirdPartyConfigClient {

    /**
     * 获取第三方集成配置
     * 
     * @param platform 平台名称
     * @return 配置信息
     */
    @GetMapping("/api/oa/thirdparty/oa/{platform}")
    ResponseResult<ThirdPartyConfigDTO> getConfig(@PathVariable("platform") String platform);

    /**
     * 保存第三方集成配置
     * 
     * @param platform 平台名称
     * @param config 配置信息
     * @return 是否成功
     */
    @PostMapping("/api/oa/thirdparty/oa/{platform}")
    ResponseResult<String> saveConfig(@PathVariable("platform") String platform, @RequestBody ThirdPartyConfigDTO config);

    /**
     * 测试第三方集成连接
     * 
     * @param platform 平台名称
     * @param config 配置信息
     * @return 是否成功
     */
    @PostMapping("/api/oa/thirdparty/oa/{platform}/test")
    ResponseResult<String> testConnection(@PathVariable("platform") String platform, @RequestBody ThirdPartyConfigDTO config);

    /**
     * 删除第三方集成配置
     * 
     * @param platform 平台名称
     * @return 是否成功
     */
    @DeleteMapping("/api/oa/thirdparty/oa/{platform}")
    ResponseResult<String> deleteConfig(@PathVariable("platform") String platform);
}