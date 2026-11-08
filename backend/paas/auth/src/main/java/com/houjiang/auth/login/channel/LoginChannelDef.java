package com.houjiang.auth.login.channel;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 统一登录渠道定义（UniLogin · 配置驱动）
 *
 * <p>一个「渠道」= 一套标准 OAuth2 授权码流程 + 一组字段映射。整个流程只由本对象的字段描述，
 * 不使用任何厂商专有分支，因此<b>新增渠道只需要加配置，不需要改 Java 代码</b>。</p>
 *
 * <p>配置前缀 {@code unilogin.channels.<code>.<字段名>}，字段名用中划线形式（如 {@code app-id}、
 * {@code authorize-url}）。内置渠道（微信/企业微信/钉钉/QQ/飞书）已带默认端点模板，
 * 通常只需补 {@code app-id} / {@code app-secret} / {@code redirect-uri} 三项凭据。</p>
 *
 * <p>模板占位符：授权地址用 {@code {appId} {redirectUri} {state} {scope}}；
 * 换 token 地址用 {@code {appId} {appSecret} {code} {redirectUri}}；
 * userinfo 地址用 {@code {accessToken} {openId}}。</p>
 *
 * @author Amos
 */
@Data
public class LoginChannelDef {

    /** 渠道编码，同时是配置键与接口路径参数，如 wechat / wecom / dingtalk / qq / feishu */
    private String code;
    /** 渠道显示名，用于日志与提示 */
    private String name;

    // ==================== 凭据（渠道是否可用的唯一判据） ====================

    private String appId;
    private String appSecret;
    /** 授权回调地址，必须落在第三方平台登记的「授权回调域」内 */
    private String redirectUri;

    // ==================== 凭据来源：宿主「设置 → 第三方集成」============================
    // 管理员在设置页填写的凭据同样可用：服务端会把该平台的配置映射到上面三个字段，
    // 避免出现「设置页填了、登录页却说未开通」。Nacos 里的 unilogin.channels.<code>.* 优先级更高。

    /** 第三方集成配置的平台键，如 wechat-login / wechat-work / dingtalk / feishu / qq-login */
    private String configPlatform;
    /** 该平台配置内承载凭据的子对象名，如 wechatLogin / wechatWork / dingtalk / feishu / qqLogin */
    private String configKey;
    /** 子对象里 AppID 对应的字段名，默认 appId（钉钉为 appKey、企业微信为 corpId） */
    private String configAppIdField = "appId";
    /** 子对象里 AppSecret 对应的字段名，默认 appSecret（企业微信为 secret） */
    private String configAppSecretField = "appSecret";
    /** 子对象里回调地址对应的字段名，默认 callbackUrl */
    private String configRedirectField = "callbackUrl";
    /**
     * 需要从配置里取出并当作模板变量注入的字段，逗号分隔的 {@code 变量名=子对象字段名}，
     * 如企业微信的 {@code agentId=agentId}（授权链接里必须带应用 agentid）。
     */
    private String configExtraFields;

    // ==================== 授权页 ====================

    /** 授权端点模板 */
    private String authorizeUrl;
    /** 追加到授权地址的查询串（不含问号），如 {@code response_type=code&scope=snsapi_login} */
    private String authorizeExtraParams;
    /** 授权地址尾部锚点，如微信要求的 {@code #wechat_redirect} */
    private String authorizeAnchor;
    /**
     * 自定义模板参数，逗号分隔的 {@code k=v}，在任意 URL/请求体模板中用 {@code {k}} 引用。
     * 用于渠道特有的非凭据参数，如企业微信的 {@code agentId=1000002}。
     */
    private String customParams;

    // ==================== code 换 token ====================

    /** 换 token 端点模板 */
    private String tokenUrl;
    /** 换 token 请求方法：GET / POST */
    private String tokenMethod = "GET";
    /** POST 时的请求体类型：form（表单）/ json */
    private String tokenBodyType = "form";
    /** 追加到换 token 请求的参数（查询串或表单键值对） */
    private String tokenExtraParams;
    /** token 响应中业务数据所在的根路径（点号分隔），如飞书的 {@code data} */
    private String tokenRootPath;
    /** token 响应数据类型：json（默认）/ form（表单串，如 QQ 互联老接口） */
    private String tokenResponseType = "json";
    /**
     * POST JSON 请求体模板，如钉钉的
     * {@code {"clientId":"{appId}","clientSecret":"{appSecret}","code":"{code}","grantType":"authorization_code"}}
     */
    private String tokenBodyTemplate;
    /** token 响应中访问令牌的字段名，默认 access_token（钉钉为 accessToken） */
    private String accessTokenField = "access_token";

