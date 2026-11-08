package com.houjiang.merchant.controller.printer;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.printer.IPrinterService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.printer.*;
import com.houjiang.merchant.vo.printer.PrinterDetailVo;
import com.houjiang.merchant.vo.printer.PrinterListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/printer.printer")
public class PrinterController {

    @Resource
    IPrinterService iPrinterService;

    @GetMapping("/lists")
    @Operation(summary="打印机列表")
    public AjaxResult<PageResult<PrinterListedVo>> list(@Validated PageValidate pageValidate,
                                                        @Validated PrinterSearchValidate searchValidate) {
        PageResult<PrinterListedVo> list = iPrinterService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="打印机详情")
    public AjaxResult<PrinterDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        PrinterDetailVo detail = iPrinterService.detail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/add")
    @Operation(summary="打印机新增")
    public AjaxResult<Object> add(@Validated @RequestBody PrinterCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iPrinterService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary="打印机编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody PrinterUpdateValidate updateValidate) {
        iPrinterService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="打印机删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iPrinterService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/set_status")
    @Operation(summary="打印机状态编辑")
    public AjaxResult<Object> setStatus(@Validated @RequestBody PrinterSetStatusValidate validate) {
        iPrinterService.setStatus(validate);
        return AjaxResult.success();
    }

    @PostMapping("test")
    @Operation(summary = "打印调试")
    public AjaxResult<Object> test(@Validated @RequestBody PrinterTestValidate testValidate) {
        iPrinterService.printTest(testValidate);
        return AjaxResult.success();
    }
}
