package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Crm服务Feign客户端
/**
 * 
 * @description 调用paas中台Crm服务(crm)的接口
 * @author Amos
 * @date 2024-12-29
 */
@FeignClient(name = "crm", url = "${feign.crm.url:}", path = "/plugin", fallback = CrmServiceFeignFallback.class, contextId = "crm")
public interface CrmServiceFeign {

    // ================= Crm管理接口 =================
    
    /**
     * 获取crm统计数据
     */
    @PostMapping("/crm/manage/statistics")
    ResponseResult getCrmStatistics(@RequestBody Map<String, Object> param);
    
    /**
     * 分页查询crm列表
     */
    @PostMapping("/crm/manage/list")
    ResponseResult queryCrmList(@RequestBody Map<String, Object> param);
    
    /**
     * 查询crm详情
     */
    @PostMapping("/crm/manage/detail")
    ResponseResult getCrmDetail(@RequestBody Map<String, Object> param);
    
    /**
     * 创建crm
     */
    @PostMapping("/crm/manage/create")
    ResponseResult createCrm(@RequestBody Map<String, Object> param);
    
    /**
     * 更新crm
     */
    @PostMapping("/crm/manage/update")
    ResponseResult updateCrm(@RequestBody Map<String, Object> param);
    
    /**
     * 删除crm
     */
    @PostMapping("/crm/manage/delete")
    ResponseResult deleteCrm(@RequestBody Map<String, Object> param);
    
    /**
     * 暂停crm
     */
    @PostMapping("/crm/manage/pause")
    ResponseResult pauseCrm(@RequestBody Map<String, Object> param);
    
    /**
     * 恢复crm
     */
    @PostMapping("/crm/manage/resume")
    ResponseResult resumeCrm(@RequestBody Map<String, Object> param);
    
    /**
     * 复制crm
     */
    @PostMapping("/crm/manage/copy")
    ResponseResult copyCrm(@RequestBody Map<String, Object> param);
    
    /**
     * 获取crm类型列表
     */
    @GetMapping("/crm/manage/types")
    ResponseResult getCrmTypes();
    
