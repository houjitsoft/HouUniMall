package com.houjiang.merchant.controller.finance;

import com.houjiang.merchant.service.finance.IFinanceWalletService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceWalletSearchValidate;
import com.houjiang.merchant.vo.finance.FinanceWalletListVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * @author Amos
 */
@RestController
@Tag(name = "余额明细管理")
@RequestMapping("/merchant/finance.account_log")
public class FinanceWalletController {

    @Resource
    IFinanceWalletService iFinanceWalletService;

    @GetMapping("/lists")
    @Operation(summary = "记录列表")
    public AjaxResult<Object> list(@Validated PageValidate pageValidate,
                                   @Validated FinanceWalletSearchValidate searchValidate) {
        PageResult<FinanceWalletListVo> list = iFinanceWalletService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }


    @GetMapping("/getUmChangeType")
    @Operation(summary = "记录列表")
    public AjaxResult<Map<Integer, String>> getUmChangeType() {
        return AjaxResult.success(iFinanceWalletService.getUmChangeType());
    }

}
