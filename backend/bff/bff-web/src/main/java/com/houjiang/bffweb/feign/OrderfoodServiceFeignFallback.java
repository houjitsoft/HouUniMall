package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 外卖订单服务Feign客户端降级处理
/**
 * 只做降级处理，不包含业务逻辑
 * @author Amos
 */
@Component
public class OrderfoodServiceFeignFallback implements OrderfoodServiceFeign {

    /** ================= 外卖订单管理相关接口 ================= */
    
    @Override
    public ResponseResult getOrderList(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getOrderDetail(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createOrder(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateOrder(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult cancelOrder(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult confirmOrder(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult finishOrder(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult completeOrder(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getOrderStatus(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    /** ================= 餐厅管理相关接口 ================= */
    
    @Override
    public ResponseResult getRestaurantList(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRestaurantDetail(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createRestaurant(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateRestaurant(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableRestaurant(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult disableRestaurant(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getRestaurantMenu(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    /** ================= 菜品管理相关接口 ================= */
    
    @Override
    public ResponseResult getDishList(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDishDetail(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createDish(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateDish(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableDish(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult disableDish(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult soldOutDish(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDishCategory(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    /** ================= 配送管理相关接口 ================= */
    
    @Override
    public ResponseResult getDeliveryTrack(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDeliveryList(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getDeliveryDetail(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult assignDelivery(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateDeliveryStatus(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult confirmPickup(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult confirmDeliver(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    /** ================= 评价管理相关接口 ================= */
    
    @Override
    public ResponseResult getReviewList(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getEvaluationList(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getEvaluationDetail(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createEvaluation(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult replyEvaluation(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult addReview(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult replyReview(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    /** ================= 优惠券管理相关接口 ================= */
    
    @Override
    public ResponseResult getCouponList(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult getCouponDetail(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createCoupon(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateCoupon(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult enableCoupon(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult disableCoupon(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult receiveCoupon(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult useCoupon(Map<String, Object> param) {
        return ResponseResult.fail("外卖订单服务暂时不可用，请稍后重试");
    }
}
