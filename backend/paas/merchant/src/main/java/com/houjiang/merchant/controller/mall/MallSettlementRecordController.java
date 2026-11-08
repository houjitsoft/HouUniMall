package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallSettlementRecordService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallSettlementRecordSearchValidate;
import com.houjiang.merchant.vo.mall.MallSettlementRecordListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@Tag(name = "结算管理")
@RequestMapping(value = {"merchant/mall.settlementRecord"})
public class MallSettlementRecordController {
    @Resource
    IMallSettlementRecordService iMallSettlementRecordService;

    @GetMapping("/lists")
    @Operation(summary="获取结算详情列表")
    public AjaxResult<PageResult<MallSettlementRecordListedVo>> list(@Validated PageValidate pageValidate, @Validated MallSettlementRecordSearchValidate searchValidate, @RequestParam("settlement_id") Integer settlementId) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        searchValidate.setSettlementId(settlementId);
        PageResult<MallSettlementRecordListedVo> list = iMallSettlementRecordService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

}
