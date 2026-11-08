package com.houjiang.merchant.controller.setting;

import com.houjiang.merchant.aop.Log;
import com.houjiang.merchant.service.ISettingLoginService;
import com.houjiang.merchant.validate.setting.SettingLoginValidate;
import com.houjiang.merchant.vo.setting.SettingLoginVo;
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
public class SettingLoginController {

    @Resource
    ISettingLoginService iSettingLoginService;

    @GetMapping("/getRegisterConfig")
    @Operation(summary="登录设置详情")
    public AjaxResult<SettingLoginVo> getRegisterConfig() {
        SettingLoginVo vo = iSettingLoginService.getRegisterConfig();
        return AjaxResult.success(vo);
    }

    @Log(title = "登录设置编辑")
    @PostMapping("/setRegisterConfig")
    @Operation(summary="登录设置编辑")
    public AjaxResult<Object> setRegisterConfig(@Validated @RequestBody SettingLoginValidate loginValidate) {
        iSettingLoginService.setRegisterConfig(loginValidate);
        return AjaxResult.success();
    }

}
