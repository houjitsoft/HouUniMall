package com.houjiang.payment.ums.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.ums.config.UmsConfigStore;
import com.houjiang.payment.ums.service.UmsCorpOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 银联企业订单 API（路径与插件一致，供 BFF /web/plugin/ums-corp-order/** 转发）。
 */
@Slf4j
@RestController
@RequestMapping("/plugin/ums-corp-order")
@RequiredArgsConstructor
public class UmsCorpOrderController {

    private final UmsCorpOrderService service;
    private final UmsConfigStore configStore;

    @PostMapping("/config/get")
    public ResponseResult<Object> getConfig(@RequestBody(required = false) Map<String, Object> body) {
        return ResponseResult.success(configStore.toSafeMap(configStore.get(orgId(body))));
    }

    @PostMapping("/config/save")
    public ResponseResult<Object> saveConfig(@RequestBody Map<String, Object> body) {
        long orgId = orgId(body);
        configStore.save(orgId, body == null ? Map.of() : body);
        return ResponseResult.success(configStore.toSafeMap(configStore.get(orgId)));
    }

    @PostMapping("/order/create")
    public ResponseResult<Object> create(@RequestBody Map<String, Object> body) {
        return wrap(() -> service.createOrder(orgId(body), body));
    }

    @PostMapping("/order/query")
    public ResponseResult<Object> query(@RequestBody Map<String, Object> body) {
        return wrap(() -> service.queryOrder(orgId(body), body));
    }

    @PostMapping("/order/close")
    public ResponseResult<Object> close(@RequestBody Map<String, Object> body) {
        return wrap(() -> service.closeOrder(orgId(body), body));
    }

    @PostMapping("/order/refund")
    public ResponseResult<Object> refund(@RequestBody Map<String, Object> body) {
        return wrap(() -> service.refund(orgId(body), body));
    }

    @PostMapping("/order/refund/query")
    public ResponseResult<Object> refundQuery(@RequestBody Map<String, Object> body) {
        return wrap(() -> service.queryRefund(orgId(body), body));
    }

    @PostMapping("/order/refund/cancel")
    public ResponseResult<Object> refundCancel(@RequestBody Map<String, Object> body) {
        return wrap(() -> service.cancelRefund(orgId(body), body));
    }

    @PostMapping("/order/page")
    public ResponseResult<Object> page(@RequestBody(required = false) Map<String, Object> body) {
        return wrap(() -> service.listOrders(orgId(body), body == null ? Map.of() : body));
    }

    @PostMapping("/transfer")
    public ResponseResult<Object> transfer(@RequestBody Map<String, Object> body) {
        return wrap(() -> service.transfer(orgId(body), body));
    }

    @PostMapping("/split")
    public ResponseResult<Object> split(@RequestBody Map<String, Object> body) {
        return wrap(() -> service.split(orgId(body), body));
    }

    @PostMapping("/op/query")
    public ResponseResult<Object> opQuery(@RequestBody Map<String, Object> body) {
        return wrap(() -> service.queryOp(orgId(body), body));
    }

    @PostMapping("/op/page")
    public ResponseResult<Object> opPage(@RequestBody(required = false) Map<String, Object> body) {
        return wrap(() -> service.listOps(orgId(body), body == null ? Map.of() : body));
    }

    @PostMapping("/balance")
    public ResponseResult<Object> balance(@RequestBody(required = false) Map<String, Object> body) {
        return wrap(() -> service.balance(orgId(body), body == null ? Map.of() : body));
    }

    @PostMapping("/notify")
    public Map<String, Object> notify(@RequestBody Map<String, Object> body) {
        try {
            service.handleNotify(orgId(body), body == null ? Map.of() : body);
            return Map.of("code", "SUCCESS", "message", "OK");
        } catch (Exception ex) {
            log.warn("[UMS] notify failed: {}", ex.getMessage());
            return Map.of("code", "FAIL", "message", ex.getMessage() == null ? "fail" : ex.getMessage());
        }
    }

    private static long orgId(Map<String, Object> body) {
        if (body == null || body.get("orgId") == null || String.valueOf(body.get("orgId")).isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(body.get("orgId")).trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static ResponseResult<Object> wrap(SupplierEx supplier) {
        try {
            return ResponseResult.success(supplier.get());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseResult.fail(400, ex.getMessage());
        } catch (Exception ex) {
            return ResponseResult.fail(500, ex.getMessage() == null ? "UMS 调用失败" : ex.getMessage());
        }
    }

    @FunctionalInterface
    private interface SupplierEx {
        Object get() throws Exception;
    }
}
