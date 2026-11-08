package com.houjiang.merchant.controller.shopRole;

import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopRoleService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleSetMenuValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopRoleDetailVo;
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
@RequestMapping("merchant/mall.shopRole")
public class ShopRoleController {

    @Resource
    IMallShopRoleService iMallShopRoleService;
    @GetMapping("/lists")
    @Operation(summary = "角色列表")
    public AjaxResult<PageResult<MallShopRoleListedVo>> lists(@Validated PageValidate pageValidate,
                                                        @Validated MallShopRoleSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        PageResult<MallShopRoleListedVo> list = iMallShopRoleService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取岗位详情")
    public AjaxResult<MallShopRoleDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        MallShopRoleDetailVo vo = iMallShopRoleService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/add")
    @Operation(summary = "岗位新增")
    public AjaxResult<Object> add(@Validated @RequestBody MallShopRoleCreateValidate createValidate) {
        createValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopRoleService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "岗位编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody MallShopRoleUpdateValidate updateValidate) {
        iMallShopRoleService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "岗位删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iMallShopRoleService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @PostMapping("/set_menu")
    @Operation(summary = "设置菜单删除")
    public AjaxResult<Object> setMenu(@Validated @RequestBody MallShopRoleSetMenuValidate menuValidate) {
        menuValidate.setShopId(MerchantThreadLocal.getShopId());
        iMallShopRoleService.setMenu(menuValidate);
        return AjaxResult.success();
    }

    @GetMapping("/other_lists")
    @Operation(summary = "其他列表")
    public AjaxResult<List<MallShopRoleListedVo>> otherLists(@Validated MallShopRoleSearchValidate searchValidate) {
        searchValidate.setShopId(MerchantThreadLocal.getShopId());
        List<MallShopRoleListedVo> list = iMallShopRoleService.otherLists(searchValidate);
        return AjaxResult.success(list);
    }
}
