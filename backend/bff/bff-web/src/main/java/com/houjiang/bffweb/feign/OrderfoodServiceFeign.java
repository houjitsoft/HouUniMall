package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 外卖订单服务Feign客户端
/**
 * 
 * @description 提供外卖订单相关功能的远程调用接口
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(value = "orderfood", fallback = OrderfoodServiceFeignFallback.class, url = "${feign.orderfood.url:}", path = "/plugin")
public interface OrderfoodServiceFeign {

    // ================= 外卖订单管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/order/list")
    ResponseResult getOrderList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/order/detail")
    ResponseResult getOrderDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/order/create")
    ResponseResult createOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/order/update")
    ResponseResult updateOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/order/cancel")
    ResponseResult cancelOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/order/confirm")
    ResponseResult confirmOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/order/finish")
    ResponseResult finishOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/order/complete")
    ResponseResult completeOrder(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/order/status")
    ResponseResult getOrderStatus(@RequestParam Map<String, Object> param);

    // ================= 餐厅管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/restaurant/list")
    ResponseResult getRestaurantList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/restaurant/detail")
    ResponseResult getRestaurantDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/restaurant/create")
    ResponseResult createRestaurant(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/restaurant/update")
    ResponseResult updateRestaurant(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/restaurant/enable")
    ResponseResult enableRestaurant(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/restaurant/disable")
    ResponseResult disableRestaurant(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/restaurant/menu")
    ResponseResult getRestaurantMenu(@RequestParam Map<String, Object> param);

    // ================= 菜品管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/dish/list")
    ResponseResult getDishList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/dish/detail")
    ResponseResult getDishDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/dish/create")
    ResponseResult createDish(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/dish/update")
    ResponseResult updateDish(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/dish/enable")
    ResponseResult enableDish(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/dish/disable")
    ResponseResult disableDish(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/dish/sold-out")
    ResponseResult soldOutDish(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/dish/category")
    ResponseResult getDishCategory(@RequestParam Map<String, Object> param);

    // ================= 配送管理相关接口=================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/delivery/track")
    ResponseResult getDeliveryTrack(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/delivery/list")
    ResponseResult getDeliveryList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/delivery/detail")
    ResponseResult getDeliveryDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/delivery/assign")
    ResponseResult assignDelivery(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/delivery/update-status")
    ResponseResult updateDeliveryStatus(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/delivery/pickup")
    ResponseResult confirmPickup(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/delivery/deliver")
    ResponseResult confirmDeliver(@RequestBody Map<String, Object> param);

    // ================= 评价管理相关接口 =================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/review/list")
    ResponseResult getReviewList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/evaluation/list")
    ResponseResult getEvaluationList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/evaluation/detail")
    ResponseResult getEvaluationDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/evaluation/create")
    ResponseResult createEvaluation(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/evaluation/reply")
    ResponseResult replyEvaluation(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/review/add")
    ResponseResult addReview(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/review/reply")
    ResponseResult replyReview(@RequestBody Map<String, Object> param);

    // ================= 优惠券管理相关接口=================
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/coupon/list")
    ResponseResult getCouponList(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @GetMapping("/orderfood/coupon/detail")
    ResponseResult getCouponDetail(@RequestParam Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/coupon/create")
    ResponseResult createCoupon(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/coupon/update")
    ResponseResult updateCoupon(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/coupon/enable")
    ResponseResult enableCoupon(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/coupon/disable")
    ResponseResult disableCoupon(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/coupon/receive")
    ResponseResult receiveCoupon(@RequestBody Map<String, Object> param);
    
    /**
     * TODO 补充方法说明
/**
     * @param param TODO
     * @return TODO
     */
    @PostMapping("/orderfood/coupon/use")
    ResponseResult useCoupon(@RequestBody Map<String, Object> param);
}