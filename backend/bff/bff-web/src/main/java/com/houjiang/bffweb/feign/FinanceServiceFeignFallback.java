package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 财务服务Feign降级处理
/**
 * @author Amos
 */
@Slf4j
@Component
public class FinanceServiceFeignFallback implements FinanceServiceFeign {

    private static final String ERROR_MSG = "财务服务暂时不可用";

    /** ================= Dashboard ================= */

    @Override
    public ResponseResult getDashboardStatistics(Integer year) {
        log.error("调用finance服务getDashboardStatistics接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getDashboardCashFlow(Map<String, Object> param) {
        log.error("调用finance服务getDashboardCashFlow接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getDashboardData() {
        log.error("调用finance服务getDashboardData接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    /** ================= 收入 ================= */

    @Override
    public ResponseResult getIncomeList(Map<String, Object> param) {
        log.error("调用finance服务getIncomeList接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult createIncome(Map<String, Object> income) {
        log.error("调用finance服务createIncome接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult importIncome(Map<String, Object> income) {
        log.error("调用finance服务importIncome接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult updateIncome(Map<String, Object> income) {
        log.error("调用finance服务updateIncome接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult deleteIncome(Integer id) {
        log.error("调用finance服务deleteIncome接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getIncomeTypes() {
        log.error("调用finance服务getIncomeTypes接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult auditIncome(Integer id, String status, Integer auditId) {
        log.error("调用finance服务auditIncome接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult batchAuditIncome(Map<String, Object> params) {
        log.error("调用finance服务batchAuditIncome接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    /** ================= 支出 ================= */

    @Override
    public ResponseResult getSpendingList(Map<String, Object> param) {
        log.error("调用finance服务getSpendingList接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult createSpending(Map<String, Object> spending) {
        log.error("调用finance服务createSpending接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult importSpending(Map<String, Object> spending) {
        log.error("调用finance服务importSpending接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult updateSpending(Map<String, Object> spending) {
        log.error("调用finance服务updateSpending接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult deleteSpending(Integer id) {
        log.error("调用finance服务deleteSpending接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult auditSpending(Integer id, String status, Integer auditId) {
        log.error("调用finance服务auditSpending接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult batchAuditSpending(Map<String, Object> params) {
        log.error("调用finance服务batchAuditSpending接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getSpendingTypes() {
        log.error("调用finance服务getSpendingTypes接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    /** ================= 收入类别 ================= */

    @Override
    public ResponseResult getIncomeTypeList(Map<String, Object> param) {
        log.error("调用finance服务getIncomeTypeList接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getAllIncomeTypes() {
        log.error("调用finance服务getAllIncomeTypes接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult createIncomeType(Map<String, Object> type) {
        log.error("调用finance服务createIncomeType接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult updateIncomeType(Map<String, Object> type) {
        log.error("调用finance服务updateIncomeType接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult deleteIncomeType(Integer id) {
        log.error("调用finance服务deleteIncomeType接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    /** ================= 支出类别 ================= */

    @Override
    public ResponseResult getSpendingTypeList(Map<String, Object> param) {
        log.error("调用finance服务getSpendingTypeList接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getAllSpendingTypes() {
        log.error("调用finance服务getAllSpendingTypes接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult createSpendingType(Map<String, Object> type) {
        log.error("调用finance服务createSpendingType接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult updateSpendingType(Map<String, Object> type) {
        log.error("调用finance服务updateSpendingType接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult deleteSpendingType(Integer id) {
        log.error("调用finance服务deleteSpendingType接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    /** ================= 账户科目 ================= */

    @Override
    public ResponseResult getSubjectsList(Map<String, Object> param) {
        log.error("调用finance服务getSubjectsList接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getAllSubjects() {
        log.error("调用finance服务getAllSubjects接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getSubjectsDetail(Integer id) {
        log.error("调用finance服务getSubjectsDetail接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult createSubjects(Map<String, Object> subjects) {
        log.error("调用finance服务createSubjects接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult batchCreateSubjects(Map<String, Object> subjects) {
        log.error("调用finance服务batchCreateSubjects接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult updateSubjects(Map<String, Object> subjects) {
        log.error("调用finance服务updateSubjects接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult deleteSubjects(Integer id) {
        log.error("调用finance服务deleteSubjects接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult updateSubjectsStatus(Integer id, Integer status) {
        log.error("调用finance服务updateSubjectsStatus接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    /** ================= 统计相关 ================= */

    @Override
    public ResponseResult getStatisticsByPeriod() {
        log.error("调用finance服务getStatisticsByPeriod接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getStatisticsByCategory() {
        log.error("调用finance服务getStatisticsByCategory接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getStatisticsByStatus() {
        log.error("调用finance服务getStatisticsByStatus接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getYearlyMonthlyStatistics(String years) {
        log.error("调用finance服务getYearlyMonthlyStatistics接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    /** ================= 资金管理 ================= */

    @Override
    public ResponseResult getReceivableList(Map<String, Object> param) {
        log.error("调用finance服务getReceivableList接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getReceivableDetail(Map<String, Object> param) {
        log.error("调用finance服务getReceivableDetail接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getReceivableStats() {
        log.error("调用finance服务getReceivableStats接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }
    
    @Override
    public ResponseResult getPayableList(Map<String, Object> param) {
        log.error("调用finance服务getPayableList接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }
    
    @Override
    public ResponseResult getPayableStats(Map<String, Object> param) {
        log.error("调用finance服务getPayableStats接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }
    
    @Override
    public ResponseResult createPayable(Map<String, Object> data) {
        log.error("调用finance服务createPayable接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }
    
    @Override
    public ResponseResult deletePayableByCode(String code) {
        log.error("调用finance服务deletePayableByCode接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override
    public ResponseResult getReportData(Map<String, Object> param) {
        log.error("调用finance服务getReportData接口失败，触发降级");
        return ResponseResult.error(ERROR_MSG);
    }

    @Override public ResponseResult getInvoiceList(Map<String, Object> params) { return down("getInvoiceList"); }
    @Override public ResponseResult getInvoiceDetail(Map<String, Object> params) { return down("getInvoiceDetail"); }
    @Override public ResponseResult getInvoiceStats(Map<String, Object> params) { return down("getInvoiceStats"); }
    @Override public ResponseResult createInvoice(Map<String, Object> data) { return down("createInvoice"); }
    @Override public ResponseResult addInvoice(Map<String, Object> data) { return down("addInvoice"); }
    @Override public ResponseResult updateInvoice(Map<String, Object> data) { return down("updateInvoice"); }
    @Override public ResponseResult cancelInvoice(Map<String, Object> params) { return down("cancelInvoice"); }
    @Override public ResponseResult printInvoice(Map<String, Object> params) { return down("printInvoice"); }
    @Override public ResponseResult getTaxList(Map<String, Object> params) { return down("getTaxList"); }
    @Override public ResponseResult getTaxPage(Map<String, Object> params) { return down("getTaxPage"); }
    @Override public ResponseResult getTaxDetail(Map<String, Object> params) { return down("getTaxDetail"); }
    @Override public ResponseResult createTax(Map<String, Object> data) { return down("createTax"); }
    @Override public ResponseResult addTax(Map<String, Object> data) { return down("addTax"); }
    @Override public ResponseResult updateTax(Map<String, Object> data) { return down("updateTax"); }
    @Override public ResponseResult deleteTax(Map<String, Object> params) { return down("deleteTax"); }

    private ResponseResult down(String method) {
        log.error("调用finance服务{}接口失败，触发降级", method);
        return ResponseResult.error(ERROR_MSG);
    }
}
