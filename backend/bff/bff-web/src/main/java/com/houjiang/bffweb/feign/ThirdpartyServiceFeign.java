package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 第三方服务Feign客户端
/**
 *
 * @description 调用 paas thirdparty（短信 /sms/sendCode 等）。勿写死 localhost：线上 BFF 与 thirdparty 往往不同机。
 * 未配置 {@code feign.thirdparty.url} 时走 Nacos 服务名 {@code thirdparty}；本地可设 {@code feign.thirdparty.url=http://127.0.0.1:实际端口}。
 * @author Amos
 */
@FeignClient(
        name = "thirdparty",
        url = "${feign.thirdparty.url:http://127.0.0.1:8194}",
        path = "/sms",
        fallback = ThirdpartyServiceFeignFallback.class
)
public interface ThirdpartyServiceFeign {

    /**
     * 发送验证码
/**
     *
     * @param paraMap 发送验证码参数
     * @return ResponseResult
     */
    @PostMapping("/sendCode")
    ResponseResult sendCode(@RequestBody Map<String, String> paraMap);

    /**
     * 查询手机号当天的短信发送回执（运营商回执）
     *
     * @param paraMap 查询参数：userMobile-必填；sendDate-选填（yyyyMMdd）
     * @return ResponseResult，data 含回执状态与友好错误文案（不含短信内容）
     */
    @PostMapping("/deliveryDetails")
    ResponseResult deliveryDetails(@RequestBody Map<String, String> paraMap);
}
