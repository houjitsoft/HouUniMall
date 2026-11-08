package com.houjiang.merchant.controller.setting;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.validate.commons.IdValidate;
import com.houjiang.merchant.service.ISettingDictTypeService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.setting.DictTypeCreateValidate;
import com.houjiang.merchant.validate.setting.DictTypeUpdateValidate;
import com.houjiang.merchant.vo.setting.SettingDictTypeVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.validator.annotation.IDMust;
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
@RequestMapping("/adminapi/setting.dict.dict_type")
public class SettingDictTypeController {

    @Resource
    ISettingDictTypeService iSettingDictTypeService;

    @GetMapping("/all")
    @Operation(summary="字典类型所有")
    public AjaxResult<List<SettingDictTypeVo>> all() {
        List<SettingDictTypeVo> list = iSettingDictTypeService.all();
        return AjaxResult.success(list);
    }

    @GetMapping("/lists")
    @Operation(summary="字典类型列表")
    public AjaxResult<PageResult<SettingDictTypeVo>> list(@Validated PageValidate pageValidate,
                       @RequestParam Map<String, String> params) {
        PageResult<SettingDictTypeVo> list = iSettingDictTypeService.list(pageValidate, params);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="字典类型详情")
    public AjaxResult<SettingDictTypeVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        SettingDictTypeVo vo = iSettingDictTypeService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "字典类型新增")
    @PostMapping("/add")
    @Operation(summary="字典类型新增")
    public AjaxResult<Object> add(@Validated @RequestBody DictTypeCreateValidate createValidate) {
        iSettingDictTypeService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "字典类型编辑")
    @PostMapping("/edit")
    @Operation(summary="字典类型编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody DictTypeUpdateValidate updateValidate) {
        iSettingDictTypeService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "字典类型删除")
    @PostMapping("/delete")
    @Operation(summary="字典类型删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iSettingDictTypeService.del(idValidate.getId());
        return AjaxResult.success();
    }

}
