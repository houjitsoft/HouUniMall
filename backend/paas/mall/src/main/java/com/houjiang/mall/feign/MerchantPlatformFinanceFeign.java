package com.houjiang.mall.feign;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.config.MerchantPlatformFeignAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/** 商城平台财务对 merchant 的窄接口契约。 */
@FeignClient(name = "merchant", contextId = "merchantPlatformFinanceFeign",
        url = "${feign.merchant.url:}", configuration = MerchantPlatformFeignAuthConfig.class)
public interface MerchantPlatformFinanceFeign {

    @PostMapping("/merchant/platform-finance/center")
    ResponseResult center(@RequestBody Map<String, Object> params);

    @PostMapping("/merchant/platform-finance/refund/stats")
    ResponseResult refundStats(@RequestBody Map<String, Object> params);

    @PostMapping("/merchant/platform-finance/refund/list")
    ResponseResult refundList(@RequestBody Map<String, Object> params);

    @PostMapping("/merchant/platform-finance/refund/retry")
    ResponseResult retryRefund(@RequestBody Map<String, Object> params);

    @PostMapping("/merchant/platform-finance/shop/balance/list")
    ResponseResult shopBalanceList(@RequestBody Map<String, Object> params);

    @PostMapping("/merchant/platform-finance/user/wallet/list")
    ResponseResult userWalletList(@RequestBody Map<String, Object> params);

    @PostMapping("/merchant/platform-finance/user/recharge/list")
    ResponseResult userRechargeList(@RequestBody Map<String, Object> params);

}
