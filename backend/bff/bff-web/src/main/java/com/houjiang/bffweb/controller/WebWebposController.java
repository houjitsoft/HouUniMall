package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.WebposServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Web POS服务Web控制器
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/webpos")
public class WebWebposController {

    @Autowired
    private WebposServiceFeign webposServiceFeign;

    /**
     * 获取POS商品列表
     */
    @GetMapping("/goods")
    public ResponseResult getPosGoods(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                    @RequestParam(value = "keyword", required = false) String keyword) {
        return webposServiceFeign.getPosGoods(categoryId, keyword);
    }

    /**
     * 创建POS订单
     */
    @PostMapping("/order")
    public ResponseResult createPosOrder(@RequestBody Object orderData) {
        return webposServiceFeign.createPosOrder(orderData);
    }

    /**
     * 获取POS订单列表
     */
    @GetMapping("/orders")
    public ResponseResult getPosOrders(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                   @RequestParam(value = "status", required = false) String status) {
        return webposServiceFeign.getPosOrders(pageNum, pageSize, status);
    }

    /**
     * 获取POS订单详情
     */
    @GetMapping("/order/{orderId}")
    public ResponseResult getPosOrderDetail(@PathVariable("orderId") Long orderId) {
        return webposServiceFeign.getPosOrderDetail(orderId);
    }

    /**
     * POS订单支付
     */
    @PostMapping("/order/{orderId}/pay")
    public ResponseResult payPosOrder(@PathVariable("orderId") Long orderId,
                                    @RequestBody Object paymentData) {
        return webposServiceFeign.payPosOrder(orderId, paymentData);
    }

    /**
     * 获取POS收银统计
     */
    @GetMapping("/statistics")
    public ResponseResult getPosStatistics(@RequestParam(value = "startDate", required = false) String startDate,
                                         @RequestParam(value = "endDate", required = false) String endDate) {
        return webposServiceFeign.getPosStatistics(startDate, endDate);
    }

    /**
     * 获取POS商品分类
     */
    @GetMapping("/categories")
    public ResponseResult getPosCategories() {
        return webposServiceFeign.getPosCategories();
    }

    /**
     * 获取POS今日汇总
     */
    @GetMapping("/summary/today")
    public ResponseResult getTodaySummary() {
        return webposServiceFeign.getTodaySummary();
    }

    /**
     * POS订单退款
     */
    @PostMapping("/order/{orderId}/refund")
    public ResponseResult refundPosOrder(@PathVariable("orderId") Long orderId,
                                       @RequestBody Object refundData) {
        return webposServiceFeign.refundPosOrder(orderId, refundData);
    }

    /**
     * 获取POS会员列表
     */
    @GetMapping("/members")
    public ResponseResult getPosMembers(@RequestParam(value = "keyword", required = false) String keyword) {
        return webposServiceFeign.getPosMembers(keyword);
    }
}
