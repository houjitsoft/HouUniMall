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
 * 收入管理控制器（BFF层）
/**
 * 提供收入凭证的CRUD接口；每次请求由财务服务校验当前公司
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/finance/income")
@Tag(name = "收入管理", description = "收入凭证相关接口")
public class FinanceIncomeController {

    @Autowired
    private FinanceServiceFeign financeServiceFeign;

    /**
     * 分页查询收入记录
/**
     * @param page 页码
     * @param size 每页大小
     * @param typeId 类别ID
     * @param status 状态
     * @return 收入列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询收入记录")
    public ResponseResult getList(@RequestBody Map<String, Object> param) {
        return financeServiceFeign.getIncomeList(param);
    }

    /**
     * 创建收入记录
/**
     * @param income 收入数据
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建收入记录")
    public ResponseResult create(@RequestBody Map<String, Object> income) {
        return financeServiceFeign.createIncome(income);
    }

    /**
     * 更新收入记录
/**
     * @param income 收入数据
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新收入记录")
    public ResponseResult update(@RequestBody Map<String, Object> income) {
        return financeServiceFeign.updateIncome(income);
    }

    /**
     * 审核收入记录
/**
     * @param id 记录ID
     * @param status 审核状态
     * @param auditId 审核人ID
     * @return 审核结果
     */
    @PostMapping("/audit")
    @Operation(summary = "审核收入记录")
    public ResponseResult audit(
            @RequestParam Integer id,
            @RequestParam String status,
            @RequestParam Integer auditId) {
        return financeServiceFeign.auditIncome(id, status, auditId);
    }

    /**
     * 批量审核收入记录
/**
     * @param params 包含 ids, status, auditId 的参数
     * @return 审核结果
     */
    @PostMapping("/batch-audit")
    @Operation(summary = "批量审核收入记录")
    public ResponseResult batchAudit(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.batchAuditIncome(params);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除收入记录")
    public ResponseResult delete(@RequestParam Integer id) {
        return financeServiceFeign.deleteIncome(id);
    }

    /**
     * 获取当前公司收入类别
/**
     * @return 类别列表
     */
    @PostMapping("/types")
    @Operation(summary = "获取所有收入类别")
    public ResponseResult getTypes() {
        return financeServiceFeign.getIncomeTypes();
    }
}
