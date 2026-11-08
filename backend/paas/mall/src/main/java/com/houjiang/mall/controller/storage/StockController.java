package com.houjiang.mall.controller.storage;

import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.feign.GoodsStockLifecycleFeign;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 可售库存已迁至 goods。本 Controller 仅拒绝写入，防止双写。
 * 入库/出库/调拨/盘点单据仍由 mall 过渡，后续归 ERP。
 */
@Slf4j
@RestController
@RequestMapping("/mall/storage/stock")
@Tag(name = "库存管理(已迁出)", description = "可售库存唯一写入面为 goods，此处拒绝写操作")
public class StockController {

    private static final String MOVED =
            "可售库存已迁至 goods（/goods/storage/stock/**），mall 不再写入 hou_mall_stock";

    /** Stage 3: lifecycle 唯一写方在 goods；本 controller 仅兼容旧 URL。 */
    @Autowired
    private GoodsStockLifecycleFeign goodsStockLifecycleFeign;

    @PostMapping("/lifecycle/reserve")
    public void reserveForOrder(@RequestBody List<MallStockReservationRequest> requests) {
        goodsStockLifecycleFeign.reserve(requests);
    }

    @PostMapping("/lifecycle/commit")
    public void commitOrder(@RequestParam("orderNo") String orderNo) {
        goodsStockLifecycleFeign.commit(orderNo);
    }

    @PostMapping("/lifecycle/release")
    public void releaseOrder(@RequestParam("orderNo") String orderNo) {
        goodsStockLifecycleFeign.release(orderNo);
    }

    @PostMapping("/lifecycle/return")
    public void returnOrderToStock(@RequestParam("orderNo") String orderNo) {
        goodsStockLifecycleFeign.returnToStock(orderNo);
    }

    @PostMapping("/list")
    @Operation(summary = "已迁出")
    public ResponseResult<?> list(@RequestBody Map<String, Object> param) {
        throw new IllegalStateException(MOVED);
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<?> detail(@PathVariable Long id) {
        throw new IllegalStateException(MOVED);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseResult<?> getByWarehouse(@PathVariable Long warehouseId) {
        throw new IllegalStateException(MOVED);
    }

    @GetMapping("/warning")
    public ResponseResult<?> getWarningList(@RequestParam(required = false) Long warehouseId) {
        throw new IllegalStateException(MOVED);
    }

    @PostMapping("/create")
    public ResponseResult<?> create(@RequestBody Map<String, Object> stock) {
        throw new IllegalStateException(MOVED);
    }

    @PostMapping("/update")
    public ResponseResult<?> update(@RequestBody Map<String, Object> stock) {
        throw new IllegalStateException(MOVED);
    }

    @PostMapping("/increase")
    public ResponseResult<?> increase(@RequestBody Map<String, Object> param) {
        throw new IllegalStateException(MOVED);
    }

    @PostMapping("/decrease")
    public ResponseResult<?> decrease(@RequestBody Map<String, Object> param) {
        throw new IllegalStateException(MOVED);
    }

    @PostMapping("/lock")
    public ResponseResult<?> lock(@RequestBody Map<String, Object> param) {
        throw new IllegalStateException(MOVED);
    }

    @PostMapping("/unlock")
    public ResponseResult<?> unlock(@RequestBody Map<String, Object> param) {
        throw new IllegalStateException(MOVED);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<?> delete(@PathVariable Long id) {
        throw new IllegalStateException(MOVED);
    }
}
