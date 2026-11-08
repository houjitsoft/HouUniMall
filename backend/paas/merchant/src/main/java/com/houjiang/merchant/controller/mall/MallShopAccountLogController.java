package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallSettlementRecordService;
import com.houjiang.merchant.service.mall.IMallShopAccountLogService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallSettlementRecordSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopAccountLogSearchValidate;
import com.houjiang.merchant.vo.mall.MallSettlementRecordListedVo;
import com.houjiang.merchant.vo.mall.MallShopAccountLogListedVo;
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
@Tag(name = "店铺账户明细控制器")
@RequestMapping(value = {"merchant/mall.shopAccountLog"})
public class MallShopAccountLogController {
    @Resource
    IMallShopAccountLogService iMallShopAccountLogService;
    @GetMapping("/lists")
    @Operation(summary="获取结算详情列表")
    public AjaxResult<PageResult<MallShopAccountLogListedVo>> list(@Validated PageValidate pageValidate, @Validated MallShopAccountLogSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallShopAccountLogListedVo> list = iMallShopAccountLogService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

}
