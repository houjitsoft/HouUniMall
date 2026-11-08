package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 企业微信服务 Feign
/**
 * @author Amos
 */
@FeignClient(
        name = "thirdparty",
        url = "${feign.thirdparty.url:http://127.0.0.1:8194}",
        path = "/api/wecom",
        contextId = "wecomServiceFeign",
        fallback = WecomServiceFeignFallback.class
)
public interface WecomServiceFeign {

    /**
     * TODO 补充方法说明
/**
     * @param body TODO
     * @return TODO
     */
    @PostMapping("/attendance/sync")
    ResponseResult<Map<String, Object>> syncAttendance(@RequestBody Map<String, Object> body);
}
