package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.bffweb.feign.CrmServiceFeign;
import com.houjiang.bffweb.feign.FinanceServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Crm服务BFF适配器
/**
 * 只做请求转发，不包含业务逻辑
 * 
 * @author Amos
 * @since 2024-12-29
 */
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/crm")
public class WebCrmController {

    @Autowired
    private CrmServiceFeign crmServiceFeign;

    /**
     * 兼容旧 /web/crm/finance 地址；发票、税务请求统一转发到 finance 服务。
     * 保留这层显式适配，避免历史前端或书签访问旧路径时落到 bff 的 404 兜底。
     * 新调用请使用 /web/finance 下的同名接口。
     */
    @Autowired
    private FinanceServiceFeign financeServiceFeign;

    // ================= Crm管理接口 =================

    /**
     * 获取crm统计数据
     */
    @PostMapping("/manage/statistics")
    public ResponseResult getStatistics() {
        return crmServiceFeign.getCrmStatistics(null);
    }

    /**
     * 分页查询crm列表
     */
    @PostMapping("/manage/list")
    public ResponseResult getCrmList(@RequestBody Map<String, Object> queryParams) {
        return crmServiceFeign.queryCrmList(queryParams);
    }

    /**
     * 查询crm详情
     */
    @PostMapping("/manage/detail")
    public ResponseResult getCrmDetail(@RequestBody Map<String, Object> param) {
        return crmServiceFeign.getCrmDetail(param);
    }

    /**
     * 创建crm
     */
    @PostMapping("/manage/create")
    public ResponseResult createCrm(@RequestBody Map<String, Object> crmData) {
        return crmServiceFeign.createCrm(crmData);
    }

    /**
     * 更新crm
     */
    @PostMapping("/manage/update")
    public ResponseResult updateCrm(@RequestBody Map<String, Object> crmData) {
        return crmServiceFeign.updateCrm(crmData);
    }

    /**
     * 删除crm
     */
    @PostMapping("/manage/delete")
    public ResponseResult deleteCrm(@RequestBody Map<String, Object> param) {
        return crmServiceFeign.deleteCrm(param);
    }

    /**
     * 暂停crm
     */
    @PostMapping("/manage/pause")
    public ResponseResult pauseCrm(@RequestBody Map<String, Object> param) {
        return crmServiceFeign.pauseCrm(param);
    }

    /**
     * 恢复crm
     */
    @PostMapping("/manage/resume")
    public ResponseResult resumeCrm(@RequestBody Map<String, Object> param) {
        return crmServiceFeign.resumeCrm(param);
    }

    /**
     * 复制crm
     */
    @PostMapping("/manage/copy")
    public ResponseResult copyCrm(@RequestBody Map<String, Object> param) {
        return crmServiceFeign.copyCrm(param);
    }

    /**
     * 获取crm类型列表
     */
    @GetMapping("/manage/types")
    public ResponseResult getCrmTypes() {
        return crmServiceFeign.getCrmTypes();
    }

