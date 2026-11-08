package com.houjiang.mall.controller.storage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.storage.StockInventory;
import com.houjiang.mall.service.storage.StockInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 库存盘点控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/storage/inventory")
@RequiredArgsConstructor
public class StockInventoryController {

    private final StockInventoryService stockInventoryService;

    @PostMapping("/list")
    public ResponseResult<IPage<StockInventory>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(stockInventoryService.pageList(params));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<StockInventory> detail(@PathVariable Long id) {
        return ResponseResult.success(stockInventoryService.getById(id));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody StockInventory inventory) {
        return ResponseResult.success(stockInventoryService.createInventory(inventory));
    }

    @PostMapping("/start/{id}")
    public ResponseResult<Boolean> start(@PathVariable Long id) {
        return ResponseResult.success(stockInventoryService.start(id));
    }

    @PostMapping("/submit")
    public ResponseResult<Boolean> submit(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer profitQuantity = Integer.parseInt(params.getOrDefault("profitQuantity", "0").toString());
        Integer lossQuantity = Integer.parseInt(params.getOrDefault("lossQuantity", "0").toString());
        return ResponseResult.success(stockInventoryService.submit(id, profitQuantity, lossQuantity));
    }

    @PostMapping("/audit")
    public ResponseResult<Boolean> audit(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        Long auditorId = Long.parseLong(params.get("auditorId").toString());
        String auditorName = (String) params.getOrDefault("auditorName", "");
        String auditRemark = (String) params.getOrDefault("auditRemark", "");
        return ResponseResult.success(stockInventoryService.audit(id, status, auditorId, auditorName, auditRemark));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(stockInventoryService.removeById(id));
    }
}
