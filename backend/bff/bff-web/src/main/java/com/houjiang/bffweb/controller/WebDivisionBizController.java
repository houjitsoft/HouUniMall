package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 自研分账 BFF 控制器（W4-T6）
/**
 *
 * <p>路径前缀：/web/pay/division/biz，对应 paas/payment 的 /api/payment/division/biz/*</p>
 *
 * @author Amos
 * @since 2026-05
 */
@Slf4j
@RestController
@RequestMapping("/web/pay/division/biz")
public class WebDivisionBizController {

    @Autowired
    private PaymentServiceFeign paymentServiceFeign;

    /** 分账接收方绑定（万分比 shareRatio） */
    @PostMapping("/receiver/bind")
    public ResponseResult receiverBind(@RequestBody Map<String, Object> param) {
        log.info("[bff/divisionBiz] receiver/bind mchNo={}", param == null ? null : param.get("mchNo"));
        return paymentServiceFeign.divisionBizReceiverBind(param);
    }

    /** 分账接收方列表 */
    @PostMapping("/receiver/list")
    public ResponseResult receiverList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.divisionBizReceiverList(param == null ? new java.util.HashMap<>() : param);
    }

    /** 分账接收方解绑 */
    @PostMapping("/receiver/unbind")
    public ResponseResult receiverUnbind(@RequestBody Map<String, Object> param) {
        log.info("[bff/divisionBiz] receiver/unbind id={}", param == null ? null : param.get("id"));
        return paymentServiceFeign.divisionBizReceiverUnbind(param);
    }

    /** 分账规则引擎计算（按订单金额 + 接收方 shareRatio 返回每位接收方分账金额） */
    @PostMapping("/ruleEngine/calc")
    public ResponseResult ruleEngineCalc(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.divisionBizRuleEngineCalc(param);
    }
}
