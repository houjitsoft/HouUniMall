package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

/**
 * Web POS服务Feign客户端降级处理
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@Component
public class WebposServiceFeignFallback implements WebposServiceFeign {

    @Override
    public ResponseResult getPosGoods(Long categoryId, String keyword) {
        return ResponseResult.error("获取POS商品列表失败");
    }

    @Override
    public ResponseResult createPosOrder(Object orderData) {
        return ResponseResult.error("创建POS订单失败");
    }

    @Override
    public ResponseResult getPosOrders(Integer pageNum, Integer pageSize, String status) {
        return ResponseResult.error("获取POS订单列表失败");
    }

    @Override
    public ResponseResult getPosOrderDetail(Long orderId) {
        return ResponseResult.error("获取POS订单详情失败");
    }

    @Override
    public ResponseResult payPosOrder(Long orderId, Object paymentData) {
        return ResponseResult.error("POS订单支付失败");
    }

    @Override
    public ResponseResult getPosStatistics(String startDate, String endDate) {
        return ResponseResult.error("获取POS收银统计失败");
    }

    @Override
    public ResponseResult getPosCategories() {
        return ResponseResult.error("获取POS商品分类失败");
    }

    @Override
    public ResponseResult getTodaySummary() {
        return ResponseResult.error("获取POS今日汇总失败");
    }

    @Override
    public ResponseResult refundPosOrder(Long orderId, Object refundData) {
        return ResponseResult.error("POS订单退款失败");
    }

    @Override
    public ResponseResult getPosMembers(String keyword) {
        return ResponseResult.error("获取POS会员列表失败");
    }
}