    /**
     * 批量操作crm状态
     */
    @PostMapping("/crm/manage/batch/status")
    ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param);
    
    // ================= 客户管理接口 =================
    
    /**
     * 分页查询客户列表
     */
    @PostMapping("/crm/customer/page")
    ResponseResult getCustomerPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取客户详情
     */
    @GetMapping("/crm/customer/detail/{id}")
    ResponseResult getCustomerDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增客户
     */
    @PostMapping("/crm/customer/add")
    ResponseResult addCustomer(@RequestBody Map<String, Object> data);
    
    /**
     * 更新客户
     */
    @PostMapping("/crm/customer/update")
    ResponseResult updateCustomer(@RequestBody Map<String, Object> data);
    
    /**
     * 删除客户
     */
    @PostMapping("/crm/customer/delete/{id}")
    ResponseResult deleteCustomer(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/tag/page")
    ResponseResult getCustomerTagPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/tag/add")
    ResponseResult addCustomerTag(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/tag/update")
    ResponseResult updateCustomerTag(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/crm/tag/delete/{id}")
    ResponseResult deleteCustomerTag(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/statistics")
    ResponseResult getCustomerStatistics(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/assign/page")
    ResponseResult getCustomerAssignPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/assign")
    ResponseResult assignCustomer(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/transfer")
    ResponseResult transferCustomer(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/assign/history")
    ResponseResult getAssignHistory(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/follow/page")
    ResponseResult getCustomerFollowPage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/follow/add")
    ResponseResult addCustomerFollow(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/follow/update")
    ResponseResult updateCustomerFollow(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/crm/customer/follow/delete/{id}")
    ResponseResult deleteCustomerFollow(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/list")
    ResponseResult getCustomerList(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/search")
    ResponseResult searchCustomer(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/crm/customer/profile/{id}")
    ResponseResult getCustomerProfile(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/value/page")
    ResponseResult getCustomerValuePage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/value/calculate")
    ResponseResult calculateCustomerValue(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/lifecycle/page")
    ResponseResult getCustomerLifecyclePage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/lifecycle/timeline")
    ResponseResult getLifecycleTimeline(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/lifecycle/change")
    ResponseResult changeLifecycleStage(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/lifecycle/stats")
    ResponseResult getLifecycleStats(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @GetMapping("/crm/customer/relation/{id}")
    ResponseResult getCustomerRelation(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/relation/add")
    ResponseResult addCustomerRelation(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @return TODO
     */
    @PostMapping("/crm/customer/relation/delete/{id}")
    ResponseResult deleteCustomerRelation(@PathVariable("id") Integer id);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/map/data")
    ResponseResult getCustomerMapData(@RequestBody Map<String, Object> params);

    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/customer/merge")
    ResponseResult mergeCustomers(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/approval/page")
    ResponseResult getFinanceApprovalPage(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/approval/approve")
    ResponseResult approveFinance(@RequestBody Map<String, Object> data);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/budget/page")
    ResponseResult getFinanceBudgetPage(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/budget/add")
    ResponseResult addFinanceBudget(@RequestBody Map<String, Object> data);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/cost/page")
    ResponseResult getFinanceCostPage(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/cost/calculate")
    ResponseResult calculateCost(@RequestBody Map<String, Object> data);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/fund/page")
    ResponseResult getFinanceFundPage(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/fund/add")
    ResponseResult addFundRecord(@RequestBody Map<String, Object> data);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/reconcile/page")
    ResponseResult getFinanceReconcilePage(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/reconcile/execute")
    ResponseResult reconcileAccount(@RequestBody Map<String, Object> data);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/remind/page")
    ResponseResult getFinanceRemindPage(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/remind/add")
    ResponseResult addFinanceRemind(@RequestBody Map<String, Object> data);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/setting")
    ResponseResult getFinanceSetting(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/setting/update")
    ResponseResult updateFinanceSetting(@RequestBody Map<String, Object> data);
    
    /**
     * TODO 补充方法说明
/**
     * @param params TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/tax/page")
    ResponseResult getFinanceTaxPage(@RequestBody Map<String, Object> params);
    
    /**
     * TODO 补充方法说明
/**
     * @param data TODO
     * @return TODO
     */
    @PostMapping("/crm/finance/tax/add")
    ResponseResult addTaxRecord(@RequestBody Map<String, Object> data);

    @PostMapping("/crm/finance/tax/detail")
    ResponseResult getFinanceTaxDetail(@RequestBody Map<String, Object> params);
    @PostMapping("/crm/finance/tax/update")
    ResponseResult updateTaxRecord(@RequestBody Map<String, Object> data);
    @PostMapping("/crm/finance/tax/delete")
    ResponseResult deleteTaxRecord(@RequestBody Map<String, Object> params);

    @PostMapping("/crm/finance/invoice/list")
    ResponseResult getFinanceInvoiceList(@RequestBody Map<String, Object> params);
    @PostMapping("/crm/finance/invoice/detail")
    ResponseResult getFinanceInvoiceDetail(@RequestBody Map<String, Object> params);
    @PostMapping("/crm/finance/invoice/stats")
    ResponseResult getFinanceInvoiceStats(@RequestBody Map<String, Object> params);
    @PostMapping("/crm/finance/invoice/create")
    ResponseResult createFinanceInvoice(@RequestBody Map<String, Object> data);
    @PostMapping("/crm/finance/invoice/add")
    ResponseResult addFinanceInvoice(@RequestBody Map<String, Object> data);
    @PostMapping("/crm/finance/invoice/update")
    ResponseResult updateFinanceInvoice(@RequestBody Map<String, Object> data);
    @PostMapping("/crm/finance/invoice/cancel")
    ResponseResult cancelFinanceInvoice(@RequestBody Map<String, Object> params);
    @PostMapping("/crm/finance/invoice/print")
    ResponseResult printFinanceInvoice(@RequestBody Map<String, Object> params);
    
    // ================= 联系人管理接口 =================
    
    /**
     * 分页查询联系人列表
     */
    @PostMapping("/crm/customerContacts/page")
    ResponseResult getContactsPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取联系人详情
     */
    @GetMapping("/crm/customerContacts/detail/{id}")
    ResponseResult getContactsDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增联系人
     */
    @PostMapping("/crm/customerContacts/add")
    ResponseResult addContacts(@RequestBody Map<String, Object> data);
    
    /**
     * 更新联系人
     */
    @PostMapping("/crm/customerContacts/update")
    ResponseResult updateContacts(@RequestBody Map<String, Object> data);
    
    /**
     * 删除联系人
     */
    @PostMapping("/crm/customerContacts/delete/{id}")
    ResponseResult deleteContacts(@PathVariable("id") Integer id);
    
    // ================= 商机管理接口 =================
    
    /**
     * 分页查询商机列表
     */
    @PostMapping("/crm/business/page")
    ResponseResult getBusinessPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取商机详情
     */
    @GetMapping("/crm/business/detail/{id}")
    ResponseResult getBusinessDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增商机
     */
    @PostMapping("/crm/business/add")
    ResponseResult addBusiness(@RequestBody Map<String, Object> data);
    
    /**
     * 更新商机
     */
    @PostMapping("/crm/business/update")
    ResponseResult updateBusiness(@RequestBody Map<String, Object> data);
    
    /**
     * 删除商机
     */
    @PostMapping("/crm/business/delete/{id}")
    ResponseResult deleteBusiness(@PathVariable("id") Integer id);
    
    // ================= 合同管理接口 =================
    
    /**
     * 分页查询合同列表
     */
    @PostMapping("/crm/contract/page")
    ResponseResult getContractPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取合同详情
     */
    @GetMapping("/crm/contract/detail/{id}")
    ResponseResult getContractDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增合同
     */
    @PostMapping("/crm/contract/add")
    ResponseResult addContract(@RequestBody Map<String, Object> data);
    
    /**
     * 更新合同
     */
    @PostMapping("/crm/contract/update")
    ResponseResult updateContract(@RequestBody Map<String, Object> data);
    
    /**
     * 删除合同
     */
    @PostMapping("/crm/contract/delete/{id}")
    ResponseResult deleteContract(@PathVariable("id") Integer id);
    
    // ================= 线索管理接口 =================
    
    /**
     * 分页查询线索列表
     */
    @PostMapping("/crm/clues/page")
    ResponseResult getCluesPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取线索详情
     */
    @GetMapping("/crm/clues/detail/{id}")
    ResponseResult getCluesDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增线索
     */
    @PostMapping("/crm/clues/add")
    ResponseResult addClues(@RequestBody Map<String, Object> data);
    
    /**
     * 更新线索
     */
    @PostMapping("/crm/clues/update")
    ResponseResult updateClues(@RequestBody Map<String, Object> data);
    
    /**
     * 删除线索
     */
    @PostMapping("/crm/clues/delete/{id}")
    ResponseResult deleteClues(@PathVariable("id") Integer id);
    
    // ================= 产品管理接口 =================
    
    /**
     * 分页查询产品列表
     */
    @PostMapping("/crm/product/page")
    ResponseResult getProductPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取产品详情
     */
    @GetMapping("/crm/product/detail/{id}")
    ResponseResult getProductDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增产品
     */
    @PostMapping("/crm/product/add")
    ResponseResult addProduct(@RequestBody Map<String, Object> data);
    
    /**
     * 更新产品
     */
    @PostMapping("/crm/product/update")
    ResponseResult updateProduct(@RequestBody Map<String, Object> data);
    
    /**
     * 删除产品
     */
    @PostMapping("/crm/product/delete/{id}")
    ResponseResult deleteProduct(@PathVariable("id") Integer id);
    
    // ================= 跟进记录接口 =================
    
    /**
     * 分页查询跟进记录列表
     */
    @PostMapping("/crm/record/page")
    ResponseResult getRecordPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取跟进记录详情
     */
    @GetMapping("/crm/record/detail/{id}")
    ResponseResult getRecordDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增跟进记录
     */
    @PostMapping("/crm/record/add")
    ResponseResult addRecord(@RequestBody Map<String, Object> data);
    
    /**
     * 更新跟进记录
     */
    @PostMapping("/crm/record/update")
    ResponseResult updateRecord(@RequestBody Map<String, Object> data);
    
    /**
     * 删除跟进记录
     */
    @PostMapping("/crm/record/delete/{id}")
    ResponseResult deleteRecord(@PathVariable("id") Integer id);
    
    /**
     * 根据客户ID分页查询跟进记录
     */
    @PostMapping("/crm/record/customer/page")
    ResponseResult getRecordPageByCustomerId(@RequestBody Map<String, Object> params);
    
    // ================= 回款计划接口 =================
    
    /**
     * 分页查询回款计划列表
     */
    @PostMapping("/crm/contractReceivables/page")
    ResponseResult getReceivablesPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取回款计划详情
     */
    @GetMapping("/crm/contractReceivables/detail/{id}")
    ResponseResult getReceivablesDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增回款计划
     */
    @PostMapping("/crm/contractReceivables/add")
    ResponseResult addReceivables(@RequestBody Map<String, Object> data);
    
    /**
     * 更新回款计划
     */
    @PostMapping("/crm/contractReceivables/update")
    ResponseResult updateReceivables(@RequestBody Map<String, Object> data);
    
    /**
     * 删除回款计划
     */
    @PostMapping("/crm/contractReceivables/delete/{id}")
    ResponseResult deleteReceivables(@PathVariable("id") Integer id);
    
    // ================= 发票管理接口 =================
    
    /**
     * 分页查询发票列表
     */
    @PostMapping("/crm/invoice/page")
    ResponseResult getInvoicePage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取发票详情
     */
    @GetMapping("/crm/invoice/detail/{id}")
    ResponseResult getInvoiceDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增发票
     */
    @PostMapping("/crm/invoice/add")
    ResponseResult addInvoice(@RequestBody Map<String, Object> data);
    
    /**
     * 更新发票
     */
    @PostMapping("/crm/invoice/update")
    ResponseResult updateInvoice(@RequestBody Map<String, Object> data);
    
    /**
     * 删除发票
     */
    @PostMapping("/crm/invoice/delete/{id}")
    ResponseResult deleteInvoice(@PathVariable("id") Integer id);
    
    // ================= 产品分类接口 =================
    
    /**
     * 分页查询产品分类列表
     */
    @PostMapping("/crm/productType/page")
    ResponseResult getProductTypePage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取产品分类详情
     */
    @GetMapping("/crm/productType/detail/{id}")
    ResponseResult getProductTypeDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增产品分类
     */
    @PostMapping("/crm/productType/add")
    ResponseResult addProductType(@RequestBody Map<String, Object> data);
    
    /**
     * 更新产品分类
     */
    @PostMapping("/crm/productType/update")
    ResponseResult updateProductType(@RequestBody Map<String, Object> data);
    
    /**
     * 删除产品分类
     */
    @PostMapping("/crm/productType/delete/{id}")
    ResponseResult deleteProductType(@PathVariable("id") Integer id);
    
    // ================= 销售机会联系人接口 =================
    
    /**
     * 分页查询销售机会联系人列表
     */
    @PostMapping("/crm/businessContacts/page")
    ResponseResult getBusinessContactsPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取销售机会联系人详情
     */
    @GetMapping("/crm/businessContacts/detail/{id}")
    ResponseResult getBusinessContactsDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增销售机会联系人
     */
    @PostMapping("/crm/businessContacts/add")
    ResponseResult addBusinessContacts(@RequestBody Map<String, Object> data);
    
    /**
     * 更新销售机会联系人
     */
    @PostMapping("/crm/businessContacts/update")
    ResponseResult updateBusinessContacts(@RequestBody Map<String, Object> data);
    
    /**
     * 删除销售机会联系人
     */
    @PostMapping("/crm/businessContacts/delete/{id}")
    ResponseResult deleteBusinessContacts(@PathVariable("id") Integer id);
    
    // ================= 合同订单接口 =================
    
    /**
     * 分页查询合同订单列表
     */
    @PostMapping("/crm/contractOrder/page")
    ResponseResult getContractOrderPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取合同订单详情
     */
    @GetMapping("/crm/contractOrder/detail/{id}")
    ResponseResult getContractOrderDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增合同订单
     */
    @PostMapping("/crm/contractOrder/add")
    ResponseResult addContractOrder(@RequestBody Map<String, Object> data);
    
    /**
     * 更新合同订单
     */
    @PostMapping("/crm/contractOrder/update")
    ResponseResult updateContractOrder(@RequestBody Map<String, Object> data);
    
    /**
     * 删除合同订单
     */
    @PostMapping("/crm/contractOrder/delete/{id}")
    ResponseResult deleteContractOrder(@PathVariable("id") Integer id);
    
    // ================= 合同产品接口 =================
    
    /**
     * 分页查询合同产品列表
     */
    @PostMapping("/crm/contractProduct/page")
    ResponseResult getContractProductPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取合同产品详情
     */
    @GetMapping("/crm/contractProduct/detail/{id}")
    ResponseResult getContractProductDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增合同产品
     */
    @PostMapping("/crm/contractProduct/add")
    ResponseResult addContractProduct(@RequestBody Map<String, Object> data);
    
    /**
     * 更新合同产品
     */
    @PostMapping("/crm/contractProduct/update")
    ResponseResult updateContractProduct(@RequestBody Map<String, Object> data);
    
    /**
     * 删除合同产品
     */
    @PostMapping("/crm/contractProduct/delete/{id}")
    ResponseResult deleteContractProduct(@PathVariable("id") Integer id);
    
    // ================= 商机产品接口 =================
    
    /**
     * 分页查询商机产品列表
     */
    @PostMapping("/crm/businessProduct/page")
    ResponseResult getBusinessProductPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取商机产品详情
     */
    @GetMapping("/crm/businessProduct/detail/{id}")
    ResponseResult getBusinessProductDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增商机产品
     */
    @PostMapping("/crm/businessProduct/add")
    ResponseResult addBusinessProduct(@RequestBody Map<String, Object> data);
    
    /**
     * 更新商机产品
     */
    @PostMapping("/crm/businessProduct/update")
    ResponseResult updateBusinessProduct(@RequestBody Map<String, Object> data);
    
    /**
     * 删除商机产品
     */
    @PostMapping("/crm/businessProduct/delete/{id}")
    ResponseResult deleteBusinessProduct(@PathVariable("id") Integer id);
    
    // ================= 产品单位接口 =================
    
    /**
     * 分页查询产品单位列表
     */
    @PostMapping("/crm/productUnit/page")
    ResponseResult getProductUnitPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取产品单位详情
     */
    @GetMapping("/crm/productUnit/detail/{id}")
    ResponseResult getProductUnitDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增产品单位
     */
    @PostMapping("/crm/productUnit/add")
    ResponseResult addProductUnit(@RequestBody Map<String, Object> data);
    
    /**
     * 更新产品单位
     */
    @PostMapping("/crm/productUnit/update")
    ResponseResult updateProductUnit(@RequestBody Map<String, Object> data);
    
    /**
     * 删除产品单位
     */
    @PostMapping("/crm/productUnit/delete/{id}")
    ResponseResult deleteProductUnit(@PathVariable("id") Integer id);
    
    // ================= 业绩统计接口 =================
    
    /**
     * 分页查询业绩统计列表
     */
    @PostMapping("/crm/achievement/page")
    ResponseResult getAchievementPage(@RequestBody Map<String, Object> params);
    
    /**
     * 获取业绩统计详情
     */
    @GetMapping("/crm/achievement/detail/{id}")
    ResponseResult getAchievementDetail(@PathVariable("id") Integer id);
    
    /**
     * 新增业绩统计
     */
    @PostMapping("/crm/achievement/add")
    ResponseResult addAchievement(@RequestBody Map<String, Object> data);
    
    /**
     * 更新业绩统计
     */
    @PostMapping("/crm/achievement/update")
    ResponseResult updateAchievement(@RequestBody Map<String, Object> data);
    
    /**
     * 删除业绩统计
     */
    @PostMapping("/crm/achievement/delete/{id}")
    ResponseResult deleteAchievement(@PathVariable("id") Integer id);
    
    // ================= 财务管理接口 =================
    @PostMapping("/crm/customer/sales/list")
    ResponseResult getSalesList(@RequestBody Map<String, Object> params);
    @PostMapping("/crm/workspace/{module}/{action}")
    ResponseResult crmWorkspace(@PathVariable("module") String module,@PathVariable("action") String action,@RequestBody Map<String,Object> params);
}
