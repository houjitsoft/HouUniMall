package com.houjiang.bffweb.controller.finance;

import com.houjiang.bffweb.feign.FinanceServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 资金管理控制器（BFF层）
/**
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/finance/fund")
@Tag(name = "资金管理", description = "资金管理相关接口")
public class FinanceFundController {

    @Autowired
    private FinanceServiceFeign financeServiceFeign;

    /**
     * 获取应收列表
     */
    @PostMapping("/receivable/list")
    @Operation(summary = "获取应收列表")
    public ResponseResult getReceivableList(@RequestBody Map<String, Object> param) {
        return financeServiceFeign.getReceivableList(param);
    }

    /**
     * 获取应收详情
     */
    @PostMapping("/receivable/detail")
    @Operation(summary = "获取应收详情")
    public ResponseResult getReceivableDetail(@RequestBody Map<String, Object> param) {
        return financeServiceFeign.getReceivableDetail(param);
    }

    /**
     * 获取应收统计
     */
    @PostMapping("/receivable/stats")
    @Operation(summary = "获取应收统计")
    public ResponseResult getReceivableStats() {
        return financeServiceFeign.getReceivableStats();
    }

    /**
     * 获取应付列表
     */
    @PostMapping("/payable/list")
    @Operation(summary = "获取应付列表")
    public ResponseResult getPayableList(@RequestBody Map<String, Object> param) {
        return financeServiceFeign.getPayableList(param);
    }

    /**
     * 获取应付统计
     */
    @PostMapping("/payable/stats")
    @Operation(summary = "获取应付统计")
    public ResponseResult getPayableStats(@RequestBody Map<String, Object> param) {
        return financeServiceFeign.getPayableStats(param);
    }
}

