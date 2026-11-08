package com.houjiang.merchant.controller.system;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.system.ISystemRoleService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.system.SystemRoleCreateValidate;
import com.houjiang.merchant.validate.system.SystemRoleUpdateValidate;
import com.houjiang.merchant.vo.system.SystemAuthRoleVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
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
@RequestMapping("/adminapi/auth.role")
public class SystemAuthRoleController {

    @Resource
    ISystemRoleService iSystemAuthRoleService;

    @GetMapping("/all")
    @Operation(summary="所有角色")
    public AjaxResult<List<SystemAuthRoleVo>> all() {
        List<SystemAuthRoleVo> list = iSystemAuthRoleService.all();
        return AjaxResult.success(list);
    }

    @Log(title = "角色列表")
    @GetMapping("/lists")
    @Operation(summary="角色列表")
    public AjaxResult<PageResult<SystemAuthRoleVo>> list(@Validated PageValidate pageValidate) {
        PageResult<SystemAuthRoleVo> list = iSystemAuthRoleService.list(pageValidate);
        return AjaxResult.success(list);
    }

    @Log(title = "角色详情")
    @GetMapping("/detail")
    @Operation(summary="角色详情")
    public AjaxResult<SystemAuthRoleVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        SystemAuthRoleVo vo = iSystemAuthRoleService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "角色新增")
    @PostMapping("/add")
    @Operation(summary="角色新增")
    public AjaxResult<Object> add(@Validated @RequestBody SystemRoleCreateValidate createValidate) {
        iSystemAuthRoleService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "角色编辑")
    @PostMapping("/edit")
    @Operation(summary="角色编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody SystemRoleUpdateValidate updateValidate) {
        iSystemAuthRoleService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "角色删除")
    @PostMapping("/delete")
    @Operation(summary="角色删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iSystemAuthRoleService.del(idValidate.getId());
        return AjaxResult.success();
    }

}
