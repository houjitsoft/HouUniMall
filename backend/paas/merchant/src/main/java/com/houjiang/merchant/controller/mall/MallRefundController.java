package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallRefundService;
import com.houjiang.merchant.service.mall.IMallSettlementService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallRefundReRefundValidate;
import com.houjiang.merchant.validate.mall.MallRefundSearchValidate;
import com.houjiang.merchant.vo.mall.MallRefundListedVo;
import com.houjiang.merchant.vo.mall.MallRefundSumVo;
import com.houjiang.merchant.vo.mall.MallSettlementCountVo;
import com.houjiang.merchant.vo.mall.MallSettlementListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping(value = {"merchant/mall.Refund"})
public class MallRefundController {
    @Resource
    IMallRefundService iMallRefundService;

    @GetMapping("/sums")
    @Operation(summary = "统计数据")
    public AjaxResult<MallRefundSumVo> sums() {
        MallRefundSumVo vo = iMallRefundService.sums(MerchantThreadLocal.getShopId());
        return AjaxResult.success(vo);
    }

    @GetMapping("/lists")
    @Operation(summary="退款记录列表")
    public AjaxResult<PageResult<MallRefundListedVo>> list(@Validated PageValidate pageValidate, MallRefundSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallRefundListedVo> list = iMallRefundService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @PostMapping("/re_refund")
    @Operation(summary = "重新退款")
    public AjaxResult<Object> reRefund(@Validated @RequestBody MallRefundReRefundValidate validate) {
        validate.setShopId(MerchantThreadLocal.getShopId());
        iMallRefundService.reRefund(validate);
        return AjaxResult.success();
    }

    @PostMapping("/deal")
    @Operation(summary = "处理退款（待退款记录执行退款）")
    public AjaxResult<Object> deal(@Validated @RequestBody MallRefundReRefundValidate validate) {
        validate.setShopId(MerchantThreadLocal.getShopId());
        iMallRefundService.deal(validate);
        return AjaxResult.success();
    }
}
