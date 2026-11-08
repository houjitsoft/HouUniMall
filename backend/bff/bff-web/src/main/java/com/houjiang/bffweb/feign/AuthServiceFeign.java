package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.bffweb.vo.UserInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证服务Feign客户端
/**
 *
 * @description 调用paas中台认证服务(auth)的接口
 * @author Amos
 * @date 2024-01-01
 */
@FeignClient(name = "auth", contextId = "authServiceFeign", url = "${feign.auth.url:}",
        fallback = AuthServiceFeignFallback.class)
public interface AuthServiceFeign {

    /**
     * 用户登录
/**
     *
     * @param loginData 登录数据
     * @return ResponseResult
     */
    @PostMapping("/user/login")
    ResponseResult login(@RequestBody Map<String, Object> loginData);

    /**
     * 手机验证码登录
/**
     *
     * @param paraMap 登录参数
     * @return ResponseResult
     */
    @RequestMapping("/user/codelogin")
    ResponseResult codelogin(@RequestBody Map<String, Object> paraMap);

    /**
     * 退出登录
/**
     *
     * @param user 用户信息
     * @return ResponseResult
     */
    @PostMapping("/user/loginOut")
    ResponseResult logout(@RequestBody UserInfoVO user);

    /**
     * 发送验证码
/**
     *
     * @param paraMap 发送验证码参数
     * @return ResponseResult
     */
    @PostMapping("/register/sjcheckphoneandsend")
    ResponseResult sendCode(@RequestBody Map<String, Object> paraMap);

    /**
     * 获取图片验证码
/**
     *
     * @return ResponseResult
     */
    @PostMapping("/platform/userinfo/getVerify")
    ResponseResult getVerify();

    /**
     * 生成滑动拼图验证码（登录验证2，匿名访问）
     *
     * @return ResponseResult data 包含 bgImage、imgToken、targetX、targetY
     */
    @PostMapping("/generate")
    ResponseResult generateSlideCaptcha();

    /**
     * 校验滑动拼图验证码（登录验证2，匿名访问）
     *
     * @param paraMap 包含 imgToken、targetX、targetY
     * @return ResponseResult data 为 true/false
     */
    @PostMapping("/verifyGenerateCaptcha")
    ResponseResult verifyGenerateCaptcha(@RequestBody Map<String, Object> paraMap);

    /**
     * 获取微信登录二维码
/**
     *
     * @return ResponseResult 包含二维码URL和二维码ID
     */
    @PostMapping("/auth/wechat/qrcode")
    ResponseResult getWechatQRCode(@RequestParam(value = "application", required = false) String application);

    /**
     * 检查微信登录状态
/**
     *
     * @param qrCodeId 二维码ID
     * @return ResponseResult 包含登录状态
     */
    @GetMapping("/auth/wechat/login-status/{qrCodeId}")
    ResponseResult checkWechatLoginStatus(@PathVariable String qrCodeId);

    /**
     * 取消微信登录
/**
     *
     * @param qrCodeId 二维码ID
     * @return ResponseResult
     */
    @PostMapping("/auth/wechat/cancel-login/{qrCodeId}")
    ResponseResult cancelWechatLogin(@PathVariable String qrCodeId);

    /**
     * 微信扫码登录 - 授权回调
/**
     *
     * @param code  微信下发的临时票据
     * @param state 二维码ID
     * @return ResponseResult 包含登录状态，confirmed 时带 token
     */
    // 注意：Feign 的方法只允许声明唯一一个 HTTP 方法，写成 method = {GET, POST} 会在
    // 创建 Feign bean 时抛 IllegalStateException（Method xxx can only contain 1 method field），
    // 导致 bff-web 整个服务起不来。回调语义是浏览器重定向，这里固定用 GET
    // （auth 侧 /auth/wechat/callback 同时接受 GET 与 POST，用 GET 不会丢参数）。
    @GetMapping("/auth/wechat/callback")
    ResponseResult wechatCallback(@RequestParam("code") String code, @RequestParam("state") String state);

    // ==================== 统一登录（UniLogin）：渠道无关 ====================

    /**
     * 统一登录 - 获取扫码地址
     *
     * @param channel 渠道编码：wechat / wecom / dingtalk / qq / feishu
     */
    @PostMapping("/auth/unilogin/{channel}/qrcode")
    ResponseResult getLoginQrCode(@PathVariable("channel") String channel,
                                   @RequestParam(value = "application", required = false) String application);

    /**
     * 统一登录 - 检查登录状态
     */
    @GetMapping("/auth/unilogin/{channel}/login-status/{qrCodeId}")
    ResponseResult checkLoginStatus(@PathVariable("channel") String channel, @PathVariable("qrCodeId") String qrCodeId);

    /**
     * 统一登录 - 取消登录
     */
    @PostMapping("/auth/unilogin/{channel}/cancel-login/{qrCodeId}")
    ResponseResult cancelLogin(@PathVariable("channel") String channel, @PathVariable("qrCodeId") String qrCodeId);

    /**
     * 统一登录 - 授权回调
     */
    // 同上：Feign 不允许 method = {GET, POST}，统一登录回调固定用 GET
    // （auth 侧 /auth/unilogin/{channel}/callback 同时接受 GET 与 POST）。
    @GetMapping("/auth/unilogin/{channel}/callback")
    ResponseResult loginCallback(@PathVariable("channel") String channel,
                                 @RequestParam("code") String code, @RequestParam("state") String state);

    /**
     * 统一登录 - 渠道概况（编码/名称/是否已配置，不含凭据）
     */
    @GetMapping("/auth/unilogin/channels")
    ResponseResult listLoginChannels();

    /**
     * 统一登录 - 扫码后绑定（手机号未注册时自动建会员号）
     *
     * @param channel 渠道编码
     * @param param   qrCodeId / userMobile / phoneCode
     */
    @PostMapping("/auth/unilogin/{channel}/bind-login")
    ResponseResult bindLogin(@PathVariable("channel") String channel, @RequestBody Map<String, Object> param);
}