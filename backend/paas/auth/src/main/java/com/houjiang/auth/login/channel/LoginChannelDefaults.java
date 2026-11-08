package com.houjiang.auth.login.channel;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内置渠道模板（UniLogin · 标准端点库）
 *
 * <p>这里只放「模板」，不放任何凭据。凭据来自两处，任一处填好即可用：</p>
 * <ol>
 *   <li>宿主「设置 → 第三方集成」页面（推荐，落库，无需碰配置中心）；</li>
 *   <li>Nacos 配置 {@code unilogin.channels.<code>.*}，以及兼容旧版的 {@code wechat.open.*}。</li>
 * </ol>
 *
 * <p>有了模板，接入一个新渠道的最短路径就是在设置页填三项凭据（AppID / AppSecret / 回调地址），
 * 或者等价地在 Nacos 里填：</p>
 * <pre>
 * unilogin.channels.wecom.app-id: ww1234567890
 * unilogin.channels.wecom.app-secret: xxxxxxxx
 * unilogin.channels.wecom.redirect-uri: https://cloud.example.com/plugin/unilogin/callback/wecom
 * unilogin.channels.wecom.custom-params: agentId=1000002   # 企业微信特有的应用 id
 * </pre>
 *
 * <p>模板里没有的渠道也可以纯配置接入：显式声明 {@code authorize-url} / {@code token-url} /
 * 字段映射即可；只有流程确实不标准的才需要写 Provider 实现类。</p>
 *
 * <p>占位符：授权地址用 {@code {appId} {redirectUri} {state}}；换 token 用
 * {@code {appId} {appSecret} {code} {redirectUri}}；拉用户信息用 {@code {accessToken} {openId} {code}}；
 * 另可用 {@code custom-params} 自定义任意 {@code {名字}}。</p>
 *
 * @author Amos
 */
public final class LoginChannelDefaults {

    private LoginChannelDefaults() {
    }

    private static final Map<String, LoginChannelDef> TEMPLATES = new LinkedHashMap<>();

    static {
        TEMPLATES.put("wechat", wechat());
        TEMPLATES.put("wecom", wecom());
        TEMPLATES.put("dingtalk", dingtalk());
        TEMPLATES.put("qq", qq());
        TEMPLATES.put("feishu", feishu());
    }

    /** 微信开放平台「网站应用」扫码登录 */
    private static LoginChannelDef wechat() {
        LoginChannelDef def = new LoginChannelDef();
        def.setCode("wechat");
        def.setName("微信");
        def.setAuthorizeUrl("https://open.weixin.qq.com/connect/qrconnect"
                + "?appid={appId}&redirect_uri={redirectUri}&response_type=code&scope=snsapi_login&state={state}");
        def.setAuthorizeAnchor("#wechat_redirect");
        def.setTokenUrl("https://api.weixin.qq.com/sns/oauth2/access_token"
                + "?appid={appId}&secret={appSecret}&code={code}&grant_type=authorization_code");
        def.setUserIdField("openid");
        def.setUnionIdField("unionid");
        def.setBindingPrefix("weixin_");
        def.setErrorCodeField("errcode");
        def.setErrorMessageField("errmsg");
        // 设置页「第三方集成 → 微信配置」
        def.setConfigPlatform("wechat-login");
        def.setConfigKey("wechatLogin");
        return def;
    }

