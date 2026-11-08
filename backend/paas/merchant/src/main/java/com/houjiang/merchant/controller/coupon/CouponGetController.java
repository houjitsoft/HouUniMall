package com.houjiang.merchant.controller.coupon;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.enums.CouponEnum;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.coupon.ICouponGetService;
import com.houjiang.merchant.service.coupon.ICouponService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.IdsValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.coupon.*;
import com.houjiang.merchant.vo.coupon.CouponDetailVo;
import com.houjiang.merchant.vo.coupon.CouponGetListedVo;
import com.houjiang.merchant.vo.coupon.CouponListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.couponGet")
public class CouponGetController {

    @Resource
    ICouponGetService iCouponGetService;
    @GetMapping("/lists")
    @Operation(summary = "领取记录列表")
    public AjaxResult<PageResult<CouponGetListedVo>> lists(@Validated PageValidate pageValidate,
                                                           @Validated CouponGetSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<CouponGetListedVo> list = iCouponGetService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @PostMapping("/invalid")
    @Operation(summary = "作废")
    public AjaxResult<Object> invalid(@Validated @RequestBody IdsValidate idsValidate) {
        iCouponGetService.invalid(idsValidate);
        return AjaxResult.success();
    }

}
