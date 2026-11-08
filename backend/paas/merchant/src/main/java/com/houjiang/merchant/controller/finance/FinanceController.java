package com.houjiang.merchant.controller.finance;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.finance.IFinanceRechargerService;
import com.houjiang.merchant.service.finance.IFinanceService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceWalletSearchValidate;
import com.houjiang.merchant.vo.finance.FinanceIndexVo;
import com.houjiang.merchant.vo.finance.FinanceWalletListVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@Tag(name = "财务中心管理")
@RequestMapping("/merchant/mall.finance")
public class FinanceController {
    @Resource
    IFinanceService iFinanceService;

    @GetMapping("/index")
    @Operation(summary = "财务中心")
    public AjaxResult<FinanceIndexVo> index() {
        FinanceIndexVo vo = iFinanceService.index(MerchantThreadLocal.getShopId());
        return AjaxResult.success(vo);
    }
}
