package com.houjiang.merchant.controller.shopAdmin;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopAdminService;
import com.houjiang.merchant.service.mall.IMallShopJobService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.*;
import com.houjiang.merchant.vo.mall.MallShopAdminDetailVo;
import com.houjiang.merchant.vo.mall.MallShopAdminListedVo;
import com.houjiang.merchant.vo.mall.MallShopJobDetailVo;
import com.houjiang.merchant.vo.mall.MallShopJobListedVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.shopAdmin")
public class ShopAdminController {

    @Resource
    IMallShopAdminService iMallShopAdminService;
    @GetMapping("/lists")
    @Operation(summary = "管理员列表")
    public AjaxResult<PageResult<MallShopAdminListedVo>> lists(@Validated PageValidate pageValidate,
                                                               @Validated MallShopAdminSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallShopAdminListedVo> list = iMallShopAdminService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/all")
    @Operation(summary = "管理员全部列表（不分页）")
    public AjaxResult<List<MallShopAdminListedVo>> all(@Validated MallShopAdminSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        return AjaxResult.success(iMallShopAdminService.all(searchValidate));
    }

    @GetMapping("/account-options")
    @Operation(summary = "统一用户账号选项")
    public AjaxResult<List<Map<String, Object>>> accountOptions(
            @RequestParam(value = "keyword", required = false) String keyword) {
        return AjaxResult.success(iMallShopAdminService.accountOptions(keyword));
    }

    @GetMapping("/detail")
    @Operation(summary = "获取管理员详情")
    public AjaxResult<MallShopAdminDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallShopAdminDetailVo vo = iMallShopAdminService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/add")
    @Operation(summary = "管理员新增")
    public AjaxResult<Object> add(@Validated @RequestBody MallShopAdminCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopAdminService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "管理员编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallShopAdminUpdateValidate updateValidate) {
        iMallShopAdminService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "管理员删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallShopAdminService.del(idValidate.getId());
        return AjaxResult.success();
    }

}
