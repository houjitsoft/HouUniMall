package com.houjiang.mall.controller.setting;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.setting.SettingPayWayService;
import com.houjiang.mall.vo.setting.SettingPaymentMethodVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 支付方式 Controller。
 * 对应 like-admin SettingPayWayController（/adminapi/setting.pay.pay_way 与 /adminapi/mall.pay_scene）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/pay_way")
@RequiredArgsConstructor
public class MallSettingPayWayController {

    private final SettingPayWayService payWayService;

    /**
     * 支付方式列表（按场景分组）。
     * GET /mall/setting/pay_way/lists
     * 返回结构：{1: [...], 2: [...], 3: [...]}
     */
    @GetMapping("/lists")
    public ResponseResult<Map<Integer, List<SettingPaymentMethodVo>>> lists() {
        return ResponseResult.success(payWayService.list());
    }

    /**
     * 支付方式批量编辑。
     * POST /mall/setting/pay_way/edit
     * 入参结构：{"1": [{id, isDefault, status}, ...], "2": [...], "3": [...]}
     */
    @PostMapping("/edit")
    public ResponseResult<Void> edit(@RequestBody Map<Integer, List<SettingPaymentMethodVo>> data) {
        payWayService.edit(data);
        return ResponseResult.success();
    }
}
