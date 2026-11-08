package com.houjiang.mall.controller.auth;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.auth.MallAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 管理员认证控制器
 * 提供登录、获取用户信息、获取菜单、退出登录接口
 * 路由前缀: /mall/auth
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/auth")
@RequiredArgsConstructor
public class MallAuthController {

    private final MallAuthService mallAuthService;

    /**
     * Mall 管理员登录
     *
     * @param params 登录参数（account, password）
     * @return 登录结果（包含token和管理员信息）
     */
    @PostMapping("/login")
    public ResponseResult login(@RequestBody Map<String, Object> params) {
        String account = (String) params.get("account");
        String password = (String) params.get("password");
        return mallAuthService.login(account, password);
    }

    /**
     * 获取当前管理员信息
     *
     * @param token JWT Token（从Authorization请求头获取）
     * @return 管理员信息
     */
    @PostMapping("/userinfo")
    public ResponseResult getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseResult.fail(401, "未登录");
        }
        return mallAuthService.getUserInfo(token);
    }

    /**
     * 获取管理员菜单
     *
     * @param token JWT Token（从Authorization请求头获取）
     * @return 菜单树
     */
    @PostMapping("/menus")
    public ResponseResult getMenus(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseResult.fail(401, "未登录");
        }
        return mallAuthService.getMenus(token);
    }

    /**
     * 退出登录
     *
     * @param token JWT Token（从Authorization请求头获取）
     * @return 退出结果
     */
    @PostMapping("/logout")
    public ResponseResult logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseResult.fail(401, "未登录");
        }
        return mallAuthService.logout(token);
    }
}
