package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 供应商服务Feign客户端
/**
 * 
 * @description 提供供应商管理相关功能的远程调用接口
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(value = "supplier", fallback = SupplierServiceFeignFallback.class, url = "${feign.supplier.url:}", path = "/plugin")
public interface SupplierServiceFeign {

    // ================= 供应商管理相关接口=================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/list")
    ResponseResult getSupplierList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/detail")
    ResponseResult getSupplierDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/create")
    ResponseResult createSupplier(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/update")
    ResponseResult updateSupplier(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/delete")
    ResponseResult deleteSupplier(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/enable")
    ResponseResult enableSupplier(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/disable")
    ResponseResult disableSupplier(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/audit")
    ResponseResult auditSupplier(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/audit/list")
    ResponseResult getSupplierAuditList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/approve")
    ResponseResult approveSupplier(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/reject")
    ResponseResult rejectSupplier(@RequestBody Map<String, Object> param);

    // ================= 供应商分类相关接口=================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/category/list")
    ResponseResult getSupplierCategoryList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/category/detail")
    ResponseResult getSupplierCategoryDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/category/create")
    ResponseResult createSupplierCategory(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/category/update")
    ResponseResult updateSupplierCategory(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/category/delete")
    ResponseResult deleteSupplierCategory(@RequestBody Map<String, Object> param);

    // ================= 合同管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/contract/list")
    ResponseResult getContractList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/contract/detail")
    ResponseResult getContractDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/contract/create")
    ResponseResult createContract(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/contract/update")
    ResponseResult updateContract(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/contract/sign")
    ResponseResult signContract(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/contract/terminate")
    ResponseResult terminateContract(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/contract/approve")
    ResponseResult approveContract(@RequestBody Map<String, Object> param);

    // ================= 产品管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/product/list")
    ResponseResult getSupplierProductList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/product/list")
    ResponseResult getProductList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/product/detail")
    ResponseResult getSupplierProductDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/product/detail")
    ResponseResult getProductDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/product/create")
    ResponseResult createSupplierProduct(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/product/create")
    ResponseResult createProduct(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/product/update")
    ResponseResult updateSupplierProduct(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/product/update")
    ResponseResult updateProduct(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/product/enable")
    ResponseResult enableSupplierProduct(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/product/enable")
    ResponseResult enableProduct(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/product/disable")
    ResponseResult disableSupplierProduct(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/product/disable")
    ResponseResult disableProduct(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/product/delete")
    ResponseResult deleteProduct(@RequestBody Map<String, Object> param);

    // ================= 采购管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/purchase/list")
    ResponseResult getPurchaseList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/purchase/detail")
    ResponseResult getPurchaseDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/purchase/create")
    ResponseResult createPurchase(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/purchase/approve")
    ResponseResult approvePurchase(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/purchase/reject")
    ResponseResult rejectPurchase(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/purchase/complete")
    ResponseResult completePurchase(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/purchase/update")
    ResponseResult updatePurchase(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/purchase/cancel")
    ResponseResult cancelPurchase(@RequestBody Map<String, Object> param);

    // ================= 评价管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/evaluation/list")
    ResponseResult getEvaluationList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/evaluation/add")
    ResponseResult addEvaluation(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/evaluation/detail")
    ResponseResult getEvaluationDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/evaluation/create")
    ResponseResult createEvaluation(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/supplier/evaluation/reply")
    ResponseResult replyEvaluation(@RequestBody Map<String, Object> param);

    // ================= 统计报表相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/statistics/overview")
    ResponseResult getSupplierStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/statistics/cooperation")
    ResponseResult getCooperationStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/statistics/quality")
    ResponseResult getQualityStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/report/supplier")
    ResponseResult getSupplierReport(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/report/product")
    ResponseResult getProductReport(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/report/purchase")
    ResponseResult getPurchaseReport(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/report/quality")
    ResponseResult getQualityReport(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/report/delivery")
    ResponseResult getDeliveryReport(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/supplier/report/finance")
    ResponseResult getFinanceReport(@RequestParam Map<String, Object> param);

    // ================= 店铺前台接口 =================

    /**
     * 查询店铺列表（前台）
     */
    @PostMapping("/supplier/shop/list")
    ResponseResult querySupplierList(@RequestBody Map<String, Object> param);

    /**
     * 查询店铺商品（前台）
     */
    @PostMapping("/supplier/shop/goods")
    ResponseResult querySupplierGoods(@RequestBody Map<String, Object> param);
}