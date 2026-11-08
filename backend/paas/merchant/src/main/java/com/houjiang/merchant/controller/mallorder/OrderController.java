package com.houjiang.merchant.controller.mallorder;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
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
import java.util.Map;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.order")
public class OrderController {

    @Resource
    IMallOrderService iMallOrderService;
    @Resource
    IMallOrderAfterSaleService iMallOrderAfterSaleService;

    @GetMapping("/lists")
    @Operation(summary = "订单列表")
    public AjaxResult<PageResult<OrderListedVo>> lists(@Validated PageValidate pageValidate,
                                                       @Validated OrderSearchValidate orderSearchValidate) {
        PageResult<OrderListedVo> lists = iMallOrderService.list(pageValidate,orderSearchValidate);
        return AjaxResult.success(lists);
    }

    @GetMapping("/detail")
    @Operation(summary = "订单详情")
    public AjaxResult<OrderDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        OrderDetailVo detail = iMallOrderService.detail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/cancel")
    @Operation(summary = "订单取消")
    public AjaxResult<Object> cancel(@Validated @RequestBody IdValidate idValidate) {
        iMallOrderService.cancel(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/set_shop_remark")
    @Operation(summary = "订单备注")
    public AjaxResult<Object> remarks(@Validated @RequestBody MallOrderRemarkValidate remarkValidate) {
        Integer orderId = remarkValidate.getId();
        String remarks = remarkValidate.getShopRemark();
        iMallOrderService.setRemarks(orderId, remarks);
        return AjaxResult.success();
    }

    @PostMapping("/send")
    @Operation(summary = "订单发货")
    public AjaxResult<Object> sendDelivery(@Validated @RequestBody MallOrderSendDeliveryValidate sendDeliveryValidate) {
        iMallOrderService.sendDelivery(sendDeliveryValidate);
        return AjaxResult.success();
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认收货")
    public AjaxResult<Object> takeDelivery(@Validated @RequestBody IdValidate idValidate) {
        iMallOrderService.takeDelivery(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/logistics")
    @Schema(description = "查看物流")
    public AjaxResult<LogisticsDetailVo> logistics(@Validated @IDMust() @RequestBody IdValidate idValidate) {
        LogisticsDetailVo vo = iMallOrderService.logistics(idValidate.getId());
        return AjaxResult.success(vo);
    }

    @PostMapping("/has_after_sale_wait_deal")
    @Schema(description = "是否有售后 待理处")
    public AjaxResult<JSONObject> hasAfterSaleWaitDeal(@Validated @RequestBody IdValidate idValidate) {
        HasAfterSaleWaitDealVo vo = iMallOrderAfterSaleService.orderSendHasWait(idValidate.getId());
        JSONObject ret = new JSONObject();
        ret.put("after_sale", StringUtils.isNull(vo) ? new JSONArray() : vo);
        return AjaxResult.success(ret);
    }

    @PostMapping("/print")
    @Schema(description = "小票打印")
    public AjaxResult<Object> print(@Validated @IDMust() @RequestBody IdValidate idValidate) {
        iMallOrderService.print(idValidate.getId());
        return AjaxResult.successHideTips();
    }
}
