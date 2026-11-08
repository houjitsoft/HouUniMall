package com.houjiang.merchant.controller.setting;

import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.ISettingStorageService;
import com.houjiang.merchant.common.core.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/setting.storage")
public class SettingStorageController {

    @Resource
    ISettingStorageService iSettingStorageService;

    @GetMapping("/lists")
    @Operation(summary="存储列表")
    public AjaxResult<List<Map<String, Object>>> list() {
        List<Map<String, Object>> list = iSettingStorageService.list();
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="存储详情")
    public AjaxResult<Map<String, Object>> detail(String engine) {
        Map<String, Object> map = iSettingStorageService.detail(engine);
        return AjaxResult.success(map);
    }

    @Log(title = "存储编辑")
    @PostMapping("/setup")
    @Operation(summary="存储编辑")
    public AjaxResult<Object> setup(@RequestBody Map<String, String> params) {
        iSettingStorageService.setup(params);
        return AjaxResult.success();
    }

    @Log(title = "存储切换")
    @PostMapping("/change")
    @Operation(summary="存储切换")
    public AjaxResult<Object> change(@RequestBody Map<String, String> params) {
        Assert.notNull(params.get("alias"), "alias参数缺失");
        Assert.notNull(params.get("status"), "status参数缺失");
        String alias = params.get("alias");
        Integer status = Integer.parseInt(params.get("status"));
        iSettingStorageService.change(alias, status);
        return AjaxResult.success();
    }

}
