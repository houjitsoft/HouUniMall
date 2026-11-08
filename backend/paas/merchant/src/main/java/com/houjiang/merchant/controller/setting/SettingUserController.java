package com.houjiang.merchant.controller.setting;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.ISettingUserService;
import com.houjiang.merchant.validate.setting.SettingUserValidate;
import com.houjiang.merchant.vo.setting.SettingUserVo;
import com.houjiang.merchant.common.core.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/adminapi/setting.user.user")
public class SettingUserController {

    @Resource
    ISettingUserService iSettingUserService;

    @GetMapping("/getConfig")
    @Operation(summary="用户设置详情")
    public AjaxResult<SettingUserVo> detail() {
        SettingUserVo vo = iSettingUserService.getConfig();
        return AjaxResult.success(vo);
    }

    @Log(title = "用户设置编辑")
    @PostMapping("/setConfig")
    @Operation(summary="用户设置编辑")
    public AjaxResult<Object> setConfig(@Validated @RequestBody SettingUserValidate userValidate) {
        iSettingUserService.setConfig(userValidate);
        return AjaxResult.success();
    }

}
