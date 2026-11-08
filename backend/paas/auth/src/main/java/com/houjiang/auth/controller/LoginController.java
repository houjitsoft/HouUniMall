package com.houjiang.auth.controller;

import com.houjiang.auth.service.LoginServcie;
import com.houjiang.commons.annnotation.AccessLimit;
import com.houjiang.commons.annnotation.RepeatClick;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.webPos.ReceptionLoginDTO;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.third.UserAndShopInfoReqVo;
import com.houjiang.commons.vo.user.UserInfoVO;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;

/**
 * @author Amos
 */
@Slf4j
@RestController
public class LoginController {

    @Autowired
    private LoginServcie loginServcie;

    @Autowired
    private RedisCache redisCache;

    /**
     * 账号密码登录

     *
     * @param user
     * @return
     */
    @RepeatClick
    @PostMapping("/user/login")
    public ResponseResult login(@RequestBody UserInfoVO user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            return loginServcie.login(user, request, response);
        } catch (Exception e) {
            log.error("登录异常: {}", e.getMessage(), e);
            return ResponseResult.fail(500, "登录处理异常: " + e.getMessage());
        }
    }

    /**
     * 手机验证码登录

     *
     * @param paraMap
     * @return
     */
    @RepeatClick
    @RequestMapping("/user/codelogin")
    public ResponseResult codelogin(@RequestBody Map<String, String> paraMap) throws IOException {

        return loginServcie.codelogin(paraMap);
    }

    /**
     * 退出登录

     *
     * @param user
     * @return
     */

    @PostMapping("/user/loginOut")
    public ResponseResult loginOut(@RequestBody UserInfoVO user) {
        return loginServcie.logout(user);
    }


    /**
     * 发送验证码

     *
     * @param paraMap
     * @return
     */
    @PostMapping("/register/sjcheckphoneandsend")
    public ResponseResult sjCheckPhoneAndSend(@RequestBody Map<String, String> paraMap) throws IOException {

        ResponseResult result = loginServcie.sendVerificationCode(paraMap);

        return result;
    }

    /**
     * 平台获取图片验证码
     */
    @PostMapping("/platform/userinfo/getVerify")
    public ResponseResult getVerify() {
        return loginServcie.getVerify();
    }

    /**
     * 商家获取图片验证码
     */
    @PostMapping("/shop/getShopVerify")
    public ResponseResult getShopVerify() {
        return loginServcie.getShopVerify();
    }

    /**
     * 商家获取图片验证码
     */
    @PostMapping("/shop/getSupplierVerify")
    public ResponseResult getSupplierVerify() {
        return loginServcie.getSupplierVerify();
    }

    /**
     * 用户注销
     */
    @RequestMapping(value = "/user/logout", method = RequestMethod.POST)
    public ResponseResult userLogOut(@RequestBody Map<String, String> paraMap) {
        String token = paraMap.get("token");
        // 统一使用HOU_USER_TOKEN_USER
        redisCache.deleteObject(HouConfig.HOU_USER_TOKEN_USER + token);
        return ResponseResult.success();
    }

    /**
     * 生成验证码
     */
    @RequestMapping(value = "/getVerify", method = RequestMethod.POST)
    public ResponseResult getVerify2() {
        ResponseResult result = loginServcie.getVerify2();
        return result;
    }

    /**
     * 生成手机验证码2

     *
     * @param map
     * @return
     * @throws IOException
     */
    @RequestMapping("/verifiPhoneCode2")
    public ResponseResult verifiPhoneCode2(@RequestBody Map<String, Object> map) {
        ResponseResult rr = loginServcie.verifiPhoneCode2(map);
        return rr;
    }

    /**
     * app 微信授权登录

     *
     * @param paraMap
     * @return
     * @throws IOException
     */
    @RequestMapping("/weixin/uniappLogin")
    public Map<String, Object> uniappLogin(@RequestBody Map<String, String> paraMap, HttpServletRequest request, HttpServletResponse response) throws IOException {

        return loginServcie.uniappLogin(paraMap, request, response);
    }

    /**
     * app 微信授权注册

     *
     * @param paraMap
     * @return
     * @throws IOException
     */
    @RequestMapping("/weixin/uniappRegister")
    public ResponseResult uniappRegister(@RequestBody Map<String, String> paraMap) throws IOException, TransactionException {

        return loginServcie.uniappRegister(paraMap);
    }

    /**
     * 微信小程序 授权登录
     */
    @RequestMapping(value = "/weixin/code2Session")
    @ResponseBody
    /** 防止恶意请求 */
    @AccessLimit()
    public Map<String, Object> code2Session(@RequestBody Map<String, String> paraMap, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return loginServcie.code2Session(paraMap, request, response);
    }

    /**
     * 微信小程序 授权注册
     */
    @GlobalTransactional
    @PostMapping(value = "/weixin/codelogin")
    public ResponseResult weixinCodelogin(@RequestBody Map<String, String> paraMap)
            throws IOException, TransactionException {
        return loginServcie.weixinCodelogin(paraMap);
    }

    /**
     * 微信小程序 获取微信小程序code
     */
    @RequestMapping(value = "/weixin/code2SessionopenId")
    @ResponseBody
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult code2SessionopenId(@RequestBody Map<String, String> paraMap) throws IOException {
        return loginServcie.code2SessionopenId(paraMap);
    }

    /**
     * 查询当前用户 微信小程序是否允许自动登录
     */
    @PostMapping(value = "/weixin/queryAutoLogin")
    public Map<String, Object> queryAutoLogin(@RequestBody Map<String, String> paraMap) throws IOException {
        return loginServcie.queryAutoLogin(paraMap);
    }

    /**
     * 小程序支付前 授权
     */
    @PostMapping("/weixin/paymentAuthorization")
    public Map<String, Object> paymentAuthorization(@RequestBody Map<String, String> paraMap) throws IOException {
        return loginServcie.paymentAuthorization(paraMap);
    }

    /**
     * 微信小程序退出

     *
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/weixin/out")
    public ResponseResult weixinOut(@RequestBody Map<String, String> paraMap) throws IOException {
        return loginServcie.weixinOut(paraMap);
    }

    /**
     * 注销账号

     *
     * @param paraMap
     * @return
     */
    @GlobalTransactional
    @PostMapping("/h5/userLogoff")
    public ResponseResult userLogoff(@RequestBody Map<String, String> paraMap) throws TransactionException {
        return loginServcie.userLogoff(paraMap);
    }

    /**
     * 微信小程序 授权登录
     */
    @RequestMapping(value = "/weixinShopApp/code2Session")
    @ResponseBody
    /** 防止恶意请求 */
    @AccessLimit()
    public Map<String, Object> code2ShopAppSession(@RequestBody Map<String, String> paraMap, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return loginServcie.code2ShopAppSession(paraMap, request, response);
    }

    /**
     * 微信小程序 授权注册
     */
    @GlobalTransactional
    @PostMapping(value = "/weixinShopApp/codelogin")
    public ResponseResult weixinShopAppCodelogin(@RequestBody Map<String, String> paraMap)
            throws IOException, TransactionException {
        return loginServcie.weixinShopAppCodelogin(paraMap);
    }

    /**
     * 微信小程序 获取微信小程序code
     */
    @RequestMapping(value = "/weixinShopApp/code2SessionopenId")
    @ResponseBody
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult code2ShopAppSessionopenId(@RequestBody Map<String, String> paraMap) throws IOException {
        return loginServcie.code2ShopAppSessionopenId(paraMap);
    }

    /**
     * 查询当前用户 微信小程序是否允许自动登录
     */
    @PostMapping(value = "/weixinShopApp/queryAutoLogin")
    public Map<String, Object> queryShopAppAutoLogin(@RequestBody Map<String, String> paraMap) throws IOException {
        return loginServcie.queryShopAppAutoLogin(paraMap);
    }

    /**
     * 骑手用户登录

     *
     * @param user
     * @return
     */
    @PostMapping("/rider/login")
    public ResponseResult riderLogin(@RequestBody UserInfoVO user) {
        return loginServcie.riderLogin(user);
    }

    /**
     * PC端微信扫码登录 - 获取二维码

     * 
     * @return ResponseResult 包含二维码URL和二维码ID
     */
    @PostMapping("/auth/wechat/qrcode")
    public ResponseResult getWechatQRCode(@RequestParam(value = "application", required = false) String application) {
        return loginServcie.getLoginQrCode("wechat", application);
    }

    /**
     * PC端微信扫码登录 - 检查登录状态

     * 
     * @param qrCodeId 二维码ID
     * @return ResponseResult 包含登录状态（waiting/scanned/confirmed/expired）和token（如果已确认）
     */
    @GetMapping("/auth/wechat/login-status/{qrCodeId}")
    public ResponseResult checkWechatLoginStatus(@PathVariable String qrCodeId) {
        return loginServcie.checkWechatLoginStatus(qrCodeId);
    }

    /**
     * PC端微信扫码登录 - 取消登录

     * 
     * @param qrCodeId 二维码ID
     * @return ResponseResult
     */
    @PostMapping("/auth/wechat/cancel-login/{qrCodeId}")
    public ResponseResult cancelWechatLogin(@PathVariable String qrCodeId) {
        return loginServcie.cancelWechatLogin(qrCodeId);
    }

    /**
     * PC端微信扫码登录 - 微信授权回调

     * 
     * @param code  微信下发的临时票据
     * @param state 二维码ID
     * @return ResponseResult 包含登录状态，confirmed 时带 token
     */
    @RequestMapping(value = "/auth/wechat/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult wechatCallback(@RequestParam("code") String code,
                                         @RequestParam("state") String state) {
        return loginServcie.wechatCallback(code, state);
    }

    // ==================== 统一登录（UniLogin）：渠道无关的扫码登录 ====================
    // 渠道编码即路径参数，新增渠道（企业微信/钉钉/QQ/飞书…）由 LoginChannelRegistry 按配置解析，
    // 无需新增任何 Controller 方法。

    /**
     * 统一登录 - 获取扫码地址
     *
     * @param channel 渠道编码：wechat / wecom / dingtalk / qq / feishu
     * @return ResponseResult 包含 enabled / channel / qrCodeId / qrCode
     */
    @PostMapping("/auth/unilogin/{channel}/qrcode")
    public ResponseResult getLoginQrCode(@PathVariable("channel") String channel,
                                         @RequestParam(value = "application", required = false) String application) {
        return loginServcie.getLoginQrCode(channel, application);
    }

    /**
     * 统一登录 - 检查登录状态
     *
     * @param channel  渠道编码
     * @param qrCodeId 会话标识
     * @return ResponseResult 包含登录状态（waiting/scanned/confirmed/expired）
     */
    @GetMapping("/auth/unilogin/{channel}/login-status/{qrCodeId}")
    public ResponseResult checkLoginStatus(@PathVariable("channel") String channel,
                                           @PathVariable("qrCodeId") String qrCodeId) {
        return loginServcie.checkLoginStatus(channel, qrCodeId);
    }

    /**
     * 统一登录 - 取消登录
     *
     * @param channel  渠道编码
     * @param qrCodeId 会话标识
     * @return ResponseResult
     */
    @PostMapping("/auth/unilogin/{channel}/cancel-login/{qrCodeId}")
    public ResponseResult cancelLogin(@PathVariable("channel") String channel,
                                      @PathVariable("qrCodeId") String qrCodeId) {
        return loginServcie.cancelLogin(channel, qrCodeId);
    }

    /**
     * 统一登录 - 授权回调
     *
     * @param channel 渠道编码
     * @param code    第三方下发的临时票据
     * @param state   会话标识
     * @return ResponseResult 包含登录状态，confirmed 时带 token
     */
    @RequestMapping(value = "/auth/unilogin/{channel}/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult loginCallback(@PathVariable("channel") String channel,
                                        @RequestParam("code") String code,
                                        @RequestParam("state") String state) {
        return loginServcie.loginCallback(channel, code, state);
    }

    /**
     * 统一登录 - 渠道概况（编码/名称/是否已配置，不含凭据）
     *
     * @return ResponseResult 渠道列表
     */
    @GetMapping("/auth/unilogin/channels")
    public ResponseResult listLoginChannels() {
        return loginServcie.listLoginChannels();
    }

    /**
     * 统一登录 - 扫码后绑定（手机号未注册时自动建会员号）
     *
     * <p>商城前台会员扫码后若该第三方账号尚未绑定平台账号，引导其补一次
     * 手机号 + 短信验证码：已注册 → 登录并写入绑定；未注册 → 先建号再绑定。
     * 免鉴权（此时还没有登录态），安全性由短信验证码与 5 分钟有效的扫码会话共同保证。</p>
     *
     * @param channel 渠道编码
     * @param param   qrCodeId（扫码会话标识，兼容旧字段 state）、userMobile、phoneCode
     * @return ResponseResult 包含 status=confirmed 与 token / userId / userMobile
     */
    @PostMapping("/auth/unilogin/{channel}/bind-login")
    public ResponseResult bindLogin(@PathVariable("channel") String channel,
                                    @RequestBody(required = false) Map<String, Object> param) {
        Map<String, Object> body = param == null ? new java.util.HashMap<>() : param;
        Object qrCodeId = body.get("qrCodeId") != null ? body.get("qrCodeId") : body.get("state");
        return loginServcie.bindLogin(channel, asText(qrCodeId), asText(body.get("userMobile")),
                asText(body.get("phoneCode")));
    }

    /** 参数转字符串；null 统一转空串，避免拼出 "null" */
    private static String asText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    /**
     * 骑手退出登录

     *
     * @param token
     * @return
     */
    @PostMapping("/rider/logout")
    public ResponseResult riderLogout(@RequestParam("token") String token) {
        return loginServcie.riderLogout(token);
    }

    /**
     * webPos员工用户登录

     *
     * @param user
     * @return
     */
    @PostMapping("/webPos/login")
    public ResponseResult webPosLogin(@RequestBody ReceptionLoginDTO user) {
        return loginServcie.webPosLogin(user);
    }

    /**
     * webPos员工退出登录

     *
     * @return
     */
    @PostMapping("/webPos/logout")
    public ResponseResult webPosLogout() {
        return loginServcie.webPosLogout();
    }

}