    /**
     * 批量操作crm状�?
     */
    @PostMapping("/manage/batch/status")
    public ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param) {
        return crmServiceFeign.batchUpdateStatus(param);
    }
    
    // ================= 客户管理接口 =================
    
    /**
     * 分页查询客户列表
     */
    @PostMapping("/customer/page")
    public ResponseResult getCustomerPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCustomerPage(params);
    }
    
    /**
     * 获取客户详情
     */
    @GetMapping("/customer/detail/{id}")
    public ResponseResult getCustomerDetail(@PathVariable Integer id) {
        return crmServiceFeign.getCustomerDetail(id);
    }
    
    /**
     * 新增客户
     */
    @PostMapping("/customer/add")
    public ResponseResult addCustomer(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addCustomer(data);
    }
    
    /**
     * 更新客户
     */
    @PostMapping("/customer/update")
    public ResponseResult updateCustomer(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateCustomer(data);
    }
    
    /**
     * 删除客户
     */
    @PostMapping("/customer/delete/{id}")
    public ResponseResult deleteCustomer(@PathVariable Integer id) {
        return crmServiceFeign.deleteCustomer(id);
    }

    @PostMapping("/customer/tag/page")
    public ResponseResult getCustomerTagPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCustomerTagPage(params);
    }

    @PostMapping("/customer/tag/add")
    public ResponseResult addCustomerTag(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.addCustomerTag(params);
    }

    @PostMapping("/customer/tag/update")
    public ResponseResult updateCustomerTag(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.updateCustomerTag(params);
    }

    @PostMapping("/customer/tag/delete/{id}")
    public ResponseResult deleteCustomerTag(@PathVariable Integer id) {
        return crmServiceFeign.deleteCustomerTag(id);
    }

    @PostMapping("/customer/statistics")
    public ResponseResult getCustomerStatistics(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCustomerStatistics(params);
    }

    @PostMapping("/customer/assign/page")
    public ResponseResult getCustomerAssignPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCustomerAssignPage(params);
    }

    @PostMapping("/customer/assign")
    public ResponseResult assignCustomer(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.assignCustomer(params);
    }

    @PostMapping("/customer/transfer")
    public ResponseResult transferCustomer(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.transferCustomer(params);
    }

    @PostMapping("/customer/sales/list")
    public ResponseResult getSalesList(@RequestBody Map<String, Object> params) { return crmServiceFeign.getSalesList(params); }

    @PostMapping("/customer/assign/history")
    public ResponseResult getAssignHistory(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getAssignHistory(params);
    }

    @PostMapping("/customer/follow/page")
    public ResponseResult getCustomerFollowPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCustomerFollowPage(params);
    }

    @PostMapping("/customer/follow/add")
    public ResponseResult addCustomerFollow(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.addCustomerFollow(params);
    }

    @PostMapping("/customer/follow/update")
    public ResponseResult updateCustomerFollow(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.updateCustomerFollow(params);
    }

    @PostMapping("/customer/follow/delete/{id}")
    public ResponseResult deleteCustomerFollow(@PathVariable Integer id) {
        return crmServiceFeign.deleteCustomerFollow(id);
    }

    @PostMapping("/customer/list")
    public ResponseResult getCustomerList(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCustomerList(params);
    }

    @PostMapping("/customer/search")
    public ResponseResult searchCustomer(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.searchCustomer(params);
    }

    @GetMapping("/customer/profile/{id}")
    public ResponseResult getCustomerProfile(@PathVariable Integer id) {
        return crmServiceFeign.getCustomerProfile(id);
    }

    @PostMapping("/customer/value/page")
    public ResponseResult getCustomerValuePage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCustomerValuePage(params);
    }

    @PostMapping("/customer/value/calculate")
    public ResponseResult calculateCustomerValue(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.calculateCustomerValue(params);
    }

    @PostMapping("/customer/lifecycle/page")
    public ResponseResult getCustomerLifecyclePage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCustomerLifecyclePage(params);
    }

    @PostMapping("/customer/lifecycle/timeline")
    public ResponseResult getLifecycleTimeline(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getLifecycleTimeline(params);
    }

    @PostMapping("/customer/lifecycle/change")
    public ResponseResult changeLifecycleStage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.changeLifecycleStage(params);
    }

    @PostMapping("/customer/lifecycle/stats")
    public ResponseResult getLifecycleStats(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getLifecycleStats(params);
    }

    @GetMapping("/customer/relation/{id}")
    public ResponseResult getCustomerRelation(@PathVariable Integer id) {
        return crmServiceFeign.getCustomerRelation(id);
    }

    @PostMapping("/customer/relation/add")
    public ResponseResult addCustomerRelation(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.addCustomerRelation(params);
    }

    @PostMapping("/customer/relation/delete/{id}")
    public ResponseResult deleteCustomerRelation(@PathVariable Integer id) {
        return crmServiceFeign.deleteCustomerRelation(id);
    }

    @PostMapping("/customer/map/data")
    public ResponseResult getCustomerMapData(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCustomerMapData(params);
    }

    @PostMapping("/customer/merge")
    public ResponseResult mergeCustomers(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.mergeCustomers(params);
    }
    
    @PostMapping("/finance/approval/page")
    public ResponseResult getFinanceApprovalPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getFinanceApprovalPage(params);
    }
    
    @PostMapping("/finance/approval/approve")
    public ResponseResult approveFinance(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.approveFinance(data);
    }
    
    @PostMapping("/finance/budget/page")
    public ResponseResult getFinanceBudgetPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getFinanceBudgetPage(params);
    }
    
    @PostMapping("/finance/budget/add")
    public ResponseResult addFinanceBudget(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addFinanceBudget(data);
    }
    
    @PostMapping("/finance/cost/page")
    public ResponseResult getFinanceCostPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getFinanceCostPage(params);
    }
    
    @PostMapping("/finance/cost/calculate")
    public ResponseResult calculateCost(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.calculateCost(data);
    }
    
    @PostMapping("/finance/fund/page")
    public ResponseResult getFinanceFundPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getFinanceFundPage(params);
    }
    
    @PostMapping("/finance/fund/add")
    public ResponseResult addFundRecord(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addFundRecord(data);
    }
    
    @PostMapping("/finance/reconcile/page")
    public ResponseResult getFinanceReconcilePage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getFinanceReconcilePage(params);
    }
    
    @PostMapping("/finance/reconcile/execute")
    public ResponseResult reconcileAccount(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.reconcileAccount(data);
    }
    
    @PostMapping("/finance/remind/page")
    public ResponseResult getFinanceRemindPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getFinanceRemindPage(params);
    }
    
    @PostMapping("/finance/remind/add")
    public ResponseResult addFinanceRemind(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addFinanceRemind(data);
    }
    
    @PostMapping("/finance/setting")
    public ResponseResult getFinanceSetting(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getFinanceSetting(params);
    }
    
    @PostMapping("/finance/setting/update")
    public ResponseResult updateFinanceSetting(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateFinanceSetting(data);
    }
    
    @PostMapping("/finance/tax/page")
    public ResponseResult getFinanceTaxPage(@RequestBody Map<String, Object> params) {
        return financeServiceFeign.getTaxPage(params);
    }
    
    @PostMapping("/finance/tax/add")
    public ResponseResult addTaxRecord(@RequestBody Map<String, Object> data) {
        return financeServiceFeign.addTax(data);
    }

    @PostMapping("/finance/tax/detail")
    public ResponseResult getFinanceTaxDetail(@RequestBody Map<String, Object> params) { return financeServiceFeign.getTaxDetail(params); }
    @PostMapping("/finance/tax/update")
    public ResponseResult updateTaxRecord(@RequestBody Map<String, Object> data) { return financeServiceFeign.updateTax(data); }
    @PostMapping("/finance/tax/delete")
    public ResponseResult deleteTaxRecord(@RequestBody Map<String, Object> params) { return financeServiceFeign.deleteTax(params); }
    @PostMapping("/finance/invoice/list")
    public ResponseResult getFinanceInvoiceList(@RequestBody Map<String, Object> params) { return financeServiceFeign.getInvoiceList(params); }
    @PostMapping("/finance/invoice/detail")
    public ResponseResult getFinanceInvoiceDetail(@RequestBody Map<String, Object> params) { return financeServiceFeign.getInvoiceDetail(params); }
    @PostMapping("/finance/invoice/stats")
    public ResponseResult getFinanceInvoiceStats(@RequestBody(required = false) Map<String, Object> params) { return financeServiceFeign.getInvoiceStats(params == null ? java.util.Collections.emptyMap() : params); }
    @PostMapping("/finance/invoice/create")
    public ResponseResult createFinanceInvoice(@RequestBody Map<String, Object> data) { return financeServiceFeign.createInvoice(data); }
    @PostMapping("/finance/invoice/add")
    public ResponseResult addFinanceInvoice(@RequestBody Map<String, Object> data) { return financeServiceFeign.addInvoice(data); }
    @PostMapping("/finance/invoice/update")
    public ResponseResult updateFinanceInvoice(@RequestBody Map<String, Object> data) { return financeServiceFeign.updateInvoice(data); }
    @PostMapping("/finance/invoice/cancel")
    public ResponseResult cancelFinanceInvoice(@RequestBody Map<String, Object> params) { return financeServiceFeign.cancelInvoice(params); }
    @PostMapping("/finance/invoice/print")
    public ResponseResult printFinanceInvoice(@RequestBody Map<String, Object> params) { return financeServiceFeign.printInvoice(params); }
    
    // ================= 联系人管理接口 =================
    
    /**
     * 分页查询联系人列表
     */
    @PostMapping("/customerContacts/page")
    public ResponseResult getContactsPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getContactsPage(params);
    }
    
    /**
     * 获取联系人详情
     */
    @GetMapping("/customerContacts/detail/{id}")
    public ResponseResult getContactsDetail(@PathVariable Integer id) {
        return crmServiceFeign.getContactsDetail(id);
    }
    
    /**
     * 新增联系人
     */
    @PostMapping("/customerContacts/add")
    public ResponseResult addContacts(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addContacts(data);
    }
    
    /**
     * 更新联系人
     */
    @PostMapping("/customerContacts/update")
    public ResponseResult updateContacts(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateContacts(data);
    }
    
    /**
     * 删除联系人
     */
    @PostMapping("/customerContacts/delete/{id}")
    public ResponseResult deleteContacts(@PathVariable Integer id) {
        return crmServiceFeign.deleteContacts(id);
    }
    
    // ================= 商机管理接口 =================
    
    /**
     * 分页查询商机列表
     */
    @PostMapping("/business/page")
    public ResponseResult getBusinessPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getBusinessPage(params);
    }
    
    /**
     * 获取商机详情
     */
    @GetMapping("/business/detail/{id}")
    public ResponseResult getBusinessDetail(@PathVariable Integer id) {
        return crmServiceFeign.getBusinessDetail(id);
    }
    
    /**
     * 新增商机
     */
    @PostMapping("/business/add")
    public ResponseResult addBusiness(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addBusiness(data);
    }
    
    /**
     * 更新商机
     */
    @PostMapping("/business/update")
    public ResponseResult updateBusiness(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateBusiness(data);
    }
    
    /**
     * 删除商机
     */
    @PostMapping("/business/delete/{id}")
    public ResponseResult deleteBusiness(@PathVariable Integer id) {
        return crmServiceFeign.deleteBusiness(id);
    }
    
    // ================= 合同管理接口 =================
    
    /**
     * 分页查询合同列表
     */
    @PostMapping("/contract/page")
    public ResponseResult getContractPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getContractPage(params);
    }
    
    /**
     * 获取合同详情
     */
    @GetMapping("/contract/detail/{id}")
    public ResponseResult getContractDetail(@PathVariable Integer id) {
        return crmServiceFeign.getContractDetail(id);
    }
    
    /**
     * 新增合同
     */
    @PostMapping("/contract/add")
    public ResponseResult addContract(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addContract(data);
    }
    
    /**
     * 更新合同
     */
    @PostMapping("/contract/update")
    public ResponseResult updateContract(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateContract(data);
    }
    
    /**
     * 删除合同
     */
    @PostMapping("/contract/delete/{id}")
    public ResponseResult deleteContract(@PathVariable Integer id) {
        return crmServiceFeign.deleteContract(id);
    }
    
    // ================= 线索管理接口 =================
    
    /**
     * 分页查询线索列表
     */
    @PostMapping("/clues/page")
    public ResponseResult getCluesPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getCluesPage(params);
    }
    
    /**
     * 获取线索详情
     */
    @GetMapping("/clues/detail/{id}")
    public ResponseResult getCluesDetail(@PathVariable Integer id) {
        return crmServiceFeign.getCluesDetail(id);
    }
    
    /**
     * 新增线索
     */
    @PostMapping("/clues/add")
    public ResponseResult addClues(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addClues(data);
    }
    
    /**
     * 更新线索
     */
    @PostMapping("/clues/update")
    public ResponseResult updateClues(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateClues(data);
    }
    
    /**
     * 删除线索
     */
    @PostMapping("/clues/delete/{id}")
    public ResponseResult deleteClues(@PathVariable Integer id) {
        return crmServiceFeign.deleteClues(id);
    }
    
    // ================= 产品管理接口 =================
    
    /**
     * 分页查询产品列表
     */
    @PostMapping("/product/page")
    public ResponseResult getProductPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getProductPage(params);
    }
    
    /**
     * 获取产品详情
     */
    @GetMapping("/product/detail/{id}")
    public ResponseResult getProductDetail(@PathVariable Integer id) {
        return crmServiceFeign.getProductDetail(id);
    }
    
    /**
     * 新增产品
     */
    @PostMapping("/product/add")
    public ResponseResult addProduct(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addProduct(data);
    }
    
    /**
     * 更新产品
     */
    @PostMapping("/product/update")
    public ResponseResult updateProduct(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateProduct(data);
    }
    
    /**
     * 删除产品
     */
    @PostMapping("/product/delete/{id}")
    public ResponseResult deleteProduct(@PathVariable Integer id) {
        return crmServiceFeign.deleteProduct(id);
    }
    
    // ================= 跟进记录接口 =================
    
    /**
     * 分页查询跟进记录列表
     */
    @PostMapping("/record/page")
    public ResponseResult getRecordPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getRecordPage(params);
    }
    
    /**
     * 获取跟进记录详情
     */
    @GetMapping("/record/detail/{id}")
    public ResponseResult getRecordDetail(@PathVariable Integer id) {
        return crmServiceFeign.getRecordDetail(id);
    }
    
    /**
     * 新增跟进记录
     */
    @PostMapping("/record/add")
    public ResponseResult addRecord(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addRecord(data);
    }
    
    /**
     * 更新跟进记录
     */
    @PostMapping("/record/update")
    public ResponseResult updateRecord(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateRecord(data);
    }
    
    /**
     * 删除跟进记录
     */
    @PostMapping("/record/delete/{id}")
    public ResponseResult deleteRecord(@PathVariable Integer id) {
        return crmServiceFeign.deleteRecord(id);
    }
    
    /**
     * 根据客户ID分页查询跟进记录
     */
    @PostMapping("/record/customer/page")
    public ResponseResult getRecordPageByCustomerId(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getRecordPageByCustomerId(params);
    }
    
    // ================= 回款计划接口 =================
    
    /**
     * 分页查询回款计划列表
     */
    @PostMapping("/contractReceivables/page")
    public ResponseResult getReceivablesPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getReceivablesPage(params);
    }
    
    /**
     * 获取回款计划详情
     */
    @GetMapping("/contractReceivables/detail/{id}")
    public ResponseResult getReceivablesDetail(@PathVariable Integer id) {
        return crmServiceFeign.getReceivablesDetail(id);
    }
    
    /**
     * 新增回款计划
     */
    @PostMapping("/contractReceivables/add")
    public ResponseResult addReceivables(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addReceivables(data);
    }
    
    /**
     * 更新回款计划
     */
    @PostMapping("/contractReceivables/update")
    public ResponseResult updateReceivables(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateReceivables(data);
    }
    
    /**
     * 删除回款计划
     */
    @PostMapping("/contractReceivables/delete/{id}")
    public ResponseResult deleteReceivables(@PathVariable Integer id) {
        return crmServiceFeign.deleteReceivables(id);
    }
    
    // ================= 发票管理接口 =================
    
    /**
     * 分页查询发票列表
     */
    @PostMapping("/invoice/page")
    public ResponseResult getInvoicePage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getInvoicePage(params);
    }
    
    /**
     * 获取发票详情
     */
    @GetMapping("/invoice/detail/{id}")
    public ResponseResult getInvoiceDetail(@PathVariable Integer id) {
        return crmServiceFeign.getInvoiceDetail(id);
    }
    
    /**
     * 新增发票
     */
    @PostMapping("/invoice/add")
    public ResponseResult addInvoice(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addInvoice(data);
    }
    
    /**
     * 更新发票
     */
    @PostMapping("/invoice/update")
    public ResponseResult updateInvoice(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateInvoice(data);
    }
    
    /**
     * 删除发票
     */
    @PostMapping("/invoice/delete/{id}")
    public ResponseResult deleteInvoice(@PathVariable Integer id) {
        return crmServiceFeign.deleteInvoice(id);
    }
    
    // ================= 产品分类接口 =================
    
    /**
     * 分页查询产品分类列表
     */
    @PostMapping("/productType/page")
    public ResponseResult getProductTypePage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getProductTypePage(params);
    }
    
    /**
     * 获取产品分类详情
     */
    @GetMapping("/productType/detail/{id}")
    public ResponseResult getProductTypeDetail(@PathVariable Integer id) {
        return crmServiceFeign.getProductTypeDetail(id);
    }
    
    /**
     * 新增产品分类
     */
    @PostMapping("/productType/add")
    public ResponseResult addProductType(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addProductType(data);
    }
    
    /**
     * 更新产品分类
     */
    @PostMapping("/productType/update")
    public ResponseResult updateProductType(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateProductType(data);
    }
    
    /**
     * 删除产品分类
     */
    @PostMapping("/productType/delete/{id}")
    public ResponseResult deleteProductType(@PathVariable Integer id) {
        return crmServiceFeign.deleteProductType(id);
    }
    
    // ================= 销售机会联系人接口 =================
    
    /**
     * 分页查询销售机会联系人列表
     */
    @PostMapping("/businessContacts/page")
    public ResponseResult getBusinessContactsPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getBusinessContactsPage(params);
    }
    
    /**
     * 获取销售机会联系人详情
     */
    @GetMapping("/businessContacts/detail/{id}")
    public ResponseResult getBusinessContactsDetail(@PathVariable Integer id) {
        return crmServiceFeign.getBusinessContactsDetail(id);
    }
    
    /**
     * 新增销售机会联系人
     */
    @PostMapping("/businessContacts/add")
    public ResponseResult addBusinessContacts(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addBusinessContacts(data);
    }
    
    /**
     * 更新销售机会联系人
     */
    @PostMapping("/businessContacts/update")
    public ResponseResult updateBusinessContacts(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateBusinessContacts(data);
    }
    
    /**
     * 删除销售机会联系人
     */
    @PostMapping("/businessContacts/delete/{id}")
    public ResponseResult deleteBusinessContacts(@PathVariable Integer id) {
        return crmServiceFeign.deleteBusinessContacts(id);
    }
    
    // ================= 合同订单接口 =================
    
    /**
     * 分页查询合同订单列表
     */
    @PostMapping("/contractOrder/page")
    public ResponseResult getContractOrderPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getContractOrderPage(params);
    }
    
    /**
     * 获取合同订单详情
     */
    @GetMapping("/contractOrder/detail/{id}")
    public ResponseResult getContractOrderDetail(@PathVariable Integer id) {
        return crmServiceFeign.getContractOrderDetail(id);
    }
    
    /**
     * 新增合同订单
     */
    @PostMapping("/contractOrder/add")
    public ResponseResult addContractOrder(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addContractOrder(data);
    }
    
    /**
     * 更新合同订单
     */
    @PostMapping("/contractOrder/update")
    public ResponseResult updateContractOrder(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateContractOrder(data);
    }
    
    /**
     * 删除合同订单
     */
    @PostMapping("/contractOrder/delete/{id}")
    public ResponseResult deleteContractOrder(@PathVariable Integer id) {
        return crmServiceFeign.deleteContractOrder(id);
    }
    
    // ================= 合同产品接口 =================
    
    /**
     * 分页查询合同产品列表
     */
    @PostMapping("/contractProduct/page")
    public ResponseResult getContractProductPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getContractProductPage(params);
    }
    
    /**
     * 获取合同产品详情
     */
    @GetMapping("/contractProduct/detail/{id}")
    public ResponseResult getContractProductDetail(@PathVariable Integer id) {
        return crmServiceFeign.getContractProductDetail(id);
    }
    
    /**
     * 新增合同产品
     */
    @PostMapping("/contractProduct/add")
    public ResponseResult addContractProduct(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addContractProduct(data);
    }
    
    /**
     * 更新合同产品
     */
    @PostMapping("/contractProduct/update")
    public ResponseResult updateContractProduct(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateContractProduct(data);
    }
    
    /**
     * 删除合同产品
     */
    @PostMapping("/contractProduct/delete/{id}")
    public ResponseResult deleteContractProduct(@PathVariable Integer id) {
        return crmServiceFeign.deleteContractProduct(id);
    }
    
    // ================= 商机产品接口 =================
    
    /**
     * 分页查询商机产品列表
     */
    @PostMapping("/businessProduct/page")
    public ResponseResult getBusinessProductPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getBusinessProductPage(params);
    }
    
    /**
     * 获取商机产品详情
     */
    @GetMapping("/businessProduct/detail/{id}")
    public ResponseResult getBusinessProductDetail(@PathVariable Integer id) {
        return crmServiceFeign.getBusinessProductDetail(id);
    }
    
    /**
     * 新增商机产品
     */
    @PostMapping("/businessProduct/add")
    public ResponseResult addBusinessProduct(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addBusinessProduct(data);
    }
    
    /**
     * 更新商机产品
     */
    @PostMapping("/businessProduct/update")
    public ResponseResult updateBusinessProduct(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateBusinessProduct(data);
    }
    
    /**
     * 删除商机产品
     */
    @PostMapping("/businessProduct/delete/{id}")
    public ResponseResult deleteBusinessProduct(@PathVariable Integer id) {
        return crmServiceFeign.deleteBusinessProduct(id);
    }
    
    // ================= 产品单位接口 =================
    
    /**
     * 分页查询产品单位列表
     */
    @PostMapping("/productUnit/page")
    public ResponseResult getProductUnitPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getProductUnitPage(params);
    }
    
    /**
     * 获取产品单位详情
     */
    @GetMapping("/productUnit/detail/{id}")
    public ResponseResult getProductUnitDetail(@PathVariable Integer id) {
        return crmServiceFeign.getProductUnitDetail(id);
    }
    
    /**
     * 新增产品单位
     */
    @PostMapping("/productUnit/add")
    public ResponseResult addProductUnit(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addProductUnit(data);
    }
    
    /**
     * 更新产品单位
     */
    @PostMapping("/productUnit/update")
    public ResponseResult updateProductUnit(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateProductUnit(data);
    }
    
    /**
     * 删除产品单位
     */
    @PostMapping("/productUnit/delete/{id}")
    public ResponseResult deleteProductUnit(@PathVariable Integer id) {
        return crmServiceFeign.deleteProductUnit(id);
    }
    
    // ================= 业绩统计接口 =================
    
    /**
     * 分页查询业绩统计列表
     */
    @PostMapping("/achievement/page")
    public ResponseResult getAchievementPage(@RequestBody Map<String, Object> params) {
        return crmServiceFeign.getAchievementPage(params);
    }
    
    /**
     * 获取业绩统计详情
     */
    @GetMapping("/achievement/detail/{id}")
    public ResponseResult getAchievementDetail(@PathVariable Integer id) {
        return crmServiceFeign.getAchievementDetail(id);
    }
    
    /**
     * 新增业绩统计
     */
    @PostMapping("/achievement/add")
    public ResponseResult addAchievement(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.addAchievement(data);
    }
    
    /**
     * 更新业绩统计
     */
    @PostMapping("/achievement/update")
    public ResponseResult updateAchievement(@RequestBody Map<String, Object> data) {
        return crmServiceFeign.updateAchievement(data);
    }
    
    /**
     * 删除业绩统计
     */
    @PostMapping("/achievement/delete/{id}")
    public ResponseResult deleteAchievement(@PathVariable Integer id) {
        return crmServiceFeign.deleteAchievement(id);
    }
    
    // ================= 财务管理接口 =================
    @PostMapping("/workspace/{module}/{action}")
    public ResponseResult crmWorkspace(@PathVariable String module,@PathVariable String action,@RequestBody Map<String,Object> params) { return crmServiceFeign.crmWorkspace(module,action,params); }
}
