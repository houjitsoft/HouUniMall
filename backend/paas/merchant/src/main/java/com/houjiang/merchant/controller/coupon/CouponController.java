package com.houjiang.merchant.controller.coupon;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.enums.CouponEnum;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.service.coupon.ICouponService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.coupon.CouponCreateValidate;
import com.houjiang.merchant.validate.coupon.CouponRenameValidate;
import com.houjiang.merchant.validate.coupon.CouponSearchValidate;
import com.houjiang.merchant.validate.coupon.CouponSysSendValidate;
import com.houjiang.merchant.validate.coupon.CouponUpdateValidate;
import com.houjiang.merchant.vo.coupon.CouponDetailVo;
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
@RequestMapping("merchant/mall.coupon")
public class CouponController {

    @Resource
    ICouponService iCouponService;
    @GetMapping("/lists")
    @Operation(summary = "优惠券列表")
    public AjaxResult<PageResult<CouponListedVo>> lists(@Validated PageValidate pageValidate,
                                                       @Validated CouponSearchValidate searchValidate) {
        PageResult<CouponListedVo> list = iCouponService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取优惠券详情")
    public AjaxResult<CouponDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        CouponDetailVo vo = iCouponService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/add")
    @Operation(summary = "优惠券新增")
    public AjaxResult<Object> add(@Validated @RequestBody CouponCreateValidate createValidate) {
        iCouponService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "优惠券编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody CouponUpdateValidate updateValidate) {
        iCouponService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/rename")
    @Operation(summary = "优惠券重命名/调整发放数量")
    public AjaxResult<Object> rename(@Validated @RequestBody CouponRenameValidate renameValidate) {
        iCouponService.rename(renameValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "优惠券删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iCouponService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/start")
    @Operation(summary = "优惠券开始")
    public AjaxResult<Object> start(@Validated @RequestBody IdValidate idValidate) {
        iCouponService.start(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/end")
    @Operation(summary = "优惠券结束")
    public AjaxResult<Object> end(@Validated @RequestBody IdValidate idValidate) {
        iCouponService.end(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/sys_send")
    @Operation(summary = "优惠券结束")
    public AjaxResult<Object> sysSend(@Validated @RequestBody CouponSysSendValidate sysSendValidate) {
        sysSendValidate.setGetType(CouponEnum.GET_TYPE_SYS.getCode());
        iCouponService.userGetCoupon(sysSendValidate);
        return AjaxResult.success();
    }
}
