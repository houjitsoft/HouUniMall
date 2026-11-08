package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Web POS服务Feign客户端
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@FeignClient(name = "webpos", url = "${feign.webpos.url:}", path = "/plugin/webpos", contextId = "webposServiceFeign")
public interface WebposServiceFeign {

    /**
     * 获取POS商品列表
     */
    @GetMapping("/goods")
    ResponseResult getPosGoods(@RequestParam(value = "categoryId", required = false) Long categoryId,
                             @RequestParam(value = "keyword", required = false) String keyword);

    /**
     * 创建POS订单
     */
    @PostMapping("/order")
    ResponseResult createPosOrder(@RequestBody Object orderData);

    /**
     * 获取POS订单列表
     */
    @GetMapping("/orders")
    ResponseResult getPosOrders(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                            @RequestParam(value = "status", required = false) String status);

    /**
     * 获取POS订单详情
     */
    @GetMapping("/order/{orderId}")
    ResponseResult getPosOrderDetail(@PathVariable("orderId") Long orderId);

    /**
     * POS订单支付
     */
    @PostMapping("/order/{orderId}/pay")
    ResponseResult payPosOrder(@PathVariable("orderId") Long orderId,
                             @RequestBody Object paymentData);

    /**
     * 获取POS收银统计
     */
    @GetMapping("/statistics")
    ResponseResult getPosStatistics(@RequestParam(value = "startDate", required = false) String startDate,
                                  @RequestParam(value = "endDate", required = false) String endDate);

    /**
     * 获取POS商品分类
     */
    @GetMapping("/categories")
    ResponseResult getPosCategories();

    /**
     * 获取POS今日汇总
     */
    @GetMapping("/summary/today")
    ResponseResult getTodaySummary();

    /**
     * POS订单退款
     */
    @PostMapping("/order/{orderId}/refund")
    ResponseResult refundPosOrder(@PathVariable("orderId") Long orderId,
                                @RequestBody Object refundData);

    /**
     * 获取POS会员列表
     */
    @GetMapping("/members")
    ResponseResult getPosMembers(@RequestParam(value = "keyword", required = false) String keyword);
}
