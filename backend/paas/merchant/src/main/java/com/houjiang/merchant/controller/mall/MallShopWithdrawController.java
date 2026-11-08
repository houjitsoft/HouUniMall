package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallSettlementRecordService;
import com.houjiang.merchant.service.mall.IMallShopWithdrawService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallSettlementRecordSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopWithdrawAddValidate;
import com.houjiang.merchant.validate.mall.MallShopWithdrawSearchValidate;
import com.houjiang.merchant.vo.mall.MallSettlementRecordListedVo;
import com.houjiang.merchant.vo.mall.MallShopWithdrawDetailVo;
import com.houjiang.merchant.vo.mall.MallShopWithdrawListedVo;
import com.houjiang.merchant.vo.mallorder.OrderDetailVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping(value = {"merchant/mall.shopWithdraw"})
public class MallShopWithdrawController {
    @Resource
    IMallShopWithdrawService iMallShopWithdrawService;
    @GetMapping("/lists")
    @Operation(summary="获取商家提现列表")
    public AjaxResult<PageResult<MallShopWithdrawListedVo>> list(@Validated PageValidate pageValidate, @Validated MallShopWithdrawSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallShopWithdrawListedVo> list = iMallShopWithdrawService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取商家提现详情")
    public AjaxResult<MallShopWithdrawDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallShopWithdrawDetailVo detail = iMallShopWithdrawService.detail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/add")
    @Operation(summary="商家提现新增")
    public AjaxResult<Object> add(@Validated @RequestBody MallShopWithdrawAddValidate validate) {
        validate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopWithdrawService.add(validate);
        return AjaxResult.success();
    }
}
