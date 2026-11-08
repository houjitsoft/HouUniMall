package com.houjiang.mall.controller.distribution;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.distribution.DistributionWithdraw;
import com.houjiang.mall.service.distribution.DistributionClientService;
import com.houjiang.mall.service.pay.MallCurrentUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 分销 C 端接口（买家/分销员视角）
 *
 * <p>路径 {@code /mall/distribution/client/**}，BFF 通道为
 * {@code /web/mall/module/distribution/client/**}。
 *
 * <p>所有接口都以 token 解析出的当前用户为维度，不接受入参 userId，杜绝越权。
 * 返回字段命名沿用 C 端旧分销中心（canWithdrawCommission / todaySettlement / …），
 * 便于前端按「换数据源」的方式改造而不是重写页面。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/distribution/client")
@RequiredArgsConstructor
public class DistributionClientController {

    private final DistributionClientService distributionClientService;
    private final MallCurrentUserResolver userResolver;

    /** 分销中心首页概览 */
    @PostMapping("/summary")
    public ResponseResult<Map<String, Object>> summary(HttpServletRequest request) {
        return ResponseResult.success(distributionClientService.summary(currentUserId(request)));
    }

    /** 我的团队（下级） */
    @PostMapping("/team")
    public ResponseResult<Map<String, Object>> team(HttpServletRequest request,
                                                   @RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> p = params == null ? new HashMap<>() : params;
        return ResponseResult.success(distributionClientService.team(currentUserId(request),
                intVal(p.get("page"), 1), intVal(p.get("size"), 10)));
    }

    /** 佣金明细 */
    @PostMapping("/commissions")
    public ResponseResult<Map<String, Object>> commissions(HttpServletRequest request,
                                                          @RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> p = params == null ? new HashMap<>() : params;
        Integer status = p.get("status") == null ? null : intVal(p.get("status"), 0);
        Integer level = p.get("level") == null ? null : intVal(p.get("level"), 0);
        boolean today = Boolean.parseBoolean(String.valueOf(p.getOrDefault("today", false)));
        return ResponseResult.success(distributionClientService.commissions(currentUserId(request), status, level,
                today, intVal(p.get("page"), 1), intVal(p.get("size"), 10)));
    }

    /** 推广订单 */
    @PostMapping("/orders")
    public ResponseResult<Map<String, Object>> orders(HttpServletRequest request,
                                                     @RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> p = params == null ? new HashMap<>() : params;
        return ResponseResult.success(distributionClientService.orders(currentUserId(request),
                intVal(p.get("page"), 1), intVal(p.get("size"), 10)));
    }

    /** 我推广的客户（按买家去重统计） */
    @PostMapping("/customers")
    public ResponseResult<Map<String, Object>> customers(HttpServletRequest request,
                                                        @RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> p = params == null ? new HashMap<>() : params;
        return ResponseResult.success(distributionClientService.customers(currentUserId(request),
                intVal(p.get("page"), 1), intVal(p.get("size"), 10)));
    }

    /** 提现记录 */
    @PostMapping("/withdraw/list")
    public ResponseResult<Map<String, Object>> withdrawList(HttpServletRequest request,
                                                           @RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> p = params == null ? new HashMap<>() : params;
        return ResponseResult.success(distributionClientService.withdrawList(currentUserId(request),
                intVal(p.get("page"), 1), intVal(p.get("size"), 10)));
    }

    /** 申请提现 */
    @PostMapping("/withdraw/apply")
    public ResponseResult<?> withdrawApply(HttpServletRequest request,
                                          @RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> p = params == null ? new HashMap<>() : params;
        try {
            DistributionWithdraw withdraw = distributionClientService.withdrawApply(currentUserId(request), p);
            return ResponseResult.success(withdraw);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 门槛/余额不足等业务校验，按失败语义返回给前端展示
            return ResponseResult.fail(500, e.getMessage());
        } catch (Exception e) {
            log.warn("分销提现申请失败: err={}", e.getMessage());
            return ResponseResult.fail(500, "提现申请失败：" + e.getMessage());
        }
    }

    /** 申请成为分销员 */
    @PostMapping("/apply")
    public ResponseResult<Map<String, Object>> apply(HttpServletRequest request,
                                                    @RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> p = params == null ? new HashMap<>() : params;
        try {
            return ResponseResult.success(distributionClientService.apply(currentUserId(request), p));
        } catch (Exception e) {
            log.warn("分销员申请失败: err={}", e.getMessage());
            Map<String, Object> res = new HashMap<>();
            res.put("ok", false);
            res.put("reason", e.getMessage());
            return ResponseResult.success(res);
        }
    }

    private Long currentUserId(HttpServletRequest request) {
        return Long.parseLong(userResolver.resolve(request));
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

    /** 金额安全转换（保留给入参校验使用） */
    private static BigDecimal decimalVal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
