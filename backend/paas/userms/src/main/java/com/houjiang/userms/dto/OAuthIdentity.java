package com.houjiang.userms.dto;

import java.util.Map;
import java.util.Set;

/** 仅接收 auth 已向提供方验证的身份，不接受手机号、用户 ID 或权限。 */
public record OAuthIdentity(String channel, String providerAppId, String openid, String unionid,
                            String nickname, String avatar, String registerTerminal, boolean allowCreate) {
    public static OAuthIdentity from(Map<String, String> body) {
        if (body == null) throw new IllegalArgumentException("OAuth 请求不能为空");
        String channel = required(body, "channel", 32);
        if (!Set.of("wechat", "qq", "wecom", "dingtalk", "feishu").contains(channel)) {
            throw new IllegalArgumentException("不支持的 OAuth 渠道");
        }
        String terminal = body.getOrDefault("registerTerminal", "houcloud");
        if (terminal == null || !Set.of("mall", "unimall", "houmall", "houcloud", "hougeo", "houocr").contains(terminal)) {
            throw new IllegalArgumentException("无效注册终端");
        }
        String allow = body.getOrDefault("allowCreate", "false");
        if (allow == null || !Set.of("true", "false").contains(allow)) {
            throw new IllegalArgumentException("allowCreate 必须为 true 或 false");
        }
        String unionid = body.get("unionid");
        if (unionid != null && unionid.isEmpty()) unionid = null;
        if (unionid != null && !unionid.matches("[\\x21-\\x7E]{1,255}")) {
            throw new IllegalArgumentException("无效 unionid");
        }
        String nickname = body.getOrDefault("nickname", "");
        String avatar = body.getOrDefault("avatar", "");
        if (nickname == null || avatar == null
                || nickname.codePointCount(0, nickname.length()) > 100 || avatar.length() > 255
                || avatar.codePoints().anyMatch(c -> c > 0xffff)) {
            throw new IllegalArgumentException("昵称或头像超出数据库字段限制");
        }
        return new OAuthIdentity(channel, required(body, "providerAppId", 128), required(body, "openid", 200),
                unionid, nickname, avatar, terminal, Boolean.parseBoolean(allow));
    }

    private static String required(Map<String, String> body, String key, int max) {
        String value = body.get(key);
        if (value == null || !value.matches("[\\x21-\\x7E]{1," + max + "}")) {
            throw new IllegalArgumentException("无效 " + key);
        }
        return value;
    }

    public int bindType() {
        return switch (channel) {
            case "wechat" -> 2;
            case "qq" -> 3;
            case "wecom" -> 4;
            case "dingtalk" -> 5;
            case "feishu" -> 6;
            default -> throw new IllegalArgumentException("不支持的 OAuth 渠道");
        };
    }
}
