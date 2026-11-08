package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.annotation.MerchantPermission;
import com.houjiang.merchant.common.enums.ErrorEnum;
import com.houjiang.merchant.common.enums.NoticeEnum;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopGoodsCategoryService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryStatusValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategoryUpdateValidate;
import com.houjiang.merchant.validate.mall.MallShopGoodsCategorySearchValidate;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryListedVo;
import com.houjiang.merchant.vo.mall.MallShopGoodsCategoryListedVo;
import com.houjiang.merchant.vo.mall.MallShopGoodsCategoryDetailVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.vo.mall.MallShopGoodsCategoryOtherListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.shopGoodsCategory")
@MerchantPermission(anyOf = "mall.GoodsCategory/lists")
public class MallShopGoodsCategoryController {

    @Resource
    IMallShopGoodsCategoryService iMallShopGoodsCategoryService;

    @GetMapping("/lists")
    @Operation(summary="商家商品分类列表")
    public AjaxResult<PageResult<MallShopGoodsCategoryListedVo>> lists(@Validated PageValidate pageValidate,
                                                     @Validated MallShopGoodsCategorySearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallShopGoodsCategoryListedVo> list = iMallShopGoodsCategoryService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="商家商品分类详情")
    public AjaxResult<MallShopGoodsCategoryDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallShopGoodsCategoryDetailVo detail = iMallShopGoodsCategoryService.detail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/add")
    @Operation(summary="商家商品分类新增")
    public AjaxResult<Object> add(@Validated @RequestBody MallShopGoodsCategoryCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopGoodsCategoryService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary="商家商品分类编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallShopGoodsCategoryUpdateValidate updateValidate) {
        updateValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopGoodsCategoryService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="商家商品分类删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallShopGoodsCategoryService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/status")
    @Operation(summary="示例分类状态修改")
    public AjaxResult<Object> status(@Validated @RequestBody MallShopGoodsCategoryStatusValidate statusValidate) {
        iMallShopGoodsCategoryService.status(statusValidate);
        return AjaxResult.success();
    }

    @GetMapping("/otherList")
    @MerchantPermission(anyOf = {
            "mall.GoodsCategory/lists",
            "mall.Goods/lists",
            "mall.Goods/add:edit",
            "gather.gather/detial",
            "application.distribution/goods",
            "mall.ms/lists",
            "mall.pt/goods",
            "mall.coupon/detail",
            "decorate.page/detail"
    })
    @Operation(summary="其他列表")
    public AjaxResult<List<MallShopGoodsCategoryOtherListedVo>> otherList() {
        List<MallShopGoodsCategoryOtherListedVo> list = iMallShopGoodsCategoryService.otherList(MerchantThreadLocal.getShopId());
        return AjaxResult.success(list);
    }
}
