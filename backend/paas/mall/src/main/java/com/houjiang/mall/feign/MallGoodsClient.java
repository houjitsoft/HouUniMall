package com.houjiang.mall.feign;

import com.houjiang.commons.dto.mall.MallSkuSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Amos
 */
@FeignClient("goods")
public interface MallGoodsClient {

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @GetMapping("/mall/goods/saleable-sku")
    MallSkuSnapshot requireSaleableSku(@RequestParam("skuId") Long skuId);
}
