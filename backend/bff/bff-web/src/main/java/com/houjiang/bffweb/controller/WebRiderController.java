package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.RiderServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 骑手Web控制器
/**
 * 
 * @description 提供骑手相关功能的Web接口
 * @author Amos
 * @date 2024-12-20
 */
@RestController
@RequestMapping("/web/rider")
public class WebRiderController {

    @Autowired
    private RiderServiceFeign riderServiceFeign;

    /** ================= 骑手管理相关接口 ================= */
    
    @GetMapping("/list")
    public ResponseResult getRiderList(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderList(param);
    }
    
    @GetMapping("/detail")
    public ResponseResult getRiderDetail(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderDetail(param);
    }
    
    @PostMapping("/create")
    public ResponseResult createRider(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.createRider(param);
    }
    
    @PostMapping("/update")
    public ResponseResult updateRider(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.updateRider(param);
    }
    
    @PostMapping("/enable")
    public ResponseResult enableRider(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.enableRider(param);
    }
    
    @PostMapping("/disable")
    public ResponseResult disableRider(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.disableRider(param);
    }
    
    @PostMapping("/delete")
    public ResponseResult deleteRider(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.deleteRider(param);
    }

    /** ================= 骑手认证相关接口 ================= */
    
    @GetMapping("/auth/list")
    public ResponseResult getRiderAuthList(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderAuthList(param);
    }
    
    @GetMapping("/auth/detail")
    public ResponseResult getRiderAuthDetail(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderAuthDetail(param);
    }
    
    @PostMapping("/auth/submit")
    public ResponseResult submitRiderAuth(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.submitRiderAuth(param);
    }
    
    @PostMapping("/auth/approve")
    public ResponseResult approveRiderAuth(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.approveRiderAuth(param);
    }
    
    @PostMapping("/auth/reject")
    public ResponseResult rejectRiderAuth(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.rejectRiderAuth(param);
    }

    /** ================= 订单配送相关接口================= */
    
    @GetMapping("/delivery/list")
    public ResponseResult getRiderDeliveryList(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderDeliveryList(param);
    }
    
    @GetMapping("/delivery/detail")
    public ResponseResult getRiderDeliveryDetail(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderDeliveryDetail(param);
    }
    
    @PostMapping("/delivery/accept")
    public ResponseResult acceptDelivery(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.acceptDelivery(param);
    }
    
    @PostMapping("/delivery/pickup")
    public ResponseResult confirmDeliveryPickup(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.confirmDeliveryPickup(param);
    }
    
    @PostMapping("/delivery/deliver")
    public ResponseResult confirmDeliveryDeliver(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.confirmDeliveryDeliver(param);
    }
    
    @PostMapping("/delivery/cancel")
    public ResponseResult cancelDelivery(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.cancelDelivery(param);
    }

    /** ================= 位置管理相关接口 ================= */
    
    @PostMapping("/location/update")
    public ResponseResult updateRiderLocation(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.updateRiderLocation(param);
    }
    
    @GetMapping("/location/get")
    public ResponseResult getRiderLocation(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderLocation(param);
    }
    
    @GetMapping("/location/history")
    public ResponseResult getRiderLocationHistory(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderLocationHistory(param);
    }

    /** ================= 收益管理相关接口 ================= */
    
    @GetMapping("/income/statistics")
    public ResponseResult getRiderIncomeStatistics(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderIncomeStatistics(param);
    }
    
    @GetMapping("/income/detail")
    public ResponseResult getRiderIncomeDetail(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderIncomeDetail(param);
    }
    
    @GetMapping("/income/withdraw/list")
    public ResponseResult getRiderWithdrawList(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderWithdrawList(param);
    }
    
    @PostMapping("/income/withdraw/apply")
    public ResponseResult applyRiderWithdraw(@RequestBody Map<String, Object> param) {
        return riderServiceFeign.applyRiderWithdraw(param);
    }

    /** ================= 绩效统计相关接口 ================= */
    
    @GetMapping("/performance/statistics")
    public ResponseResult getRiderPerformanceStatistics(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderPerformanceStatistics(param);
    }
    
    @GetMapping("/performance/ranking")
    public ResponseResult getRiderPerformanceRanking(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderPerformanceRanking(param);
    }
    
    @GetMapping("/performance/detail")
    public ResponseResult getRiderPerformanceDetail(@RequestParam Map<String, Object> param) {
        return riderServiceFeign.getRiderPerformanceDetail(param);
    }
}