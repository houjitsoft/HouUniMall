package com.houjiang.mall.service.auth;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.mall.entity.auth.MallAdmin;
import com.houjiang.mall.util.MallAdminTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商城独立后台登录时，同步写入云后台 JWT Redis（{@code hou-user-token-user_}），
 * 使 thirdparty / goods 等仍走 commons JWT 过滤器的接口能识别同一 Bearer Token。
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallCloudTokenSyncService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${mall.auth.sync-cloud-token:true}")
    private boolean syncCloudToken;

    public void bindOnLogin(String jwtToken, MallAdmin admin) {
        if (!syncCloudToken || admin == null || jwtToken == null) {
            return;
        }
        String token = MallAdminTokenHelper.normalize(jwtToken);
        UserInfoVO user = new UserInfoVO();
        user.setUserId(String.valueOf(admin.getId()));
        user.setUserName(admin.getAccount());
        user.setUserAccount(admin.getAccount());
        user.setToken(token);
        user.setUserState(2);

        UserInfoDTO dto = new UserInfoDTO();
        dto.setUser(user);
        dto.setPermissions(resolveCloudPermissions(admin));
        dto.setEnabled(true);
        dto.setAccountNonExpired(true);
        dto.setAccountNonLocked(true);
        dto.setCredentialsNonExpired(true);

        String key = HouConfig.HOU_USER_TOKEN_USER + token;
        String json = JSON.toJSONString(dto);
        redisTemplate.opsForValue().set(key, json, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
        log.info("商城登录已同步云后台 token: adminId={}, account={}", admin.getId(), admin.getAccount());
    }

    public void refreshTtl(String rawToken) {
        if (!syncCloudToken || rawToken == null) {
            return;
        }
        String token = MallAdminTokenHelper.normalize(rawToken);
        String key = HouConfig.HOU_USER_TOKEN_USER + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
        }
    }

    /**
     * 已登录商城但缺少云后台 Redis 时补写（兼容升级前已发的 token）。
     */
    public void ensureBound(String rawToken, java.util.Map<String, Object> adminInfo) {
        if (!syncCloudToken || rawToken == null || adminInfo == null) {
            return;
        }
        String token = MallAdminTokenHelper.normalize(rawToken);
        String key = HouConfig.HOU_USER_TOKEN_USER + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            return;
        }
        MallAdmin admin = new MallAdmin();
        Object id = adminInfo.get("id");
        if (id != null) {
            admin.setId(id instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(id)));
        }
        admin.setAccount(String.valueOf(adminInfo.getOrDefault("account", "")));
        Object root = adminInfo.get("root");
        if (root instanceof Number n) {
            admin.setRoot(n.intValue());
        } else if (root != null) {
            admin.setRoot(Integer.parseInt(String.valueOf(root)));
        }
        bindOnLogin(token, admin);
    }

    public void revokeOnLogout(String rawToken) {
        if (!syncCloudToken || rawToken == null) {
            return;
        }
        String token = MallAdminTokenHelper.normalize(rawToken);
        redisTemplate.delete(HouConfig.HOU_USER_TOKEN_USER + token);
        log.debug("商城退出已清理云后台 token: {}", token.substring(0, Math.min(8, token.length())));
    }

    private static List<String> resolveCloudPermissions(MallAdmin admin) {
        if (admin.getRoot() != null && admin.getRoot() == 1) {
            return List.of("*");
        }
        return Collections.emptyList();
    }
}
