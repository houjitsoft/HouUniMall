package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mall 管理员认证 Controller
/**
 * 独立于现有 permission 服务，使用 hou_mall_admin 和 hou_mall_system_* 表
 * 通过 Feign 调用 Mall 服务的认证接口
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/auth")
public class MallAuthController {
    
    @Autowired
    private MallServiceFeign mallServiceFeign;
    
    /**
     * 浏览器直接打开 API 地址时会发 GET，返回明确提示（非登录失败）。
     */
    @GetMapping("/login")
    public ResponseResult<Void> loginHint() {
        return ResponseResult.fail(400,
                "这是登录接口，请使用 POST 调用；商城管理后台入口：https://bbc.houjit.com/admin/");
    }

    /**
     * Mall 管理员登录
/**
     *
     * @param params 登录参数（account, password）
     * @return 登录结果（包含token）
     */
    @PostMapping("/login")
    public ResponseResult login(@RequestBody Map<String, Object> params) {
        try {
            return mallServiceFeign.mallLogin(params);
        } catch (Exception e) {
            log.error("Mall管理员登录失败", e);
            return ResponseResult.fail(500, "登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前管理员信息
/**
     *
     * @param token JWT Token
     * @return 管理员信息
     */
    @PostMapping("/userinfo")
    public ResponseResult getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            return mallServiceFeign.mallGetUserInfo(token);
        } catch (Exception e) {
            log.error("获取管理员信息失败", e);
            return ResponseResult.fail(500, "获取信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取管理员菜单
/**
     *
     * @param token JWT Token
     * @return 菜单树
     */
    @PostMapping("/menus")
    public ResponseResult getMenus(@RequestHeader("Authorization") String token) {
        try {
            return mallServiceFeign.mallGetMenus(token);
        } catch (Exception e) {
            log.error("获取菜单失败", e);
            return ResponseResult.fail(500, "获取菜单失败: " + e.getMessage());
        }
    }
    
    /**
     * 退出登录
/**
     *
     * @param token JWT Token
     * @return 退出结果
     */
    @PostMapping("/logout")
    public ResponseResult logout(@RequestHeader("Authorization") String token) {
        try {
            return mallServiceFeign.mallLogout(token);
        } catch (Exception e) {
            log.error("退出登录失败", e);
            return ResponseResult.fail(500, "退出失败: " + e.getMessage());
        }
    }
}
