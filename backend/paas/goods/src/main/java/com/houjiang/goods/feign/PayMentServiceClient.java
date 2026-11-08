package com.houjiang.goods.feign;


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
     *
     * @param param
     * @return
     */
    @PostMapping("/pay/payConfig/queryFeign")
    ResponseResult selectPayConfig(@RequestBody Map<String, Object> param);

    /**
     * 获取品牌小程序支付配置 feign
     *
     * @param param
     * @return
     */
    @PostMapping("/pay/config/shop/selectFeign")
    ResponseResult selectPayConfigFeign(@RequestBody Map<String, Object> param);
}
