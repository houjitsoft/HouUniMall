package com.houjiang.merchant.controller.setting;


import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.service.ISettingPaymentService;
import com.houjiang.merchant.validate.setting.SettingPayConfigValidate;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.entity.setting.MallPay;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/setting.pay.pay_config")
public class SettingPayConfigController {

    @Resource
    ISettingPaymentService iSettingPaymentService;

    @GetMapping("/lists")
    @Operation(summary="支付配置列表")
    public AjaxResult<JSONObject> list() {
        JSONObject result = iSettingPaymentService.list();
        return AjaxResult.success(result);
    }

    @GetMapping("/getConfig")
    @Operation(summary="支付配置详情")
    public AjaxResult<Object> getConfig(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallPay vo = iSettingPaymentService.getConfig(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/setConfig")
    @Operation(summary="支付配置编辑")
    public AjaxResult<Object> setConfig(@Validated @RequestBody SettingPayConfigValidate configValidate) {
        iSettingPaymentService.setConfig(configValidate);
        return AjaxResult.success();
    }


}
