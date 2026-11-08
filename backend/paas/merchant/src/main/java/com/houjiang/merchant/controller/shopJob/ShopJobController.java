package com.houjiang.merchant.controller.shopJob;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopJobService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopJobCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopJobSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopJobUpdateValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleSearchValidate;
import com.houjiang.merchant.vo.mall.MallShopJobDetailVo;
import com.houjiang.merchant.vo.mall.MallShopJobListedVo;
import com.houjiang.merchant.vo.mall.MallShopRoleListedVo;
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
@RequestMapping("merchant/mall.shopJob")
public class ShopJobController {

    @Resource
    IMallShopJobService iMallShopJobService;
    @GetMapping("/lists")
    @Operation(summary = "岗位列表")
    public AjaxResult<PageResult<MallShopJobListedVo>> lists(@Validated PageValidate pageValidate,
                                                        @Validated MallShopJobSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallShopJobListedVo> list = iMallShopJobService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取岗位详情")
    public AjaxResult<MallShopJobDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallShopJobDetailVo vo = iMallShopJobService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/add")
    @Operation(summary = "岗位新增")
    public AjaxResult<Object> add(@Validated @RequestBody MallShopJobCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopJobService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "岗位编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallShopJobUpdateValidate updateValidate) {
        iMallShopJobService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "岗位删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallShopJobService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @GetMapping("/other_lists")
    @Operation(summary = "其他列表")
    public AjaxResult<List<MallShopJobListedVo>> otherLists(@Validated MallShopJobSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        List<MallShopJobListedVo> list = iMallShopJobService.otherLists(searchValidate);
        return AjaxResult.success(list);
    }
}
