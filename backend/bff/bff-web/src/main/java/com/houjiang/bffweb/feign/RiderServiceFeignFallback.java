package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 骑手服务Feign客户端降级处理
/**
 * 只做降级处理，不包含业务逻辑
 * @author Amos
 */
@Component
public class RiderServiceFeignFallback implements RiderServiceFeign {

    /** ================= 骑手管理相关接口 ================= */
    
    @Override
    public ResponseResult getRiderList(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderDetail(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult registerRider(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createRider(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateRider(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deleteRider(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approveRider(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult rejectRider(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableRider(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult disableRider(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    /** ================= 骑手认证相关接口 ================= */
    
    @Override
    public ResponseResult getRiderAuthList(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderAuthDetail(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult submitRiderAuth(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult approveRiderAuth(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult rejectRiderAuth(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }
    
    @Override
    public ResponseResult submitCertification(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCertificationStatus(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult reSubmitCertification(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    /** ================= 订单配送相关接口 ================= */
    
    @Override
    public ResponseResult getRiderOrderList(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderOrderDetail(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderDeliveryList(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderDeliveryDetail(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult grabOrder(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult acceptOrder(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult acceptDelivery(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult pickupOrder(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult confirmDeliveryPickup(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult deliverOrder(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult confirmDeliveryDeliver(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult cancelDelivery(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult completeOrder(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    /** ================= 位置管理相关接口 ================= */
    
    @Override
    public ResponseResult updateRiderLocation(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderLocation(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderLocationHistory(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    /** ================= 收益管理相关接口 ================= */
    
    @Override
    public ResponseResult getTodayIncome(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getTotalIncome(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderIncomeDetail(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderIncomeStatistics(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderWithdrawList(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult applyRiderWithdraw(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    /** ================= 绩效统计相关接口 ================= */
    
    @Override
    public ResponseResult getRiderPerformanceStatistics(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderPerformanceRanking(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRiderPerformanceDetail(Map<String, Object> param) {
        return ResponseResult.fail("骑手服务暂时不可用，请稍后重试");
    }
}