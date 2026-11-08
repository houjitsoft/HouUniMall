package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.UserServiceFeign;
import com.houjiang.bffweb.feign.AuthServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall商城用户接口控制器
/**
 * 面向PC前台用户的认证和用户信息接口
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/user")
public class MallUserController {
    
    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private AuthServiceFeign authServiceFeign;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseResult login(@RequestBody Map<String, Object> param) {
        // 登录认证属于 auth 服务；userms 仅负责用户资料，误调用其 login 会触发 11006。
        // 商城旧版密码登录和短信登录共用此路径，按请求字段转发到对应认证接口。
        Map<String, Object> loginParam = param == null
                ? new java.util.HashMap<>()
                : new java.util.HashMap<>(param);
        // 兼容旧商城页面使用的 code/phone 字段，统一成 auth 短信登录接口的字段名。
        if (!hasText(loginParam.get("phoneCode")) && hasText(loginParam.get("code"))) {
            loginParam.put("phoneCode", loginParam.get("code"));
        }
        if (!hasText(loginParam.get("userMobile")) && hasText(loginParam.get("phone"))) {
            loginParam.put("userMobile", loginParam.get("phone"));
        }
        // 商城 PC 登录入口只允许买家平台。客户端即使漏传或传错平台字段，
        // 也不能被 auth 服务按商家后台登录处理并触发旧图形验证码校验。
        loginParam.remove("platForm");
        loginParam.put("loginPlatform", 1); // PC 买家平台，确保 auth 不走商家验证码分支
        if (hasText(loginParam.get("phoneCode"))) {
            return authServiceFeign.codelogin(loginParam);
        }
        return authServiceFeign.login(loginParam);
    }

    private boolean hasText(Object value) {
        return value != null && !String.valueOf(value).trim().isEmpty();
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseResult register(@RequestBody Map<String, Object> param) {
        return userServiceFeign.register(param);
    }
    
    /**
     * 获取用户信息
     */
    @PostMapping("/info")
    public ResponseResult getUserInfo(@RequestBody Map<String, Object> param) {
        return userServiceFeign.getUserInfo(param);
    }
    
    /**
     * 修改用户信息
     */
    @PostMapping("/update")
    public ResponseResult updateUserInfo(@RequestBody Map<String, Object> param) {
        return userServiceFeign.updateUserInfo(param);
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/password")
    public ResponseResult changePassword(@RequestBody Map<String, Object> param) {
        return userServiceFeign.changePassword(param);
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseResult logout(@RequestBody Map<String, Object> param) {
        return userServiceFeign.logout(param);
    }
}
