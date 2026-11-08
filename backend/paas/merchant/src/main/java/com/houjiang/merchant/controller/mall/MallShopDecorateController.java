package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.enums.ShopDecorateEnum;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopDecorateService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopDecorateCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopDecorateSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopDecorateUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopDecorateDetailVo;
import com.houjiang.merchant.vo.mall.MallShopDecorateListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 店铺控制器
 * Class MallShopController
 * @author Amos
 */
@RestController
@RequestMapping({"merchant/mall.shopDecorate"})
@Tag(name = "店铺装修控制器")
public class MallShopDecorateController {

    @Resource
    IMallShopDecorateService iMallShopDecorateService;

    @GetMapping("/home_lists")
    @Operation(summary="店铺装修列表")
    public AjaxResult<PageResult<MallShopDecorateListedVo>> list(@Validated PageValidate pageValidate,
                                                             @Validated MallShopDecorateSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        searchValidate.setType(ShopDecorateEnum.TYPE_HOME);
        PageResult<MallShopDecorateListedVo> list = iMallShopDecorateService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/get")
    @Operation(summary="店铺装修详情")
    public AjaxResult<MallShopDecorateDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallShopDecorateDetailVo detail = iMallShopDecorateService.detail(id);
        return AjaxResult.success(detail);
    }

    @PostMapping("/add")
    @Operation(summary="店铺装修新增")
    public AjaxResult<Object> add(@Validated @RequestBody MallShopDecorateCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        Integer id = iMallShopDecorateService.add(createValidate);
        return AjaxResult.success("成功", id);
    }

    @PostMapping("/save")
    @Operation(summary="店铺装修编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallShopDecorateUpdateValidate updateValidate) {
        iMallShopDecorateService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="店铺装修删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallShopDecorateService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/set_home")
    @Operation(summary="设置首页")
    public AjaxResult<Object> setHome(@Validated @RequestBody IdValidate idValidate) {
        iMallShopDecorateService.setHome(idValidate.getId());
        return AjaxResult.success();
    }

}
