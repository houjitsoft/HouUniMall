package com.houjiang.mall.controller.distribution;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.distribution.DistributionSettleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分销佣金结算控制器
 *
 * 挂在 /mall/distribution/settle 下，经 BFF 商城模块直通通道访问，白名单已覆盖。
 * 供后台「佣金结算」页手动触发，也供定时任务 / 订单完成事件调用。
 *
 * @author Amos
 */
@RestController
@RequestMapping("/mall/distribution/settle")
@RequiredArgsConstructor
public class DistributionSettleController {

    private final DistributionSettleService distributionSettleService;

    /**
     * 一键结算：生成已完成订单的佣金 + 结算到期佣金（T+N）
     */
    @PostMapping("/run")
    public ResponseResult<Map<String, Object>> run(@RequestBody(required = false) Map<String, Object> params) {
        Long shopId = params != null && params.get("shopId") != null
                ? Long.parseLong(params.get("shopId").toString()) : null;
        return ResponseResult.success(distributionSettleService.runSettlement(shopId));
    }

    /**
     * 仅生成佣金（不结算）
     */
    @PostMapping("/generate")
    public ResponseResult<Map<String, Object>> generate(@RequestBody(required = false) Map<String, Object> params) {
        Long shopId = params != null && params.get("shopId") != null
                ? Long.parseLong(params.get("shopId").toString()) : null;
        return ResponseResult.success(distributionSettleService.generateCommissions(shopId));
    }

    /**
     * 仅结算到期佣金（T+N）
     */
    @PostMapping("/settle-due")
    public ResponseResult<Map<String, Object>> settleDue(@RequestBody(required = false) Map<String, Object> params) {
        Long shopId = params != null && params.get("shopId") != null
                ? Long.parseLong(params.get("shopId").toString()) : null;
        return ResponseResult.success(distributionSettleService.settleDueRecords(shopId));
    }

    /**
     * 结算概览：待结算 / 已结算金额与笔数
     */
    @PostMapping("/stats")
    public ResponseResult<Map<String, Object>> stats(@RequestBody(required = false) Map<String, Object> params) {
        Long shopId = params != null && params.get("shopId") != null
                ? Long.parseLong(params.get("shopId").toString()) : null;
        return ResponseResult.success(distributionSettleService.stats(shopId));
    }
}
