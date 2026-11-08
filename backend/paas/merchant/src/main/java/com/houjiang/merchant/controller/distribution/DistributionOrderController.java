package com.houjiang.merchant.controller.distribution;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.distribution.IDistributionOrderService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.distribution.DistributionOrderSearchValidate;
import com.houjiang.merchant.vo.distribution.DistributionOrderListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.distributionOrder")
public class DistributionOrderController {

    @Resource
    IDistributionOrderService iDistributionOrderService;

    @GetMapping("/lists")
    @Operation(summary="分销订单列表")
    public AjaxResult<PageResult<DistributionOrderListedVo>> list(@Validated PageValidate pageValidate,
                                                                  @Validated DistributionOrderSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<DistributionOrderListedVo> list = iDistributionOrderService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }


}