    /** 企业微信自建应用扫码登录（授权链接必须带应用的 agentid，取自第三方集成配置） */
    private static LoginChannelDef wecom() {
        LoginChannelDef def = new LoginChannelDef();
        def.setCode("wecom");
        def.setName("企业微信");
        def.setAuthorizeUrl("https://login.work.weixin.qq.com/wwlogin/sso/login"
                + "?login_type=CorpApp&appid={appId}&agentid={agentId}"
                + "&redirect_uri={redirectUri}&state={state}");
        // 先取企业级 access_token（不含 code），再用它 + code 换成员身份，属企业微信特有的两跳流程
        def.setTokenUrl("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid={appId}&corpsecret={appSecret}");
        def.setUserInfoUrl("https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo"
                + "?access_token={accessToken}&code={code}");
        // 内部成员返回 userid；外部联系人才返回 openid
        def.setUserIdField("userid,openid");
        def.setBindingPrefix("wecom_");
        def.setErrorCodeField("errcode");
        def.setErrorMessageField("errmsg");
        // 设置页「第三方集成 → 企业微信集成」：corpId 当 appId、secret 当 appSecret
        def.setConfigPlatform("wechat-work");
        def.setConfigKey("wechatWork");
        def.setConfigAppIdField("corpId");
        def.setConfigAppSecretField("secret");
        def.setConfigExtraFields("agentId=agentId");
        return def;
    }

    /** 钉钉开放平台「扫码登录」（新版 oauth2） */
    private static LoginChannelDef dingtalk() {
        LoginChannelDef def = new LoginChannelDef();
        def.setCode("dingtalk");
        def.setName("钉钉");
        def.setAuthorizeUrl("https://login.dingtalk.com/oauth2/auth"
                + "?redirect_uri={redirectUri}&response_type=code&client_id={appId}&scope=openid&state={state}");
        def.setTokenUrl("https://api.dingtalk.com/v1.0/oauth2/userAccessToken");
        def.setTokenMethod("POST");
        def.setTokenBodyType("json");
        def.setTokenBodyTemplate("{\"clientId\":\"{appId}\",\"clientSecret\":\"{appSecret}\","
                + "\"code\":\"{code}\",\"grantType\":\"authorization_code\"}");
        def.setAccessTokenField("accessToken");
        def.setUserInfoUrl("https://api.dingtalk.com/v1.0/contact/users/me");
        def.setUserInfoTokenHeader("x-acs-dingtalk-access-token");
        def.setUserIdField("openId,unionId");
        def.setUnionIdField("unionId");
        def.setNicknameField("nick");
        def.setAvatarField("avatarUrl");
        def.setBindingPrefix("dingtalk_");
        def.setErrorCodeField("code");
        def.setErrorMessageField("message");
        // 设置页「第三方集成 → 钉钉集成」：appKey 当 appId
        def.setConfigPlatform("dingtalk");
        def.setConfigKey("dingtalk");
        def.setConfigAppIdField("appKey");
        return def;
    }

    /** QQ 互联网站应用登录 */
    private static LoginChannelDef qq() {
        LoginChannelDef def = new LoginChannelDef();
        def.setCode("qq");
        def.setName("QQ");
        def.setAuthorizeUrl("https://graph.qq.com/oauth2.0/authorize"
                + "?response_type=code&client_id={appId}&redirect_uri={redirectUri}&state={state}");
        def.setTokenUrl("https://graph.qq.com/oauth2.0/token"
                + "?grant_type=authorization_code&client_id={appId}&client_secret={appSecret}"
                + "&code={code}&redirect_uri={redirectUri}&fmt=json");
        // QQ 的 openid 不在 token 响应里，要再调一次 /me
        def.setUserInfoUrl("https://graph.qq.com/oauth2.0/me?access_token={accessToken}&fmt=json");
        def.setUserIdField("openid");
        def.setBindingPrefix("qq_");
        def.setErrorCodeField("error");
        def.setErrorMessageField("error_description");
        // 设置页「第三方集成 → QQ 登录配置」（宿主设置页暂未提供该 Tab，可由统一登录插件设置页填写）
        def.setConfigPlatform("qq-login");
        def.setConfigKey("qqLogin");
        return def;
    }

