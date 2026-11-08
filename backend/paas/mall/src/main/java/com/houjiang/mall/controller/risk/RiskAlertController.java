package com.houjiang.mall.controller.risk;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.risk.RiskAlert;
import com.houjiang.mall.service.risk.RiskAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 风控预警控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/risk/alert")
@RequiredArgsConstructor
public class RiskAlertController {

    private final RiskAlertService riskAlertService;

    @PostMapping("/list")
    public ResponseResult<IPage<RiskAlert>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(riskAlertService.pageList(params));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<RiskAlert> detail(@PathVariable Long id) {
        return ResponseResult.success(riskAlertService.getById(id));
    }

    @GetMapping("/pendingCount")
    public ResponseResult<Long> pendingCount(@RequestParam(required = false) Long shopId) {
        return ResponseResult.success(riskAlertService.getPendingCount(shopId));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody RiskAlert alert) {
        return ResponseResult.success(riskAlertService.createAlert(alert));
    }

    @PostMapping("/handle")
    public ResponseResult<Boolean> handle(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Long handlerId = Long.parseLong(params.get("handlerId").toString());
        String handlerName = (String) params.getOrDefault("handlerName", "");
        String handleResult = (String) params.getOrDefault("handleResult", "");
        String handleRemark = (String) params.getOrDefault("handleRemark", "");
        return ResponseResult.success(riskAlertService.handle(id, handlerId, handlerName, handleResult, handleRemark));
    }

    @PostMapping("/ignore")
    public ResponseResult<Boolean> ignore(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        String remark = (String) params.getOrDefault("remark", "");
        return ResponseResult.success(riskAlertService.ignore(id, remark));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(riskAlertService.removeById(id));
    }
}
