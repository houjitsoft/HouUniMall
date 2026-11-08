package com.houjiang.merchant.controller.decorate;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.IDecorateTabbarService;
import com.houjiang.merchant.validate.decorate.DecorateTabsValidate;
import com.houjiang.merchant.vo.decorate.DecorateTabbarVo;
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
@RequestMapping("/adminapi/decorate.tabbar")
public class DecorateTabbarController {

    @Resource
    IDecorateTabbarService iDecorateTabbarService;

    @GetMapping("/detail")
    @Operation(summary="底部导航详情")
    public AjaxResult<DecorateTabbarVo> detail() {
        DecorateTabbarVo vo = iDecorateTabbarService.detail();
        return AjaxResult.success(vo);
    }

    @Log(title = "底部导航编辑")
    @PostMapping("/save")
    @Operation(summary="底部导航编辑")
    public AjaxResult<Object> save(@Validated @RequestBody DecorateTabsValidate tabsValidate) {
        iDecorateTabbarService.save(tabsValidate);
        return AjaxResult.success();
    }

}
