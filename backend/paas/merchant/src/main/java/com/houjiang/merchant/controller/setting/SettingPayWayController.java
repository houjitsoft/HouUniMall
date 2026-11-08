package com.houjiang.merchant.controller.setting;


import com.houjiang.merchant.service.ISettingPaymentService;
import com.houjiang.merchant.vo.setting.SettingPaymentMethodVo;
import com.houjiang.merchant.common.core.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/setting.pay.pay_way")
public class SettingPayWayController {

    @Resource
    ISettingPaymentService iSettingPaymentService;

    @GetMapping("/getPayWay")
    @Operation(summary="支付方式列表")
    public AjaxResult<HashMap> method() {
        HashMap list = iSettingPaymentService.getPayWay();
        return AjaxResult.success(list);
    }

    @PostMapping("/setPayWay")
    @Operation(summary="支付方式编辑")
    public AjaxResult<Object> setPayWay(@Validated @RequestBody HashMap<Integer, List<SettingPaymentMethodVo>> data) {
        iSettingPaymentService.setPayWay(data);
        return AjaxResult.success();
    }

}
