package com.houjiang.merchant.controller.shopDept;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopDeptService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptTopUpdateValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopDeptDetailVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("merchant/mall.shopDept")
public class ShopDeptController {

    @Resource
    IMallShopDeptService iMallShopDeptService;


    @GetMapping("/lists")
    @Operation(summary="部门列表")
    public AjaxResult<JSONArray> list(@Validated MallShopDeptSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        JSONArray list = iMallShopDeptService.list(searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="部门详情")
    public AjaxResult<MallShopDeptDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallShopDeptDetailVo vo = iMallShopDeptService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/add")
    @Operation(summary="部门新增")
    public AjaxResult<Object> add(@Validated @RequestBody MallShopDeptCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopDeptService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary="部门编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallShopDeptUpdateValidate updateValidate) {
        updateValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopDeptService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/top_edit")
    @Operation(summary = "顶级部门编辑")
    public AjaxResult<Object> topEdit(@Validated @RequestBody MallShopDeptTopUpdateValidate updateValidate) {
        iMallShopDeptService.topEdit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="部门删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallShopDeptService.del(idValidate.getId());
        return AjaxResult.success();
    }

}