    /** 飞书开放平台网页应用登录（authen v2） */
    private static LoginChannelDef feishu() {
        LoginChannelDef def = new LoginChannelDef();
        def.setCode("feishu");
        def.setName("飞书");
        def.setAuthorizeUrl("https://accounts.feishu.cn/open-apis/authen/v1/authorize"
                + "?client_id={appId}&redirect_uri={redirectUri}&scope=contact:user.base:readonly&state={state}");
        def.setTokenUrl("https://open.feishu.cn/open-apis/authen/v2/oauth/token");
        def.setTokenMethod("POST");
        def.setTokenBodyType("json");
        def.setTokenBodyTemplate("{\"grant_type\":\"authorization_code\",\"client_id\":\"{appId}\","
                + "\"client_secret\":\"{appSecret}\",\"code\":\"{code}\",\"redirect_uri\":\"{redirectUri}\"}");
        def.setAccessTokenField("access_token");
        def.setUserInfoUrl("https://open.feishu.cn/open-apis/authen/v1/user_info");
        def.setUserInfoTokenHeader("Authorization");
        // 不加尾空格：请求头按「前缀 + 空格 + 令牌」拼装，避免配置项尾空格被 Nacos 剥掉
        def.setUserInfoTokenHeaderPrefix("Bearer");
        def.setUserInfoRootPath("data");
        def.setUserIdField("open_id,union_id");
        def.setUnionIdField("union_id");
        def.setNicknameField("name");
        def.setAvatarField("avatar_url");
        def.setBindingPrefix("feishu_");
        def.setErrorCodeField("code");
        def.setErrorMessageField("msg");
        // 设置页「第三方集成 → 飞书集成」
        def.setConfigPlatform("feishu");
        def.setConfigKey("feishu");
        return def;
    }

    /** 内置模板的渠道编码（有序） */
    public static List<String> codes() {
        return new ArrayList<>(TEMPLATES.keySet());
    }

