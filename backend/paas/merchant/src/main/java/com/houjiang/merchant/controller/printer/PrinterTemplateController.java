package com.houjiang.merchant.controller.printer;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.printer.IPrinterTemplateService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.printer.PrinterTemplateCreateValidate;
import com.houjiang.merchant.validate.printer.PrinterTemplateSearchValidate;
import com.houjiang.merchant.validate.printer.PrinterTemplateUpdateValidate;
import com.houjiang.merchant.vo.printer.PrinterTemplateDetailVo;
import com.houjiang.merchant.vo.printer.PrinterTemplateListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/printer.printerTemplate")
public class PrinterTemplateController {

    @Resource
    IPrinterTemplateService iPrinterTemplateService;

    @GetMapping("/lists")
    @Operation(summary="打印机模版列表")
    public AjaxResult<PageResult<PrinterTemplateListedVo>> list(@Validated PageValidate pageValidate,
                                                                @Validated PrinterTemplateSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<PrinterTemplateListedVo> list = iPrinterTemplateService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="打印机模版详情")
    public AjaxResult<PrinterTemplateDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        PrinterTemplateDetailVo detail = iPrinterTemplateService.detail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/add")
    @Operation(summary="打印机模版新增")
    public AjaxResult<Object> add(@Validated @RequestBody PrinterTemplateCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iPrinterTemplateService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary="打印机模版编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody PrinterTemplateUpdateValidate updateValidate) {
        iPrinterTemplateService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="打印机模版删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iPrinterTemplateService.del(idValidate.getId());
        return AjaxResult.success();
    }

}
