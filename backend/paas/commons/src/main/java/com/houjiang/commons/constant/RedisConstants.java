package com.houjiang.commons.constant;

/**
 * @ClassName RedisConstants
 * @Description redis的key值前缀
 * @Author Amos
 * @Date 2023/4/7 14:16
 * @Version 1.0
 * @author Amos
 */
public class RedisConstants {

    /**
     * 根据code存储用户信息,只取userid
     */
    public final static String USER_TOKEN_USERID = "hou-user-token-userid_";

    /**
     * 根据code存储用户信息（完整信息）
     */
    public final static String USER_TOKEN_USER = "hou-user-token-user_";

    /**
     * 存放手机验证码对应sessionId
     */
    public final static String VERIFICODE_CLIENT_SESSION_ID = "hou_VerifiCode_client_session_id";

    /**
     * GEO AI 创作 · 扩展模式配置（不常更新，永久缓存，由 GeoExpandModeService 维护）
     */
    public final static String GEO_EXPAND_MODES = "geo:content:expand-modes:v1";
}
