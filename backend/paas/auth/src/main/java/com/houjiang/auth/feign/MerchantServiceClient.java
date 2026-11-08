package com.houjiang.auth.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 商户服务 Feign（原 business，已并入 merchant）
 */
@FeignClient(name = "merchant", fallback = MerchantServiceClientFallback.class)
public interface MerchantServiceClient {

    /**
     * 根据用户查询门店/店铺
     */
    @PostMapping("/merchant/internal/queryGateShopIdByUserId")
    ResponseResult queryGateShopIdByUserId(@RequestBody Map<String, String> map);

    /**
     * 查询是否正常商家
     */
    @PostMapping("/merchant/internal/queryShopStatus")
    ResponseResult queryShopStatus(@RequestBody Map<String, String> map);

    /**
     * 删除店铺信息
     */
    @PostMapping("/merchant/internal/delShopInfo")
    ResponseResult delShopInfo(@RequestBody Map<String, String> map);
}
