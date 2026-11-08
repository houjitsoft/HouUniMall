package com.houjiang.merchant.controller;

import com.houjiang.merchant.service.system.ILoginService;
import com.houjiang.merchant.validate.system.LoginsValidate;
import com.houjiang.merchant.vo.system.LoginVo;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.enums.ErrorEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 系统登录管理控制器
 * 登录和退出接口已在SecurityConfig白名单中配置，无需认证
 * 
 * @author Amos
 * @since 2026-04-28
 */
@RestController
@RequestMapping("merchant/login")
@Tag(name = "系统登录管理")
public class LoginController {
    @Resource
    ILoginService iLoginService;

    /**
     * 登录系统
     * 
     * @param loginsValidate 登录参数
     * @return AjaxResult<LoginVo> 登录结果，包含token和用户信息
     */
    @PostMapping("/login")
    @Operation(summary="登录系统")
    public AjaxResult<LoginVo> login(@Validated() @RequestBody LoginsValidate loginsValidate) {
        LoginVo vo = iLoginService.login(loginsValidate);
        return AjaxResult.success(1, "登陆成功", vo, ErrorEnum.HIDE_MSG.getCode());
    }

    /**
     * 退出登录
     * 
     * @param request HTTP请求对象，用于获取Authorization头
     * @return AjaxResult<Object> 退出结果
     */
    @PostMapping("/out")
    @Operation(summary="退出登录")
    public AjaxResult<Object> logout(HttpServletRequest request) {
        iLoginService.logout(request.getHeader("Authorization"));
        return AjaxResult.success();
    }

}
