package com.houjiang.merchant.common.plugin.wechat;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.houjiang.merchant.common.util.ConfigUtils;
import com.houjiang.merchant.common.util.HttpUtils;
import com.houjiang.merchant.common.util.MapUtils;
import com.houjiang.merchant.common.util.StringUtils;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.security.MessageDigest;
import java.util.Map;

/**
 * 微信基础驱动
 */
@Component
public class WxMnpDriver {

    private static WxMaService wxMaService;

    private static WxMpService wxMpService;

    /**
     * 微信小程序依赖注入
     */
    @Resource
    public void setWxMaService(WxMaService wxMaService) {
        WxMnpDriver.wxMaService = wxMaService;
    }

    /**
     * 微信公众号依赖注入
     */
    @Resource
    public void setWxOaService(WxMpService wxMpService) {
        WxMnpDriver.wxMpService = wxMpService;
    }


    /**
     * 微信小程序
     *
     * @author Amos
     * @return WxMaService
     */
    public static WxMaService mnp() {
        Map<String, String> config = ConfigUtils.get("mnp_setting");

        WxMaDefaultConfigImpl wxConfig = new WxMaDefaultConfigImpl();
        wxConfig.setAppid(config.getOrDefault("app_id", ""));
        wxConfig.setSecret(config.getOrDefault("app_secret", ""));
        wxMaService.setWxMaConfig(wxConfig);

        return wxMaService;
    }

    /**
     * 微信公众号
     *
     * @author Amos
     * @return WxMpService
     */
    public static WxMpService oa() {
        Map<String, String> config = ConfigUtils.get("oa_setting");

        WxMpDefaultConfigImpl wxMpDefaultConfig = new WxMpDefaultConfigImpl();
        wxMpDefaultConfig.setAppId(config.getOrDefault("app_id", "").trim());
        wxMpDefaultConfig.setSecret(config.getOrDefault("app_secret", "").trim());
        wxMpDefaultConfig.setToken(config.getOrDefault("token", "").trim());
        wxMpDefaultConfig.setAesKey(config.getOrDefault("encoding_aes_key", "").trim());
        wxMpService.setWxMpConfigStorage(wxMpDefaultConfig);

        return wxMpService;
    }

    /**
     * 微信开放平台
     *
     * @author Amos
     * @return WxMpService
     */
    public static WxMpService openSetting() {
        Map<String, String> config = ConfigUtils.get("open_platform");

        WxMpDefaultConfigImpl wxMpDefaultConfig = new WxMpDefaultConfigImpl();
        wxMpDefaultConfig.setAppId(config.getOrDefault("app_id", "").trim());
        wxMpDefaultConfig.setSecret(config.getOrDefault("app_secret", "").trim());
        wxMpDefaultConfig.setToken(config.getOrDefault("token", "").trim());
        wxMpDefaultConfig.setAesKey(config.getOrDefault("encoding_aes_key", "").trim());
        wxMpService.setWxMpConfigStorage(wxMpDefaultConfig);

        return wxMpService;
    }

    /**
     * 获取授权页ticket
     *
     * @author Amos
     * @param accessToken token
     * @return String
     */
    public static String getJsSdkGetTicket(String accessToken) {
        String jsSdkGetTicketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
        String url = String.format(jsSdkGetTicketUrl, accessToken);
        String results = HttpUtils.sendGet(url);
        Map<String, String> resultMap = MapUtils.jsonToMap(results);
        String jsapi_ticket = null;
        if (resultMap != null && StringUtils.isNotNull(resultMap.get("ticket"))) {
            jsapi_ticket = resultMap.get("ticket");
        }
        return jsapi_ticket;
    }

    /**
     * 构建JsSDK
     *
     * @author Amos
     * @param ticket ticket
     * @param timestamp 时间戳
     * @param nonceStr 串
     * @param url 地址
     * @return String
     * @throws Exception 异常
     */
    public static String buildJSSDKSignature(String ticket, String timestamp, String nonceStr, String url) throws Exception {
        String orderedString = "jsapi_ticket=" + ticket
                + "&noncestr=" + nonceStr
                + "&timestamp=" + timestamp
                + "&url=" + url;
        return sha1(orderedString);
    }

    /**
     * Sha1算法
     *
     * @author Amos
     * @param orderedString 字符串
     * @return String
     * @throws Exception 异常
     */
    private static String sha1(String orderedString) throws Exception {
        String ciphertext;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(orderedString.getBytes());
        ciphertext = byteToStr(digest);
        return ciphertext.toLowerCase();
    }

    /**
     * 字节转字符
     *
     * @author Amos
     * @param byteArray 字节
     * @return String
     */
    private static String byteToStr(byte[] byteArray) {
        StringBuilder strDigest = new StringBuilder();
        for (byte b : byteArray) {
            strDigest.append(byteToHexStr(b));
        }
        return strDigest.toString();
    }

    /**
     * 字节转Hex
     *
     * @author Amos
     * @param mByte 字节
     * @return String
     */
    private static String byteToHexStr(byte mByte) {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        return new String(tempArr);
    }

}
