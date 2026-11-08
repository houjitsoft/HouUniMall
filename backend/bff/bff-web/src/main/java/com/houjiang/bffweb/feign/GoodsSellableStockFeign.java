package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 可售库存（hou_mall_stock）唯一经 goods 读写。
 */
@FeignClient(name = "goods", contextId = "goodsSellableStockFeign", url = "${feign.goods.url:}",
        fallback = GoodsSellableStockFeignFallback.class)
public interface GoodsSellableStockFeign {

    @PostMapping("/goods/storage/stock/list")
    ResponseResult getStockList(@RequestBody Map<String, Object> param);

    @GetMapping("/goods/storage/stock/detail/{id}")
    ResponseResult getStockDetail(@PathVariable("id") Long id);

    @GetMapping("/goods/storage/stock/warehouse/{warehouseId}")
    ResponseResult getStocksByWarehouse(@PathVariable("warehouseId") Long warehouseId);

    @GetMapping("/goods/storage/stock/warning")
    ResponseResult getWarningStocks(@RequestParam(value = "warehouseId", required = false) Long warehouseId);

    @PostMapping("/goods/storage/stock/create")
    ResponseResult createStock(@RequestBody Map<String, Object> stock);

    @PostMapping("/goods/storage/stock/update")
    ResponseResult updateStock(@RequestBody Map<String, Object> stock);

    @PostMapping("/goods/storage/stock/increase")
    ResponseResult increaseStock(@RequestBody Map<String, Object> param);

    @PostMapping("/goods/storage/stock/decrease")
    ResponseResult decreaseStock(@RequestBody Map<String, Object> param);

    @PostMapping("/goods/storage/stock/lock")
    ResponseResult lockStock(@RequestBody Map<String, Object> param);

    @PostMapping("/goods/storage/stock/unlock")
    ResponseResult unlockStock(@RequestBody Map<String, Object> param);

    @DeleteMapping("/goods/storage/stock/delete/{id}")
    ResponseResult deleteStock(@PathVariable("id") Long id);
}
