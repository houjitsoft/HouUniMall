package com.houjiang.thirdparty.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName RiderServiceClient
 * @Description 骑手模块
 * @Author Amos
 * @Date 2024/5/7 17:24
 * @Version 1.0
 * @author Amos
 */
@FeignClient("rider")
public interface RiderServiceClient {

    /**
     * 通过手机号码查询骑手信息-fegin
     * @param userMobile
     * @return
     */
    @PostMapping("/rider/base/queryRiderInfoByMobile")
    String queryRiderInfoByMobile(@RequestParam("userMobile") String userMobile);
}
