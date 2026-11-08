package com.houjiang.mall.controller.distribution;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.distribution.DistributionOrderHookService;
import com.houjiang.mall.service.distribution.DistributionSettleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 分销内部事件端点（订单服务 → 商城服务）
 *
 * <p>路径 {@code /internal/distribution/**} 由 {@code PluginInternalJobTokenFilter}
 * 用 {@code X-Internal-Job-Token} 认证（与插件内部任务同源的凭据），并在
 * {@code SecurityConfig} 中 permitAll，不带用户态。
 *
 * <p>调用方：order 服务的 {@code MallDistributionClient}（Feign）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/internal/distribution")
@RequiredArgsConstructor
public class DistributionHookController {

    private final DistributionOrderHookService distributionOrderHookService;
    private final DistributionSettleService distributionSettleService;

    /** 下单事件：登记分销订单 */
    @PostMapping("/order/create")
    public ResponseResult<Map<String, Object>> orderCreate(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> body = params == null ? new HashMap<>() : params;
        try {
            return ResponseResult.success(distributionOrderHookService.onCreate(body));
        } catch (Exception e) {
            log.warn("分销订单登记失败: params={}, err={}", body, e.getMessage());
            Map<String, Object> res = new HashMap<>();
            res.put("ok", false);
            res.put("reason", e.getMessage());
            return ResponseResult.success(res);
        }
    }

    /** 订单完成（确认收货）：置已完成并生成佣金；未登记的订单会先用入参补登记 */
    @PostMapping("/order/finish")
    public ResponseResult<Map<String, Object>> orderFinish(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> body = params == null ? new HashMap<>() : params;
        try {
            return ResponseResult.success(distributionOrderHookService.onFinish(body));
        } catch (Exception e) {
            log.warn("分销订单完成处理失败: params={}, err={}", body, e.getMessage());
            Map<String, Object> res = new HashMap<>();
            res.put("ok", false);
            res.put("reason", e.getMessage());
            return ResponseResult.success(res);
        }
    }

    /**
     * 订单失效 / 退款：按 refundAmount 等比冲减待结算；无金额或 fullInvalidate=true 则整单作废。
     * body: orderId / refundAmount? / fullInvalidate?
     */
    @PostMapping("/order/cancel")
    public ResponseResult<Map<String, Object>> orderCancel(@RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> body = params == null ? new HashMap<>() : params;
        try {
            return ResponseResult.success(distributionOrderHookService.onCancel(body));
        } catch (Exception e) {
            log.warn("分销订单失效处理失败: params={}, err={}", body, e.getMessage());
            Map<String, Object> res = new HashMap<>();
            res.put("ok", false);
            res.put("reason", e.getMessage());
            return ResponseResult.success(res);
        }
    }

    /** 自动结算：把到期的待结算佣金转为可提现（供定时任务/调度器调用） */
    @PostMapping("/settle/due")
    public ResponseResult<Map<String, Object>> settleDue(@RequestBody(required = false) Map<String, Object> params) {
        Long shopId = params == null ? null : toLong(params.get("shopId"));
        return ResponseResult.success(distributionSettleService.settleDueRecords(shopId));
    }

    /**
     * 手动触发孤儿分销订单清理（运维用）。
     *
     * <p>定时任务每天跑一次，这里给运维一个即时入口：下单登记后长期未付款 /
     * 下单事务回滚残留的「已下单」记录，超过 days 天且未产生佣金的会被逻辑删除。
     */
    @PostMapping("/order/cleanup")
    public ResponseResult<Map<String, Object>> cleanupStale(@RequestBody(required = false) Map<String, Object> params) {
        int days = params == null ? 7 : intVal(params.get("days"), 7);
        Map<String, Object> res = new HashMap<>();
        res.put("removed", distributionOrderHookService.cleanupStaleOrders(days));
        res.put("days", days);
        return ResponseResult.success(res);
    }

    private static int intVal(Object value, int def) {
        if (value == null) {
            return def;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
