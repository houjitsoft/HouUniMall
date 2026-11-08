package com.houjiang.merchant.controller.setting;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.ISettingSmsService;
import com.houjiang.merchant.common.core.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 短信设置管理
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/notice.sms_config")
@Tag(name = "配置短信引擎")
public class SettingSmsController {

    @Resource
    ISettingSmsService iSettingSmsService;

    @GetMapping("/getConfig")
    @Operation(summary="短信引擎列表")
    public AjaxResult<List<Map<String, Object>>> getConfig() {
        List<Map<String, Object>> list = iSettingSmsService.getConfig();
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="短信引擎详情")
    public AjaxResult<Map<String, Object>> detail(String type) {
        Map<String, Object> map = iSettingSmsService.detail(type);
        return AjaxResult.success(map);
    }

    @Log(title = "短信引擎编辑")
    @PostMapping("/setConfig")
    @Operation(summary="短信引擎编辑")
    public AjaxResult<Object> setConfig(@RequestBody JSONObject params) {
        iSettingSmsService.setConfig(params);
        return AjaxResult.success("操作成功");
    }

}
