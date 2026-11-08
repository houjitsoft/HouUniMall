package com.houjiang.merchant.controller.setting;

import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.validate.commons.IdArrayValidate;
import com.houjiang.merchant.service.ISettingDictDataService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.setting.DictDataCreateValidate;
import com.houjiang.merchant.validate.setting.DictDataUpdateValidate;
import com.houjiang.merchant.vo.setting.SettingDictDataVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.util.StringUtils;
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
@RequestMapping("/adminapi/setting.dict.dict_data")
public class SettingDictDataController {

    @Resource
    ISettingDictDataService iSettingDictDataService;

    @GetMapping("/all")
    @Operation(summary="字典数据所有")
    public AjaxResult<List<SettingDictDataVo>> all(@RequestParam Map<String, String> params) {
        Assert.isFalse(StringUtils.isEmpty(params.get("type_id")), "type_id缺失");
        List<SettingDictDataVo> list = iSettingDictDataService.all(params);
        return AjaxResult.success(list);
    }

    @GetMapping("/lists")
    @Operation(summary="字典数据列表")
    public AjaxResult<PageResult<SettingDictDataVo>> list(@Validated PageValidate pageValidate,
                                                          @RequestParam Map<String, String> params) {
        Assert.isFalse(StringUtils.isEmpty(params.get("type_id")), "type_id缺失");
        PageResult<SettingDictDataVo> list = iSettingDictDataService.list(pageValidate, params);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @Operation(summary="字典数据详情")
    public AjaxResult<SettingDictDataVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        SettingDictDataVo vo = iSettingDictDataService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "字典数据新增")
    @PostMapping("/add")
    @Operation(summary="字典数据新增")
    public AjaxResult<Object> add(@Validated @RequestBody DictDataCreateValidate createValidate) {
        iSettingDictDataService.add(createValidate);
        return AjaxResult.success("操作成功");
    }

    @Log(title = "字典数据编辑")
    @PostMapping("/edit")
    @Operation(summary="字典数据编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody DictDataUpdateValidate updateValidate) {
        iSettingDictDataService.edit(updateValidate);
        return AjaxResult.success("操作成功");
    }

    @Log(title = "字典数据删除")
    @PostMapping("/delete")
    @Operation(summary="字典数据删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdArrayValidate idArrayValidate) {
        iSettingDictDataService.del(idArrayValidate.getId());
        return AjaxResult.success("操作成功");
    }

}
