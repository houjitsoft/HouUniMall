package com.houjiang.merchant.controller.marketing;

import com.houjiang.merchant.service.IMarketingRechargeService;
import com.houjiang.merchant.validate.marketing.MarketingRechargeValidate;
import com.houjiang.merchant.vo.marketing.MarketingRechargeVo;
import com.houjiang.merchant.common.core.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping({"adminapi/recharge/recharge", "adminapi/recharge.recharge"})
public class MarketingRechargeController {

    @Resource
    IMarketingRechargeService iMarketingRechargeService;

    @GetMapping("/getConfig")
    @Schema(description = "充值配置详情")
    public AjaxResult<MarketingRechargeVo> detail() {
        MarketingRechargeVo vo = iMarketingRechargeService.detail();
        return AjaxResult.success(vo);
    }

    @PostMapping("/setConfig")
    @Schema(description = "充值配置保存")
    public AjaxResult<Object> save(@Validated @RequestBody MarketingRechargeValidate rechargeValidate) {
        iMarketingRechargeService.save(rechargeValidate);
        return AjaxResult.success();
    }

}