    /** 取内置模板的副本；非内置渠道返回 null */
    public static LoginChannelDef templateOf(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }
        LoginChannelDef def = TEMPLATES.get(code.trim().toLowerCase());
        return def == null ? null : copy(def);
    }

    /**
     * 用配置项覆盖模板字段：配置里有值（非空）才覆盖，空值一律保留模板默认，
     * 避免「在 Nacos 里建了键但没填值」把默认端点清空。
     *
     * @param base    模板（可为 null，表示全新渠道）
     * @param code    渠道编码
     * @param lookup  配置读取函数，入参为「字段的中划线名」，返回配置值
     */
    public static LoginChannelDef override(LoginChannelDef base, String code, Lookup lookup) {
        LoginChannelDef def = base == null ? new LoginChannelDef() : copy(base);
        def.setCode(code);
        def.setName(or(lookup.get("name"), def.getName()));
        def.setAppId(or(lookup.get("app-id"), def.getAppId()));
        def.setAppSecret(or(lookup.get("app-secret"), def.getAppSecret()));
        def.setRedirectUri(or(lookup.get("redirect-uri"), def.getRedirectUri()));
        def.setConfigPlatform(or(lookup.get("config-platform"), def.getConfigPlatform()));
        def.setConfigKey(or(lookup.get("config-key"), def.getConfigKey()));
        def.setConfigAppIdField(or(lookup.get("config-app-id-field"), def.getConfigAppIdField()));
        def.setConfigAppSecretField(or(lookup.get("config-app-secret-field"), def.getConfigAppSecretField()));
        def.setConfigRedirectField(or(lookup.get("config-redirect-field"), def.getConfigRedirectField()));
        def.setConfigExtraFields(or(lookup.get("config-extra-fields"), def.getConfigExtraFields()));
        def.setAuthorizeUrl(or(lookup.get("authorize-url"), def.getAuthorizeUrl()));
        def.setAuthorizeExtraParams(or(lookup.get("authorize-extra-params"), def.getAuthorizeExtraParams()));
        def.setAuthorizeAnchor(or(lookup.get("authorize-anchor"), def.getAuthorizeAnchor()));
        def.setCustomParams(or(lookup.get("custom-params"), def.getCustomParams()));
        def.setTokenUrl(or(lookup.get("token-url"), def.getTokenUrl()));
        def.setTokenMethod(or(lookup.get("token-method"), def.getTokenMethod()));
        def.setTokenBodyType(or(lookup.get("token-body-type"), def.getTokenBodyType()));
        def.setTokenExtraParams(or(lookup.get("token-extra-params"), def.getTokenExtraParams()));
        def.setTokenRootPath(or(lookup.get("token-root-path"), def.getTokenRootPath()));
        def.setTokenResponseType(or(lookup.get("token-response-type"), def.getTokenResponseType()));
        def.setTokenBodyTemplate(or(lookup.get("token-body-template"), def.getTokenBodyTemplate()));
        def.setAccessTokenField(or(lookup.get("access-token-field"), def.getAccessTokenField()));
        def.setUserInfoUrl(or(lookup.get("user-info-url"), def.getUserInfoUrl()));
        def.setUserInfoMethod(or(lookup.get("user-info-method"), def.getUserInfoMethod()));
        def.setUserInfoTokenParam(or(lookup.get("user-info-token-param"), def.getUserInfoTokenParam()));
        def.setUserInfoTokenHeader(or(lookup.get("user-info-token-header"), def.getUserInfoTokenHeader()));
        def.setUserInfoTokenHeaderPrefix(or(lookup.get("user-info-token-header-prefix"), def.getUserInfoTokenHeaderPrefix()));
        def.setUserInfoExtraParams(or(lookup.get("user-info-extra-params"), def.getUserInfoExtraParams()));
        def.setUserInfoRootPath(or(lookup.get("user-info-root-path"), def.getUserInfoRootPath()));
        def.setUserIdField(or(lookup.get("user-id-field"), def.getUserIdField()));
        def.setUnionIdField(or(lookup.get("union-id-field"), def.getUnionIdField()));
        def.setNicknameField(or(lookup.get("nickname-field"), def.getNicknameField()));
        def.setAvatarField(or(lookup.get("avatar-field"), def.getAvatarField()));
        def.setBindingPrefix(or(lookup.get("binding-prefix"), def.getBindingPrefix()));
        def.setAutoRegister(parseBoolean(lookup.get("auto-register"), def.isAutoRegister()));
        def.setRegisterTerminal(or(lookup.get("register-terminal"), def.getRegisterTerminal()));
        def.setAutoRegisterMobilePrefix(
                or(lookup.get("auto-register-mobile-prefix"), def.getAutoRegisterMobilePrefix()));
        def.setErrorCodeField(or(lookup.get("error-code-field"), def.getErrorCodeField()));
        def.setErrorMessageField(or(lookup.get("error-message-field"), def.getErrorMessageField()));
        def.setErrorSuccessValues(or(lookup.get("error-success-values"), def.getErrorSuccessValues()));
        // name 为空时回退编码，保证任何情况下提示文案都有可读名字
        if (StrUtil.isBlank(def.getName())) {
            def.setName(code);
        }
        return def;
    }

    private static String or(String configured, String fallback) {
        return StrUtil.isNotBlank(configured) ? configured.trim() : fallback;
    }

    /** 布尔配置解析：true/1/yes/on 为真，false/0/no/off 为假，其余按缺省值处理 */
    private static boolean parseBoolean(String configured, boolean fallback) {
        if (StrUtil.isBlank(configured)) {
            return fallback;
        }
        String value = configured.trim().toLowerCase();
        if ("true".equals(value) || "1".equals(value) || "yes".equals(value) || "on".equals(value)) {
            return true;
        }
        if ("false".equals(value) || "0".equals(value) || "no".equals(value) || "off".equals(value)) {
            return false;
        }
        return fallback;
    }

    private static LoginChannelDef copy(LoginChannelDef src) {
        return JSONObject.parseObject(JSONObject.toJSONString(src), LoginChannelDef.class);
    }

    /** 配置读取回调：入参为字段的中划线名（如 app-id），返回 null 或空串表示未配置 */
    @FunctionalInterface
    public interface Lookup {
        String get(String kebabFieldName);
    }
}
