package com.houjiang.merchant.controller.setting;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.ISettingSearchService;
import com.houjiang.merchant.validate.setting.SettingSearchValidate;
import com.houjiang.merchant.vo.setting.SettingSearchDetailVo;
import com.houjiang.merchant.common.core.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/setting.hot_search")
public class SettingSearchController {

    @Resource
    ISettingSearchService iSettingSearchService;

    @GetMapping("/getConfig")
    @Operation(summary="热门搜索详情")
    public AjaxResult<SettingSearchDetailVo> getConfig() {
        SettingSearchDetailVo vo = iSettingSearchService.getConfig();
        return AjaxResult.success(vo);
    }

    @Log(title = "热门搜索编辑")
    @PostMapping("/setConfig")
    @Operation(summary="热门搜索编辑")
    public AjaxResult<Object> setConfig(@Validated @RequestBody SettingSearchValidate searchValidate) {
        iSettingSearchService.setConfig(searchValidate);
        return AjaxResult.success("操作成功");
    }

}
