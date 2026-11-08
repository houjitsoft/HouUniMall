package com.houjiang.mall.service.setting;

import java.util.Map;

/**
 * 商城后台 - 用户设置 Service。
 * 对应 like-admin SettingUserService + SettingLoginService（同前缀 /setting.user.user）。
 *
 * @author Amos
 */
public interface SettingUserService {

    /**
     * 用户基础设置详情：default_avatar / default_image / invite_*。
     */
    Map<String, Object> getUserConfig();

    /**
     * 用户基础设置保存。
     */
    void setUserConfig(Map<String, Object> params);

    /**
     * 登录注册规则详情：login_way / coerce_mobile / login_agreement / third_auth / wechat_auth / qq_auth。
     */
    Map<String, Object> getLoginConfig();

    /**
     * 登录注册规则保存（login_way 必须至少包含一项）。
     */
    void setLoginConfig(Map<String, Object> params);
}
