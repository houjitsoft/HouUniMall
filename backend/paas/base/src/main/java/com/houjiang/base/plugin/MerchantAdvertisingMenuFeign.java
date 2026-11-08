package com.houjiang.base.plugin;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

/** Narrow service-to-service contract for the mall-advertising bcenter menu. */
@FeignClient(name = "merchant", contextId = "merchantAdvertisingMenuFeign",
        configuration = AdvertisingMerchantMenuFeignConfiguration.class)
public interface MerchantAdvertisingMenuFeign {

    @PostMapping("/merchant/internal/advertising/menu/{transition}")
    Map<String, Object> transition(@PathVariable("transition") String transition);
}
