package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopBankService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopBankCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopBankSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopBankUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopBankDetailVo;
import com.houjiang.merchant.vo.mall.MallShopBankListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.shopBank")
public class MallShopBankController {

    @Resource
    IMallShopBankService iMallShopBankService;

    @GetMapping("/lists")
    @Operation(summary="商家银行卡列表")
    public AjaxResult<PageResult<MallShopBankListedVo>> list(@Validated PageValidate pageValidate,
                                                             @Validated MallShopBankSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallShopBankListedVo> list = iMallShopBankService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="商家银行卡详情")
    public AjaxResult<MallShopBankDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallShopBankDetailVo detail = iMallShopBankService.detail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/add")
    @Operation(summary="商家银行卡新增")
    public AjaxResult<Object> add(@Validated @RequestBody MallShopBankCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopBankService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary="商家银行卡编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallShopBankUpdateValidate updateValidate) {
        iMallShopBankService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="商家银行卡删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallShopBankService.del(idValidate.getId());
        return AjaxResult.success();
    }

}
