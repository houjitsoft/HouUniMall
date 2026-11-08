package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 对账清算 BFF 控制器（W4-T3）
/**
 *
 * <p>路径前缀：/web/pay/recon，统一透传到 paas/payment 的 /api/payment/recon/*</p>
 *
 * @author Amos
 * @since 2026-05
 */
@Slf4j
@RestController
@RequestMapping("/web/pay/recon")
public class WebReconController {

    @Autowired
    private PaymentServiceFeign paymentServiceFeign;

    /** 渠道对账单拉取（T+1 批量拉取指定渠道/日期账单） */
    @PostMapping("/pullChannelBill")
    public ResponseResult pullChannelBill(@RequestBody Map<String, Object> param) {
        log.info("[bff/recon] pullChannelBill param={}", param);
        return paymentServiceFeign.reconPullChannelBill(param);
    }

    /** 对账差异分页列表 */
    @PostMapping("/diffList")
    public ResponseResult diffList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.reconDiffList(param == null ? new java.util.HashMap<>() : param);
    }

    /** 对账差异处理（补单/冲正/人工关闭） */
    @PostMapping("/diffHandle")
    public ResponseResult diffHandle(@RequestBody Map<String, Object> param) {
        log.info("[bff/recon] diffHandle diffId={}", param == null ? null : param.get("id"));
        return paymentServiceFeign.reconDiffHandle(param);
    }

    /** 对账报告生成 */
    @PostMapping("/reportGen")
    public ResponseResult reportGen(@RequestBody Map<String, Object> param) {
        return paymentServiceFeign.reconReportGen(param);
    }

    /** 对账核对（比对本地订单与渠道账单） */
    @PostMapping("/diffCheck")
    public ResponseResult diffCheck(@RequestBody Map<String, Object> param) {
        log.info("[bff/recon] diffCheck param={}", param);
        return paymentServiceFeign.reconDiffCheck(param);
    }

    /** 对账报表分页查询 */
    @PostMapping("/reportList")
    public ResponseResult reportList(@RequestBody(required = false) Map<String, Object> param) {
        return paymentServiceFeign.reconReportList(param == null ? new java.util.HashMap<>() : param);
    }

    /** 手动触发对账（拉取+核对） */
    @PostMapping("/trigger")
    public ResponseResult trigger(@RequestBody(required = false) Map<String, Object> param) {
        log.info("[bff/recon] trigger param={}", param);
        return paymentServiceFeign.reconTrigger(param == null ? new java.util.HashMap<>() : param);
    }
}
