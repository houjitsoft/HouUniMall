package com.houjiang.job.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Amos
 * @date 2023/5/17 15:43
 */
@FeignClient("thirdparty")
public interface ThirdPartyServiceClient {


    /**
     * 发送缴纳质保金短信
     *
     * @param userMobile
     * @param result1
     */
    @GetMapping("/sms/sendRetentionMoneyMessage")
    ResponseResult sendRetentionMoneyMessage(@RequestParam("userMobile") String userMobile, @RequestParam("result1") String result1);
}
