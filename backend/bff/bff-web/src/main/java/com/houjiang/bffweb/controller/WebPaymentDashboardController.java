package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付中心工作台（Dashboard）BFF 控制器
 *
 * <p>路径：/web/payment/dashboard/*
 * 前端入口：frontend/admin/src/views/payment/PaymentDashboard.vue</p>
 *
 * <p>概览指标透传 paas/payment 的 StatsController#overview（实时聚合
 * hou_pay_consume_trade / hou_pay_refund_order），返回：
 * 今日交易金额、今日交易笔数、今日退款金额、支付成功率。</p>
 *
 * @author Amos
 * @since 2026-05
 */
@Slf4j
@RestController
@RequestMapping("/web/payment/dashboard")
public class WebPaymentDashboardController {

    @Autowired
    private PaymentServiceFeign paymentServiceFeign;

    /**
     * 获取支付中心工作台概览指标
     *
     * <p>透传 payment 的 /api/payment/stats/overview 并映射为前端字段：
     * <ul>
     *   <li>todayAmount   - 今日交易金额（元）</li>
     *   <li>todayCount    - 今日交易笔数</li>
     *   <li>todayRefund   - 今日退款金额（元）</li>
     *   <li>successRate   - 支付成功率（百分比数值，如 99.5 表示 99.5%）</li>
     * </ul></p>
     */
    @PostMapping("/overview")
    public ResponseResult overview(@RequestBody(required = false) Map<String, Object> req) {
        ResponseResult result = paymentServiceFeign.statsOverview(req == null ? new HashMap<>() : req);
        if (result == null || result.getData() == null) {
            return result;
        }
        Object data = result.getData();
        Map<String, Object> mapped = new LinkedHashMap<>();
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> src = (Map<String, Object>) data;
            mapped.put("todayAmount", num(src.get("todayIncome")));
            mapped.put("todayCount", num(src.get("todayOrders")));
            mapped.put("todayRefund", num(src.get("todayRefund")));
            mapped.put("successRate", num(src.get("successRate")));
        }
        return ResponseResult.success(mapped);
    }

    private static Number num(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return (Number) v;
        try {
            return Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}