package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.SupplierServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

import java.util.Map;

/**
 * 供应商Web控制器
/**
 * 
 * @description 提供供应商相关功能的Web接口
 * @author Amos
 * @date 2024-12-20
 */
@RestController
@Profile("legacy-web-routes")
@RequestMapping("/web/supplier")
public class WebSupplierController {

    @Autowired
    private SupplierServiceFeign supplierServiceFeign;

    /** ================= 供应商管理相关接口================= */
    
    @GetMapping("/list")
    public ResponseResult getSupplierList(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getSupplierList(param);
    }
    
    @GetMapping("/detail")
    public ResponseResult getSupplierDetail(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getSupplierDetail(param);
    }
    
    @PostMapping("/create")
    public ResponseResult createSupplier(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.createSupplier(param);
    }
    
    @PostMapping("/update")
    public ResponseResult updateSupplier(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.updateSupplier(param);
    }
    
    @PostMapping("/enable")
    public ResponseResult enableSupplier(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.enableSupplier(param);
    }
    
    @PostMapping("/disable")
    public ResponseResult disableSupplier(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.disableSupplier(param);
    }
    
    @PostMapping("/delete")
    public ResponseResult deleteSupplier(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.deleteSupplier(param);
    }
    
    @GetMapping("/audit/list")
    public ResponseResult getSupplierAuditList(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getSupplierAuditList(param);
    }
    
    @PostMapping("/audit/approve")
    public ResponseResult approveSupplier(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.approveSupplier(param);
    }
    
    @PostMapping("/audit/reject")
    public ResponseResult rejectSupplier(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.rejectSupplier(param);
    }

    /** ================= 供应商分类相关接口================= */
    
    @GetMapping("/category/list")
    public ResponseResult getSupplierCategoryList(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getSupplierCategoryList(param);
    }
    
    @GetMapping("/category/detail")
    public ResponseResult getSupplierCategoryDetail(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getSupplierCategoryDetail(param);
    }
    
    @PostMapping("/category/create")
    public ResponseResult createSupplierCategory(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.createSupplierCategory(param);
    }
    
    @PostMapping("/category/update")
    public ResponseResult updateSupplierCategory(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.updateSupplierCategory(param);
    }
    
    @PostMapping("/category/delete")
    public ResponseResult deleteSupplierCategory(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.deleteSupplierCategory(param);
    }

    /** ================= 供应商合同相关接口================= */
    
    @GetMapping("/contract/list")
    public ResponseResult getContractList(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getContractList(param);
    }
    
    @GetMapping("/contract/detail")
    public ResponseResult getContractDetail(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getContractDetail(param);
    }
    
    @PostMapping("/contract/create")
    public ResponseResult createContract(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.createContract(param);
    }
    
    @PostMapping("/contract/update")
    public ResponseResult updateContract(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.updateContract(param);
    }
    
    @PostMapping("/contract/approve")
    public ResponseResult approveContract(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.approveContract(param);
    }
    
    @PostMapping("/contract/sign")
    public ResponseResult signContract(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.signContract(param);
    }
    
    @PostMapping("/contract/terminate")
    public ResponseResult terminateContract(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.terminateContract(param);
    }

    /** ================= 供应商产品相关接口================= */
    
    @GetMapping("/product/list")
    public ResponseResult getProductList(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getProductList(param);
    }
    
    @GetMapping("/product/detail")
    public ResponseResult getProductDetail(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getProductDetail(param);
    }
    
    @PostMapping("/product/create")
    public ResponseResult createProduct(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.createProduct(param);
    }
    
    @PostMapping("/product/update")
    public ResponseResult updateProduct(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.updateProduct(param);
    }
    
    @PostMapping("/product/enable")
    public ResponseResult enableProduct(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.enableProduct(param);
    }
    
    @PostMapping("/product/disable")
    public ResponseResult disableProduct(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.disableProduct(param);
    }
    
    @PostMapping("/product/delete")
    public ResponseResult deleteProduct(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.deleteProduct(param);
    }

    /** ================= 供应商采购相关接口================= */
    
    @GetMapping("/purchase/list")
    public ResponseResult getPurchaseList(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getPurchaseList(param);
    }
    
    @GetMapping("/purchase/detail")
    public ResponseResult getPurchaseDetail(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getPurchaseDetail(param);
    }
    
    @PostMapping("/purchase/create")
    public ResponseResult createPurchase(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.createPurchase(param);
    }
    
    @PostMapping("/purchase/update")
    public ResponseResult updatePurchase(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.updatePurchase(param);
    }
    
    @PostMapping("/purchase/approve")
    public ResponseResult approvePurchase(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.approvePurchase(param);
    }
    
    @PostMapping("/purchase/reject")
    public ResponseResult rejectPurchase(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.rejectPurchase(param);
    }
    
    @PostMapping("/purchase/complete")
    public ResponseResult completePurchase(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.completePurchase(param);
    }
    
    @PostMapping("/purchase/cancel")
    public ResponseResult cancelPurchase(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.cancelPurchase(param);
    }

    /** ================= 供应商评价相关接口================= */
    
    @GetMapping("/evaluation/list")
    public ResponseResult getEvaluationList(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getEvaluationList(param);
    }
    
    @GetMapping("/evaluation/detail")
    public ResponseResult getEvaluationDetail(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getEvaluationDetail(param);
    }
    
    @PostMapping("/evaluation/create")
    public ResponseResult createEvaluation(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.createEvaluation(param);
    }
    
    @PostMapping("/evaluation/reply")
    public ResponseResult replyEvaluation(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.replyEvaluation(param);
    }

    /** ================= 供应商统计报表相关接口================= */
    
    @GetMapping("/report/supplier")
    public ResponseResult getSupplierReport(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getSupplierReport(param);
    }
    
    @GetMapping("/report/product")
    public ResponseResult getProductReport(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getProductReport(param);
    }
    
    @GetMapping("/report/purchase")
    public ResponseResult getPurchaseReport(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getPurchaseReport(param);
    }
    
    @GetMapping("/report/quality")
    public ResponseResult getQualityReport(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getQualityReport(param);
    }
    
    @GetMapping("/report/delivery")
    public ResponseResult getDeliveryReport(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getDeliveryReport(param);
    }
    
    @GetMapping("/report/finance")
    public ResponseResult getFinanceReport(@RequestParam Map<String, Object> param) {
        return supplierServiceFeign.getFinanceReport(param);
    }
}
