package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 财务服务 Feign 客户端。
 * <p>
 * 财务作为 Mall PF4J 插件运行，所有调用固定进入 Mall 的插件分发通道。
 *
 * @author Amos
 */
@FeignClient(value = "finance", url = "${feign.finance.url:}", path = "/plugin/finance", fallback = FinanceServiceFeignFallback.class)
public interface FinanceServiceFeign {

    // ================= Dashboard相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/dashboard/statistics")
    ResponseResult getDashboardStatistics(@RequestParam(required = false) Integer year);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/dashboard/cashflow")
    ResponseResult getDashboardCashFlow(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/dashboard/data")
    ResponseResult getDashboardData();
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/dashboard/statistics/period")
    ResponseResult getStatisticsByPeriod();
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/dashboard/statistics/category")
    ResponseResult getStatisticsByCategory();
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/dashboard/statistics/status")
    ResponseResult getStatisticsByStatus();
    
    /**
     * TODO 补充方法说明
/**
     * @param false TODO
     * @return TODO
     */
    @GetMapping("/dashboard/statistics/yearly-monthly")
    ResponseResult getYearlyMonthlyStatistics(@RequestParam(required = false) String years);

    // ================= 收入相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/income/list")
    ResponseResult getIncomeList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param income TODO
     * @return TODO
     */
    /** 智能入库只允许以 unaudited 状态调用此新增接口。 */
    @PostMapping("/income/create")
    ResponseResult createIncome(@RequestBody Map<String, Object> income);

    /** Returns the actual generated income primary key in data.id. */
    @PostMapping("/income/import")
    ResponseResult importIncome(@RequestBody Map<String, Object> income);
    
    /**
     * TODO 补充方法说明
/**
     * @param income TODO
     * @return TODO
     */
    @PostMapping("/income/update")
    ResponseResult updateIncome(@RequestBody Map<String, Object> income);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/income/delete")
    ResponseResult deleteIncome(@RequestParam("id") Integer id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/income/types")
    ResponseResult getIncomeTypes();
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/income/audit")
    ResponseResult auditIncome(@RequestParam("id") Integer id, @RequestParam("status") String status, @RequestParam("auditId") Integer auditId);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/income/batch-audit")
    ResponseResult batchAuditIncome(@RequestBody Map<String, Object> params);

    // ================= 支出相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/spending/list")
    ResponseResult getSpendingList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param spending TODO
     * @return TODO
     */
    /** 智能入库只允许以 unaudited 状态调用此新增接口。 */
    @PostMapping("/spending/create")
    ResponseResult createSpending(@RequestBody Map<String, Object> spending);

    /** Returns the actual generated spending primary key in data.id. */
    @PostMapping("/spending/import")
    ResponseResult importSpending(@RequestBody Map<String, Object> spending);
    
    /**
     * TODO 补充方法说明
/**
     * @param spending TODO
     * @return TODO
     */
    @PostMapping("/spending/update")
    ResponseResult updateSpending(@RequestBody Map<String, Object> spending);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/spending/delete")
    ResponseResult deleteSpending(@RequestParam("id") Integer id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/spending/audit")
    ResponseResult auditSpending(@RequestParam("id") Integer id, @RequestParam("status") String status, @RequestParam("auditId") Integer auditId);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/spending/batch-audit")
    ResponseResult batchAuditSpending(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/spending/types")
    ResponseResult getSpendingTypes();

    // ================= 收入类别接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/income-type/list")
    ResponseResult getIncomeTypeList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/income-type/all")
    ResponseResult getAllIncomeTypes();
    
    /**
     * TODO 补充方法说明
/**
     * @param type TODO
     * @return TODO
     */
    @PostMapping("/income-type/create")
    ResponseResult createIncomeType(@RequestBody Map<String, Object> type);
    
    /**
     * TODO 补充方法说明
/**
     * @param type TODO
     * @return TODO
     */
    @PostMapping("/income-type/update")
    ResponseResult updateIncomeType(@RequestBody Map<String, Object> type);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/income-type/delete")
    ResponseResult deleteIncomeType(@RequestParam("id") Integer id);

    // ================= 支出类别接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/spending-type/list")
    ResponseResult getSpendingTypeList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/spending-type/all")
    ResponseResult getAllSpendingTypes();
    
    /**
     * TODO 补充方法说明
/**
     * @param type TODO
     * @return TODO
     */
    @PostMapping("/spending-type/create")
    ResponseResult createSpendingType(@RequestBody Map<String, Object> type);
    
    /**
     * TODO 补充方法说明
/**
     * @param type TODO
     * @return TODO
     */
    @PostMapping("/spending-type/update")
    ResponseResult updateSpendingType(@RequestBody Map<String, Object> type);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/spending-type/delete")
    ResponseResult deleteSpendingType(@RequestParam("id") Integer id);

    // ================= 账户科目接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/subjects/list")
    ResponseResult getSubjectsList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/subjects/all")
    ResponseResult getAllSubjects();
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/subjects/detail")
    ResponseResult getSubjectsDetail(@RequestParam("id") Integer id);
    
    /**
     * TODO 补充方法说明
/**
     * @param subjects TODO
     * @return TODO
     */
    @PostMapping("/subjects/create")
    ResponseResult createSubjects(@RequestBody Map<String, Object> subjects);

    @PostMapping("/finance/subjects/batch-create")
    ResponseResult batchCreateSubjects(@RequestBody Map<String, Object> subjects);
    
    /**
     * TODO 补充方法说明
/**
     * @param subjects TODO
     * @return TODO
     */
    @PostMapping("/subjects/update")
    ResponseResult updateSubjects(@RequestBody Map<String, Object> subjects);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/subjects/delete")
    ResponseResult deleteSubjects(@RequestParam("id") Integer id);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/subjects/update-status")
    ResponseResult updateSubjectsStatus(@RequestParam("id") Integer id, @RequestParam("status") Integer status);

    // ================= 资金管理接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/fund/receivable/list")
    ResponseResult getReceivableList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/fund/receivable/detail")
    ResponseResult getReceivableDetail(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/fund/receivable/stats")
    ResponseResult getReceivableStats();
    
    // 应付账款接口
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/fund/payable/list")
    ResponseResult getPayableList(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/fund/payable/stats")
    ResponseResult getPayableStats(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/fund/payable/create")
    ResponseResult createPayable(@RequestBody Map<String, Object> data);
    
    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/fund/payable/delete-by-code/{code}")
    ResponseResult deletePayableByCode(@PathVariable("code") String code);

    // ================= 财务报表接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/report/data")
    ResponseResult getReportData(@RequestBody Map<String, Object> param);

    // ================= 发票及税务（finance 服务） =================
    @PostMapping("/invoice/list")
    ResponseResult getInvoiceList(@RequestBody Map<String, Object> params);
    @PostMapping("/invoice/detail")
    ResponseResult getInvoiceDetail(@RequestBody Map<String, Object> params);
    @PostMapping("/invoice/stats")
    ResponseResult getInvoiceStats(@RequestBody(required = false) Map<String, Object> params);
    /** status 使用 finance 的整数状态：0 待开票、1 已开票、2 已作废。 */
    @PostMapping("/invoice/create")
    ResponseResult createInvoice(@RequestBody Map<String, Object> data);
    @PostMapping("/invoice/add")
    ResponseResult addInvoice(@RequestBody Map<String, Object> data);
    @PostMapping("/invoice/update")
    ResponseResult updateInvoice(@RequestBody Map<String, Object> data);
    @PostMapping("/invoice/cancel")
    ResponseResult cancelInvoice(@RequestBody Map<String, Object> params);
    @PostMapping("/invoice/print")
    ResponseResult printInvoice(@RequestBody Map<String, Object> params);
    @PostMapping("/tax/list")
    ResponseResult getTaxList(@RequestBody(required = false) Map<String, Object> params);
    @PostMapping("/tax/page")
    ResponseResult getTaxPage(@RequestBody(required = false) Map<String, Object> params);
    @PostMapping("/tax/detail")
    ResponseResult getTaxDetail(@RequestBody Map<String, Object> params);
    @PostMapping("/tax/create")
    ResponseResult createTax(@RequestBody Map<String, Object> data);
    @PostMapping("/tax/add")
    ResponseResult addTax(@RequestBody Map<String, Object> data);
    @PostMapping("/tax/update")
    ResponseResult updateTax(@RequestBody Map<String, Object> data);
    @PostMapping("/tax/delete")
    ResponseResult deleteTax(@RequestBody Map<String, Object> params);
}
