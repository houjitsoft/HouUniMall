package com.houjiang.mall.controller.storage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.storage.StockOutbound;
import com.houjiang.mall.service.storage.StockOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 出库单控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/storage/outbound")
@RequiredArgsConstructor
public class StockOutboundController {

    private final StockOutboundService stockOutboundService;

    @PostMapping("/list")
    public ResponseResult<IPage<StockOutbound>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(stockOutboundService.pageList(params));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<StockOutbound> detail(@PathVariable Long id) {
        return ResponseResult.success(stockOutboundService.getById(id));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody StockOutbound outbound) {
        return ResponseResult.success(stockOutboundService.createOutbound(outbound));
    }

    @PostMapping("/confirm/{id}")
    public ResponseResult<Boolean> confirm(@PathVariable Long id) {
        return ResponseResult.success(stockOutboundService.confirm(id));
    }

    @PostMapping("/cancel/{id}")
    public ResponseResult<Boolean> cancel(@PathVariable Long id) {
        return ResponseResult.success(stockOutboundService.cancel(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(stockOutboundService.removeById(id));
    }
}
