package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 骑手服务Feign客户端
/**
 * 
 * @description 提供骑手管理相关功能的远程调用接口
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(value = "rider", fallback = RiderServiceFeignFallback.class, url = "${feign.rider.url:}", path = "/plugin")
public interface RiderServiceFeign {

    // ================= 骑手管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/list")
    ResponseResult getRiderList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/detail")
    ResponseResult getRiderDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/register")
    ResponseResult registerRider(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/create")
    ResponseResult createRider(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/update")
    ResponseResult updateRider(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/delete")
    ResponseResult deleteRider(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/approve")
    ResponseResult approveRider(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/reject")
    ResponseResult rejectRider(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/enable")
    ResponseResult enableRider(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/disable")
    ResponseResult disableRider(@RequestBody Map<String, Object> param);

    // ================= 骑手认证相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/auth/list")
    ResponseResult getRiderAuthList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/auth/detail")
    ResponseResult getRiderAuthDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/auth/submit")
    ResponseResult submitRiderAuth(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/auth/approve")
    ResponseResult approveRiderAuth(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/auth/reject")
    ResponseResult rejectRiderAuth(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/certification/submit")
    ResponseResult submitCertification(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/certification/status")
    ResponseResult getCertificationStatus(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/certification/re-submit")
    ResponseResult reSubmitCertification(@RequestBody Map<String, Object> param);

    // ================= 订单配送相关接口=================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/order/list")
    ResponseResult getRiderOrderList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/order/detail")
    ResponseResult getRiderOrderDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/delivery/list")
    ResponseResult getRiderDeliveryList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/delivery/detail")
    ResponseResult getRiderDeliveryDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/order/grab")
    ResponseResult grabOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/order/accept")
    ResponseResult acceptOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/delivery/accept")
    ResponseResult acceptDelivery(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/order/pickup")
    ResponseResult pickupOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/delivery/pickup")
    ResponseResult confirmDeliveryPickup(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/order/deliver")
    ResponseResult deliverOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/delivery/deliver")
    ResponseResult confirmDeliveryDeliver(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/delivery/cancel")
    ResponseResult cancelDelivery(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/order/complete")
    ResponseResult completeOrder(@RequestBody Map<String, Object> param);

    // ================= 位置管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/location/update")
    ResponseResult updateRiderLocation(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/location/get")
    ResponseResult getRiderLocation(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/location/history")
    ResponseResult getRiderLocationHistory(@RequestParam Map<String, Object> param);

    // ================= 收益管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/income/today")
    ResponseResult getTodayIncome(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/income/total")
    ResponseResult getTotalIncome(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/income/detail")
    ResponseResult getRiderIncomeDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/income/statistics")
    ResponseResult getRiderIncomeStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/withdraw/list")
    ResponseResult getRiderWithdrawList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/rider/withdraw/apply")
    ResponseResult applyRiderWithdraw(@RequestBody Map<String, Object> param);

    // ================= 绩效统计相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/statistics/performance")
    ResponseResult getRiderPerformanceStatistics(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/statistics/ranking")
    ResponseResult getRiderPerformanceRanking(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/rider/performance/detail")
    ResponseResult getRiderPerformanceDetail(@RequestParam Map<String, Object> param);
}