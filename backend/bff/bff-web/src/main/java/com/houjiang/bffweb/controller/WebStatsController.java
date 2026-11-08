package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付统计 BFF 控制器
/**
 *
 * <p>路径前缀：/web/pay/stats，透传到 paas/payment 的 /api/payment/stats/*</p>
 *
 * @author Amos
 * @since 2026-05
 */
@Slf4j
@RestController
@RequestMapping("/web/pay/stats")
public class WebStatsController {

    @Autowired
    private PaymentServiceFeign paymentServiceFeign;

    /** 统计概览（今日收入/本月收入/今日订单/成功率） */
    @PostMapping("/overview")
    public ResponseResult overview(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.statsOverview(param == null ? new HashMap<>() : param);
    }

    /** 收入趋势（近N天的每日收入和订单量） */
    @PostMapping("/trend")
    public ResponseResult trend(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.statsTrend(param == null ? new HashMap<>() : param);
    }

    /** 渠道分布（支付方式饼图数据） */
    @PostMapping("/channelDist")
    public ResponseResult channelDist(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.statsChannelDist(param == null ? new HashMap<>() : param);
    }

    /** 商户 TOP N 交易排行（柱状图数据） */
    @PostMapping("/merchantRank")
    public ResponseResult merchantRank(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.statsMerchantRank(param == null ? new HashMap<>() : param);
    }
}
