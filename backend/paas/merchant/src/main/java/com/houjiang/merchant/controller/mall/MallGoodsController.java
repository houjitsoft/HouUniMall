package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.annotation.MerchantPermission;
import com.houjiang.merchant.common.enums.ErrorEnum;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallGoodsCategoryService;
import com.houjiang.merchant.service.mall.IMallGoodsService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.*;
import com.houjiang.merchant.vo.mall.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping(value = {"merchant/mall.Goods", "merchant/mall.goods"})
public class MallGoodsController {

    @Resource
    IMallGoodsService iMallGoodsService;
    @Resource
    IMallGoodsCategoryService iMallGoodsCategoryService;

    @GetMapping("/lists")
    @MerchantPermission(anyOf = "mall.Goods/lists")
    @Operation(summary="商品列表")
    public AjaxResult<PageResult<MallGoodsListedVo>> list(@Validated PageValidate pageValidate,
                                                          @Validated MallGoodsSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallGoodsListedVo> list = iMallGoodsService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @MerchantPermission(anyOf = {"mall.Goods/lists", "mall.Goods/add:edit"})
    @Operation(summary="商品详情")
    public AjaxResult<MallGoodsDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallGoodsDetailVo detail = iMallGoodsService.detail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/add")
    @MerchantPermission(anyOf = "mall.Goods/add:edit")
    @Operation(summary="商品新增")
    public AjaxResult<Integer> add(@Validated @RequestBody MallGoodsCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        Integer goodsId = iMallGoodsService.add(createValidate);
        return AjaxResult.success(
                ErrorEnum.SUCCESS.getCode(), goodsId, ErrorEnum.HIDE_MSG.getCode());
    }

    @PostMapping("/edit")
    @MerchantPermission(anyOf = "mall.Goods/add:edit")
    @Operation(summary="商品编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallGoodsUpdateValidate updateValidate) {
        updateValidate.setShopId(MerchantThreadLocal.getShopId());
        return AjaxResult.success(iMallGoodsService.edit(updateValidate));
    }

    @PostMapping("/delete")
    @MerchantPermission(anyOf = "mall.Goods/add:edit")
    @Operation(summary="商品删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallGoodsService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @GetMapping("/category_lists")
    @MerchantPermission(anyOf = {
            "mall.Goods/lists",
            "mall.Goods/add:edit",
            "gather.gather/detial",
            "application.distribution/goods"
    })
    @Operation(summary="平台分类")
    public AjaxResult<List<MallGoodsCategoryListedVo>> categoryLists(@Validated MallGoodsCategorySearchValidate searchValidate) {
        List<MallGoodsCategoryListedVo> list = iMallGoodsCategoryService.list(searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/otherList")
    @MerchantPermission(anyOf = {"mall.Goods/lists", "mall.Goods/add:edit"})
    @Operation(summary="其他列表")
    public AjaxResult<MallGoodsOtherListVo> otherList() {
        MallGoodsOtherListVo vo = iMallGoodsService.otherList();
        return AjaxResult.success(vo);
    }
    @PostMapping("/status")
    @MerchantPermission(anyOf = "mall.Goods/add:edit")
    @Operation(summary="示例分类状态修改")
    public AjaxResult<Object> status(@Validated @RequestBody MallShopGoodsStatusValidate statusValidate) {
        iMallGoodsService.status(statusValidate);
        return AjaxResult.success();
    }

    @GetMapping("/commonList")
    @MerchantPermission(anyOf = "mall.Goods/lists")
    @Operation(summary="商品列表")
    public AjaxResult<PageResult<MallGoodsListedVo>> commonList(@Validated PageValidate pageValidate, @Validated MallGoodsSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallGoodsListedVo> list = iMallGoodsService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @PostMapping("/discount_set")
    @MerchantPermission(anyOf = "mall.goods/discount_set")
    @Operation(summary="参与/不参与折扣")
    public AjaxResult<Object> discountSet(@Validated @RequestBody MallShopGoodsDiscountSetValidate setValidate) {
        iMallGoodsService.discountSet(setValidate);
        return AjaxResult.success();
    }

    @GetMapping("/discount_detail")
    @MerchantPermission(anyOf = "mall.goods/discount_detail")
    @Operation(summary="商品折扣详情")
    public AjaxResult<MallGoodsDiscountDetailVo> discountDetail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallGoodsDiscountDetailVo detail = iMallGoodsService.discountDetail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/discount_save")
    @MerchantPermission(anyOf = "mall.goods/discount_set")
    @Operation(summary="商品保存折扣")
    public AjaxResult<Object> discountSave(@Validated @RequestBody MallShopGoodsDiscountSaveValidate setValidate) {
        iMallGoodsService.discountSave(setValidate);
        return AjaxResult.success();
    }

    @PostMapping("/distribution_save")
    @MerchantPermission(anyOf = "application.distribution/goods")
    @Operation(summary="设置分销")
    public AjaxResult<Object> distributionSave(@Validated @RequestBody MallShopGoodsDistributionSaveValidate setValidate) {
        iMallGoodsService.distributionSave(setValidate);
        return AjaxResult.success();
    }

    @PostMapping("/distribution_set")
    @MerchantPermission(anyOf = "application.distribution/goods")
    @Operation(summary="参与/不参与分销")
    public AjaxResult<Object> distributionSet(@Validated @RequestBody MallShopGoodsDistributionSetValidate setValidate) {
        iMallGoodsService.distributionSet(setValidate);
        return AjaxResult.success();
    }

    @GetMapping("/distribution_detail")
    @MerchantPermission(anyOf = "application.distribution/goods")
    @Operation(summary="商品分销详情")
    public AjaxResult<MallGoodsDistributionDetailVo> distribution_detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallGoodsDistributionDetailVo detail = iMallGoodsService.distributionDetail(id);
        return AjaxResult.success(detail);
    }
}
