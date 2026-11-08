package com.houjiang.mall.controller.setting;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.setting.SettingOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 商城后台 - 订单设置 Controller（交易 + 结算）。
 * 对应 like-admin SettingOrderController（/adminapi/mall.orderSetting）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/order")
@RequiredArgsConstructor
public class MallSettingOrderController {

    private final SettingOrderService orderService;

    /**
     * 交易设置详情。
     * GET /mall/setting/order/transaction
     */
    @GetMapping("/transaction")
    public ResponseResult<Map<String, Object>> getTransaction() {
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("transaction", orderService.getTransaction());
        return ResponseResult.success(wrapper);
    }

    /**
     * 交易设置保存。
     * POST /mall/setting/order/transaction/save
     */
    @PostMapping("/transaction/save")
    public ResponseResult<Void> saveTransaction(@RequestBody Map<String, Object> params) {
        orderService.saveTransaction(params);
        return ResponseResult.success();
    }

    /**
     * 结算设置详情。
     * GET /mall/setting/order/settlement
     */
    @GetMapping("/settlement")
    public ResponseResult<Map<String, Object>> getSettlement() {
        return ResponseResult.success(orderService.getSettlement());
    }

    /**
     * 结算设置保存。
     * POST /mall/setting/order/settlement/save
     */
    @PostMapping("/settlement/save")
    public ResponseResult<Void> saveSettlement(@RequestBody Map<String, Object> params) {
        orderService.saveSettlement(params);
        return ResponseResult.success();
    }
}
