package com.houjiang.merchant.controller.userDistribution;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.userDistribution.IUserDistributionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.userDistribution")
public class UserDistributionController {

    @Resource
    IUserDistributionService userDistributionService;

    @GetMapping("/count")
    @Operation(summary="分销概览")
    public AjaxResult<JSONObject> count() {
        JSONObject result = userDistributionService.count(MerchantThreadLocal.getShopId());
        return AjaxResult.success(result);
    }
}
