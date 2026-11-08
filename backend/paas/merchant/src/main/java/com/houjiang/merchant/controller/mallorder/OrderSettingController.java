package com.houjiang.merchant.controller.mallorder;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.IConfigService;
import com.houjiang.merchant.service.mall.IMallOrderAfterSaleService;
import com.houjiang.merchant.service.mall.IMallOrderService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mallorder.MallOrderRemarkValidate;
import com.houjiang.merchant.validate.mallorder.MallOrderSendDeliveryValidate;
import com.houjiang.merchant.validate.mallorder.OrderSearchValidate;
import com.houjiang.merchant.vo.mall.HasAfterSaleWaitDealVo;
import com.houjiang.merchant.vo.mallorder.LogisticsDetailVo;
import com.houjiang.merchant.vo.mallorder.OrderDetailVo;
import com.houjiang.merchant.vo.mallorder.OrderListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.orderSetting")
public class OrderSettingController {

    @Resource
    IConfigService iConfigService;

    @GetMapping("/settlement_get")
    @Operation(summary="结算设置 获取")
    public AjaxResult<JSONObject> settlementGet() {
        JSONObject setting =  iConfigService.settlementGet(MerchantThreadLocal.getShopId());
        return AjaxResult.success(setting);
    }

}
