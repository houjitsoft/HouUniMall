package com.houjiang.bffweb.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 限时折扣只读：打到 goods 宿主兼容桥。
 */
@FeignClient(name = "goods", contextId = "discountQueryFeign", url = "${feign.goods.url:}")
public interface DiscountQueryFeign {

    @PostMapping("/activity/discountGoods/selectByGoodsId")
    Map<String, Object> selectByGoodsId(@RequestParam("goodsId") Integer goodsId);

    @PostMapping("/activity/discountGoods/selectDiscountByGoodsIdAndTime")
    Map<String, Object> selectByGoodsIdAndTime(@RequestParam("goodsId") Integer goodsId);
}
