package com.houjiang.merchant.controller.decorate;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.IDecoratePageService;
import com.houjiang.merchant.validate.decorate.DecoratePageValidate;
import com.houjiang.merchant.vo.decorate.DecoratePageVo;
import com.houjiang.merchant.common.core.AjaxResult;
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
@RequestMapping("/adminapi/decorate.page")
public class DecoratePagesController {

    @Resource
    IDecoratePageService iDecoratePageService;

    @GetMapping("/detail")
    @Operation(summary="页面装修详情")
    public AjaxResult<DecoratePageVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        DecoratePageVo vo = iDecoratePageService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "页面装修保存")
    @PostMapping("/save")
    @Operation(summary="页面装修保存")
    public AjaxResult<Object> save(@RequestBody DecoratePageValidate decoratePageValidate) {
        iDecoratePageService.save(decoratePageValidate);
        return AjaxResult.success();
    }

}
