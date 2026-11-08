package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 供应商服务降级处理类
/**
 * 
 * @description 当供应商服务不可用时，提供降级响应
 * @author Amos
 * @date 2024-12-20
 */
@Component
public class SupplierServiceFeignFallback implements SupplierServiceFeign {

    @Override
    public ResponseResult getSupplierList(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierDetail(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createSupplier(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateSupplier(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteSupplier(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableSupplier(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult disableSupplier(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult auditSupplier(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierAuditList(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approveSupplier(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult rejectSupplier(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierCategoryList(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierCategoryDetail(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createSupplierCategory(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateSupplierCategory(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteSupplierCategory(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractList(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getContractDetail(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createContract(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateContract(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult signContract(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult terminateContract(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approveContract(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierProductList(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProductList(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierProductDetail(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProductDetail(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createSupplierProduct(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createProduct(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateSupplierProduct(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateProduct(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableSupplierProduct(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableProduct(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult disableSupplierProduct(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult disableProduct(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteProduct(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getPurchaseList(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getPurchaseDetail(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createPurchase(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approvePurchase(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult rejectPurchase(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult completePurchase(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updatePurchase(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult cancelPurchase(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getEvaluationList(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addEvaluation(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getEvaluationDetail(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createEvaluation(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult replyEvaluation(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierStatistics(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCooperationStatistics(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getQualityStatistics(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getSupplierReport(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getProductReport(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getPurchaseReport(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getQualityReport(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDeliveryReport(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getFinanceReport(Map<String, Object> param) {
        return ResponseResult.fail("供应商服务暂时不可用，请稍后重试");
    }

    /** ================= 店铺前台接口降级 ================= */

    @Override
    public ResponseResult querySupplierList(Map<String, Object> param) {
        return ResponseResult.fail("店铺服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult querySupplierGoods(Map<String, Object> param) {
        return ResponseResult.fail("店铺服务暂时不可用，请稍后重试");
    }
}