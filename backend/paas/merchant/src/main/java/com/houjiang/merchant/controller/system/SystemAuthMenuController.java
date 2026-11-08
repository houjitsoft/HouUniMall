package com.houjiang.merchant.controller.system;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.system.ISystemMenuService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.system.SystemMenuCreateValidate;
import com.houjiang.merchant.validate.system.SystemMenuUpdateValidate;
import com.houjiang.merchant.vo.system.SystemAuthMenuVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
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
@RequestMapping("/adminapi/auth.menu")
public class SystemAuthMenuController {

    @Resource
    ISystemMenuService iSystemAuthMenuService;

    @GetMapping("/route")
    @Operation(summary="获取菜单路由")
    public AjaxResult<JSONArray> route() {
        List<Integer> roleIds = MerchantThreadLocal.getRoleIds();
        JSONArray lists = iSystemAuthMenuService.selectMenuByRoleId(roleIds);
        return AjaxResult.success(lists);
    }

    @GetMapping("/lists")
    @Operation(summary="获取菜单列表")
    public AjaxResult<JSONObject> list() {
        JSONObject result = iSystemAuthMenuService.list();
        return AjaxResult.success(result);
    }

    @GetMapping("/systemMenuLists")
    @Operation(summary="获取菜单列表")
    public AjaxResult<Object> systemMenuLists() {
        JSONArray result = iSystemAuthMenuService.systemMenuLists();
        return AjaxResult.success(result);
    }



    @GetMapping("/all")
    @Operation(summary="获取菜单列表")
    public AjaxResult<JSONArray> all() {
        JSONArray result = iSystemAuthMenuService.all();
        return AjaxResult.success(result);
    }

    @GetMapping("/detail")
    @Operation(summary="获取菜单详情")
    public AjaxResult<SystemAuthMenuVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        SystemAuthMenuVo vo = iSystemAuthMenuService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "菜单新增")
    @PostMapping("/add")
    @Operation(summary="新增菜单")
    public AjaxResult<Object> add(@Validated @RequestBody SystemMenuCreateValidate createValidate) {
        iSystemAuthMenuService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "菜单编辑")
    @PostMapping("/edit")
    @Operation(summary="菜单编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody SystemMenuUpdateValidate updateValidate) {
        iSystemAuthMenuService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "菜单删除")
    @PostMapping("/delete")
    @Operation(summary="菜单删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iSystemAuthMenuService.del(idValidate.getId());
        return AjaxResult.success();
    }

}
