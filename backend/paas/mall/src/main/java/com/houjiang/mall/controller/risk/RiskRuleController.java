package com.houjiang.mall.controller.risk;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.risk.RiskRule;
import com.houjiang.mall.service.risk.RiskRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 风控规则控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/risk/rule")
@RequiredArgsConstructor
public class RiskRuleController {

    private final RiskRuleService riskRuleService;

    @PostMapping("/list")
    public ResponseResult<IPage<RiskRule>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(riskRuleService.pageList(params));
    }

    @GetMapping("/enabled")
    public ResponseResult<List<RiskRule>> listEnabled(@RequestParam(required = false) Long shopId) {
        return ResponseResult.success(riskRuleService.listEnabled(shopId));
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<RiskRule> detail(@PathVariable Long id) {
        return ResponseResult.success(riskRuleService.getById(id));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody RiskRule rule) {
        return ResponseResult.success(riskRuleService.createRule(rule));
    }

    @PostMapping("/update")
    public ResponseResult<Boolean> update(@RequestBody RiskRule rule) {
        return ResponseResult.success(riskRuleService.updateRule(rule));
    }

    @PostMapping("/status")
    public ResponseResult<Boolean> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        return ResponseResult.success(riskRuleService.updateStatus(id, status));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(riskRuleService.removeById(id));
    }
}
