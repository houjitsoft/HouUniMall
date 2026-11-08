package com.houjiang.job.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * webpos client
 * @author Amos
 */
@FeignClient("webpos")
public interface WebPosServiceClient {

    /**
     * 查询支付宝当面付/微信当面付是否支付成功
     */
    @PostMapping("/webPos/order/zfbOrWxOrderStatus")
    void zfbOrWxOrderStatus();

    /**
     * 查询支付宝当面付/微信当面付是否支付成功
     */
    @PostMapping("/webPos/order/updateOrderStatus")
    void updateOrderStatus();

}
