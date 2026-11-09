package com.houjiang.commons.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.exception.GlobalExceptionEnum;
import com.houjiang.commons.vo.user.PlatformUserInfoVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Token解析工具类 - 统一使用RedisCache进行Redis操作
 * 
 * @author Amos
 * @since 2025-01-30
 */
@Slf4j
@Component
public class ParseTokenUtils {
    
    private static RedisCache redisCache;
    
    @Autowired
    public void setRedisCache(RedisCache redisCache) {
        ParseTokenUtils.redisCache = redisCache;
    }

    /**
     * 根据token得到userInfo
     * 统一使用RedisCache进行Redis操作
     *
     * @param token
     * @return
     */
    public static UserInfoVO getUserInfoByToken(String token) {
        if (token == null || token.isEmpty()){
            return null;
        }
        if (redisCache == null) {
            log.error("RedisCache未初始化，无法获取用户信息");
            throw new DefinedException(GlobalExceptionEnum.NO_FORBIDDEN.getErrorCode(), "Redis缓存未初始化");
        }
        
        //从redis得到token转换userInfo - 统一使用HOU_USER_TOKEN_USER
        String redisKey = HouConfig.HOU_USER_TOKEN_USER + token;
        
        boolean isExist = redisCache.hasKey(redisKey);
        
        if (!isExist) {
            throw new DefinedException(GlobalExceptionEnum.NO_FORBIDDEN.getErrorCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        
        String userInfoStr = (String) redisCache.getCacheObject(redisKey);
        return parseSessionUser(userInfoStr);
    }

    /**
     * 解析会话缓存里的用户信息，兼容两种历史写法（2026-11-09 修复商家入驻报「系统错误」）：
     * 一是 auth（平台 / 会员端）写入的 UserInfoDTO，形如 {@code {"user":{...},"permissions":[...]}}；
     * 二是 merchant（商家端）直接写入的裸 UserInfoVO，形如 {@code {"userId":"...","userName":"..."}}。
     * 只用 DTO 解析时裸 VO 会得到 user=null，调用方随即 NPE（商家入驻协议接口即踩此坑）。
     */
    private static UserInfoVO parseSessionUser(String userInfoStr) {
        if (StringUtils.isEmpty(userInfoStr)) {
            return null;
        }
        try {
            UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
            if (dto != null && dto.getUser() != null) {
                return dto.getUser();
            }
            return JSON.parseObject(userInfoStr, UserInfoVO.class);
        } catch (Exception e) {
            log.warn("会话缓存解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断 token 会话是否平台超管（2026-10-30 平台约定：hou_user_info.is_admin=1 的账号
     * 有意不绑 RBAC 角色，登录态 permissions 为字面 ["*"]）。管理端查询（如订单全平台
     * 列表）以本方法判定是否放开「仅本人数据」过滤。
     * 任何解析失败/键不存在一律按非超管处理（fail-closed）。
     *
     * @param token 裸 JWT（不带 Bearer 前缀）
     */
    public static boolean isPlatformAdmin(String token) {
        try {
            if (token == null || token.isEmpty() || redisCache == null) {
                return false;
            }
            String redisKey = HouConfig.HOU_USER_TOKEN_USER + token;
            if (!redisCache.hasKey(redisKey)) {
                return false;
            }
            String userInfoStr = (String) redisCache.getCacheObject(redisKey);
            if (StringUtils.isEmpty(userInfoStr)) {
                return false;
            }
            UserInfoDTO dto = JSON.parseObject(userInfoStr, UserInfoDTO.class);
            return dto != null && dto.getPermissions() != null && dto.getPermissions().contains("*");
        } catch (Exception e) {
            log.warn("判定平台超管失败，按非超管处理: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 根据token得到userInfo
     * token 不存在 也返回
     * 统一使用RedisCache进行Redis操作
     * @param token
     * @return
     */
    public static UserInfoVO getUserInfoByTokenNoIsExist(String token) {
        if (token == null || token.isEmpty()){
            return null;
        }
        if (redisCache == null) {
            log.warn("RedisCache未初始化，无法获取用户信息");
            return null;
        }
        
        //从redis得到token转换userInfo - 统一使用HOU_USER_TOKEN_USER
        String redisKey = HouConfig.HOU_USER_TOKEN_USER + token;
        boolean isExist = redisCache.hasKey(redisKey);
        if (!isExist) {
            return null;
        }
        
        String userInfoStr = (String) redisCache.getCacheObject(redisKey);
        return parseSessionUser(userInfoStr);
    }

    /**
     * 得到商家用户信息
     * 统一使用RedisCache进行Redis操作
     *
     * @param token
     * @return
     */
    public static ResponseResult getShopUserInfo(String token) {
        // 验证token是否为空
        if (token == null){
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL);
        }
        if (redisCache == null) {
            log.error("RedisCache未初始化，无法获取用户信息");
            return ResponseResult.fail(BizCodeEnume.QUERY_USER_FAIL);
        }
        
        // 统一使用HOU_USER_TOKEN_USER
        String userTokenKey = HouConfig.HOU_USER_TOKEN_USER + token;
        String yiyatongTokenKey = HouConfig.HOU_YIYATONG_TOKEN_USER + token;
        
        String userInfoStr = (String) redisCache.getCacheObject(userTokenKey);
        String userInfoYiYaTong = (String) redisCache.getCacheObject(yiyatongTokenKey);
        
        // 验证获取的用户信息是否为空
        if (StringUtils.isEmpty(userInfoStr) && StringUtils.isEmpty(userInfoYiYaTong)) {
            return ResponseResult.fail(BizCodeEnume.QUERY_USER_FAIL);
        }
        
        UserInfoVO ui = null;
        if(null != userInfoYiYaTong){
            // 根据token判断是否是商家账号
            ui = getUserInfoYiYaTongByToken(token);
        }else{
            // 根据token判断是否是商家账号
            ui = getUserInfoByToken(token);
        }
        
        // 判断是否是商家账号
        if (null == ui || !CommonType.commonStatus.YES.getCode().equals(ui.getIsShop()) && ui.getShopId() == null) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS.getCode(), BizCodeEnume.INSUFFICIENT_PERMISSIONS.getMsg());
        }
        
        String userIdKey = HouConfig.HOU_USER_TOKEN_USERID + token;
        String userId = (String) redisCache.getCacheObject(userIdKey);
        return ResponseResult.success(userId, ui);
    }

    public static UserInfoVO getUserInfoYiYaTongByToken(Object token) {
        if (token == null || StringUtils.isEmpty(token.toString())){
            return null;
        }
        if (redisCache == null) {
            log.warn("RedisCache未初始化，无法获取用户信息");
            return null;
        }
        
        // 检查token在Redis中是否存在 - 统一使用RedisCache
        String yiyatongTokenKey = HouConfig.HOU_YIYATONG_TOKEN_USER + token;
        boolean isExist = redisCache.hasKey(yiyatongTokenKey);
        if (!isExist) {
            return null;
        }
        
        // 从Redis获取token对应的用户信息字符串
        String userInfoStr = (String) redisCache.getCacheObject(yiyatongTokenKey);
        if(StrUtil.isEmpty(userInfoStr)){
            return null;
        }
        
        // 将用户信息字符串反序列化为UserInfoVO对象
        UserInfoVO dto = JSON.parseObject(userInfoStr, UserInfoVO.class);
        if (dto != null) {
            // 返回用户信息
            return dto;
        } else {
            return null;
        }
    }

    /**
     * 得到系统用户信息
     * 统一使用RedisCache进行Redis操作
     *
     * @param token
     * @return
     */
    public static UserInfoVO getPlatformUser(String token) {
        if (redisCache == null) {
            log.error("RedisCache未初始化，无法获取用户信息");
            throw new DefinedException(GlobalExceptionEnum.NO_FORBIDDEN.getErrorCode(), "Redis缓存未初始化");
        }
        
        // 统一使用HOU_USER_TOKEN_USER
        String redisKey = HouConfig.HOU_USER_TOKEN_USER + token;
        
        String userInfoStr = (String) redisCache.getCacheObject(redisKey);
        if (null == userInfoStr || userInfoStr.isEmpty()) {
            throw new DefinedException(GlobalExceptionEnum.NO_FORBIDDEN.getErrorCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        
        return parseSessionUser(userInfoStr);
    }

    public static ResponseResult getShopUser(String token) {
        // 判断是否是商家账号
        UserInfoVO ui = getUserInfoByToken(token);
        if (null != ui && ui.getShopId() != null) {
            return ResponseResult.success(ui);
        }
        return ResponseResult.fail(BizCodeEnume.NO_SHOP_ACCOUNT);
    }

    /**
     * 根据token得到userInfo
     * 统一使用RedisCache进行Redis操作
     *
     * @param token
     * @return
     */
    public static ResponseResult getUserByToken(Object token) {
        if (token == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL);
        }
        if (redisCache == null) {
            log.error("RedisCache未初始化，无法获取用户信息");
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        
        // 统一使用HOU_USER_TOKEN_USER
        String redisKey = HouConfig.HOU_USER_TOKEN_USER + token;
        
        String userInfoStr = (String) redisCache.getCacheObject(redisKey);
        if (StrUtil.isEmpty(userInfoStr)) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        
        //从redis得到token转换userInfo（兼容 DTO / 裸 VO 两种写法）
        UserInfoVO sessionUser = parseSessionUser(userInfoStr);
        if (sessionUser != null) {
            return ResponseResult.success(sessionUser);
        } else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_OFFSITE_LOGIN);
        }
    }

    /**
     * 得到系统用户信息
     * 统一使用RedisCache进行Redis操作
     *
     * @param token
     * @return
     */
    public static PlatformUserInfoVO getPlatformUserInfoVO(String token) {
        if (redisCache == null) {
            log.error("RedisCache未初始化，无法获取用户信息");
            throw new DefinedException(GlobalExceptionEnum.NO_FORBIDDEN.getErrorCode(), "Redis缓存未初始化");
        }
        
        // 统一使用HOU_USER_TOKEN_USER
        String redisKey = HouConfig.HOU_USER_TOKEN_USER + token;
        
        String userInfoStr = (String) redisCache.getCacheObject(redisKey);
        if (null == userInfoStr || userInfoStr.isEmpty()) {
            throw new DefinedException(GlobalExceptionEnum.NO_FORBIDDEN.getErrorCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        
        PlatformUserInfoVO dto = JSON.parseObject(userInfoStr, PlatformUserInfoVO.class);
        if (dto != null) {
            return dto;
        } else {
            return null;
        }
    }

    public static UserInfoVO getProfile() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userInfoStr = request.getHeader("userInfo");
            if (!StringUtils.isEmpty(userInfoStr)) {
                byte[] bytes = Base64.getDecoder().decode(userInfoStr);
                String encodedJson = new String(bytes, StandardCharsets.UTF_8);
                Map<String, String> profile = JSON.parseObject(encodedJson, HashMap.class);

                return JSON.parseObject(JSON.toJSONString(profile), UserInfoVO.class);
            }
        }
        return null;
    }
}
