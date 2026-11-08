package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 百度翻译相关 Feign（path=/baidu），与短信 Feign（path=/sms）拆开避免冲突。
 */
@FeignClient(
        name = "thirdparty",
        url = "${feign.thirdparty.url:http://127.0.0.1:8194}",
        path = "/baidu",
        contextId = "thirdpartyBaiduFeign",
        fallback = ThirdpartyBaiduFeignFallback.class
)
public interface ThirdpartyBaiduFeign {

    @PostMapping("/trans")
    ResponseResult trans(@RequestBody Map<String, String> body);

    @PostMapping("/queryLanguageAll")
    ResponseResult queryLanguageAll();
}
