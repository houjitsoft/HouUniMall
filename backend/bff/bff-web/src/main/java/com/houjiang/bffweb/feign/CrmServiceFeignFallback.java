package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Crm服务Feign降级处理
/**
 * 
 * @description 当Crm服务不可用时提供降级响应
 * @author Amos
 * @date 2024-12-29
 */
@Component
public class CrmServiceFeignFallback implements CrmServiceFeign {

    private static final Logger log = LoggerFactory.getLogger(CrmServiceFeignFallback.class);

    @Override
    public ResponseResult getCrmStatistics(Map<String, Object> param) {
        log.error("获取CRM统计数据失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "获取CRM统计数据失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult queryCrmList(Map<String, Object> param) {
        log.error("查询CRM列表失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询CRM列表失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult getCrmDetail(Map<String, Object> param) {
        log.error("查询CRM详情失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询CRM详情失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult createCrm(Map<String, Object> param) {
        log.error("创建CRM记录失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "创建CRM记录失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult updateCrm(Map<String, Object> param) {
        log.error("更新CRM记录失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "更新CRM记录失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult deleteCrm(Map<String, Object> param) {
        log.error("删除CRM记录失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "删除CRM记录失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult pauseCrm(Map<String, Object> param) {
        log.error("暂停CRM记录失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "暂停CRM记录失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult resumeCrm(Map<String, Object> param) {
        log.error("恢复CRM记录失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "恢复CRM记录失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult copyCrm(Map<String, Object> param) {
        log.error("复制CRM记录失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "复制CRM记录失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult getCrmTypes() {
        log.error("获取CRM类型列表失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "获取CRM类型列表失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult batchUpdateStatus(Map<String, Object> param) {
        log.error("批量更新CRM状态失败：CRM服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "批量更新CRM状态失败：CRM服务暂时不可用，请检查CRM服务是否正常启动或网络连接是否正常");
    }
    
    /** ================= 客户管理降级 ================= */
    
    @Override
    public ResponseResult getCustomerPage(Map<String, Object> params) {
        log.error("分页查询客户列表失败：CRM服务不可用");
        return ResponseResult.fail(503, "客户服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getCustomerDetail(Integer id) {
        log.error("获取客户详情失败：CRM服务不可用, 客户ID: {}", id);
        return ResponseResult.fail(503, "客户服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult addCustomer(Map<String, Object> data) {
        log.error("新增客户失败：CRM服务不可用");
        return ResponseResult.fail(503, "客户服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateCustomer(Map<String, Object> data) {
        log.error("更新客户失败：CRM服务不可用");
        return ResponseResult.fail(503, "客户服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteCustomer(Integer id) {
        log.error("删除客户失败：CRM服务不可用, 客户ID: {}", id);
        return ResponseResult.fail(503, "客户服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCustomerTagPage(Map<String, Object> params) {
        log.error("客户标签分页失败");
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult addCustomerTag(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult updateCustomerTag(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult deleteCustomerTag(Integer id) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getCustomerStatistics(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getCustomerAssignPage(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult assignCustomer(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult transferCustomer(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getAssignHistory(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getCustomerFollowPage(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult addCustomerFollow(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult updateCustomerFollow(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult deleteCustomerFollow(Integer id) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getCustomerList(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult searchCustomer(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getCustomerProfile(Integer id) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getCustomerValuePage(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult calculateCustomerValue(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getCustomerLifecyclePage(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getLifecycleTimeline(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult changeLifecycleStage(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getLifecycleStats(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getCustomerRelation(Integer id) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult addCustomerRelation(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult deleteCustomerRelation(Integer id) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult getCustomerMapData(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }

    @Override
    public ResponseResult mergeCustomers(Map<String, Object> params) {
        return ResponseResult.fail(503, "服务不可用");
    }
    
    /** ================= 联系人管理降级 ================= */
    
    @Override
    public ResponseResult getContactsPage(Map<String, Object> params) {
        log.error("分页查询联系人列表失败：CRM服务不可用");
        return ResponseResult.fail(503, "联系人服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult getContactsDetail(Integer id) {
        log.error("获取联系人详情失败：CRM服务不可用, 联系人ID: {}", id);
        return ResponseResult.fail(503, "联系人服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult addContacts(Map<String, Object> data) {
        log.error("新增联系人失败：CRM服务不可用");
        return ResponseResult.fail(503, "联系人服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult updateContacts(Map<String, Object> data) {
        log.error("更新联系人失败：CRM服务不可用");
        return ResponseResult.fail(503, "联系人服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult deleteContacts(Integer id) {
        log.error("删除联系人失败：CRM服务不可用, 联系人ID: {}", id);
        return ResponseResult.fail(503, "联系人服务暂时不可用，请稍后重试");
    }
    @Override
    public ResponseResult getFinanceApprovalPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approveFinance(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFinanceBudgetPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addFinanceBudget(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFinanceCostPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult calculateCost(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFinanceFundPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addFundRecord(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFinanceReconcilePage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult reconcileAccount(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFinanceRemindPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addFinanceRemind(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFinanceSetting(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateFinanceSetting(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFinanceTaxPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addTaxRecord(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override public ResponseResult getFinanceTaxDetail(Map<String,Object> p) { return down(); }
    @Override public ResponseResult updateTaxRecord(Map<String,Object> p) { return down(); }
    @Override public ResponseResult deleteTaxRecord(Map<String,Object> p) { return down(); }
    @Override public ResponseResult getFinanceInvoiceList(Map<String,Object> p) { return down(); }
    @Override public ResponseResult getFinanceInvoiceDetail(Map<String,Object> p) { return down(); }
    @Override public ResponseResult getFinanceInvoiceStats(Map<String,Object> p) { return down(); }
    @Override public ResponseResult createFinanceInvoice(Map<String,Object> p) { return down(); }
    @Override public ResponseResult addFinanceInvoice(Map<String,Object> p) { return down(); }
    @Override public ResponseResult updateFinanceInvoice(Map<String,Object> p) { return down(); }
    @Override public ResponseResult cancelFinanceInvoice(Map<String,Object> p) { return down(); }
    @Override public ResponseResult printFinanceInvoice(Map<String,Object> p) { return down(); }

    public ResponseResult getBusinessPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getBusinessDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addBusiness(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateBusiness(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteBusiness(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addContract(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateContract(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteContract(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCluesPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCluesDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addClues(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateClues(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteClues(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProductPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProductDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addProduct(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateProduct(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteProduct(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRecordPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRecordDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addRecord(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateRecord(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteRecord(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRecordPageByCustomerId(Map<String, Object> params) {
        log.error("根据客户ID分页查询跟进记录失败：CRM服务不可用");
        return ResponseResult.fail(503, "CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getReceivablesPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getReceivablesDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addReceivables(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateReceivables(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteReceivables(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getInvoicePage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getInvoiceDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addInvoice(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateInvoice(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteInvoice(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProductTypePage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProductTypeDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addProductType(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateProductType(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteProductType(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getBusinessContactsPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getBusinessContactsDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addBusinessContacts(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateBusinessContacts(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteBusinessContacts(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractOrderPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractOrderDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addContractOrder(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateContractOrder(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteContractOrder(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractProductPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractProductDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addContractProduct(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateContractProduct(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteContractProduct(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getBusinessProductPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getBusinessProductDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addBusinessProduct(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateBusinessProduct(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteBusinessProduct(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProductUnitPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProductUnitDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addProductUnit(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateProductUnit(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteProductUnit(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAchievementPage(Map<String, Object> params) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getAchievementDetail(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addAchievement(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateAchievement(Map<String, Object> data) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteAchievement(Integer id) {
        return ResponseResult.fail("CRM服务暂时不可用，请稍后重试");
    }
    private ResponseResult down() {
        return ResponseResult.fail("CRM鏈嶅姟鏆傛椂涓嶅彲鐢紝璇风◢鍚庨噸璇?");
    }
    @Override
    public ResponseResult getSalesList(Map<String, Object> params) { return ResponseResult.error("CRM服务暂时不可用"); }
    @Override
    public ResponseResult crmWorkspace(String module,String action,Map<String,Object> params) { return ResponseResult.error("CRM服务暂时不可用"); }
}
