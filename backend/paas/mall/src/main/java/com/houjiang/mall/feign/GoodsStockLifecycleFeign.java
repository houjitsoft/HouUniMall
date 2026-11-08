package com.houjiang.mall.feign;

import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 过渡期兼容：mall 旧 /mall/storage/stock/lifecycle/* 委托给 goods 唯一写方。
 */
@FeignClient(name = "goods", url = "${feign.goods.url:}", contextId = "goodsStockLifecycleFeign")
public interface GoodsStockLifecycleFeign {

    @PostMapping("/goods/storage/stock/lifecycle/reserve")
    void reserve(@RequestBody List<MallStockReservationRequest> requests);

    @PostMapping("/goods/storage/stock/lifecycle/commit")
    void commit(@RequestParam("orderNo") String orderNo);

    @PostMapping("/goods/storage/stock/lifecycle/release")
    void release(@RequestParam("orderNo") String orderNo);

    @PostMapping("/goods/storage/stock/lifecycle/return")
    void returnToStock(@RequestParam("orderNo") String orderNo);
}
