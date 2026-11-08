package com.houjiang.merchant.controller.system;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.service.system.IDeptService;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.validate.system.DeptCreateValidate;
import com.houjiang.merchant.validate.system.DeptSearchValidate;
import com.houjiang.merchant.validate.system.DeptUpdateValidate;
import com.houjiang.merchant.vo.system.DeptVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/dept.dept")
public class DeptController {

    @Resource
    IDeptService deptService;

    @GetMapping("/all")
    @Operation(summary="部门所有")
    public AjaxResult<JSONArray> all() {
        JSONArray list = deptService.all();
        return AjaxResult.success(list);
    }

    @GetMapping("/lists")
    @Operation(summary="部门列表")
    public AjaxResult<JSONArray> list(@Validated DeptSearchValidate searchValidate) {
        JSONArray list = deptService.list(searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="部门详情")
    public AjaxResult<DeptVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        DeptVo vo = deptService.detail(id);
        return AjaxResult.success(vo);
    }

    @PostMapping("/add")
    @Operation(summary="部门新增")
    public AjaxResult<Object> add(@Validated @RequestBody DeptCreateValidate createValidate) {
        deptService.add(createValidate);
        return AjaxResult.success();
    }

    @PostMapping("/edit")
    @Operation(summary="部门编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody DeptUpdateValidate updateValidate) {
        deptService.edit(updateValidate);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Operation(summary="部门删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        deptService.del(idValidate.getId());
        return AjaxResult.success();
    }

}