    // ==================== 可选 userinfo（部分渠道 token 响应里没有用户标识） ====================

    /** 拉取用户信息端点模板，留空表示直接用 token 响应中的字段 */
    private String userInfoUrl;
    private String userInfoMethod = "GET";
    /** access_token 作为哪个查询参数传给 userinfo（userinfo 地址里已含 {accessToken} 占位时不生效） */
    private String userInfoTokenParam = "access_token";
    /** access_token 作为请求头发送时的头名，如钉钉 x-acs-dingtalk-access-token、飞书 Authorization */
    private String userInfoTokenHeader;
    /** 请求头令牌前缀，如飞书的 {@code Bearer } */
    private String userInfoTokenHeaderPrefix;
    /** 追加到 userinfo 请求的参数 */
    private String userInfoExtraParams;
    /** userinfo 响应中业务数据所在的根路径（点号分隔），如飞书的 {@code data} */
    private String userInfoRootPath;

    // ==================== 字段映射（决定身份识别与展示） ====================

    /**
     * 用户唯一标识字段名，支持逗号分隔的候选列表（按序取第一个非空），
     * 如企业微信 {@code userid,openid}（内部成员取 userid、外部联系人取 openid）。
     */
    private String userIdField = "openid";
    /** 开放平台跨应用互通标识字段名，可空 */
    private String unionIdField = "unionid";
    private String nicknameField;
    private String avatarField;
    /**
     * 绑定表中的账号标识前缀。登录成功后按 {@code bindingPrefix + 标识} 去 userms 查绑定关系，
     * 与既有微信绑定数据（{@code weixin_xxx}）保持一致。
     */
    private String bindingPrefix = "weixin_";

    // ==================== 自动注册（扫码即建号）============================

    /**
     * 是否在扫码登录回调时自动注册新用户。
     *
     * <p>关闭（默认）时，第三方账号未绑定平台账号会返回 {@code unbound}，
     * 引导用户先用账号密码登录、再到账号设置里绑定；</p>
     *
     * <p>打开后，扫码回调会先判断用户表里有没有这个身份：没有就当场建号，
     * 有则只补写绑定关系，扫码即可直接进入。</p>
     */
    private boolean autoRegister = false;

    /**
     * 自动注册时写入用户表的注册类型（{@code hou_user_info.register_terminal}），
     * 如 {@code houocr}（OCR 站点）/ {@code hougeo}（GEO 站点）/ {@code mall}（商城）。
     * 留空则由 userms 按默认口径归一。
     */
    private String registerTerminal;

    /**
     * 自动注册时使用的占位手机号前缀。
     *
     * <p>第三方扫码只给得出 openid/unionid，拿不到手机号，而用户表的手机号列不允许为空，
     * 因此用「前缀 + openid 摘要」派生一个稳定占位值：同一身份每次都得到同一个手机号，
     * 可据此判断「这个身份此前是否已注册过」。</p>
     */
    private String autoRegisterMobilePrefix = "auto";

    // ==================== 错误判定 ====================

    /** 错误码字段名；字段缺失视为成功 */
    private String errorCodeField = "errcode";
    /** 错误描述字段名 */
    private String errorMessageField = "errmsg";
    /** 视为成功的错误码取值，逗号分隔；默认 0 与 200 */
    private String errorSuccessValues = "0,200";

    // ==================== 派生方法 ====================

    /** 凭据齐备且端点模板完整，才允许在登录页展示入口 */
    public boolean isConfigured() {
        return StrUtil.isNotBlank(appId)
                && StrUtil.isNotBlank(appSecret)
                && StrUtil.isNotBlank(redirectUri)
                && StrUtil.isNotBlank(authorizeUrl)
                && StrUtil.isNotBlank(tokenUrl);
    }

    public String displayName() {
        return StrUtil.isNotBlank(name) ? name : code;
    }

    /** 未配置时的用户可读提示，登录页直接展示，避免出现扫不动的死二维码 */
    public String notConfiguredMessage() {
        return displayName() + "登录尚未开通，请先在「设置 → 第三方集成」中配置该渠道凭据。";
    }

    /** 空串归一为 null，避免 Nacos 里配置了键但值为空时覆盖掉内置默认值 */
    public static String blankToNull(String value) {
        return StrUtil.isBlank(value) ? null : value.trim();
    }
}
