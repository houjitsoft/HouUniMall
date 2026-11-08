package com.houjiang.mall.controller.storage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.storage.StockInbound;
import com.houjiang.mall.service.storage.StockInboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 入库单控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/storage/inbound")
@RequiredArgsConstructor
public class StockInboundController {

    private final StockInboundService stockInboundService;

    @PostMapping("/list")
    public ResponseResult<IPage<StockInbound>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(stockInboundService.pageList(params));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<StockInbound> detail(@PathVariable Long id) {
        return ResponseResult.success(stockInboundService.getById(id));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody StockInbound inbound) {
        return ResponseResult.success(stockInboundService.createInbound(inbound));
    }

    @PostMapping("/confirm/{id}")
    public ResponseResult<Boolean> confirm(@PathVariable Long id) {
        return ResponseResult.success(stockInboundService.confirm(id));
    }

    @PostMapping("/cancel/{id}")
    public ResponseResult<Boolean> cancel(@PathVariable Long id) {
        return ResponseResult.success(stockInboundService.cancel(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(stockInboundService.removeById(id));
    }
}
