package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.OrderfoodServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 外卖订单Web控制器
/**
 * 
 * @description 提供外卖订单相关功能的Web接口
 * @author Amos
 * @date 2024-12-20
 */
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/orderfood")
public class WebOrderfoodController {

    @Autowired
    private OrderfoodServiceFeign orderfoodServiceFeign;

    /** ================= 外卖订单管理相关接口 ================= */
    
    @GetMapping("/order/list")
    public ResponseResult getOrderList(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getOrderList(param);
    }
    
    @GetMapping("/order/detail")
    public ResponseResult getOrderDetail(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getOrderDetail(param);
    }
    
    @PostMapping("/order/create")
    public ResponseResult createOrder(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.createOrder(param);
    }
    
    @PostMapping("/order/update")
    public ResponseResult updateOrder(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.updateOrder(param);
    }
    
    @PostMapping("/order/cancel")
    public ResponseResult cancelOrder(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.cancelOrder(param);
    }
    
    @PostMapping("/order/confirm")
    public ResponseResult confirmOrder(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.confirmOrder(param);
    }
    
    @PostMapping("/order/complete")
    public ResponseResult completeOrder(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.completeOrder(param);
    }

    /** ================= 餐厅管理相关接口 ================= */
    
    @GetMapping("/restaurant/list")
    public ResponseResult getRestaurantList(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getRestaurantList(param);
    }
    
    @GetMapping("/restaurant/detail")
    public ResponseResult getRestaurantDetail(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getRestaurantDetail(param);
    }
    
    @PostMapping("/restaurant/create")
    public ResponseResult createRestaurant(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.createRestaurant(param);
    }
    
    @PostMapping("/restaurant/update")
    public ResponseResult updateRestaurant(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.updateRestaurant(param);
    }
    
    @PostMapping("/restaurant/enable")
    public ResponseResult enableRestaurant(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.enableRestaurant(param);
    }
    
    @PostMapping("/restaurant/disable")
    public ResponseResult disableRestaurant(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.disableRestaurant(param);
    }

    /** ================= 菜品管理相关接口 ================= */
    
    @GetMapping("/dish/list")
    public ResponseResult getDishList(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getDishList(param);
    }
    
    @GetMapping("/dish/detail")
    public ResponseResult getDishDetail(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getDishDetail(param);
    }
    
    @PostMapping("/dish/create")
    public ResponseResult createDish(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.createDish(param);
    }
    
    @PostMapping("/dish/update")
    public ResponseResult updateDish(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.updateDish(param);
    }
    
    @PostMapping("/dish/enable")
    public ResponseResult enableDish(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.enableDish(param);
    }
    
    @PostMapping("/dish/disable")
    public ResponseResult disableDish(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.disableDish(param);
    }
    
    @PostMapping("/dish/soldout")
    public ResponseResult soldOutDish(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.soldOutDish(param);
    }

    /** ================= 配送管理相关接口================= */
    
    @GetMapping("/delivery/list")
    public ResponseResult getDeliveryList(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getDeliveryList(param);
    }
    
    @GetMapping("/delivery/detail")
    public ResponseResult getDeliveryDetail(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getDeliveryDetail(param);
    }
    
    @PostMapping("/delivery/assign")
    public ResponseResult assignDelivery(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.assignDelivery(param);
    }
    
    @PostMapping("/delivery/pickup")
    public ResponseResult confirmPickup(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.confirmPickup(param);
    }
    
    @PostMapping("/delivery/deliver")
    public ResponseResult confirmDeliver(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.confirmDeliver(param);
    }

    /** ================= 评价管理相关接口 ================= */
    
    @GetMapping("/evaluation/list")
    public ResponseResult getEvaluationList(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getEvaluationList(param);
    }
    
    @GetMapping("/evaluation/detail")
    public ResponseResult getEvaluationDetail(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getEvaluationDetail(param);
    }
    
    @PostMapping("/evaluation/create")
    public ResponseResult createEvaluation(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.createEvaluation(param);
    }
    
    @PostMapping("/evaluation/reply")
    public ResponseResult replyEvaluation(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.replyEvaluation(param);
    }

    /** ================= 优惠券管理相关接口================= */
    
    @GetMapping("/coupon/list")
    public ResponseResult getCouponList(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getCouponList(param);
    }
    
    @GetMapping("/coupon/detail")
    public ResponseResult getCouponDetail(@RequestParam Map<String, Object> param) {
        return orderfoodServiceFeign.getCouponDetail(param);
    }
    
    @PostMapping("/coupon/create")
    public ResponseResult createCoupon(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.createCoupon(param);
    }
    
    @PostMapping("/coupon/update")
    public ResponseResult updateCoupon(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.updateCoupon(param);
    }
    
    @PostMapping("/coupon/enable")
    public ResponseResult enableCoupon(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.enableCoupon(param);
    }
    
    @PostMapping("/coupon/disable")
    public ResponseResult disableCoupon(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.disableCoupon(param);
    }
    
    @PostMapping("/coupon/receive")
    public ResponseResult receiveCoupon(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.receiveCoupon(param);
    }
    
    @PostMapping("/coupon/use")
    public ResponseResult useCoupon(@RequestBody Map<String, Object> param) {
        return orderfoodServiceFeign.useCoupon(param);
    }
}
