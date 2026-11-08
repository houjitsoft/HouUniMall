package com.houjiang.order.feign;

import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 可售库存生命周期：唯一打到 goods，禁止再打 mall。
 */
@FeignClient(name = "goods", contextId = "goodsStockServiceClient")
public interface GoodsStockServiceClient {

    @PostMapping("/goods/storage/stock/lifecycle/reserve")
    void reserve(@RequestBody List<MallStockReservationRequest> requests);

    @PostMapping("/goods/storage/stock/lifecycle/commit")
    void commit(@RequestParam("orderNo") String orderNo);

    @PostMapping("/goods/storage/stock/lifecycle/release")
    void release(@RequestParam("orderNo") String orderNo);

    @PostMapping("/goods/storage/stock/lifecycle/return")
    void returnToStock(@RequestParam("orderNo") String orderNo);
}
