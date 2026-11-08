package com.houjiang.mall.controller.storage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.storage.StockTransfer;
import com.houjiang.mall.service.storage.StockTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 库存调拨控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/storage/transfer")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @PostMapping("/list")
    public ResponseResult<IPage<StockTransfer>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(stockTransferService.pageList(params));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<StockTransfer> detail(@PathVariable Long id) {
        return ResponseResult.success(stockTransferService.getById(id));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody StockTransfer transfer) {
        return ResponseResult.success(stockTransferService.createTransfer(transfer));
    }

    @PostMapping("/audit")
    public ResponseResult<Boolean> audit(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        Long auditorId = Long.parseLong(params.get("auditorId").toString());
        String auditorName = (String) params.getOrDefault("auditorName", "");
        String auditRemark = (String) params.getOrDefault("auditRemark", "");
        return ResponseResult.success(stockTransferService.audit(id, status, auditorId, auditorName, auditRemark));
    }

    @PostMapping("/complete/{id}")
    public ResponseResult<Boolean> complete(@PathVariable Long id) {
        return ResponseResult.success(stockTransferService.complete(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(stockTransferService.removeById(id));
    }
}
