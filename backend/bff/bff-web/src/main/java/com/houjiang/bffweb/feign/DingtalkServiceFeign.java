package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.dto.ThirdPartyConfigDTO;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 钉钉集成服务 Feign 客户端
/**
 * @author Amos
 */
@FeignClient(
        name = "thirdparty",
        url = "${feign.thirdparty.url:http://127.0.0.1:8194}",
        path = "/api/dingtalk",
        contextId = "dingtalkServiceFeign",
        fallback = DingtalkServiceFeignFallback.class
)
public interface DingtalkServiceFeign {

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/config")
    ResponseResult<ThirdPartyConfigDTO> getConfig();

    /**
     * TODO 补充方法说明
/**
     * @param config TODO
     * @return TODO
     */
    @PostMapping("/config")
    ResponseResult<String> saveConfig(@RequestBody ThirdPartyConfigDTO config);

    /**
     * TODO 补充方法说明
/**
     * @param config TODO
     * @return TODO
     */
    @PostMapping("/config/test")
    ResponseResult<String> testConfig(@RequestBody ThirdPartyConfigDTO config);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/health")
    ResponseResult<Map<String, Object>> healthCheck();

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/attendance-platform")
    ResponseResult<Map<String, Object>> getAttendancePlatformConfig();

    /**
     * TODO 补充方法说明
/**
     * @param config TODO
     * @return TODO
     */
    @PostMapping("/attendance-platform")
    ResponseResult<String> saveAttendancePlatformConfig(@RequestBody Map<String, Object> config);
}
