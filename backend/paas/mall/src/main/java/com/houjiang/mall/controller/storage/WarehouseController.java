package com.houjiang.mall.controller.storage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.storage.Warehouse;
import com.houjiang.mall.service.storage.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 仓库控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/storage/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/list")
    public ResponseResult<IPage<Warehouse>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(warehouseService.pageList(params));
    }

    @GetMapping("/enabled")
    public ResponseResult<List<Warehouse>> listEnabled(@RequestParam(required = false) Long shopId) {
        return ResponseResult.success(warehouseService.listEnabled(shopId));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<Warehouse> detail(@PathVariable Long id) {
        return ResponseResult.success(warehouseService.getById(id));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody Warehouse warehouse) {
        return ResponseResult.success(warehouseService.createWarehouse(warehouse));
    }

    @PostMapping("/update")
    public ResponseResult<Boolean> update(@RequestBody Warehouse warehouse) {
        return ResponseResult.success(warehouseService.updateWarehouse(warehouse));
    }

    @PostMapping("/setDefault")
    public ResponseResult<Boolean> setDefault(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Long shopId = Long.parseLong(params.get("shopId").toString());
        return ResponseResult.success(warehouseService.setDefault(id, shopId));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(warehouseService.removeById(id));
    }
}
