package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallSettlementService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.mall.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping(value = {"merchant/mall.settlement"})
public class MallSettlementController {
    @Resource
    IMallSettlementService iMallSettlementService;

    @GetMapping("/count")
    @Operation(summary = "统计数据")
    public AjaxResult<MallSettlementCountVo> count() {
        MallSettlementCountVo vo = iMallSettlementService.count(MerchantThreadLocal.getShopId() );
        return AjaxResult.success(vo);
    }

    @GetMapping("/lists2")
    @Operation(summary="统计数据列表")
    public AjaxResult<PageResult<MallSettlementListedVo>> list(@Validated PageValidate pageValidate) {
        PageResult<MallSettlementListedVo> list = iMallSettlementService.list(pageValidate, MerchantThreadLocal.getShopId());
        return AjaxResult.success(list);
    }

}
