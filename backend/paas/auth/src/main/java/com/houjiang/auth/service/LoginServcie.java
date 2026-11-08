package com.houjiang.auth.service;

import com.houjiang.commons.dto.webPos.ReceptionLoginDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.third.UserAndShopInfoReqVo;
import com.houjiang.commons.vo.user.UserInfoVO;
import io.seata.core.exception.TransactionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @author Amos
 */
public interface LoginServcie {

    /**
     * TODO 补充方法说明
     * @param user TODO
     * @param request TODO
     * @param response TODO
     * @return TODO
     */
    ResponseResult login(UserInfoVO user, HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * TODO 补充方法说明
     * @param user TODO
     * @return TODO
     */
    ResponseResult logout(UserInfoVO user);

    /**
     * TODO 补充方法说明
     * @param paraMap TODO
     * @return TODO
     */
    ResponseResult codelogin(Map<String, String> paraMap);

    /**
     * TODO 补充方法说明
     * @param paraMap TODO
     * @return TODO
     */
    ResponseResult sendVerificationCode(Map<String, String> paraMap);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ResponseResult getVerify();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ResponseResult getVerify2();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ResponseResult getShopVerify();

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    ResponseResult getSupplierVerify();

    /**
     * 生成手机验证码2
     *
     * @param map
     * @return
     * @throws IOException
     */
    ResponseResult verifiPhoneCode2(Map<String, Object> map);

    /**
     * app 授权登录
     *
     * @param paraMap
     * @return
     */
    Map<String, Object> uniappLogin(Map<String, String> paraMap, HttpServletRequest request, HttpServletResponse response);

    /**
     * app 微信授权注册
     *
     * @param paraMap
     * @return
     */
    ResponseResult uniappRegister(Map<String, String> paraMap) throws IOException, TransactionException;

    /**
     * 微信小程序 授权登录
     *
     * @param paraMap
     * @param request
     * @param response
     * @return
     */
    Map<String, Object> code2Session(Map<String, String> paraMap, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException;

    /**
     * 微信小程序 授权注册
     *
     * @param paraMap
     * @return
     */
    ResponseResult weixinCodelogin(Map<String, String> paraMap) throws IOException, TransactionException;

    /**
     * 获取微信小程序code
     *
     * @param paraMap
     * @return
     * @throws IOException
     */
    ResponseResult code2SessionopenId(Map<String, String> paraMap) throws IOException;

    /**
     * 微信小程序是否允许自动登录
     *
     * @param paraMap
     * @return
     */
    Map<String, Object> queryAutoLogin(Map<String, String> paraMap) throws UnsupportedEncodingException;

    /**
     * 小程序支付前 授权
     *
     * @param paraMap
     * @return
     */
    Map<String, Object> paymentAuthorization(Map<String, String> paraMap) throws UnsupportedEncodingException;

    /**
     * 微信小程序退出
     *
     * @param paraMap
     * @return
     */
    ResponseResult weixinOut(Map<String, String> paraMap);

    /**
     * 注销账号
     *
     * @param paraMap
     * @return
     */
    ResponseResult userLogoff(Map<String, String> paraMap) throws TransactionException;

    /**
     * 微信小程序 授权登录
     *
     * @param paraMap
     * @param request
     * @param response
     * @return
     */
    Map<String, Object> code2ShopAppSession(Map<String, String> paraMap, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException;

    /**
     * 微信小程序 授权注册
     *
     * @param paraMap
     * @return
     */
    ResponseResult weixinShopAppCodelogin(Map<String, String> paraMap) throws IOException, TransactionException;

    /**
     * 获取微信小程序code
     *
     * @param paraMap
     * @return
     * @throws IOException
     */
    ResponseResult code2ShopAppSessionopenId(Map<String, String> paraMap) throws IOException;

    /**
     * 微信小程序是否允许自动登录
     *
     * @param paraMap
     * @return
     */
    Map<String, Object> queryShopAppAutoLogin(Map<String, String> paraMap) throws UnsupportedEncodingException;

    /**
     * 骑手用户登录
     *
     * @param user
     * @return
     */
    ResponseResult riderLogin(UserInfoVO user);

    /**
     * 骑手退出登录
     *
     * @param token
     * @return
     */
    ResponseResult riderLogout(String token);

    /**
     * webPos员工用户登录
     *
     * @param user
     * @return
     */
    ResponseResult webPosLogin(ReceptionLoginDTO user);

    /**
     * webPos员工退出登录
     *
     * @return
     */
    ResponseResult webPosLogout();

    /**
     * 统一登录 - 获取扫码地址
     *
     * <p>渠道无关：微信/企业微信/钉钉/QQ/飞书等共用同一套流程，渠道差异由配置描述。
     * 渠道未配置凭据时返回 enabled=false，前端据此隐藏入口。</p>
     *
     * @param channel 渠道编码，如 wechat / wecom / dingtalk / qq / feishu
     * @return ResponseResult 包含 enabled / channel / qrCodeId / qrCode
     */
    default ResponseResult getLoginQrCode(String channel) {
        return getLoginQrCode(channel, null);
    }

    ResponseResult getLoginQrCode(String channel, String application);

    /**
     * 统一登录 - 授权回调
     *
     * @param channel 渠道编码
     * @param code    第三方下发的临时票据
     * @param state   会话标识，用于防 CSRF
     * @return ResponseResult 包含 status（confirmed/unbound/expired/failed），confirmed 时带 token
     */
    ResponseResult loginCallback(String channel, String code, String state);

    /**
     * 统一登录 - 检查登录状态
     *
     * @param channel  渠道编码
     * @param qrCodeId 会话标识
     * @return ResponseResult 包含登录状态（waiting/scanned/confirmed/expired）和token（如果已确认）
     */
    ResponseResult checkLoginStatus(String channel, String qrCodeId);

    /**
     * 统一登录 - 取消登录
     *
     * @param channel  渠道编码
     * @param qrCodeId 会话标识
     * @return ResponseResult
     */
    ResponseResult cancelLogin(String channel, String qrCodeId);

    /**
     * 统一登录 - 渠道概况（编码/名称/是否已配置，不含凭据）
     *
     * @return ResponseResult 渠道列表
     */
    ResponseResult listLoginChannels();

    /**
     * 统一登录 - 扫码后绑定（手机号不存在时自动注册会员）
     *
     * <p>扫码后若该第三方账号尚未绑定平台账号，前端引导用户填手机号 + 短信验证码，
     * 校验通过后：手机号已注册则直接登录并写入绑定，未注册则先自动建号再绑定。
     * 这样会员「首次扫码即可完成注册 + 绑定 + 登录」，不需要先有账号。</p>
     *
     * @param channel    渠道编码
     * @param qrCodeId   扫码会话标识（取码接口返回的 qrCodeId）
     * @param userMobile 手机号
     * @param phoneCode  短信验证码
     * @return ResponseResult 包含 status=confirmed 与 token / userId / userMobile
     */
    ResponseResult bindLogin(String channel, String qrCodeId, String userMobile, String phoneCode);


    /**
     * PC端微信扫码登录 - 获取二维码（等价于 getLoginQrCode("wechat")）
     *
     * @return ResponseResult 包含二维码URL和二维码ID
     * @deprecated 改用渠道无关的 {@link #getLoginQrCode(String)}
     */
    @Deprecated
    ResponseResult getWechatQRCode();

    /**
     * PC端微信扫码登录 - 微信授权回调
     *
     * @param code  微信下发的临时票据
     * @param state 二维码ID，用于防 CSRF
     * @return ResponseResult 包含 status（confirmed/unbound/expired/failed），confirmed 时带 token
     * @deprecated 改用渠道无关的 {@link #loginCallback(String, String, String)}
     */
    @Deprecated
    ResponseResult wechatCallback(String code, String state);

    /**
     * PC端微信扫码登录 - 检查登录状态
     *
     * @param qrCodeId 二维码ID
     * @return ResponseResult 包含登录状态（waiting/scanned/confirmed/expired）和token（如果已确认）
     * @deprecated 改用渠道无关的 {@link #checkLoginStatus(String, String)}
     */
    @Deprecated
    ResponseResult checkWechatLoginStatus(String qrCodeId);

    /**
     * PC端微信扫码登录 - 取消登录
     *
     * @param qrCodeId 二维码ID
     * @return ResponseResult
     * @deprecated 改用渠道无关的 {@link #cancelLogin(String, String)}
     */
    @Deprecated
    ResponseResult cancelWechatLogin(String qrCodeId);
}
