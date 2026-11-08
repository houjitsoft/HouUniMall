package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoodsSellableStockFeignFallback implements GoodsSellableStockFeign {

    private static ResponseResult unavailable() {
        return ResponseResult.fail(503, "goods 可售库存服务暂时不可用");
    }

    @Override
    public ResponseResult getStockList(Map<String, Object> param) {
        return unavailable();
    }

    @Override
    public ResponseResult getStockDetail(Long id) {
        return unavailable();
    }

    @Override
    public ResponseResult getStocksByWarehouse(Long warehouseId) {
        return unavailable();
    }

    @Override
    public ResponseResult getWarningStocks(Long warehouseId) {
        return unavailable();
    }

    @Override
    public ResponseResult createStock(Map<String, Object> stock) {
        return unavailable();
    }

    @Override
    public ResponseResult updateStock(Map<String, Object> stock) {
        return unavailable();
    }

    @Override
    public ResponseResult increaseStock(Map<String, Object> param) {
        return unavailable();
    }

    @Override
    public ResponseResult decreaseStock(Map<String, Object> param) {
        return unavailable();
    }

    @Override
    public ResponseResult lockStock(Map<String, Object> param) {
        return unavailable();
    }

    @Override
    public ResponseResult unlockStock(Map<String, Object> param) {
        return unavailable();
    }

    @Override
    public ResponseResult deleteStock(Long id) {
        return unavailable();
    }
}
