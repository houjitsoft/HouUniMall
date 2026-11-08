package com.houjiang.thirdparty.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author Amos
 * @date 2023/3/27 10:06
 */
@FeignClient("payment")
public interface PayMentServiceClient {

    /**
     * 获取支付配置
     */
    @PostMapping("/pay/payConfig/queryPayConfigFeign")
    ResponseResult selectPayConfig(@RequestBody Map<String, String> param);

}
