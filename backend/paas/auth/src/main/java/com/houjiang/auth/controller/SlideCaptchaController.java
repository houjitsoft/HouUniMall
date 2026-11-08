package com.houjiang.auth.controller;

import com.houjiang.auth.service.SlideCaptchaService;
import com.houjiang.commons.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 滑动拼图验证码接口（登录「验证2」）。
 *
 * <p>与旧 mall-auth-server 的 /generate、/verifyGenerateCaptcha 协议保持一致，
 * 供商城 PC/uniapp 的 SlideVerify 组件调用。两个接口均在登录前使用，
 * 已在 SecurityConfig 白名单中放行，允许匿名访问。</p>
 *
 * @author Amos
 */
@Slf4j
@RestController
public class SlideCaptchaController {

    @Autowired
    private SlideCaptchaService slideCaptchaService;

    /**
     * 生成滑动拼图验证码。
     *
     * @return ResponseResult data 包含 bgImage（Base64 背景图）、imgToken、targetX、targetY
     */
    @PostMapping("/generate")
    public ResponseResult generate() {
        try {
            return ResponseResult.success(slideCaptchaService.generate());
        } catch (Exception e) {
            log.error("生成滑动验证码异常: {}", e.getMessage(), e);
            return ResponseResult.fail(500, "验证码生成失败，请稍后重试");
        }
    }

    /**
     * 校验滑动拼图验证码。
     *
     * @param paraMap 包含 imgToken、targetX（用户拖动横向位置）、targetY
     * @return ResponseResult data 为 true/false
     */
    @PostMapping("/verifyGenerateCaptcha")
    public ResponseResult verifyGenerateCaptcha(@RequestBody Map<String, Object> paraMap) {
        String imgToken = paraMap.get("imgToken") == null ? "" : String.valueOf(paraMap.get("imgToken"));
        int targetX = parseInt(paraMap.get("targetX"));
        return ResponseResult.success(slideCaptchaService.verify(imgToken, targetX));
    }

    /**
     * 安全解析整型参数，兼容字符串/数值两种传参形式。
     *
     * @param value 原始参数值
     * @return 解析结果，非法输入返回 Integer.MIN_VALUE（必然校验失败）
     */
    private int parseInt(Object value) {
        if (value == null) {
            return Integer.MIN_VALUE;
        }
        try {
            return (int) Math.round(Double.parseDouble(String.valueOf(value)));
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }
}
