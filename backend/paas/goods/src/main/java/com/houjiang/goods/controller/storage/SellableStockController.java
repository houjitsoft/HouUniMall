package com.houjiang.goods.controller.storage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.entity.storage.SellableStock;
import com.houjiang.goods.service.storage.SellableStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 可售库存 API。订单生命周期与库存 CRUD 的唯一写入入口。
 */
@Slf4j
@RestController
@RequestMapping("/goods/storage/stock")
@Tag(name = "可售库存", description = "销售库存查询、增减、锁定与订单预占生命周期")
public class SellableStockController {

    @Autowired
    private SellableStockService stockService;

    // 订单生命周期（reserve/commit/release/return）唯一入口已迁到 SaleableStockLifecycleController
    // （/goods/storage/stock/lifecycle/*）。此处保留同名映射会触发 Ambiguous mapping，goods 直接起不来。

    @PostMapping("/list")
    @Operation(summary = "分页查询库存")
    public ResponseResult<IPage<SellableStock>> list(@RequestBody Map<String, Object> param) {
        return ResponseResult.success(stockService.getStockList(param));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取库存详情")
    public ResponseResult<SellableStock> detail(@PathVariable Long id) {
        return ResponseResult.success(stockService.getStockById(id));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "获取仓库库存列表")
    public ResponseResult<List<SellableStock>> getByWarehouse(@PathVariable Long warehouseId) {
        return ResponseResult.success(stockService.getStocksByWarehouse(warehouseId));
    }

    @GetMapping("/warning")
    @Operation(summary = "获取库存预警列表")
    public ResponseResult<List<SellableStock>> getWarningList(
            @RequestParam(required = false) Long warehouseId) {
        return ResponseResult.success(stockService.getWarningStocks(warehouseId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建库存记录")
    public ResponseResult<Boolean> create(@RequestBody SellableStock stock) {
        return ResponseResult.success(stockService.createStock(stock));
    }

    @PostMapping("/update")
    @Operation(summary = "更新库存")
    public ResponseResult<Boolean> update(@RequestBody SellableStock stock) {
        return ResponseResult.success(stockService.updateStock(stock));
    }

    @PostMapping("/increase")
    @Operation(summary = "增加库存")
    public ResponseResult<Boolean> increase(@RequestBody Map<String, Object> param) {
        Long warehouseId = Long.parseLong(param.get("warehouseId").toString());
        Long skuId = Long.parseLong(param.get("skuId").toString());
        Integer quantity = Integer.parseInt(param.get("quantity").toString());
        return ResponseResult.success(stockService.increaseStock(warehouseId, skuId, quantity));
    }

    @PostMapping("/decrease")
    @Operation(summary = "减少库存")
    public ResponseResult<Boolean> decrease(@RequestBody Map<String, Object> param) {
        Long warehouseId = Long.parseLong(param.get("warehouseId").toString());
        Long skuId = Long.parseLong(param.get("skuId").toString());
        Integer quantity = Integer.parseInt(param.get("quantity").toString());
        return ResponseResult.success(stockService.decreaseStock(warehouseId, skuId, quantity));
    }

    @PostMapping("/lock")
    @Operation(summary = "锁定库存")
    public ResponseResult<Boolean> lock(@RequestBody Map<String, Object> param) {
        Long warehouseId = Long.parseLong(param.get("warehouseId").toString());
        Long skuId = Long.parseLong(param.get("skuId").toString());
        Integer quantity = Integer.parseInt(param.get("quantity").toString());
        return ResponseResult.success(stockService.lockStock(warehouseId, skuId, quantity));
    }

    @PostMapping("/unlock")
    @Operation(summary = "释放锁定库存")
    public ResponseResult<Boolean> unlock(@RequestBody Map<String, Object> param) {
        Long warehouseId = Long.parseLong(param.get("warehouseId").toString());
        Long skuId = Long.parseLong(param.get("skuId").toString());
        Integer quantity = Integer.parseInt(param.get("quantity").toString());
        return ResponseResult.success(stockService.unlockStock(warehouseId, skuId, quantity));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除库存")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(stockService.deleteStock(id));
    }
}
