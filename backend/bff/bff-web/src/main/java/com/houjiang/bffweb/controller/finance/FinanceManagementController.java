package com.houjiang.bffweb.controller.finance;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.feign.OaServiceFeign;
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
 * 财务中心管理控制器（BFF层）
/**
 * 统一管理应付、发票、收款、报表、分析等功能
 * 从CRM模块迁移到财务中心
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/finance")
@Tag(name = "财务中心管理", description = "财务中心管理相关接口")
public class FinanceManagementController {

    @Autowired
    private OaServiceFeign oaServiceFeign;

    @Autowired
    private MallServiceFeign mallServiceFeign;

    @Autowired
    private FinanceServiceFeign financeServiceFeign;

    /** ================= 应付管理 ================= */
    
    @PostMapping("/payable/list")
    @Operation(summary = "获取应付列表")
    public ResponseResult getPayableList(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取应付列表，请求参数: {}", params);
            // 调用OA服务获取付款计划列表（即应付列表）
            ResponseResult response = oaServiceFeign.getPaymentPlanList(params);
            log.info("OA服务返回结果: code={}, message={}, data={}", 
                response != null ? response.getCode() : "null",
                response != null ? response.getMessage() : "null",
                response != null && response.getData() != null ? response.getData().toString() : "null");
            
            if (response != null && (response.getCode() == 1 || response.getCode() == 200)) {
                return response;
            } else {
                log.error("调用OA服务获取付款计划列表失败: code={}, message={}", 
                    response != null ? response.getCode() : "null",
                    response != null ? response.getMessage() : "null");
                Map<String, Object> result = new HashMap<>();
                result.put("list", new java.util.ArrayList<>());
                result.put("total", 0);
                return ResponseResult.success(result);
            }
        } catch (Exception e) {
            log.error("获取应付列表失败", e);
            return ResponseResult.error("获取应付列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/payable/stats")
    @Operation(summary = "获取应付统计")
    public ResponseResult getPayableStats() {
        try {
            // 调用OA服务获取付款计划统计
            ResponseResult response = oaServiceFeign.getPaymentPlanStats();
            if (response != null && response.getCode() == 1) {
                return response;
            } else {
                log.error("调用OA服务获取付款计划统计失败: {}", response);
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalAmount", 0);
                stats.put("paidAmount", 0);
                stats.put("unpaidAmount", 0);
                stats.put("supplierCount", 0);
                return ResponseResult.success(stats);
            }
        } catch (Exception e) {
            log.error("获取应付统计失败", e);
            return ResponseResult.error("获取应付统计失败: " + e.getMessage());
        }
    }

    @PostMapping("/payable/unrecorded-paid")
    @Operation(summary = "获取已打款但未记录的付款计划")
    public ResponseResult getUnrecordedPaidPlans(@RequestBody Map<String, Object> params) {
        try {
            log.info("获取已打未记录付款计划，请求参数: {}", params);
            // 调用OA服务获取已打未记录付款计划
            ResponseResult response = oaServiceFeign.getUnrecordedPaidPlans(params);
            if (response != null && (response.getCode() == 1 || response.getCode() == 200)) {
                return response;
            } else {
                log.error("调用OA服务获取已打未记录付款计划失败: code={}, message={}", 
                    response != null ? response.getCode() : "null",
                    response != null ? response.getMessage() : "null");
                Map<String, Object> result = new HashMap<>();
                result.put("list", new java.util.ArrayList<>());
                result.put("total", 0);
                return ResponseResult.success(result);
            }
        } catch (Exception e) {
            log.error("获取已打未记录付款计划失败", e);
            return ResponseResult.error("获取已打未记录付款计划失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/payable/batch-generate-finance-records")
    @Operation(summary = "批量生成财务记录")
    public ResponseResult batchGenerateFinanceRecords(@RequestBody Map<String, Object> params) {
        try {
            log.info("批量生成财务记录，请求参数: {}", params);
            // 调用OA服务批量生成财务记录
            ResponseResult response = oaServiceFeign.batchGenerateFinanceRecords(params);
            if (response != null && (response.getCode() == 1 || response.getCode() == 200)) {
                return response;
            } else {
                log.error("调用OA服务批量生成财务记录失败: code={}, message={}", 
                    response != null ? response.getCode() : "null",
                    response != null ? response.getMessage() : "null");
                return ResponseResult.error(response != null ? response.getMessage() : "服务异常");
            }
        } catch (Exception e) {
            log.error("批量生成财务记录失败", e);
            return ResponseResult.error("批量生成财务记录失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/payable/detail")
    @Operation(summary = "获取应付详情")
    public ResponseResult getPayableDetail(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new HashMap<>());
    }
    
    @PostMapping("/payable/create")
    @Operation(summary = "新增应付")
    public ResponseResult createPayable(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/payable/update")
    @Operation(summary = "更新应付")
    public ResponseResult updatePayable(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/payable/delete")
    @Operation(summary = "删除应付")
    public ResponseResult deletePayable(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/payable/pay")
    @Operation(summary = "付款")
    public ResponseResult pay(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }

    /** ================= 发票管理 ================= */
    
    @PostMapping("/invoice/list")
    @Operation(summary = "获取发票列表")
    public ResponseResult getInvoiceList(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.getInvoiceList(params == null ? java.util.Collections.emptyMap() : params);
    }
    
    @PostMapping("/invoice/stats")
    @Operation(summary = "获取发票统计")
    public ResponseResult getInvoiceStats(@RequestBody(required = false) Map<String, Object> params) {
        return financeServiceFeign.getInvoiceStats(params == null ? java.util.Collections.emptyMap() : params);
    }
    
    @PostMapping("/invoice/detail")
    @Operation(summary = "获取发票详情")
    public ResponseResult getInvoiceDetail(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.getInvoiceDetail(params == null ? java.util.Collections.emptyMap() : params);
    }
    
    @PostMapping("/invoice/create")
    @Operation(summary = "申请开票")
    public ResponseResult createInvoice(@RequestBody Map<String, Object> data) {
        return financeServiceFeign.createInvoice(data == null ? java.util.Collections.emptyMap() : data);
    }

    /** 兼容前端“新增发票”动作，统一调用 finance 服务。 */
    @PostMapping("/invoice/add")
    @Operation(summary = "新增发票")
    public ResponseResult addInvoice(@RequestBody Map<String, Object> data) {
        return financeServiceFeign.addInvoice(data == null ? java.util.Collections.emptyMap() : data);
    }
    
    @PostMapping("/invoice/update")
    @Operation(summary = "更新发票")
    public ResponseResult updateInvoice(@RequestBody Map<String, Object> data) {
        return financeServiceFeign.updateInvoice(data == null ? java.util.Collections.emptyMap() : data);
    }
    
    @PostMapping("/invoice/cancel")
    @Operation(summary = "作废发票")
    public ResponseResult cancelInvoice(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.cancelInvoice(params == null ? java.util.Collections.emptyMap() : params);
    }
    
    @PostMapping("/invoice/print")
    @Operation(summary = "打印发票")
    public ResponseResult printInvoice(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.printInvoice(params == null ? java.util.Collections.emptyMap() : params);
    }

    /**
     * ================= 收款管理（商城后台 finance 复用）=================
/**
     * Pass 2 修复：原 /payment/list /payment/confirm /payment/detail 为占位空平数据，
     * 现按 type 字段路由到 paas/mall finance 真实接口。
     * 映射规则：
     * type='recharge'  → mall/finance/income/list （充值记录）
     * type='withdraw' / 无 type → mall/finance/withdraw/list （提现与余额明细默认走提现表）
     * confirm  → mall/finance/withdraw/audit （提现审核 / 转账）
     * detail   → mall/finance/withdraw/detail
     */

    @PostMapping("/payment/list")
    @Operation(summary = "获取收款列表（商城后台）")
    public ResponseResult getPaymentList(@RequestBody Map<String, Object> params) {
        try {
            String type = params != null ? (String) params.get("type") : null;
            if ("recharge".equals(type)) {
                return mallServiceFeign.getFinanceIncomeList(params);
            }
            return mallServiceFeign.getWithdrawList(params);
        } catch (Exception e) {
            log.error("获取收款列表失败", e);
            return ResponseResult.error("获取收款列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/payment/stats")
    @Operation(summary = "获取收款统计")
    public ResponseResult getPaymentStats() {
        try {
            return mallServiceFeign.getWithdrawStats(Map.of("accountType", "shop"));
        } catch (Exception e) {
            log.error("获取收款统计失败", e);
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAmount", 0);
            stats.put("monthAmount", 0);
            stats.put("pendingCount", 0);
            stats.put("confirmedCount", 0);
            return ResponseResult.success(stats);
        }
    }
    
    @PostMapping("/payment/detail")
    @Operation(summary = "获取收款详情 ")
    public ResponseResult getPaymentDetail(@RequestBody Map<String, Object> params) {
        try {
            return mallServiceFeign.getWithdrawDetail(params);
        } catch (Exception e) {
            log.error("获取收款详情失败", e);
            return ResponseResult.error("获取收款详情失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/payment/create")
    @Operation(summary = "新增收款")
    public ResponseResult createPayment(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/payment/confirm")
    @Operation(summary = "确认收款（提现审核 / 转账）")
    public ResponseResult confirmPayment(@RequestBody Map<String, Object> params) {
        try {
            // 前端 apiWithdrawAudit 传 audit_status；apiWithdrawTransfer 传 transfer_status；
            // mall/finance/withdraw/audit 期望 result=approved/rejected 字段。
            if (params != null && !params.containsKey("result")) {
                Object auditStatus = params.get("audit_status");
                Object agree = params.get("agree");
                Object transferStatus = params.get("transfer_status");
                if (auditStatus != null) {
                    params.put("result", "1".equals(auditStatus.toString()) ? "approved" : "rejected");
                } else if (agree != null) {
                    params.put("result", "1".equals(agree.toString()) ? "approved" : "rejected");
                } else if (transferStatus != null) {
                    // 商家提现转账完成与否均作为 approved 进入后续物流
                    params.put("result", "approved");
                }
                // 备注字段判以上多个同义传入名
                Object remark = params.get("audit_content");
                if (remark == null) remark = params.get("transfer_content");
                if (remark == null) remark = params.get("content");
                if (remark != null) {
                    params.put("remark", remark);
                }
            }
            return mallServiceFeign.auditWithdraw(params);
        } catch (Exception e) {
            log.error("确认收款失败", e);
            return ResponseResult.error("确认收款失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/payment/cancel")
    @Operation(summary = "作废收款")
    public ResponseResult cancelPayment(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(true);
    }

    /** ================= 财务报表 ================= */
    
    @PostMapping("/report/generate")
    @Operation(summary = "生成报表")
    public ResponseResult generateReport(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new HashMap<>());
    }
    
    @PostMapping("/report/export")
    @Operation(summary = "导出报表")
    public ResponseResult exportReport(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/report/balance-sheet")
    @Operation(summary = "获取资产负债表")
    public ResponseResult getBalanceSheet(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/report/profit-statement")
    @Operation(summary = "获取利润表")
    public ResponseResult getProfitStatement(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/report/cash-flow")
    @Operation(summary = "获取现金流量表")
    public ResponseResult getCashFlow(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/report/subject-balance")
    @Operation(summary = "获取科目余额表")
    public ResponseResult getSubjectBalance(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/report/income-spending")
    @Operation(summary = "获取收支报表")
    public ResponseResult getIncomeSpending(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("incomeTotal", 0);
        result.put("spendingTotal", 0);
        result.put("data", new java.util.ArrayList<>());
        return ResponseResult.success(result);
    }

    /** ================= 财务分析 ================= */
    
    @PostMapping("/analysis/metrics")
    @Operation(summary = "获取核心指标")
    public ResponseResult getMetrics(@RequestBody Map<String, Object> params) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("revenue", 0);
        metrics.put("revenueGrowth", 0);
        metrics.put("netProfit", 0);
        metrics.put("profitGrowth", 0);
        metrics.put("grossMargin", 0);
        metrics.put("netMargin", 0);
        return ResponseResult.success(metrics);
    }
    
    @PostMapping("/analysis/revenue-trend")
    @Operation(summary = "获取收入趋势")
    public ResponseResult getRevenueTrend(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/analysis/spending-structure")
    @Operation(summary = "获取支出结构")
    public ResponseResult getSpendingStructure(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/analysis/cash-flow")
    @Operation(summary = "获取现金流分析")
    public ResponseResult getCashFlowAnalysis(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/analysis/ratio")
    @Operation(summary = "获取财务比率")
    public ResponseResult getRatioAnalysis(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/analysis/comparison")
    @Operation(summary = "获取同比环比数据")
    public ResponseResult getComparison(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/analysis/export")
    @Operation(summary = "导出分析报告")
    public ResponseResult exportAnalysis(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(true);
    }

    /** ================= 成本管理 ================= */
    
    @PostMapping("/cost/list")
    @Operation(summary = "获取成本列表")
    public ResponseResult getCostList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/cost/detail")
    @Operation(summary = "获取成本详情")
    public ResponseResult getCostDetail(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new HashMap<>());
    }
    
    @PostMapping("/cost/create")
    @Operation(summary = "新增成本")
    public ResponseResult createCost(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/cost/update")
    @Operation(summary = "更新成本")
    public ResponseResult updateCost(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/cost/delete")
    @Operation(summary = "删除成本")
    public ResponseResult deleteCost(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(true);
    }

    /** ================= 预算管理 ================= */
    
    @PostMapping("/budget/list")
    @Operation(summary = "获取预算列表")
    public ResponseResult getBudgetList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/budget/detail")
    @Operation(summary = "获取预算详情")
    public ResponseResult getBudgetDetail(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new HashMap<>());
    }
    
    @PostMapping("/budget/create")
    @Operation(summary = "新增预算")
    public ResponseResult createBudget(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/budget/update")
    @Operation(summary = "更新预算")
    public ResponseResult updateBudget(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/budget/delete")
    @Operation(summary = "删除预算")
    public ResponseResult deleteBudget(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(true);
    }

    /** ================= 税务管理 ================= */
    
    @PostMapping({"/tax/list", "/tax/page"})
    @Operation(summary = "获取税务列表")
    public ResponseResult getTaxList(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.getTaxList(params == null ? java.util.Collections.emptyMap() : params);
    }
    
    @PostMapping("/tax/detail")
    @Operation(summary = "获取税务详情")
    public ResponseResult getTaxDetail(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.getTaxDetail(params == null ? java.util.Collections.emptyMap() : params);
    }
    
    @PostMapping("/tax/create")
    @Operation(summary = "新增税务记录")
    public ResponseResult createTax(@RequestBody Map<String, Object> data) {
        return financeServiceFeign.createTax(data == null ? java.util.Collections.emptyMap() : data);
    }

    /** 兼容前端“新增税务”动作，统一调用 finance 服务。 */
    @PostMapping("/tax/add")
    @Operation(summary = "新增税务记录")
    public ResponseResult addTax(@RequestBody Map<String, Object> data) {
        return financeServiceFeign.addTax(data == null ? java.util.Collections.emptyMap() : data);
    }
    
    @PostMapping("/tax/update")
    @Operation(summary = "更新税务记录")
    public ResponseResult updateTax(@RequestBody Map<String, Object> data) {
        return financeServiceFeign.updateTax(data == null ? java.util.Collections.emptyMap() : data);
    }

    @PostMapping("/tax/delete")
    @Operation(summary = "删除税务记录")
    public ResponseResult deleteTax(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.deleteTax(params == null ? java.util.Collections.emptyMap() : params);
    }

    /** ================= 财务审批 ================= */
    
    @PostMapping("/approval/list")
    @Operation(summary = "获取审批列表")
    public ResponseResult getApprovalList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/approval/approve")
    @Operation(summary = "审批")
    public ResponseResult approve(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }

    /** ================= 财务对账 ================= */
    
    @PostMapping("/reconcile/list")
    @Operation(summary = "获取对账列表")
    public ResponseResult getReconcileList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/reconcile/submit")
    @Operation(summary = "对账")
    public ResponseResult reconcile(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }

    /** ================= 财务提醒 ================= */
    
    @PostMapping("/remind/list")
    @Operation(summary = "获取提醒列表")
    public ResponseResult getRemindList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/remind/read")
    @Operation(summary = "标记已读")
    public ResponseResult markRead(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(true);
    }

    /** ================= 资金核销 ================= */
    
    @PostMapping("/fund/writeoff/list")
    @Operation(summary = "获取核销记录列表")
    public ResponseResult getWriteoffList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/fund/writeoff/detail")
    @Operation(summary = "获取核销详情")
    public ResponseResult getWriteoffDetail(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new HashMap<>());
    }
    
    @PostMapping("/fund/writeoff/create")
    @Operation(summary = "创建核销记录")
    public ResponseResult createWriteoff(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/fund/writeoff/delete")
    @Operation(summary = "删除核销记录")
    public ResponseResult deleteWriteoff(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(true);
    }

    /** ================= 资金收据 ================= */
    
    @PostMapping("/fund/receipt/list")
    @Operation(summary = "获取收据列表")
    public ResponseResult getReceiptList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        return ResponseResult.success(result);
    }
    
    @PostMapping("/fund/receipt/detail")
    @Operation(summary = "获取收据详情")
    public ResponseResult getReceiptDetail(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(new HashMap<>());
    }
    
    @PostMapping("/fund/receipt/create")
    @Operation(summary = "创建收据")
    public ResponseResult createReceipt(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/fund/receipt/update")
    @Operation(summary = "更新收据")
    public ResponseResult updateReceipt(@RequestBody Map<String, Object> data) {
        return ResponseResult.success(true);
    }
    
    @PostMapping("/fund/receipt/delete")
    @Operation(summary = "删除收据")
    public ResponseResult deleteReceipt(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(true);
    }

    /** ================= 财务报表 ================= */
    
    @PostMapping("/report/data")
    @Operation(summary = "获取报表数据")
    public ResponseResult getReportData(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("income", 0);
        result.put("expense", 0);
        result.put("profit", 0);
        result.put("chartData", new java.util.ArrayList<>());
        return ResponseResult.success(result);
    }
}
