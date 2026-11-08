package com.houjiang.userms.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Amos
 */
@FeignClient("payment")
public interface PaymentServiceClient {


    /**
     * 创建用户
     *
     * @param userId
     * @return
     */
    @GetMapping("/pay/userBase/createUser")
    ResponseResult createUser(@RequestParam("userId") String userId);

}
