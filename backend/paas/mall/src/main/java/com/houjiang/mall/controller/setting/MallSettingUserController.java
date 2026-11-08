package com.houjiang.mall.controller.setting;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.setting.SettingUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商城后台 - 用户设置 Controller。
 * 对应 like-admin SettingUserController + SettingLoginController（共同前缀 /setting.user.user）。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/setting/user")
@RequiredArgsConstructor
public class MallSettingUserController {

    private final SettingUserService userService;

    // ===== 用户基础设置 =====

    /** GET /mall/setting/user/lists —— 用户基础设置详情 */
    @GetMapping("/lists")
    public ResponseResult<Map<String, Object>> lists() {
        return ResponseResult.success(userService.getUserConfig());
    }

    /** POST /mall/setting/user/edit —— 用户基础设置保存 */
    @PostMapping("/edit")
    public ResponseResult<Void> edit(@RequestBody Map<String, Object> params) {
        userService.setUserConfig(params);
        return ResponseResult.success();
    }

    // ===== 登录注册规则 =====

    /** GET /mall/setting/user/login/lists —— 登录注册详情 */
    @GetMapping("/login/lists")
    public ResponseResult<Map<String, Object>> loginLists() {
        return ResponseResult.success(userService.getLoginConfig());
    }

    /** POST /mall/setting/user/login/edit —— 登录注册保存 */
    @PostMapping("/login/edit")
    public ResponseResult<Void> loginEdit(@RequestBody Map<String, Object> params) {
        userService.setLoginConfig(params);
        return ResponseResult.success();
    }
}
