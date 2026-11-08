package com.houjiang.goods.controller.storage;

import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import com.houjiang.goods.service.storage.SaleableStockLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 可售库存订单生命周期 API（goods 唯一写方）。
 * order / 过渡期 mall 兼容入口均调用本接口。
 */
@RestController
@RequestMapping("/goods/storage/stock/lifecycle")
@RequiredArgsConstructor
@Tag(name = "可售库存生命周期", description = "reserve/commit/release/return — goods 唯一写方")
public class SaleableStockLifecycleController {

    private final SaleableStockLifecycleService lifecycleService;

    @PostMapping("/reserve")
    @Operation(summary = "订单预占可售库存")
    public void reserve(@RequestBody List<MallStockReservationRequest> requests) {
        lifecycleService.reserveForOrder(requests);
    }

    @PostMapping("/commit")
    @Operation(summary = "支付成功实扣预占库存")
    public void commit(@RequestParam("orderNo") String orderNo) {
        lifecycleService.commitOrder(orderNo);
    }

    @PostMapping("/release")
    @Operation(summary = "未支付关闭释放预占")
    public void release(@RequestParam("orderNo") String orderNo) {
        lifecycleService.releaseOrder(orderNo);
    }

    @PostMapping("/return")
    @Operation(summary = "退货验收入库后回补可售库存")
    public void returnToStock(@RequestParam("orderNo") String orderNo) {
        lifecycleService.returnOrderToStock(orderNo);
    }
}
