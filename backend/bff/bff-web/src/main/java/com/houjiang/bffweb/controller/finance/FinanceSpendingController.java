package com.houjiang.bffweb.controller.finance;

import com.houjiang.bffweb.feign.FinanceServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支出管理控制器（BFF层）
/**
 * 提供支出凭证的CRUD接口
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/finance/spending")
@Tag(name = "支出管理", description = "支出凭证相关接口")
public class FinanceSpendingController {

    @Autowired
    private FinanceServiceFeign financeServiceFeign;

    /**
     * 分页查询支出记录
/**
     * @param page 页码
     * @param size 每页大小
     * @param typeId 类别ID
     * @param status 状态
     * @return 支出列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询支出记录")
    public ResponseResult getList(@RequestBody Map<String, Object> param) {
        return financeServiceFeign.getSpendingList(param);
    }

    /**
     * 创建支出记录
/**
     * @param spending 支出数据
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建支出记录")
    public ResponseResult create(@RequestBody Map<String, Object> spending) {
        return financeServiceFeign.createSpending(spending);
    }

    /**
     * 更新支出记录
/**
     * @param spending 支出数据
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新支出记录")
    public ResponseResult update(@RequestBody Map<String, Object> spending) {
        return financeServiceFeign.updateSpending(spending);
    }

    /**
     * 审核支出记录
/**
     * @param id 记录ID
     * @param status 审核状态
     * @param auditId 审核人ID
     * @return 审核结果
     */
    @PostMapping("/audit")
    @Operation(summary = "审核支出记录")
    public ResponseResult audit(
            @RequestParam Integer id,
            @RequestParam String status,
            @RequestParam Integer auditId) {
        return financeServiceFeign.auditSpending(id, status, auditId);
    }

    /**
     * 批量审核支出记录
/**
     * @param params 包含 ids, status, auditId 的参数
     * @return 审核结果
     */
    @PostMapping("/batch-audit")
    @Operation(summary = "批量审核支出记录")
    public ResponseResult batchAudit(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.batchAuditSpending(params);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除支出记录")
    public ResponseResult delete(@RequestParam Integer id) {
        return financeServiceFeign.deleteSpending(id);
    }

    /**
     * 获取所有支出类别
/**
     * @return 类别列表
     */
    @PostMapping("/types")
    @Operation(summary = "获取所有支出类别")
    public ResponseResult getTypes() {
        return financeServiceFeign.getSpendingTypes();
    }
}
