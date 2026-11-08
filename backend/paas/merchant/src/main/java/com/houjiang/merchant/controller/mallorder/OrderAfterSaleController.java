package com.houjiang.merchant.controller.mallorder;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallOrderAfterSaleService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mallorder.OrderAfterAuditValidate;
import com.houjiang.merchant.validate.mallorder.OrderAfterGoodsReceiveValidate;
import com.houjiang.merchant.validate.mallorder.OrderAfterSearchValidate;
import com.houjiang.merchant.validate.mallorder.OrderSearchValidate;
import com.houjiang.merchant.vo.mallorder.OrderAfterDetailVo;
import com.houjiang.merchant.vo.mallorder.OrderAfterListVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/merchant/mall.orderAfterSale")
@Tag(name = "售后管理")
public class OrderAfterSaleController {

    @Resource
    IMallOrderAfterSaleService iMallOrderAfterSaleService;

    @GetMapping("/lists")
    @Operation(summary = "售后列表")
    public AjaxResult<PageResult<OrderAfterListVo>> lists(@Validated PageValidate pageValidate, @Validated OrderAfterSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<OrderAfterListVo> list = iMallOrderAfterSaleService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    /**
     * @PostMapping("/delivery")
     * @Operation(summary = "买家退货")
     * public AjaxResult<Object> delivery(@Validated @RequestBody OrderAfterDeliveryValidate deliveryValidate) {
     * Integer userId = LikeFrontThreadLocal.getUserId();
     * iOrderAfterService.delivery(userId, deliveryValidate);
     * return AjaxResult.success();
     * }
     * 
     * @PostMapping("/add")
     * @Operation(summary = "申请售后")
     * public AjaxResult<OrderAfterCreateVo> add(@Validated @RequestBody OrderAfterCreateValidate orderAfterCreateValidate) {
     * Integer userId = LikeFrontThreadLocal.getUserId();
     * OrderAfterCreateVo orderAfterCreateVo = iOrderAfterService.add(userId, orderAfterCreateValidate);
     * return AjaxResult.success(orderAfterCreateVo);
     * }
     * 
     */
    @GetMapping("/detail")
    @Operation(summary = "售后详情")
    public AjaxResult<OrderAfterDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        OrderAfterDetailVo detailVo = iMallOrderAfterSaleService.detail(id);
        return AjaxResult.success(detailVo);
    }

    @PostMapping("/refund")
    @Operation(summary = "确认退款")
    public AjaxResult<Object> refund(@Validated @RequestBody IdValidate idValidate) {
        iMallOrderAfterSaleService.refund(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/audit")
    @Operation(summary = "审核")
    public AjaxResult<Object> audit(@Validated @RequestBody OrderAfterAuditValidate validate) {
        iMallOrderAfterSaleService.audit(validate);
        return AjaxResult.success();
    }

    @PostMapping("/goodsReceive")
    @Operation(summary = "商家收货")
    public AjaxResult<Object> goodsReceive(@Validated @RequestBody OrderAfterGoodsReceiveValidate validate) {
        iMallOrderAfterSaleService.goodsReceive(validate);
        return AjaxResult.success();
    }

    @PostMapping("/deal")
    @Operation(summary = "商家处理")
    public AjaxResult<Object> deal(@Validated @RequestBody IdValidate idValidate) {
        iMallOrderAfterSaleService.deal(idValidate.getId());
        return AjaxResult.success();
    }
}
