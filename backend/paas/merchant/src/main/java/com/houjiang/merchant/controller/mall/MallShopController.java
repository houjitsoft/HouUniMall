package com.houjiang.merchant.controller.mall;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopAdminService;
import com.houjiang.merchant.service.mall.IMallShopService;
import com.houjiang.merchant.validate.mall.MallShopUpdateValidate;
import com.houjiang.merchant.validate.shop.MallShopAccountUpdateValidate;
import com.houjiang.merchant.vo.admin.AdminAccountDetailVo;
import com.houjiang.merchant.vo.mall.MallShopDetailVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 店铺控制器
 * Class MallShopController
 */
@RestController
@RequestMapping({"merchant/mall.shop"})
@Tag(name = "店铺控制器")
public class MallShopController {

    @Resource
    IMallShopAdminService iMallShopAdminService;
    @Resource
    IMallShopService iMallShopService;
    /**
     * @notes 个人资料
     * @author Amos
     */
    @PostMapping("/account_detail")
    @Operation(summary="个人资料")
    public AjaxResult<AdminAccountDetailVo> accountDetail() {
        AdminAccountDetailVo accountDetailVo = iMallShopAdminService.accountDetail(MerchantThreadLocal.getAdminId());
        return AjaxResult.success(accountDetailVo);
    }

    @PostMapping("/account_update")
    @Operation(summary="管理员更新")
    public AjaxResult<Object> accountUpdate(@Validated(value = MallShopAccountUpdateValidate.upInfo.class) @RequestBody MallShopAccountUpdateValidate mallShopAccountUpdateValidate) {
        iMallShopAdminService.accountUpdate(mallShopAccountUpdateValidate, MerchantThreadLocal.getAdminId());
        return AjaxResult.success("编辑成功");
    }

    @GetMapping("/detail")
    @Operation(summary="商家店铺详情")
    public AjaxResult<MallShopDetailVo> detail() {
        MallShopDetailVo detail = iMallShopService.detail(MerchantThreadLocal.getShopId());
        return AjaxResult.success(detail);
    }

    @PostMapping("/edit")
    @Operation(summary="商家店铺编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallShopUpdateValidate updateValidate) {
        updateValidate.setId(MerchantThreadLocal.getShopId());
        iMallShopService.edit(updateValidate);
        return AjaxResult.success();
    }
}
