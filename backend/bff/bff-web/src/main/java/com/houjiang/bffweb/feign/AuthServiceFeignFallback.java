package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.bffweb.vo.UserInfoVO;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 认证服务Feign客户端熔断器
/**
 * 当认证服务不可用时提供降级处理
 * @author Amos
 */
@Component
public class AuthServiceFeignFallback implements AuthServiceFeign {

    private static final String SERVICE_UNAVAILABLE = "认证服务暂时不可用";

    @Override
    public ResponseResult login(Map<String, Object> loginData) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult codelogin(Map<String, Object> paraMap) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult logout(UserInfoVO user) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult sendCode(Map<String, Object> paraMap) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult getVerify() {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult generateSlideCaptcha() {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult verifyGenerateCaptcha(Map<String, Object> paraMap) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult getWechatQRCode(String application) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult checkWechatLoginStatus(String qrCodeId) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult cancelWechatLogin(String qrCodeId) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult wechatCallback(String code, String state) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult getLoginQrCode(String channel, String application) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult checkLoginStatus(String channel, String qrCodeId) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult cancelLogin(String channel, String qrCodeId) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult loginCallback(String channel, String code, String state) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult listLoginChannels() {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseResult bindLogin(String channel, Map<String, Object> param) {
        return ResponseResult.fail(SERVICE_UNAVAILABLE);
    }
}

