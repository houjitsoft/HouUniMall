package com.houjiang.mall.controller.risk;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.risk.RiskMonitor;
import com.houjiang.mall.service.risk.RiskMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 风控监控控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/risk/monitor")
@RequiredArgsConstructor
public class RiskMonitorController {

    private final RiskMonitorService riskMonitorService;

    @PostMapping("/list")
    public ResponseResult<IPage<RiskMonitor>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(riskMonitorService.pageList(params));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<RiskMonitor> detail(@PathVariable Long id) {
        return ResponseResult.success(riskMonitorService.getById(id));
    }

    @GetMapping("/statistics")
    public ResponseResult<Map<String, Object>> statistics(@RequestParam(required = false) Long shopId) {
        return ResponseResult.success(riskMonitorService.getStatistics(shopId));
    }

    @PostMapping("/save")
    public ResponseResult<Boolean> save(@RequestBody RiskMonitor monitor) {
        return ResponseResult.success(riskMonitorService.saveOrUpdateMonitor(monitor));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(riskMonitorService.removeById(id));
    }
}
