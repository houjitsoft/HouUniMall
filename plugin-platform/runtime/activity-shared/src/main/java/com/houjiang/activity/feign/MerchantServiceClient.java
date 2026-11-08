package com.houjiang.activity.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 商户服务 Feign（原 business，已并入 merchant）
 */
@FeignClient(name = "merchant", contextId = "activityMerchantServiceClient", configuration = ActivityFeignConfig.class)
public interface MerchantServiceClient {

    @PostMapping("/merchant/internal/selectShopName")
    String selectShopNameByShopId(@RequestParam("shopId") Integer shopId);

    @PostMapping("/merchant/internal/selectShopName")
    String selectShopNameByShopId(@RequestParam("shopId") String shopId);

    @PostMapping("/merchant/internal/queryShopNameByShopId")
    String queryShopNameByShopId(@RequestParam("shopId") Integer shopId);

    @PostMapping("/merchant/internal/queryInvoiceSwitch")
    ResponseResult queryInvoiceSwitch(@RequestBody Map<String, Object> param);
}
