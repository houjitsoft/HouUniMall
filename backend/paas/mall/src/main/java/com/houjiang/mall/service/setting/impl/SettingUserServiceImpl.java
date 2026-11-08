package com.houjiang.mall.service.setting.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.mall.service.setting.CommonConfigService;
import com.houjiang.mall.service.setting.SettingUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户设置 Service 实现。对应 like-admin SettingUserServiceImpl + SettingLoginServiceImpl。
 *
 * 配置分布：
 *   - default_image.user_avatar：默认用户头像
 *   - user_setting.invite_open / invite_condition / invite_qualification / invite_qualification_levels(JSON 数组)
 *   - login.login_way (JSON 数组) / coerce_mobile / login_agreement / third_auth / wechat_auth / qq_auth
 *
 * 字段命名：
 *   返回/接收均使用下划线键（与 like-admin 序列化后字段保持一致，前端 user.ts 已按下划线契约）。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingUserServiceImpl implements SettingUserService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String TYPE_DEFAULT_IMAGE = "default_image";
    private static final String TYPE_USER_SETTING = "user_setting";
    private static final String TYPE_LOGIN = "login";

    private final CommonConfigService commonConfigService;

    /** ============================== 用户基础设置 ============================== */

    @Override
    public Map<String, Object> getUserConfig() {
        String avatar = commonConfigService.getOrDefault(TYPE_DEFAULT_IMAGE, "user_avatar", "");
        Map<String, Object> vo = new LinkedHashMap<>();
        // toAdminAbsoluteUrl 在 paas/mall 暂无依赖，相对路径透传由前端 + 网关静态域处理
        vo.put("default_avatar", avatar);
        vo.put("default_image", avatar);
        vo.put("invite_open", parseInt(commonConfigService.getOrDefault(TYPE_USER_SETTING, "invite_open", "1"), 1));
        vo.put("invite_condition", parseInt(commonConfigService.getOrDefault(TYPE_USER_SETTING, "invite_condition", "0"), 0));
        vo.put("invite_qualification", parseInt(commonConfigService.getOrDefault(TYPE_USER_SETTING, "invite_qualification", "0"), 0));
        vo.put("invite_qualification_levels", parseJsonArray(
                commonConfigService.getOrDefault(TYPE_USER_SETTING, "invite_qualification_levels", "[]")));
        return vo;
    }

    @Override
    public void setUserConfig(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        String avatar = readString(params, "default_avatar", "defaultAvatar", "");
        commonConfigService.set(TYPE_DEFAULT_IMAGE, "user_avatar", avatar);

        commonConfigService.set(TYPE_USER_SETTING, "invite_open",
                String.valueOf(readInt(params, "invite_open", "inviteOpen", 1)));
        commonConfigService.set(TYPE_USER_SETTING, "invite_condition",
                String.valueOf(readInt(params, "invite_condition", "inviteCondition", 0)));
        commonConfigService.set(TYPE_USER_SETTING, "invite_qualification",
                String.valueOf(readInt(params, "invite_qualification", "inviteQualification", 0)));

        Object levels = params.get("invite_qualification_levels");
        if (levels == null) levels = params.get("inviteQualificationLevels");
        commonConfigService.set(TYPE_USER_SETTING, "invite_qualification_levels", toJson(levels));
    }

    /** ============================== 登录注册规则 ============================== */

    @Override
    public Map<String, Object> getLoginConfig() {
        Map<String, String> all = commonConfigService.getAll(TYPE_LOGIN);
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("login_way", parseJsonArray(all.getOrDefault("login_way", "[]")));
        vo.put("coerce_mobile", parseInt(all.getOrDefault("coerce_mobile", "0"), 0));
        vo.put("login_agreement", parseInt(all.getOrDefault("login_agreement", "0"), 0));
        vo.put("third_auth", parseInt(all.getOrDefault("third_auth", "0"), 0));
        vo.put("wechat_auth", parseInt(all.getOrDefault("wechat_auth", "0"), 0));
        vo.put("qq_auth", parseInt(all.getOrDefault("qq_auth", "0"), 0));
        return vo;
    }

    @Override
    public void setLoginConfig(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Object loginWayObj = params.get("login_way");
        if (loginWayObj == null) loginWayObj = params.get("loginWay");
        List<Object> loginWay = asList(loginWayObj);
        if (loginWay.isEmpty()) {
            throw new IllegalArgumentException("系统通用登录方式，至少选择一项");
        }
        commonConfigService.set(TYPE_LOGIN, "login_way", toJson(loginWay));
        commonConfigService.set(TYPE_LOGIN, "coerce_mobile",
                String.valueOf(readInt(params, "coerce_mobile", "coerceMobile", 0)));
        commonConfigService.set(TYPE_LOGIN, "login_agreement",
                String.valueOf(readInt(params, "login_agreement", "loginAgreement", 0)));
        commonConfigService.set(TYPE_LOGIN, "third_auth",
                String.valueOf(readInt(params, "third_auth", "thirdAuth", 0)));
        commonConfigService.set(TYPE_LOGIN, "wechat_auth",
                String.valueOf(readInt(params, "wechat_auth", "wechatAuth", 0)));
        commonConfigService.set(TYPE_LOGIN, "qq_auth",
                String.valueOf(readInt(params, "qq_auth", "qqAuth", 0)));
    }

    // ============================== 工具方法 ==============================

    /** 解析 JSON 数组字符串为 List；失败返回空 List。 */
    private List<Object> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<Object>>() {});
        } catch (Exception e) {
            log.warn("[SettingUser] 解析 JSON 数组失败，原值={}, err={}", json, e.getMessage());
            return new ArrayList<>();
        }
    }

    /** 把任意对象序列化为 JSON 字符串，失败返回 "[]"。 */
    private String toJson(Object obj) {
        if (obj == null) return "[]";
        if (obj instanceof String) return ((String) obj).isEmpty() ? "[]" : (String) obj;
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[SettingUser] 序列化 JSON 失败：{}", e.getMessage());
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> asList(Object obj) {
        if (obj == null) return Collections.emptyList();
        if (obj instanceof List) return (List<Object>) obj;
        if (obj instanceof String) {
            String s = ((String) obj).trim();
            if (s.isEmpty()) return Collections.emptyList();
            try {
                return OBJECT_MAPPER.readValue(s, new TypeReference<List<Object>>() {});
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private String readString(Map<String, Object> params, String snake, String camel, String defaults) {
        Object v = params.get(snake);
        if (v == null) v = params.get(camel);
        return v == null ? defaults : v.toString();
    }

    private int readInt(Map<String, Object> params, String snake, String camel, int defaults) {
        Object v = params.get(snake);
        if (v == null) v = params.get(camel);
        return v == null ? defaults : parseInt(v.toString(), defaults);
    }

    private int parseInt(String s, int defaults) {
        if (s == null || s.isEmpty()) return defaults;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaults;
        }
    }
}
