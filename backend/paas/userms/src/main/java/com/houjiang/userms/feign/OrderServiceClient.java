package com.houjiang.userms.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("order")
public interface OrderServiceClient {

    /**
     * TODO 补充方法说明
     * @param map TODO
     * @return TODO
     */
    @PostMapping("/order/base/queryUserConsumption")
    Map<String,Object> queryUserConsumption(@RequestBody Map<String, String> map);

}
